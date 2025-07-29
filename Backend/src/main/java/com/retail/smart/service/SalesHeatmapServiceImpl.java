package com.retail.smart.service;

import com.retail.smart.grpc.sales.SalesAreaPerformance;
import com.retail.smart.grpc.sales.SalesHeatmapGrpc;
import com.retail.smart.grpc.sales.SalesRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SalesHeatmapServiceImpl extends SalesHeatmapGrpc.SalesHeatmapImplBase {

    private static final Map<String, List<String>> areaCategories = Map.of(
            "A101", List.of("cleaning", "hygiene"),
            "B202", List.of("dairy", "beverages"),
            "C303", List.of("cleaning"),
            "D404", List.of("meat", "bakery")
    );

    @Override
    public void getHeatmap(SalesRequest request, StreamObserver<SalesAreaPerformance> responseObserver) {
        int weekOffset = extractWeekOffset(request.getRequestTime());
        Random random = new Random(System.currentTimeMillis() + weekOffset * 1000L);

        System.out.println("\uD83D\uDCCA gRPC 'getHeatmap' called for week offset: " + weekOffset);

        for (String area : areaCategories.keySet()) {
            int baseSales = 200 - (weekOffset * 30);
            int totalSales = Math.max(0, baseSales + random.nextInt(100) - 50);

            String suggestion = totalSales < 100
                    ? "Consider relocating high-demand products to area " + area
                    : "Area " + area + " is performing well";

            SalesAreaPerformance performance = SalesAreaPerformance.newBuilder()
                    .setAreaCode(area)
                    .setTotalSales(totalSales)
                    .addAllTopCategories(areaCategories.getOrDefault(area, List.of("general")))
                    .setSuggestion(suggestion)
                    .build();

            System.out.println("✅ Sending area " + area + " with sales: " + totalSales);
            responseObserver.onNext(performance);
        }

        responseObserver.onCompleted();
    }

    private int extractWeekOffset(String requestTime) {
        try {
            if (requestTime != null && requestTime.startsWith("WEEK_")) {
                return Integer.parseInt(requestTime.substring(5));
            }
        } catch (Exception e) {
            System.out.println("⚠️ Failed to parse week offset: " + e.getMessage());
        }
        return 0;
    }
}