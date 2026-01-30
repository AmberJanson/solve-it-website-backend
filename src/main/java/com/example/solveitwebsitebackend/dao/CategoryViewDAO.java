package com.example.solveitwebsitebackend.dao;

import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.CategoryViewMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Repository
public class CategoryViewDAO {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<CategoryViewMap.CategoryView> fetchCategoryViews(String categoryViewGithubUrl) {
        try {
            String categoryViewJson = restTemplate.getForObject(categoryViewGithubUrl, String.class);
            JavaType categoryViewType = objectMapper.getTypeFactory().constructCollectionType(List.class, CategoryViewMap.CategoryView.class);
            return objectMapper.readValue(categoryViewJson, categoryViewType);
        } catch (RestClientException e) {
            throw new DAOExceptions.FetchException("Failed to fetch CategoryView JSON", e);
        } catch (JsonProcessingException e) {
            throw new DAOExceptions.ParseException("Failed to parse CategoryView JSON", e);
        }
    }
}
