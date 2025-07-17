package com.retail.smart;

import com.retail.smart.service.InventoryRefillServiceImpl;
import com.retail.smart.service.SalesHeatmapServiceImpl;
import com.retail.smart.service.SecurityMonitorServiceImpl;
import com.retail.smart.service.SmartPricingServiceImpl;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.retail.smart")
public class SmartRetailSystemApplication {

    @Autowired
    private InventoryRefillServiceImpl inventoryRefillService;

    @Autowired
    private SalesHeatmapServiceImpl salesHeatmapService;

    @Autowired
    private SmartPricingServiceImpl smartPricingService;

    @Autowired
    private SecurityMonitorServiceImpl securityMonitorService;

    private Server grpcServer;

    public static void main(String[] args) {
        SpringApplication.run(SmartRetailSystemApplication.class, args);
        System.out.println("✅ SmartRetailSystemApplication started and REST + gRPC enabled");
    }

    @PostConstruct
    public void startGrpcServer() {
        try {
            grpcServer = ServerBuilder.forPort(9090)
                    .addService(inventoryRefillService)
                    .addService(salesHeatmapService)
                    .addService(smartPricingService)
                    .addService(securityMonitorService)
                    .build();

            grpcServer.start();
            System.out.println("🚀 gRPC Server started on port 9090");
            System.out.println("✅ InventoryRefillServiceImpl registered");
            System.out.println("✅ SalesHeatmapServiceImpl registered");
            System.out.println("✅ SmartPricingServiceImpl registered");
            System.out.println("✅ SecurityMonitorServiceImpl registered");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("🛑 Shutting down gRPC server...");
                if (grpcServer != null) grpcServer.shutdown();
            }));

        } catch (Exception e) {
            System.err.println("❌ Failed to start gRPC server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
