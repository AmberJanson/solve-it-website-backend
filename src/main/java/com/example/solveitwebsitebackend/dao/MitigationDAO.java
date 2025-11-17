package com.example.solveitwebsitebackend.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MitigationDAO {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<JsonNode> fetchMitigations(String mitigationGithubUrl) {
        try {
            JsonNode filesArray = objectMapper.readTree(restTemplate.getForObject(mitigationGithubUrl, String.class));

            List<JsonNode> mitigations = new ArrayList<>();

            for (JsonNode fileNode : filesArray) {
                if ("file".equals(fileNode.get("type").asText()) && fileNode.get("name").asText().endsWith(".json")) {
                    String singleUrl = fileNode.get("download_url").asText();
                    String jsonContent = restTemplate.getForObject(singleUrl, String.class);

                    JsonNode mitigationNode = objectMapper.readTree(jsonContent);
                    mitigations.add(mitigationNode);
                }
            }

            return mitigations;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch mitigations dynamically");
        }
    }

    // --Print data for test--
    public void printFetchedMitigations(String url) {
        List<JsonNode> jsonNodes = fetchMitigations(url);
        for (JsonNode jsonNode : jsonNodes) {
            System.out.println(jsonNode.toPrettyString());
        }
    }

    public static void main(String[] args) {
        MitigationDAO dao = new MitigationDAO();
        dao.printFetchedMitigations(
                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/mitigations/"
        );
    }
}
