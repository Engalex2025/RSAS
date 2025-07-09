package com.retail.smart.service;

import com.retail.smart.grpc.security.SecurityAlert;
import com.retail.smart.grpc.security.SecurityEvent;
import com.retail.smart.grpc.security.SecurityMonitorGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SecurityMonitorServiceImpl extends SecurityMonitorGrpc.SecurityMonitorImplBase {

    private final Random random = new Random();

    @Override
    public StreamObserver<SecurityEvent> monitorSuspects(StreamObserver<SecurityAlert> responseObserver) {
        return new StreamObserver<>() {

            @Override
public void onNext(SecurityEvent event) {
    System.out.println("Received event: " + event.getDetectedBehavior()); // ← aqui

    String behavior = event.getDetectedBehavior().toLowerCase();
    String alertLevel;
    String message;

    if (behavior.contains("intruder") || behavior.contains("suspicious")) {
        alertLevel = "HIGH";
        message = "Immediate action required!";
    } else if (behavior.contains("loitering")) {
        alertLevel = "MEDIUM";
        message = "Monitor the situation.";
    } else {
        alertLevel = "LOW";
        message = "No threat detected.";
    }

    SecurityAlert alert = SecurityAlert.newBuilder()
            .setAlertLevel(alertLevel)
            .setMessage(message)
            .setLocation("Camera: " + event.getCameraId())
            .build();

    responseObserver.onNext(alert);
}


            @Override
            public void onError(Throwable t) {
                System.err.println("Security stream error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
