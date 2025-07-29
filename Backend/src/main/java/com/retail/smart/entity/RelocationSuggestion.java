package com.retail.smart.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "relocation_suggestions")
public class RelocationSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "category")
    private String category;

    @Column(name = "from_area")
    private String fromArea;

    @Column(name = "to_area")
    private String toArea;

    @Column(name = "reason")
    private String reason;

    @Column(name = "week")
    private int week;

    // Constructors

    public RelocationSuggestion() {
    }

    public RelocationSuggestion(String productId, String productName, String category,
                                String fromArea, String toArea, String reason, int week) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.fromArea = fromArea;
        this.toArea = toArea;
        this.reason = reason;
        this.week = week;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFromArea() {
        return fromArea;
    }

    public void setFromArea(String fromArea) {
        this.fromArea = fromArea;
    }

    public String getToArea() {
        return toArea;
    }

    public void setToArea(String toArea) {
        this.toArea = toArea;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }
}
