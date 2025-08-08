package com.retail.smart.gateway;

import com.retail.smart.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping
    public String addProduct(@RequestBody ProductDTO productDTO) {
        String sql = "INSERT INTO products (product_id, name, quantity, minimum_quantity, price) VALUES (?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql,
                    productDTO.getProductId(),
                    productDTO.getName(),
                    productDTO.getQuantity(),
                    productDTO.getMinimumQuantity(),
                    productDTO.getPrice()
            );
            return "Product added successfully.";
        } catch (Exception e) {
            return "Failed to add product: " + e.getMessage();
        }
    }

  
    @PutMapping("/{productId}")
    public String updateProduct(@PathVariable String productId, @RequestBody ProductDTO dto) {
        String sql = "UPDATE products SET name = ?, quantity = ?, minimum_quantity = ?, price = ?, area = ? WHERE product_id = ?";

        try {
            int rows = jdbcTemplate.update(sql,
                    dto.getName(),
                    dto.getQuantity(),
                    dto.getMinimumQuantity(),
                    dto.getPrice(),
                    dto.getArea(),
                    productId
            );
            if (rows == 0) {
                return "No product found with ID: " + productId;
            }
            return "Product updated successfully.";
        } catch (Exception e) {
            return "Failed to update product: " + e.getMessage();
        }
    }

    @PutMapping("/{productId}/area")
    public String updateProductArea(@PathVariable String productId, @RequestParam String area) {
        String sql = "UPDATE products SET area = ? WHERE product_id = ?";

        try {
            int rows = jdbcTemplate.update(sql, area, productId);
            if (rows == 0) {
                return "No product found with ID: " + productId;
            }
            return "Product area updated successfully.";
        } catch (Exception e) {
            return "Failed to update product area: " + e.getMessage();
        }
    }
}
