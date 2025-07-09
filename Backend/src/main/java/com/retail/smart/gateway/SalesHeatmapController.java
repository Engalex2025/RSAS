package com.retail.smart.gateway;

import com.retail.smart.dto.SalesHeatmapDTO;
import com.retail.smart.dto.SalesHeatmapDTO.HeatmapEntry;
import com.retail.smart.dto.SalesHeatmapDTO.RelocationSuggestionEntry;
import com.retail.smart.grpc.sales.SalesHeatmapGrpc;
import com.retail.smart.grpc.sales.SalesRequest;
import com.retail.smart.grpc.sales.SalesAreaPerformance;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@RestController
public class SalesHeatmapController {

    @GetMapping("/api/sales/heatmap")
    public SalesHeatmapDTO getSalesHeatmap() throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        SalesHeatmapGrpc.SalesHeatmapStub stub = SalesHeatmapGrpc.newStub(channel);

        List<HeatmapEntry> heatmap = new ArrayList<>();
        List<RelocationSuggestionEntry> suggestions = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        stub.getHeatmap(SalesRequest.newBuilder()
                .setRequestTime(new Date().toString())
                .build(), new StreamObserver<>() {
            @Override
            public void onNext(SalesAreaPerformance value) {
                List<String> categories = getTopCategories(value.getAreaCode());

                heatmap.add(new HeatmapEntry(
                        value.getAreaCode(),
                        value.getTotalSales(),
                        categories,
                        value.getSuggestion()
                ));

                if (value.getTotalSales() < 100) {
                    suggestions.add(new RelocationSuggestionEntry(
                            "R" + (3000 + new Random().nextInt(999)),
                            getProductNameByCategory(categories.get(0)),
                            categories.get(0),
                            value.getAreaCode(),
                            "A101",
                            "Low sales in " + value.getAreaCode() + " and high demand in A101 for category " + categories.get(0)
                    ));
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        latch.await();
        channel.shutdown();
        return new SalesHeatmapDTO(heatmap, suggestions);
    }

    private List<String> getTopCategories(String areaCode) {
        return switch (areaCode) {
            case "A101" -> List.of("limpeza", "higiene");
            case "B202" -> List.of("laticinios", "bebidas");
            case "C303" -> List.of("limpeza");
            case "D404" -> List.of("carnes", "padaria");
            default -> List.of("geral");
        };
    }

    private String getProductNameByCategory(String category) {
        return switch (category) {
            case "limpeza" -> "Desinfetante Pinho";
            case "laticinios" -> "Iogurte Natural";
            case "bebidas" -> "Suco de Laranja";
            case "higiene" -> "Sabonete Neutro";
            case "carnes" -> "Filé Bovino";
            case "padaria" -> "Pão Integral";
            default -> "Produto Genérico";
        };
    }
}
