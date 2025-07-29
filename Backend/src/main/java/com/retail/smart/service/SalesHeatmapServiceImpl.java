package com.retail.smart.service;

import com.retail.smart.grpc.sales.SalesAreaPerformance;
import com.retail.smart.grpc.sales.SalesHeatmapGrpc;
import com.retail.smart.grpc.sales.SalesRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SalesHeatmapServiceImpl extends SalesHeatmapGrpc.SalesHeatmapImplBase {

    private static final Map<Integer, Map<String, Integer>> weeklySales = Map.of(
        0, Map.of("A101", 160, "B202", 150, "C303", 60, "D404", 100),
        1, Map.of("A101", 155, "B202", 165, "C303", 50, "D404", 105),
        2, Map.of("A101", 170, "B202", 140, "C303", 70, "D404", 95),
        3, Map.of("A101", 165, "B202", 155, "C303", 65, "D404", 90)
    );

    private static final Map<String, List<String>> areaCategories = Map.of(
        "A101", List.of("cleaning", "hygiene"),
        "B202", List.of("dairy", "beverages"),
        "C303", List.of("cleaning"),
        "D404", List.of("meat", "bakery")
    );

    @Override
    public void getHeatmap(SalesRequest request, StreamObserver<SalesAreaPerformance> responseObserver) {
        int week = extractWeekOffset(request.getRequestTime());
        Map<String, Integer> current = weeklySales.getOrDefault(week, weeklySales.get(0));
        Map<String, Integer> previous = weeklySales.getOrDefault(week - 1, current);

        for (String area : areaCategories.keySet()) {
            int currentSales = current.getOrDefault(area, 0);
            int previousSales = previous.getOrDefault(area, currentSales);
            int diff = currentSales - previousSales;

            String suggestion;
            if (currentSales < 100) {
                suggestion = String.format("Sales dropped %d%% compared to previous week in area %s",
                        getPercentageDrop(previousSales, currentSales), area);
            } else {
                suggestion = "Area " + area + " is performing well";
            }

            SalesAreaPerformance performance = SalesAreaPerformance.newBuilder()
                    .setAreaCode(area)
                    .setTotalSales(currentSales)
                    .addAllTopCategories(areaCategories.getOrDefault(area, List.of("general")))
                    .setSuggestion(suggestion)
                    .build();

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
            System.out.println(" Failed to parse week offset: " + e.getMessage());
        }
        return 0;
    }

    private int getPercentageDrop(int previous, int current) {
        if (previous == 0) return 100;
        return (int) Math.round(((double) (previous - current) / previous) * 100);
    }
}
