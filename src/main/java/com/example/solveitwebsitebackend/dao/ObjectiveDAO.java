package com.example.solveitwebsitebackend.dao;

import java.util.List;

import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.ObjectiveMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
public class ObjectiveDAO {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ObjectiveMapper.RawObjective> fetchRawObjectives(String objectivesGithubUrl) {
        try {
            String objectivesJson = restTemplate.getForObject(objectivesGithubUrl, String.class);
            JavaType objectivesType = objectMapper.getTypeFactory().constructCollectionType(List.class, ObjectiveMapper.RawObjective.class);
            return objectMapper.readValue(objectivesJson, objectivesType);
        } catch (RestClientException e) {
            throw new DAOExceptions.FetchException("Failed to fetch Objective JSON", e);
        } catch (JsonProcessingException e) {
            throw new DAOExceptions.ParseException("Failed to parse Objective JSON", e);
        }
    }

    public List<ObjectiveMapper.NewObjective> mapFetchedObjectives(String objectivesGithubUrl) {
        List<ObjectiveMapper.RawObjective> rawObjectiveList = fetchRawObjectives(objectivesGithubUrl);
        return rawObjectiveList.stream().map(ObjectiveMapper::map).toList();
    }

    public void printMappedObjectives(String url) {
        try {
            List<ObjectiveMapper.NewObjective> objectives = mapFetchedObjectives(url);
            String objectivesJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(objectives);

            System.out.println(objectivesJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to print objectives in Json", e);
        }
    }

//    //--Fetch data to print--
//    public JsonNode fetchObjectives(String objectiveGithubUrl) {
//        try {
//            String objectivesJson = restTemplate.getForObject(objectiveGithubUrl, String.class);
//
//            return objectMapper.readTree(objectivesJson);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to fetch or parse Objective json-file", e);
//        }
//    }
//
//    //--Print fetched data--
//    public void printFetchedObjectives(String url) {
//        JsonNode jsonNode = fetchObjectives(url);
//        System.out.println(jsonNode.toPrettyString());
//    }

    public static void main(String[] args) {
        ObjectiveDAO dao = new ObjectiveDAO();

        dao.printMappedObjectives(
                "https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it/refs/heads/main/data/solve-it.json");

//        dao.printFetchedObjectives(
//                "https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it/refs/heads/main/data/solve-it.json"
//        );
    }
}