package com.retail.smart.service;

import com.retail.smart.grpc.inventory.InventoryRefillGrpc;
import com.retail.smart.grpc.inventory.InventoryRequest;
import com.retail.smart.grpc.inventory.InventoryResponse;
import com.retail.smart.grpc.inventory.RestockItem;
import com.retail.smart.entity.Product;
import com.retail.smart.entity.RestockLog;
import com.retail.smart.repository.ProductRepository;
import com.retail.smart.repository.RestockLogRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryRefillServiceImpl extends InventoryRefillGrpc.InventoryRefillImplBase {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestockLogRepository restockLogRepository;

    @Override
    public StreamObserver<InventoryRequest> streamDeliveries(StreamObserver<InventoryResponse> responseObserver) {
        List<RestockItem> restockedItems = new ArrayList<>();

        return new StreamObserver<>() {

            @Override
            public void onNext(InventoryRequest request) {
                String productId = request.getProductId();
                int quantity = request.getQuantityReceived();

                Product product = productRepository.findById(productId).orElse(null);

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
                        .setReorderTriggered(product.getQuantity() < 10)
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

    public void refillInventory(String productId, int quantity) {
        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);

        RestockLog log = new RestockLog();
        log.setProduct(product);
        log.setQuantityAdded(quantity);
        log.setTimestamp(LocalDateTime.now());
        restockLogRepository.save(log);
    }
}
