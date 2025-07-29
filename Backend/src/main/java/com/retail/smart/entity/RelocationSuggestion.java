package com.retail.smart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    private String category;

    @Column(name = "from_area")
    private String fromArea;

    @Column(name = "to_area")
    private String toArea;

    private String reason;

    @Column(name = "suggestion_time")
    private LocalDateTime suggestionTime = LocalDateTime.now();

    public RelocationSuggestion() {}

    public RelocationSuggestion(String productId, String productName, String category,
                                 String fromArea, String toArea, String reason) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.fromArea = fromArea;
        this.toArea = toArea;
        this.reason = reason;
        this.suggestionTime = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public String getFromArea() { return fromArea; }
    public String getToArea() { return toArea; }
    public String getReason() { return reason; }
    public LocalDateTime getSuggestionTime() { return suggestionTime; }

    // Setters (if needed)
    public void setProductId(String productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setCategory(String category) { this.category = category; }
    public void setFromArea(String fromArea) { this.fromArea = fromArea; }
    public void setToArea(String toArea) { this.toArea = toArea; }
    public void setReason(String reason) { this.reason = reason; }
    public void setSuggestionTime(LocalDateTime suggestionTime) { this.suggestionTime = suggestionTime; }
} 
