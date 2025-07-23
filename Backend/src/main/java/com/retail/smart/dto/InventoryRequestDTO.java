package com.retail.smart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class InventoryRequestDTO {

    @NotBlank(message = "Product ID is required")
    @Pattern(regexp = "^R\\d{1,4}$", message = "Product ID must start with 'R' followed by up to 4 digits")
    private String productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
