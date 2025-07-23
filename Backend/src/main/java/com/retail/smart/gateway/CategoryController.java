package com.retail.smart.gateway;

import com.retail.smart.dto.CategoryDTO;
import com.retail.smart.entity.Category;
import com.retail.smart.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    public ResponseEntity<String> addCategory(@Valid @RequestBody CategoryDTO dto) {
        if (categoryRepository.existsById(dto.getName())) {
            return ResponseEntity.badRequest().body("Category already exists.");
        }
        categoryRepository.save(new Category(dto.getName()));
        return ResponseEntity.ok("Category added.");
    }

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
