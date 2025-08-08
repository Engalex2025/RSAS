package com.retail.smart.gateway;

import com.retail.smart.dto.SalesHeatmapDTO;
import com.retail.smart.dto.SalesHeatmapDTO.HeatmapEntry;
import com.retail.smart.dto.SalesHeatmapDTO.RelocationSuggestionEntry;
import com.retail.smart.entity.RelocationSuggestion;
import com.retail.smart.grpc.sales.SalesAreaPerformance;
import com.retail.smart.grpc.sales.SalesHeatmapGrpc;
import com.retail.smart.grpc.sales.SalesRequest;
import com.retail.smart.repository.RelocationSuggestionRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@RestController
public class SalesHeatmapController {

    private final RelocationSuggestionRepository relocationSuggestionRepository;

    public SalesHeatmapController(RelocationSuggestionRepository relocationSuggestionRepository) {
        this.relocationSuggestionRepository = relocationSuggestionRepository;
    }

    @GetMapping("/api/sales/heatmap")
    public SalesHeatmapDTO getSalesHeatmap(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int week) throws InterruptedException {
        return computeFullHeatmap(category, week);
    }

    @GetMapping("/api/sales/heatmap/areas")
    public List<HeatmapEntry> getHeatmapAreas(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int week) throws InterruptedException {
        return computeFullHeatmap(category, week).getHeatmap();
    }

    @GetMapping("/api/sales/heatmap/relocations")
    public List<RelocationSuggestionEntry> getRelocationSuggestions(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int week) throws InterruptedException {
        return computeFullHeatmap(category, week).getRelocationSuggestions();
    }

    private SalesHeatmapDTO computeFullHeatmap(String category, int week) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        SalesHeatmapGrpc.SalesHeatmapStub stub = SalesHeatmapGrpc.newStub(channel);

        List<HeatmapEntry> heatmap = new ArrayList<>();
        List<RelocationSuggestionEntry> suggestions = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Map<String, Integer> salesMap = new HashMap<>();
        Map<String, Integer> previousSalesMap = new HashMap<>();

        // Load previous week's sales
        ManagedChannel previewChannel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        SalesHeatmapGrpc.SalesHeatmapBlockingStub previewStub = SalesHeatmapGrpc.newBlockingStub(previewChannel);

        SalesRequest previousRequest = SalesRequest.newBuilder()
                .setRequestTime("WEEK_" + (week - 1))
                .build();

        previewStub.getHeatmap(previousRequest).forEachRemaining(area -> {
            previousSalesMap.put(area.getAreaCode(), area.getTotalSales());
        });
        previewChannel.shutdown();

        stub.getHeatmap(SalesRequest.newBuilder()
                .setRequestTime("WEEK_" + week)
                .build(), new StreamObserver<>() {

                    @Override
                    public void onNext(SalesAreaPerformance value) {
                        List<String> categories = value.getTopCategoriesList();
                        salesMap.put(value.getAreaCode(), value.getTotalSales());

                        heatmap.add(new HeatmapEntry(
                                value.getAreaCode(),
                                value.getTotalSales(),
                                categories,
                                value.getSuggestion()));

                        if (value.getTotalSales() < 100) {
                            String chosenCategory = null;

                            if (category != null && categories.contains(category.toLowerCase())) {
                                chosenCategory = category.toLowerCase();
                            } else if (category == null && !categories.isEmpty()) {
                                chosenCategory = categories.get(0);
                            }

                            if (chosenCategory != null) {
                                String fromArea = value.getAreaCode();
                                int fromSales = value.getTotalSales();
                                int fromPrevious = previousSalesMap.getOrDefault(fromArea, fromSales);
                                int dropPercent = getPercentageDrop(fromPrevious, fromSales);

                                Optional<Map.Entry<String, Integer>> toAreaEntry = salesMap.entrySet().stream()
                                        .filter(e -> {
                                            String area = e.getKey();
                                            int current = e.getValue();
                                            int previous = previousSalesMap.getOrDefault(area, current);
                                            return !area.equals(fromArea) && current >= previous;
                                        })
                                        .max(Map.Entry.comparingByValue());

                                if (toAreaEntry.isPresent()) {
                                    String toArea = toAreaEntry.get().getKey();
                                    int toCurrent = toAreaEntry.get().getValue();
                                    int toPrevious = previousSalesMap.getOrDefault(toArea, toCurrent);
                                    int risePercent = getPercentageRise(toPrevious, toCurrent);

                                    String productId = "R" + (3000 + new Random().nextInt(999));
                                    String productName = getProductNameByCategory(chosenCategory);

                                    String reason = String.format(
                                            "Sales of %s products in %s dropped %d%% compared to previous week. %s had a %d%% rise. Suggested move to rebalance demand.",
                                            chosenCategory, fromArea, dropPercent, toArea, risePercent);

                                    suggestions.add(new RelocationSuggestionEntry(
                                            productId,
                                            productName,
                                            chosenCategory,
                                            fromArea,
                                            toArea,
                                            reason));

                                    relocationSuggestionRepository.save(new RelocationSuggestion(
                                            productId,
                                            productName,
                                            chosenCategory,
                                            fromArea,
                                            toArea,
                                            reason,
                                            week));
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

    private int getPercentageDrop(int previous, int current) {
        if (previous == 0)
            return 100;
        return (int) Math.round(((double) (previous - current) / previous) * 100);
    }

    private int getPercentageRise(int previous, int current) {
        if (previous == 0)
            return 100;
        return (int) Math.round(((double) (current - previous) / previous) * 100);
    }
}
