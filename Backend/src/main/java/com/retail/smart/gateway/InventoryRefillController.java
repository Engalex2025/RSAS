package com.retail.smart.gateway;

import com.retail.smart.dto.RecentRestockDTO;
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
     * Manual refill – only productId required.
     * The system will calculate the refill quantity based on the product area.
     */
    @PostMapping("/manual-refill")
    public String manualRefill(@RequestParam String productId) {
        return inventoryService.manualRefill(productId);
    }

    /**
     * Request manual replenishment with a specific quantity.
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
     * Get all products restocked in the last 24 hours.
     */
    @GetMapping("/recent-restocks")
    public List<RecentRestockDTO> getRecentRestocks() {
        return inventoryService.getRecentRestocksDTO();
    }

    /**
     * Notify purchasing department about products near minimum stock level.
     * Triggered when a product is 10 units or less above the minimum.
     */
    @GetMapping("/notify-purchasing")
    public Map<String, String> notifyPurchasingDepartment() {
        return inventoryService.notifyPurchasingDepartment();
    }
}
