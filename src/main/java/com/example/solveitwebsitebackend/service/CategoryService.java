package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.dao.CategoryDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.CategoryMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {

    private final CategoryDAO dao;

    private Map<String, CategoryMapper.NewCategory> categoryCache = new HashMap<>();

    public CategoryService(CategoryDAO dao) {
        this.dao = dao;
    }

    public void refreshCache(List<String> githubUrls) {

        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            attempts++;

            try {
                System.out.println("Attempt " + attempts + " to refresh categories cache...");

                List<CategoryMapper.NewCategory> categories = dao.mapFetchedCategories(githubUrls);

                Map<String, CategoryMapper.NewCategory> newCache = new HashMap<>();
                for (CategoryMapper.NewCategory category : categories) {
                    newCache.put(category.id, category);
                }

                categoryCache = newCache;

                System.out.println("Successfully updated categories cache!");
                return;

            } catch (DAOExceptions.FetchException | DAOExceptions.ParseException e) {
                System.err.println("Refresh failed on attempt " + attempts + ": " + e.getMessage());

                if (attempts >= maxRetries) {
                    System.err.println("All retries failed. Keeping existing categories cache.");
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    public CategoryMapper.NewCategory getCategoryById(String id) {
        CategoryMapper.NewCategory category = categoryCache.get(id);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Couldn't find an Category with id: " + id);
        }
        return category;
    }

    public Map<String, CategoryMapper.NewCategory> getAllCategories() {
        return categoryCache;
    }
}
