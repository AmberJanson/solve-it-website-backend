package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.mapper.CategoryViewMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryViewService {

    private Map<String, CategoryViewMap.CategoryView> categoryViewCache = new HashMap<>();

    public List<CategoryViewMap.CategoryView> loadCategoryViews(String filepath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(
                new ClassPathResource(filepath).getInputStream(),
                new TypeReference<>() {
                }
        );
    }

    public void refreshCache(String filepath) {

        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            attempts++;

            try {
                System.out.println("Attempt " + attempts + " to refresh categoryViews cache...");

                List<CategoryViewMap.CategoryView> categoryViews = loadCategoryViews(filepath);

                Map<String, CategoryViewMap.CategoryView> newCache = new HashMap<>();
                for (CategoryViewMap.CategoryView categoryView : categoryViews) {
                    newCache.put(categoryView.id, categoryView);
                }

                categoryViewCache = newCache;

                System.out.println("Successfully updated categoryViews cache!");
                return;

            } catch (Exception e) {
                System.err.println("Refresh failed on attempt " + attempts + ": " + e.getMessage());

                if (attempts >= maxRetries) {
                    System.err.println("All retries failed. Keeping existing categoryViews cache.");
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    public CategoryViewMap.CategoryView getCategoryViewById(String id) {
        CategoryViewMap.CategoryView categoryView = categoryViewCache.get(id);
        if (categoryView == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Couldn't find an CategoryView with id: " + id);
        }
        return categoryView;
    }

    public Map<String, CategoryViewMap.CategoryView> getAllCategoryViews() {
        return categoryViewCache;
    }
}
