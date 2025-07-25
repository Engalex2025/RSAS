package com.retail.smart.gateway;

import com.retail.smart.dto.SalesHeatmapDTO;
import com.retail.smart.dto.SalesHeatmapDTO.HeatmapEntry;
import com.retail.smart.dto.SalesHeatmapDTO.RelocationSuggestionEntry;
import com.retail.smart.entity.Category;
import com.retail.smart.grpc.sales.SalesHeatmapGrpc;
import com.retail.smart.grpc.sales.SalesRequest;
import com.retail.smart.grpc.sales.SalesAreaPerformance;
import com.retail.smart.repository.CategoryRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@RestController
public class SalesHeatmapController {

    private final CategoryRepository categoryRepository;

    public SalesHeatmapController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/api/sales/heatmap")
    public SalesHeatmapDTO getSalesHeatmap() throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        SalesHeatmapGrpc.SalesHeatmapStub stub = SalesHeatmapGrpc.newStub(channel);

        List<HeatmapEntry> heatmap = new ArrayList<>();
        List<RelocationSuggestionEntry> suggestions = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        List<String> areaCodes = List.of("A101", "B202", "C303", "D404");

        for (String areaCode : areaCodes) {
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
        }

        latch.await();
        channel.shutdown();
        return new SalesHeatmapDTO(heatmap, suggestions);
    }

    private List<String> getTopCategories(String areaCode) {
        List<Category> allCategories = categoryRepository.findAll();
        if (allCategories.isEmpty()) {
            return List.of("general");
        }
        return allCategories.stream()
                .map(Category::getName)
                .limit(2)
                .collect(Collectors.toList());
    }

    private String getProductNameByCategory(String category) {
        return switch (category.toLowerCase()) {
            case "cleaning" -> "Pine Disinfectant";
            case "dairy" -> "Natural Yogurt";
            case "beverages" -> "Orange Juice";
            case "hygiene" -> "Neutral Soap";
            case "meat" -> "Beef Fillet";
            case "bakery" -> "Whole Wheat Bread";
            default -> "Generic Product";
        };
    }
}
