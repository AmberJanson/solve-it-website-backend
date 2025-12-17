package com.example.solveitwebsitebackend.dao;

import java.util.ArrayList;
import java.util.Arrays;
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

    public List<CategoryMapper.RawCategory> fetchRawCategories(List<String> categoriesGithubUrls) {
        List<CategoryMapper.RawCategory> allCategories = new ArrayList<>();

        for (String url : categoriesGithubUrls) {
            try {
                String categoriesJson = restTemplate.getForObject(url, String.class);
                JavaType categoriesType = objectMapper.getTypeFactory().constructCollectionType(List.class, CategoryMapper.RawCategory.class);
                List<CategoryMapper.RawCategory> categories = objectMapper.readValue(categoriesJson, categoriesType);
                allCategories.addAll(categories);
            } catch (RestClientException e) {
                throw new DAOExceptions.FetchException("Failed to fetch Category JSON", e);
            } catch (JsonProcessingException e) {
                throw new DAOExceptions.ParseException("Failed to parse Category JSON", e);
            }
        }

        return allCategories;
    }

    public List<CategoryMapper.NewCategory> mapFetchedCategories(List<String> categoriesGithubUrls) {
        List<CategoryMapper.RawCategory> rawCategoryList = fetchRawCategories(categoriesGithubUrls);
        return rawCategoryList.stream().map(CategoryMapper::map).toList();
    }

    public void printMappedCategories(List<String> urls) {
        try {
            List<CategoryMapper.NewCategory> categories = mapFetchedCategories(urls);
            String categoriesJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(categories);

            System.out.println(categoriesJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to print categories in Json", e);
        }
    }

//    //--Fetch data to print--
//    public JsonNode fetchCategories(String categoryGithubUrl) {
//        try {
//            String categoriesJson = restTemplate.getForObject(categoryGithubUrl, String.class);
//
//            return objectMapper.readTree(categoriesJson);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to fetch or parse Category json-file", e);
//        }
//    }
//
//    //--Print fetched data--
//    public void printFetchedCategories(String url) {
//        JsonNode jsonNode = fetchCategories(url);
//        System.out.println(jsonNode.toPrettyString());
//    }

    public static void main(String[] args) {
        CategoryDAO dao = new CategoryDAO();

        List<String> categoryUrls = Arrays.asList(
                "https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it/refs/heads/main/data/solve-it.json",
                "https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it-examples/refs/heads/main/reorganization_of_techniques/dfrws.json"
        );

        dao.printMappedCategories(categoryUrls);

//        dao.printFetchedCategories(
//                "https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it/refs/heads/main/data/solve-it.json"
//        );
    }
}