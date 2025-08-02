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
}
