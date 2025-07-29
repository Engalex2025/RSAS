package com.retail.smart.gateway;

import com.retail.smart.dto.SalesHeatmapDTO;
import com.retail.smart.dto.SalesHeatmapDTO.HeatmapEntry;
import com.retail.smart.dto.SalesHeatmapDTO.RelocationSuggestionEntry;
import com.retail.smart.entity.Category;
import com.retail.smart.entity.RelocationSuggestion;
import com.retail.smart.grpc.sales.SalesHeatmapGrpc;
import com.retail.smart.grpc.sales.SalesRequest;
import com.retail.smart.grpc.sales.SalesAreaPerformance;
import com.retail.smart.repository.CategoryRepository;
import com.retail.smart.repository.RelocationSuggestionRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@RestController
public class SalesHeatmapController {

    private final CategoryRepository categoryRepository;
    private final RelocationSuggestionRepository relocationSuggestionRepository;

    public SalesHeatmapController(CategoryRepository categoryRepository,
                                  RelocationSuggestionRepository relocationSuggestionRepository) {
        this.categoryRepository = categoryRepository;
        this.relocationSuggestionRepository = relocationSuggestionRepository;
    }

    @GetMapping("/api/sales/heatmap")
    public SalesHeatmapDTO getSalesHeatmap(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int week
    ) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        SalesHeatmapGrpc.SalesHeatmapStub stub = SalesHeatmapGrpc.newStub(channel);

        List<HeatmapEntry> heatmap = new ArrayList<>();
        List<RelocationSuggestionEntry> suggestions = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        stub.getHeatmap(SalesRequest.newBuilder()
                .setRequestTime("WEEK_" + week)
                .build(), new StreamObserver<>() {

            final Map<String, Integer> salesMap = new HashMap<>();

            @Override
            public void onNext(SalesAreaPerformance value) {
                List<String> categories = getTopCategories(value.getAreaCode());
                salesMap.put(value.getAreaCode(), value.getTotalSales());

                heatmap.add(new HeatmapEntry(
                        value.getAreaCode(),
                        value.getTotalSales(),
                        categories,
                        value.getSuggestion()
                ));

                if (value.getTotalSales() < 100) {
                    String chosenCategory = null;

                    if (category != null && categories.contains(category.toLowerCase())) {
                        chosenCategory = category.toLowerCase();
                    } else if (category == null && !categories.isEmpty()) {
                        chosenCategory = categories.get(0);
                    }

                    if (chosenCategory != null) {
                        String toArea = salesMap.entrySet().stream()
                                .filter(e -> !e.getKey().equals(value.getAreaCode()))
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse("A101");

                        if (!toArea.equals(value.getAreaCode())) {
                            String productId = "R" + (3000 + new Random().nextInt(999));
                            String productName = getProductNameByCategory(chosenCategory);
                            String reason = "Low sales in " + value.getAreaCode() +
                                    " and high demand in " + toArea + " for category " + chosenCategory;

                            suggestions.add(new RelocationSuggestionEntry(
                                    productId,
                                    productName,
                                    chosenCategory,
                                    value.getAreaCode(),
                                    toArea,
                                    reason
                            ));

                            relocationSuggestionRepository.save(new RelocationSuggestion(
                                    productId,
                                    productName,
                                    chosenCategory,
                                    value.getAreaCode(),
                                    toArea,
                                    reason
                            ));
                        }
                    }
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
