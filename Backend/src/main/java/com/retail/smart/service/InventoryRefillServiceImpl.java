package com.retail.smart.service;

import com.retail.smart.dto.ProductDTO;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryRefillServiceImpl extends InventoryRefillGrpc.InventoryRefillImplBase {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestockLogRepository restockLogRepository;

    // === gRPC method ===
    @Override
    public StreamObserver<InventoryRequest> streamDeliveries(StreamObserver<InventoryResponse> responseObserver) {
        List<RestockItem> restockedItems = new ArrayList<>();

        return new StreamObserver<>() {
            @Override
            public void onNext(InventoryRequest request) {
                String productId = request.getProductId();
                int quantity = request.getQuantityReceived();

                Product product = productRepository.findByProductId(productId);
                if (product == null) return;

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

    // === REST methods ===

    public List<ProductDTO> getLowStockProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getQuantity() < p.getMinimumQuantity())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public String requestReplenishment(String productId, int quantity) {
        Product product = productRepository.findByProductId(productId);
        if (product == null) {
            return "Product not found: " + productId;
        }

        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);

        RestockLog log = new RestockLog();
        log.setProduct(product);
        log.setQuantityAdded(quantity);
        log.setTimestamp(LocalDateTime.now());
        restockLogRepository.save(log);

        return "Restock request completed for product: " + productId;
    }

    public List<RestockLog> getRestockHistory(String productId) {
        return restockLogRepository.findByProduct_ProductId(productId);
    }

    public Map<String, String> generateRestockSuggestions() {
        Map<String, String> suggestions = new HashMap<>();
        List<Product> lowStockProducts = productRepository.findAll().stream()
                .filter(p -> p.getQuantity() < p.getMinimumQuantity())
                .collect(Collectors.toList());

        for (Product product : lowStockProducts) {
            String suggestion = "Reorder " + (product.getMinimumQuantity() * 2) +
                    " units of " + product.getName() +
                    " based on current demand trends.";
            suggestions.put(product.getProductId(), suggestion);
        }

        return suggestions;
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setQuantity(product.getQuantity());
        dto.setMinimumQuantity(product.getMinimumQuantity());
        dto.setPrice(product.getPrice().doubleValue()); // Corrigido para double
        return dto;
    }
}
