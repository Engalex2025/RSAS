package com.retail.smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public class PriceUpdateDTO {

    @NotBlank(message = "Product ID is required")
    @Pattern(regexp = "^R\\d{1,4}$", message = "Product ID must start with 'R' followed by up to 4 digits")
    private String productId;

    @PositiveOrZero(message = "Old price must be zero or greater")
    private double oldPrice;

    @PositiveOrZero(message = "New price must be zero or greater")
    private double newPrice;

    @NotBlank(message = "Updated by is required")
    private String updatedBy;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
