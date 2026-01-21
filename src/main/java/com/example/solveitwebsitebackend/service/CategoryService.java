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

    public Map<String, CategoryMapper.NewCategory> setSingleCache(String githubUrl) {

        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            attempts++;

            try {
                System.out.println("Attempt " + attempts + " to refresh single categories cache...");

                List<CategoryMapper.RawCategory> rawCategories = dao.fetchRawCategories(githubUrl);

                Map<String, CategoryMapper.NewCategory> newCache = new HashMap<>();
                for (CategoryMapper.RawCategory category : rawCategories) {

                    CategoryMapper.NewCategory mapped = dao.mapFetchedCategory(category);

                    newCache.put(mapped.id, mapped);
                }

                System.out.println("Successfully retrieved single categories cache!");
                return newCache;

            } catch (DAOExceptions.FetchException | DAOExceptions.ParseException e) {
                System.err.println("Refresh failed on attempt " + attempts + ": " + e.getMessage());

                if (attempts >= maxRetries) {
                    System.err.println("All retries failed. Continue without this categories cache.");
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
        return null;
    }

    public void  refreshCache(Map<String, CategoryMapper.NewCategory> newCache) {
        if (newCache == null || newCache.isEmpty()) {
            System.err.println("No single cache was retrieved. Keeping existing categories cache.");
        } else {
            categoryCache = newCache;
            System.out.println("Successfully updated categories cache!");
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
