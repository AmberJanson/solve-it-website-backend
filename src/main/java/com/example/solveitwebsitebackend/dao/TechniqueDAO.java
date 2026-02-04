package com.example.solveitwebsitebackend.dao;

import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.TechniqueMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TechniqueDAO {

    private static final Logger log = LoggerFactory.getLogger(TechniqueDAO.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TechniqueMapper.RawTechnique> fetchRawTechniques(String techniqueGithubUrl) {
        try {
            String response = restTemplate.getForObject(techniqueGithubUrl, String.class);
            JsonNode filesArray = objectMapper.readTree(response);

            List<TechniqueMapper.RawTechnique> techniques = new ArrayList<>();

            for (JsonNode fileNode : filesArray) {
                if (!"file".equals(fileNode.path("type").asText())) continue;
                if (!fileNode.path("name").asText().endsWith(".json")) continue;

                String singleUrl = fileNode.get("download_url").asText();

                try {
                    String techniqueJson = restTemplate.getForObject(singleUrl, String.class);
                    JavaType techniqueType = objectMapper.getTypeFactory().constructType(TechniqueMapper.RawTechnique.class);

                    TechniqueMapper.RawTechnique rawTechnique = objectMapper.readValue(techniqueJson, techniqueType);
                    techniques.add(rawTechnique);
                } catch (RestClientException e) {
                    log.warn("Skipping Technique due to fetch error", e);
                } catch (JsonProcessingException e) {
                    log.warn("Skipping Technique due to parse error", e);
                }
            }

            return techniques;

        } catch (RestClientException e) {
            throw new DAOExceptions.FetchException("Failed to fetch Technique filesArray JSON", e);
        } catch (JsonProcessingException e) {
            throw new DAOExceptions.ParseException("Failed to parse Technique filesArray JSON", e);
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

    public static void main(String[] args) {
        TechniqueDAO dao = new TechniqueDAO();

        dao.printMappedTechniques(
                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/techniques/"
        );
    }
}
