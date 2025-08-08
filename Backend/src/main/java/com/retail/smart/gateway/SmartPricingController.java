package com.retail.smart.gateway;

import com.retail.smart.dto.AutoPriceAdjustmentDTO;
import com.retail.smart.dto.ProductPriceDTO;
import com.retail.smart.entity.Product;
import com.retail.smart.grpc.pricing.*;
import com.retail.smart.repository.ProductRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/pricing")
public class SmartPricingController {

    private final SmartPricingGrpc.SmartPricingBlockingStub blockingStub;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public SmartPricingController() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        this.blockingStub = SmartPricingGrpc.newBlockingStub(channel);
    }

    @GetMapping("/price")
    public ResponseEntity<?> getPrice(@RequestParam String productId) {
        System.out.println(">>> SmartPricingController.getPrice called with: " + productId);

        try {
            Optional<Product> optional = productRepository.findByProductId(productId);
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in database: " + productId);
            }

            Product product = optional.get();

            ProductPriceDTO dto = new ProductPriceDTO(
                    product.getProductId(),
                    product.getName(),
                    product.getPrice().doubleValue(),
                    product.getArea()
            );

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            System.err.println(">>> Error in getPrice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch price: " + e.getMessage());
        }
    }

    @PostMapping("/auto-adjust")
public ResponseEntity<?> autoAdjustPrice(@RequestParam String productId) {
    System.out.println(">>> SmartPricingController.autoAdjustPrice called for: " + productId);

    try {
        Optional<Product> optional = productRepository.findByProductId(productId);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found in database: " + productId);
        }
        Product product = optional.get();

        // Request pricing suggestion from gRPC service
        ProductRequest request = ProductRequest.newBuilder()
                .setProductId(productId)
                .build();

        ProductResponse response = blockingStub.getPrice(request);

        //  Capture the logged-in username
        String currentUser = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .filter(name -> !name.equalsIgnoreCase("anonymousUser"))
                .orElse("system-auto-adjust");

        // Send update request with actual user
        PriceUpdateRequest updateRequest = PriceUpdateRequest.newBuilder()
                .setProductId(productId)
                .setNewPrice(response.getAdjustedPrice())
                .setUpdatedBy(currentUser)
                .build();

        blockingStub.updatePrice(updateRequest);

        // Prepare rich response DTO
        AutoPriceAdjustmentDTO dto = new AutoPriceAdjustmentDTO(
                product.getProductId(),
                product.getName(),
                response.getOriginalPrice(),
                response.getAdjustedPrice(),
                response.getAdjustmentReason(),
                product.getArea(),
                response.getRecommendation()
        );

        return ResponseEntity.ok(dto);

    } catch (Exception e) {
        System.err.println(">>> Error in autoAdjustPrice: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to auto-adjust price: " + e.getMessage());
    }
}

    @GetMapping("/history")
    public ResponseEntity<?> getPriceAdjustmentHistory(@RequestParam(required = false) String productId) {
        System.out.println(">>> SmartPricingController.getPriceAdjustmentHistory called");

        try {
            String sql = """
                SELECT p.product_id, p.name AS product_name, h.old_price, h.new_price,
                       h.updated_by, h.update_time
                FROM price_updates h
                JOIN products p ON p.product_id = h.product_id
                """;

            Object[] params = new Object[]{};

            if (productId != null && !productId.isBlank()) {
                sql += " WHERE p.product_id = ?";
                params = new Object[]{productId};
            }

            sql += " ORDER BY h.update_time DESC";

            var historyList = jdbcTemplate.queryForList(sql, params);

            if (historyList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No price adjustment history found" +
                                (productId != null ? " for product: " + productId : ""));
            }

            return ResponseEntity.ok(historyList);

        } catch (Exception e) {
            System.err.println(">>> Error in getPriceAdjustmentHistory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch price adjustment history: " + e.getMessage());
        }
    }
}
