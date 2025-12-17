package com.example.solveitwebsitebackend.controller;

import com.example.solveitwebsitebackend.mapper.CategoryMapper;
import com.example.solveitwebsitebackend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String , CategoryMapper.NewCategory>> getAllCategories() {
        return ResponseEntity.ok(service.getAllCategories());
    }

    @GetMapping(params = "categoryId")
    public ResponseEntity<CategoryMapper.NewCategory> getCategoryById(@RequestParam String categoryId) {
        return ResponseEntity.ok(service.getCategoryById(categoryId));
    }
}
