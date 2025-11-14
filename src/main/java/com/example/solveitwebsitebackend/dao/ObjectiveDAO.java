package com.example.solveitwebsitebackend.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class ObjectiveDAO {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchObjectives(String objectiveGithubUrl) {
        try {
            String objectivesJson = restTemplate.getForObject(objectiveGithubUrl, String.class);

            return objectMapper.readTree(objectivesJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch or parse Objective json-file");
        }
    }

    // --Print data for test--
    public void printFetchedObjectives(String url) {
        JsonNode jsonNode = fetchObjectives(url);
        System.out.println(jsonNode.toPrettyString());
    }

    public static void main(String[] args) {
        ObjectiveDAO dao = new ObjectiveDAO();

        dao.printFetchedObjectives(
                "https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it/refs/heads/main/data/solve-it.json"
        );
    }
}