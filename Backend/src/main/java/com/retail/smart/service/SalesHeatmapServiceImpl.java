package com.retail.smart.service;

import com.retail.smart.dto.SalesHeatmapDTO;
import com.retail.smart.dto.SalesHeatmapDTO.HeatmapEntry;
import com.retail.smart.dto.SalesHeatmapDTO.RelocationSuggestionEntry;
import com.retail.smart.grpc.sales.SalesAreaPerformance;
import com.retail.smart.grpc.sales.SalesHeatmapGrpc;
import com.retail.smart.grpc.sales.SalesRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SalesHeatmapServiceImpl extends SalesHeatmapGrpc.SalesHeatmapImplBase {

    private final Random random = new Random();

    private static final Map<String, List<String>> areaCategories = Map.of(
            "A101", List.of("limpeza", "higiene"),
            "B202", List.of("laticinios", "bebidas"),
            "C303", List.of("limpeza"),
            "D404", List.of("carnes", "padaria")
    );

    @Override
    public void getHeatmap(SalesRequest request, StreamObserver<SalesAreaPerformance> responseObserver) {
        for (String area : areaCategories.keySet()) {
            int totalSales = random.nextInt(300);
            String suggestion = totalSales < 100
                    ? "Consider relocating high-demand products to area " + area
                    : "Area " + area + " is performing well";

            SalesAreaPerformance performance = SalesAreaPerformance.newBuilder()
                    .setAreaCode(area)
                    .setTotalSales(totalSales)
                    .setSuggestion(suggestion)
                    .build();

            responseObserver.onNext(performance);
        }

        responseObserver.onCompleted();
    }
}
