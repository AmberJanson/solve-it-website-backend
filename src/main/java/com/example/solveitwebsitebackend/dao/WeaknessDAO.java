package com.example.solveitwebsitebackend.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Repository
public class WeaknessDAO {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<JsonNode> fetchWeaknesses(String weaknessGithubUrl) {
        try {
            JsonNode filesArray = objectMapper.readTree(restTemplate.getForObject(weaknessGithubUrl, String.class));

            List<JsonNode> weaknesses = new ArrayList<>();

            for (JsonNode fileNode : filesArray) {
                if ("file".equals(fileNode.get("type").asText()) && fileNode.get("name").asText().endsWith(".json")) {
                    String singleUrl = fileNode.get("download_url").asText();
                    String jsonContent = restTemplate.getForObject(singleUrl, String.class);

                    JsonNode weaknessNode = objectMapper.readTree(jsonContent);
                    weaknesses.add(weaknessNode);
                }
            }

            return weaknesses;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weaknesses dynamically");
        }
    }

    // --Print data for test--
    public void printFetchedWeaknesses(String url) {
        List<JsonNode> jsonNodes = fetchWeaknesses(url);
        for (JsonNode jsonNode : jsonNodes) {
            System.out.println(jsonNode.toPrettyString());
        }
    }

    public static void main(String[] args) {
        WeaknessDAO dao = new WeaknessDAO();
        dao.printFetchedWeaknesses(
                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/weaknesses/"
        );
    }
}
