package com.retail.smart.gateway;

import com.retail.smart.dto.PriceUpdateDTO;
import com.retail.smart.grpc.pricing.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
public class SmartPricingController {

    private final SmartPricingGrpc.SmartPricingBlockingStub blockingStub;

    public SmartPricingController() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        blockingStub = SmartPricingGrpc.newBlockingStub(channel);
    }

    @GetMapping("/price")
    public ProductResponse getPrice(@RequestParam String productId) {
        ProductRequest request = ProductRequest.newBuilder()
                .setProductId(productId)
                .build();

        return blockingStub.getPrice(request);
    }

    @PostMapping("/price")
    public PriceUpdateResponse updatePrice(@RequestBody PriceUpdateDTO dto) {
        PriceUpdateRequest request = PriceUpdateRequest.newBuilder()
                .setProductId(dto.getProductId())
                .setNewPrice(dto.getNewPrice())
                .setUpdatedBy(dto.getUpdatedBy())
                .build();

        return blockingStub.updatePrice(request);
    }
}
