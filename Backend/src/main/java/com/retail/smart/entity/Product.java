package com.retail.smart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; 

    @Column(name = "product_id", nullable = false, unique = true)
    private String productId; 

    private String name;

    private int quantity;

    @Column(name = "minimum_quantity")
    private int minimumQuantity;

    private BigDecimal price;

    @Column(name = "last_updated")
    private java.time.LocalDateTime lastUpdated;
}
