package com.example.solveitwebsitebackend.controller;

import com.example.solveitwebsitebackend.mapper.CategoryViewMap;
import com.example.solveitwebsitebackend.service.CategoryViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/categoryViews")
public class CategoryViewController {

    private final CategoryViewService service;

    public CategoryViewController(CategoryViewService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Map<String , CategoryViewMap.CategoryView>> getAllCategoryViews() {
        return ResponseEntity.ok(service.getAllCategoryViews());
    }

    @GetMapping(params = "categoryViewId")
    public ResponseEntity<CategoryViewMap.CategoryView> getCategoryViewById(@RequestParam String categoryViewId) {
        return ResponseEntity.ok(service.getCategoryViewById(categoryViewId));
    }
}
