package com.retail.smart.gateway;

import com.retail.smart.dto.InventoryRequestDTO;
import com.retail.smart.service.InventoryRefillServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryRefillController {

    private final InventoryRefillServiceImpl inventoryService;

    public InventoryRefillController(InventoryRefillServiceImpl inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/refill")
    public ResponseEntity<String> refillInventory(@Valid @RequestBody InventoryRequestDTO request) {
        inventoryService.refillInventory(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok("Inventory refill request processed.");
    }
}
