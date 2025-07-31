package com.retail.smart.service;

import com.retail.smart.grpc.pricing.*;
import com.retail.smart.entity.Product;
import com.retail.smart.repository.ProductRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class SmartPricingServiceImpl extends SmartPricingGrpc.SmartPricingImplBase {

    @Autowired
    private ProductRepository productRepository;

    private final Random random = new Random();

    @Override
    public void getPrice(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        String productId = request.getProductId();

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            responseObserver.onError(new RuntimeException("Product not found"));
            return;
        }

        BigDecimal originalPrice = product.getPrice();
        BigDecimal adjustedPrice;
        String reason;

        int adjustmentType = random.nextInt(4);
        switch (adjustmentType) {
            case 0 -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(0.9));
                reason = "10% discount";
            }
            case 1 -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(0.8));
                reason = "20% discount";
            }
            case 2 -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(1.1));
                reason = "10% increase";
            }
            default -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(1.2));
                reason = "20% increase";
            }
        }

        ProductResponse response = ProductResponse.newBuilder()
                .setProductId(productId)
                .setOriginalPrice(originalPrice.doubleValue())
                .setAdjustedPrice(adjustedPrice.doubleValue())
                .setAdjustmentReason(reason)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePrice(PriceUpdateRequest request, StreamObserver<PriceUpdateResponse> responseObserver) {
        String productId = request.getProductId();
        double newPriceDouble = request.getNewPrice();
        BigDecimal newPrice = BigDecimal.valueOf(newPriceDouble);

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            PriceUpdateResponse response = PriceUpdateResponse.newBuilder()
                    .setProductId(productId)
                    .setSuccess(false)
                    .setMessage("Product not found")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        product.setPrice(newPrice);
        productRepository.save(product);

        PriceUpdateResponse response = PriceUpdateResponse.newBuilder()
                .setProductId(productId)
                .setSuccess(true)
                .setMessage("Price updated successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
