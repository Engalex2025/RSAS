package com.retail.smart.dto;

public class AutoPriceAdjustmentDTO {

    private String productId;
    private String productName;
    private double originalPrice;
    private double adjustedPrice;
    private String adjustmentReason;
    private String area;
    private String recommendation;

    public AutoPriceAdjustmentDTO(String productId, String productName, double originalPrice,
                                  double adjustedPrice, String adjustmentReason,
                                  String area, String recommendation) {
        this.productId = productId;
        this.productName = productName;
        this.originalPrice = originalPrice;
        this.adjustedPrice = adjustedPrice;
        this.adjustmentReason = adjustmentReason;
        this.area = area;
        this.recommendation = recommendation;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public double getAdjustedPrice() {
        return adjustedPrice;
    }

    public String getAdjustmentReason() {
        return adjustmentReason;
    }

    public String getArea() {
        return area;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
