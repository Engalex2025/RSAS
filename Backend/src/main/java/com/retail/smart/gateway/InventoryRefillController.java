package com.retail.smart.gateway;

import com.retail.smart.dto.ProductDTO;
import com.retail.smart.entity.RestockLog;
import com.retail.smart.service.InventoryRefillServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryRefillController {

    @Autowired
    private InventoryRefillServiceImpl inventoryService;

    /**
     * Get all products with quantity below the minimum threshold.
     */
    @GetMapping("/low-stock")
    public List<ProductDTO> getLowStockProducts() {
        return inventoryService.getLowStockProducts();
    }

    /**
     * Request manual replenishment for a product.
     */
    @PostMapping("/request-replenishment")
    public String requestReplenishment(
            @RequestParam String productId,
            @RequestParam int quantity
    ) {
        return inventoryService.requestReplenishment(productId, quantity);
    }

    /**
     * View restocking history for a product.
     */
    @GetMapping("/restock-history")
    public List<RestockLog> getRestockHistory(@RequestParam String productId) {
        return inventoryService.getRestockHistory(productId);
    }

    /**
     * Get automated restock suggestions for low-stock products.
     */
    @GetMapping("/suggestions")
    public Map<String, String> getSuggestions() {
        return inventoryService.generateRestockSuggestions();
    }
}
