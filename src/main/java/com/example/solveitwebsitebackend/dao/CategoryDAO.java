package com.example.solveitwebsitebackend.dao;

import java.util.List;

import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.CategoryMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
public class CategoryDAO {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<CategoryMapper.RawCategory> fetchRawCategories(String categoriesGithubUrl) {
        try {
            String categoriesJson = restTemplate.getForObject(categoriesGithubUrl, String.class);
            JavaType categoriesType = objectMapper.getTypeFactory().constructCollectionType(List.class, CategoryMapper.RawCategory.class);
            return objectMapper.readValue(categoriesJson, categoriesType);
        } catch (RestClientException e) {
            throw new DAOExceptions.FetchException("Failed to fetch Category JSON", e);
        } catch (JsonProcessingException e) {
            throw new DAOExceptions.ParseException("Failed to parse Category JSON", e);
        }
    }

    public CategoryMapper.NewCategory mapFetchedCategory(CategoryMapper.RawCategory category) {
        return CategoryMapper.map(category);
    }
}