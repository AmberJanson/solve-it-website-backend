package com.example.solveitwebsitebackend.dao;

import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.MitigationMapper;
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
public class MitigationDAO {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<MitigationMapper.RawMitigation> fetchRawMitigations(String mitigationGithubUrl) {
        try {
            JsonNode filesArray = objectMapper.readTree(restTemplate.getForObject(mitigationGithubUrl, String.class));

            List<MitigationMapper.RawMitigation> mitigations = new ArrayList<>();

            for (JsonNode fileNode : filesArray) {
                if ("file".equals(fileNode.get("type").asText()) && fileNode.get("name").asText().endsWith(".json")) {
                    String singleUrl = fileNode.get("download_url").asText();

                    String mitigationJson = restTemplate.getForObject(singleUrl, String.class);
                    JavaType mitigationType = objectMapper.getTypeFactory().constructType(MitigationMapper.RawMitigation.class);

                    MitigationMapper.RawMitigation rawMitigation = objectMapper.readValue(mitigationJson, mitigationType);
                    mitigations.add(rawMitigation);
                }
            }

            return mitigations;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch mitigations dynamically");
        }
    }

    public List<MitigationMapper.NewMitigation> mapFetchedMitigations(String mitigationGithubUrl) {
        List<MitigationMapper.RawMitigation> rawMitigations = fetchRawMitigations(mitigationGithubUrl);
        return rawMitigations.stream().map(MitigationMapper::map).toList();
    }

    public void printMappedMitigations(String url) {
        try {
            List<MitigationMapper.NewMitigation> mitigations = mapFetchedMitigations(url);
            String mitigationJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mitigations);

            System.out.println(mitigationJson);
        } catch (RestClientException e) {
            throw new DAOExceptions.FetchException("Failed to fetch Mitigation JSON", e);
        } catch (JsonProcessingException e) {
            throw new DAOExceptions.ParseException("Failed to parse Mitigation JSON", e);
        }
    }

//    //--Fetch data to print--
//    public List<JsonNode> fetchMitigations(String mitigationGithubUrl) {
//        try {
//            JsonNode filesArray = objectMapper.readTree(restTemplate.getForObject(mitigationGithubUrl, String.class));
//
//            List<JsonNode> mitigations = new ArrayList<>();
//
//            for (JsonNode fileNode : filesArray) {
//                if ("file".equals(fileNode.get("type").asText()) && fileNode.get("name").asText().endsWith(".json")) {
//                    String singleUrl = fileNode.get("download_url").asText();
//                    String jsonContent = restTemplate.getForObject(singleUrl, String.class);
//
//                    JsonNode mitigationNode = objectMapper.readTree(jsonContent);
//                    mitigations.add(mitigationNode);
//                }
//            }
//
//            return mitigations;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to fetch mitigations dynamically");
//        }
//    }
//
//    //--Print fetched data--
//    public void printFetchedMitigations(String url) {
//        List<JsonNode> jsonNodes = fetchMitigations(url);
//        for (JsonNode jsonNode : jsonNodes) {
//            System.out.println(jsonNode.toPrettyString());
//        }
//    }

    public static void main(String[] args) {
        MitigationDAO dao = new MitigationDAO();

        dao.printMappedMitigations(
                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/mitigations/"
        );
//        dao.printFetchedMitigations(
//                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/mitigations/"
//        );
    }
}
