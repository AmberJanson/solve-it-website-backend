package com.example.solveitwebsitebackend.dao;

import com.example.solveitwebsitebackend.mapper.TechniqueMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TechniqueDAO {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TechniqueMapper.RawTechnique> fetchRawTechniques(String techniqueGithubUrl) {
        try {
            JsonNode filesArray = objectMapper.readTree(restTemplate.getForObject(techniqueGithubUrl, String.class));

            List<TechniqueMapper.RawTechnique> techniques = new ArrayList<>();

            for (JsonNode fileNode : filesArray) {
                if ("file".equals(fileNode.get("type").asText()) && fileNode.get("name").asText().endsWith(".json")) {
                    String singleUrl = fileNode.get("download_url").asText();

                    String techniqueJson = restTemplate.getForObject(singleUrl, String.class);
                    JavaType techniqueType = objectMapper.getTypeFactory().constructType(TechniqueMapper.RawTechnique.class);

                    TechniqueMapper.RawTechnique rawTechnique = objectMapper.readValue(techniqueJson, techniqueType);
                    techniques.add(rawTechnique);
                }
            }

            return techniques;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch techniques dynamically");
        }
    }

    public List<TechniqueMapper.NewTechnique> mapFetchedTechniques(String techniquesGithubUrl) {
        List<TechniqueMapper.RawTechnique> rawTechniques = fetchRawTechniques(techniquesGithubUrl);
        return rawTechniques.stream().map(TechniqueMapper::map).toList();
    }

    public void printMappedTechniques(String url) {
        try {
            List<TechniqueMapper.NewTechnique> techniques = mapFetchedTechniques(url);
            String techniquesJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(techniques);

            System.out.println(techniquesJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to print techniques in Json", e);
        }
    }

//    //--Fetch data to print
//    public List<JsonNode> fetchTechniques(String techniqueGithubUrl) {
//        try {
//            JsonNode filesArray = objectMapper.readTree(restTemplate.getForObject(techniqueGithubUrl, String.class));
//
//            List<JsonNode> techniques = new ArrayList<>();
//
//            for (JsonNode fileNode : filesArray) {
//                if ("file".equals(fileNode.get("type").asText()) && fileNode.get("name").asText().endsWith(".json")) {
//                    String singleUrl = fileNode.get("download_url").asText();
//                    String jsonContent = restTemplate.getForObject(singleUrl, String.class);
//
//                    JsonNode techniqueNode = objectMapper.readTree(jsonContent);
//                    techniques.add(techniqueNode);
//                }
//            }
//
//            return techniques;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to fetch techniques dynamically");
//        }
//    }
//
//    //--Print fetched data--
//    public void printFetchedTechniques(String url) {
//        List<JsonNode> jsonNodes = fetchTechniques(url);
//        for (JsonNode jsonNode : jsonNodes) {
//            System.out.println(jsonNode.toPrettyString());
//        }
//    }

    public static void main(String[] args) {
        TechniqueDAO dao = new TechniqueDAO();

        dao.printMappedTechniques(
                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/techniques/"
        );
//        dao.printFetchedTechniques(
//                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/techniques/"
//        );
    }
}
