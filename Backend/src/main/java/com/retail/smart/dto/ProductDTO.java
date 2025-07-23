package com.retail.smart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public class ProductDTO {

    @NotBlank(message = "Product ID is required")
    @Pattern(regexp = "^R\\d{1,4}$", message = "Product ID must start with 'R' followed by up to 4 digits")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String name;

    @Min(value = 0, message = "Quantity must be zero or more")
    private int quantity;

    @Min(value = 0, message = "Minimum quantity must be zero or more")
    private int minimumQuantity;

    @PositiveOrZero(message = "Price must be zero or greater")
    private double price;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
