package com.retail.smart.service;

import com.retail.smart.dto.RecentRestockDTO;
import com.retail.smart.entity.Product;
import com.retail.smart.entity.RestockLog;
import com.retail.smart.grpc.inventory.InventoryRefillGrpc;
import com.retail.smart.grpc.inventory.InventoryRequest;
import com.retail.smart.grpc.inventory.InventoryResponse;
import com.retail.smart.grpc.inventory.RestockItem;
import com.retail.smart.repository.ProductRepository;
import com.retail.smart.repository.RestockLogRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class InventoryRefillServiceImpl extends InventoryRefillGrpc.InventoryRefillImplBase {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestockLogRepository restockLogRepository;
    // Deque to hold scheduled alerts for purchasing department
    private final Deque<String> scheduledAlerts = new ConcurrentLinkedDeque<>();
    // Maximum number of alerts to keep in memory
    private static final int MAX_ALERTS_STORED = 100;

    // Fixed sales data copied from SalesHeatmapServiceImpl
    private static final Map<String, Integer> fixedSales = Map.of(
            "A101", 160,
            "B202", 150,
            "C303", 60,
            "D404", 100);

    @Override
    public StreamObserver<InventoryRequest> streamDeliveries(StreamObserver<InventoryResponse> responseObserver) {
        List<RestockItem> restockedItems = new ArrayList<>();

        return new StreamObserver<>() {
            @Override
            public void onNext(InventoryRequest request) {
                String productId = request.getProductId();
                int quantity = request.getQuantityReceived();

                Optional<Product> optional = productRepository.findByProductId(productId);
                if (optional.isEmpty())
                    return;

                Product product = optional.get();
                product.setQuantity(product.getQuantity() + quantity);
                productRepository.save(product);

                RestockLog log = new RestockLog();
                log.setProduct(product);
                log.setQuantityAdded(quantity);
                log.setTimestamp(LocalDateTime.now());
                restockLogRepository.save(log);

                RestockItem item = RestockItem.newBuilder()
                        .setProductId(productId)
                        .setCurrentQuantity(product.getQuantity())
                        .setReorderTriggered(product.getQuantity() < product.getMinimumQuantity())
                        .build();

                restockedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Stream error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                InventoryResponse response = InventoryResponse.newBuilder()
                        .setMessage("Inventory updated successfully.")
                        .addAllRestockedItems(restockedItems)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    public String requestReplenishment(String productId, int quantity) {
        Optional<Product> optional = productRepository.findByProductId(productId);
        if (optional.isEmpty()) {
            return "Product not found: " + productId;
        }

        Product product = optional.get();
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);

        RestockLog log = new RestockLog();
        log.setProduct(product);
        log.setQuantityAdded(quantity);
        log.setTimestamp(LocalDateTime.now());
        restockLogRepository.save(log);

        return "Restock request completed for product: " + productId;
    }

    public String manualRefill(String productId) {
        Optional<Product> optional = productRepository.findByProductId(productId);
        if (optional.isEmpty()) {
            return "Product not found: " + productId;
        }

        Product product = optional.get();

        if (product.getArea() == null || product.getArea().isBlank()) {
            return "Product area is not defined for product: " + productId;
        }

        int refillQuantity = decideRefillQuantity(product.getArea());

        product.setQuantity(product.getQuantity() + refillQuantity);
        productRepository.save(product);

        RestockLog log = new RestockLog();
        log.setProduct(product);
        log.setQuantityAdded(refillQuantity);
        log.setTimestamp(LocalDateTime.now());
        restockLogRepository.save(log);

        return "Manual refill completed for product: " + productId +
                ". Quantity added: " + refillQuantity;
    }

    private int decideRefillQuantity(String areaCode) {
        Integer sales = fixedSales.get(areaCode);
        if (sales == null)
            return 100;

        if (sales >= 140)
            return 150;
        if (sales >= 100)
            return 100;
        return 50;
    }

    public List<RestockLog> getRestockHistory(String productId) {
        return restockLogRepository.findByProduct_ProductId(productId);
    }

    public List<RecentRestockDTO> getRecentRestocksDTO() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return restockLogRepository.findByTimestampAfter(since).stream()
                .map(log -> {
                    RecentRestockDTO dto = new RecentRestockDTO();
                    dto.setProductId(log.getProduct().getProductId());
                    dto.setProductName(log.getProduct().getName());
                    dto.setQuantityAdded(log.getQuantityAdded());
                    dto.setTimestamp(log.getTimestamp());
                    return dto;
                }).collect(Collectors.toList());
    }

    public Map<String, String> notifyPurchasingDepartment() {
        Map<String, String> notifications = new HashMap<>();

        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            int diff = product.getQuantity() - product.getMinimumQuantity();
            if (diff > 0 && diff <= 10) {
                String message = "Product '" + product.getName() + "' (ID: " + product.getProductId() +
                        ") is approaching minimum stock. Current: " + product.getQuantity() +
                        ", Minimum: " + product.getMinimumQuantity() +
                        ". Please evaluate restocking soon.";
                notifications.put(product.getProductId(), message);

                System.out.println("📢 [NOTICE] Sent alert: " + message);
            }
        }

        return notifications;
    }

    @Scheduled(fixedRate = 60000)
    public void simulateSalesAndRestocking() {
        List<Product> allProducts = productRepository.findAll();

        for (Product product : allProducts) {
            int sold = ThreadLocalRandom.current().nextInt(1, 11);
            int newQuantity = product.getQuantity() - sold;
            product.setQuantity(Math.max(newQuantity, 0));
            productRepository.save(product);

            storeAlert(" Sold " + sold + " units of " + product.getName()
                    + ". New quantity: " + product.getQuantity());

            int diff = product.getQuantity() - product.getMinimumQuantity();

            if (diff > 0 && diff <= 10) {
                System.out.println(" [NOTICE] '" + product.getName() +
                        "' is nearing minimum stock. Notify purchasing department.");
            }

            if (product.getQuantity() <= product.getMinimumQuantity()) {
                System.out.println("[ALERT] '" + product.getName() +
                        "' is below minimum stock. Awaiting manual refill.");
            }
        }
    }

    private void storeAlert(String msg) {
        scheduledAlerts.addLast(msg);
        if (scheduledAlerts.size() > MAX_ALERTS_STORED) {
            scheduledAlerts.removeFirst();
        }
        System.out.println(msg);
    }

    public List<String> getScheduledAlerts() {
        return new ArrayList<>(scheduledAlerts);
    }
}
