package com.example.solveitwebsitebackend.dao;

import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.WeaknessMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Repository
public class WeaknessDAO {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<WeaknessMapper.RawWeakness> fetchRawWeaknesses(String weaknessGithubUrl) {
        try {
            JsonNode filesArray = objectMapper.readTree(restTemplate.getForObject(weaknessGithubUrl, String.class));

            List<WeaknessMapper.RawWeakness> weaknesses = new ArrayList<>();

            for (JsonNode fileNode : filesArray) {
                if ("file".equals(fileNode.get("type").asText()) && fileNode.get("name").asText().endsWith(".json")) {
                    String singleUrl = fileNode.get("download_url").asText();

                    String weaknessJson = restTemplate.getForObject(singleUrl, String.class);
                    JavaType weaknessType = objectMapper.getTypeFactory().constructType(WeaknessMapper.RawWeakness.class);

                    WeaknessMapper.RawWeakness rawWeakness = objectMapper.readValue(weaknessJson, weaknessType);
                    weaknesses.add(rawWeakness);
                }
            }

            return weaknesses;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weaknesses dynamically");
        }
    }

    public List<WeaknessMapper.NewWeakness> mapFetchedWeaknesses(String weaknessGithubUrl) {
        List<WeaknessMapper.RawWeakness> rawWeaknesses = fetchRawWeaknesses(weaknessGithubUrl);
        return rawWeaknesses.stream().map(WeaknessMapper::map).toList();
    }

    public void printMappedWeaknesses(String url) {
        try {
            List<WeaknessMapper.NewWeakness> weaknesses = mapFetchedWeaknesses(url);
            String weaknessJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(weaknesses);

            System.out.println(weaknessJson);
        } catch (RestClientException e) {
            throw new DAOExceptions.FetchException("Failed to fetch Objective JSON", e);
        } catch (JsonProcessingException e) {
            throw new DAOExceptions.ParseException("Failed to parse Objective JSON", e);
        }
    }

//    //--Fetch data to print--
//    public List<JsonNode> fetchWeaknesses(String weaknessGithubUrl) {
//        try {
//            JsonNode filesArray = objectMapper.readTree(restTemplate.getForObject(weaknessGithubUrl, String.class));
//
//            List<JsonNode> weaknesses = new ArrayList<>();
//
//            for (JsonNode fileNode : filesArray) {
//                if ("file".equals(fileNode.get("type").asText()) && fileNode.get("name").asText().endsWith(".json")) {
//                    String singleUrl = fileNode.get("download_url").asText();
//                    String jsonContent = restTemplate.getForObject(singleUrl, String.class);
//
//                    JsonNode weaknessNode = objectMapper.readTree(jsonContent);
//                    weaknesses.add(weaknessNode);
//                }
//            }
//
//            return weaknesses;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to fetch weaknesses dynamically");
//        }
//    }
//
//    //--Print fetched data--
//    public void printFetchedWeaknesses(String url) {
//        List<JsonNode> jsonNodes = fetchWeaknesses(url);
//        for (JsonNode jsonNode : jsonNodes) {
//            System.out.println(jsonNode.toPrettyString());
//        }
//    }

    public static void main(String[] args) {
        WeaknessDAO dao = new WeaknessDAO();

        dao.printMappedWeaknesses(
                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/weaknesses/"
        );
//        dao.printFetchedWeaknesses(
//                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/weaknesses/"
//        );
    }
}
