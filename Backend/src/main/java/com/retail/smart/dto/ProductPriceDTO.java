package com.retail.smart.dto;

public class ProductPriceDTO {

    private String productId;
    private String productName;
    private double currentPrice;
    private String area;

    public ProductPriceDTO() {}

    public ProductPriceDTO(String productId, String productName, double currentPrice, String area) {
        this.productId = productId;
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.area = area;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getArea() {
        return area;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
