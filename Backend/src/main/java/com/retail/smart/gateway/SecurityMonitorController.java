package com.retail.smart.gateway;

import com.retail.smart.dto.SecurityMonitorDTO.SecurityEventEntry;
import com.retail.smart.dto.SecurityMonitorDTO.SecurityAlertEntry;
import com.retail.smart.grpc.security.SecurityAlert;
import com.retail.smart.grpc.security.SecurityEvent;
import com.retail.smart.grpc.security.SecurityMonitorGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/security")
public class SecurityMonitorController {

    @PostMapping("/analyze")
    public List<SecurityAlertEntry> analyzeEvents(@Valid @RequestBody List<SecurityEventEntry> events) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        SecurityMonitorGrpc.SecurityMonitorStub stub = SecurityMonitorGrpc.newStub(channel);

        List<SecurityAlertEntry> alertResults = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<SecurityEvent> requestObserver = stub.monitorSuspects(new StreamObserver<>() {
            @Override
            public void onNext(SecurityAlert value) {
                alertResults.add(new SecurityAlertEntry(
                        value.getAlertLevel(),
                        value.getMessage(),
                        value.getLocation()
                ));
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        for (SecurityEventEntry eventDTO : events) {
            SecurityEvent event = SecurityEvent.newBuilder()
                    .setCameraId(eventDTO.getCameraId())
                    .setTimestamp(eventDTO.getTimestamp())
                    .setDetectedBehavior(eventDTO.getDetectedBehavior())
                    .build();
            requestObserver.onNext(event);
        }

        requestObserver.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
        channel.shutdown();

        return alertResults;
    }
}
