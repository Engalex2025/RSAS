package com.retail.smart.service;

import com.retail.smart.entity.Product;
import com.retail.smart.grpc.pricing.*;
import com.retail.smart.repository.ProductRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Random;

@Service
public class SmartPricingServiceImpl extends SmartPricingGrpc.SmartPricingImplBase {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();

    @Override
    public void getPrice(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        String productId = request.getProductId();

        Optional<Product> optionalProduct = productRepository.findByProductId(productId);
        if (optionalProduct.isEmpty()) {
            responseObserver.onError(new RuntimeException("Product not found"));
            return;
        }

        Product product = optionalProduct.get();
        BigDecimal originalPrice = product.getPrice();

        if (originalPrice == null) {
            responseObserver.onError(new RuntimeException("Original price is null for product: " + productId));
            return;
        }

        String area = product.getArea() != null ? product.getArea() : "Unknown";
        BigDecimal adjustedPrice;
        String reason;
        String recommendation;

        int adjustmentType = random.nextInt(6); // 0 to 5 (6 possibilities)

        switch (adjustmentType) {
            case 0 -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(0.9));
                reason = "10% discount due to moderate drop in sales in area " + area;
                recommendation = "Monitor stock levels and evaluate future discounts.";
            }
            case 1 -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(0.8));
                reason = "20% discount to stimulate demand in area " + area;
                recommendation = "Promote the product with signage and visibility.";
            }
            case 2 -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(0.7));
                reason = "30% discount for clearance in area " + area;
                recommendation = "Temporary markdown — re-evaluate in 3 days.";
            }
            case 3 -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(1.1));
                reason = "10% price increase due to stable high demand in area " + area;
                recommendation = "Maintain new price and observe sales over the week.";
            }
            case 4 -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(1.2));
                reason = "20% price increase due to rising supply costs in area " + area;
                recommendation = "Check competitor prices to ensure competitiveness.";
            }
            default -> {
                adjustedPrice = originalPrice.multiply(BigDecimal.valueOf(1.3));
                reason = "30% price increase due to limited stock in area " + area;
                recommendation = "Urgent restocking recommended if sales remain high.";
            }
        }

        adjustedPrice = adjustedPrice.setScale(2, RoundingMode.HALF_UP);

        ProductResponse response = ProductResponse.newBuilder()
                .setProductId(productId)
                .setOriginalPrice(originalPrice.doubleValue())
                .setAdjustedPrice(adjustedPrice.doubleValue())
                .setAdjustmentReason(reason)
                .setRecommendation(recommendation)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


   @Override
public void updatePrice(PriceUpdateRequest request, StreamObserver<PriceUpdateResponse> responseObserver) {
    String productId = request.getProductId();
    double newPriceDouble = request.getNewPrice();
    BigDecimal newPrice = BigDecimal.valueOf(newPriceDouble);

    Optional<Product> optionalProduct = productRepository.findByProductId(productId);
    if (optionalProduct.isEmpty()) {
        PriceUpdateResponse response = PriceUpdateResponse.newBuilder()
                .setProductId(productId)
                .setSuccess(false)
                .setMessage("Product not found")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        return;
    }

    Product product = optionalProduct.get();
    BigDecimal oldPrice = product.getPrice();
    product.setPrice(newPrice);
    productRepository.save(product);

    // Always use the value provided by the controller
    String updatedByUser = request.getUpdatedBy() != null && !request.getUpdatedBy().isBlank()
            ? request.getUpdatedBy()
            : "unknown";

    try {
        jdbcTemplate.update(
                "INSERT INTO price_updates (product_id, old_price, new_price, updated_by) VALUES (?, ?, ?, ?)",
                productId, oldPrice, newPrice, updatedByUser
        );
    } catch (Exception e) {
        System.err.println("⚠ Failed to insert into price_updates: " + e.getMessage());
    }

    PriceUpdateResponse response = PriceUpdateResponse.newBuilder()
            .setProductId(productId)
            .setSuccess(true)
            .setMessage("Price updated successfully")
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
}
}
