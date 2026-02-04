package com.example.solveitwebsitebackend.dao;

import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.WeaknessMapper;
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
public class WeaknessDAO {

    private static final Logger log = LoggerFactory.getLogger(WeaknessDAO.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<WeaknessMapper.RawWeakness> fetchRawWeaknesses(String weaknessGithubUrl) {
        try {
            String response = restTemplate.getForObject(weaknessGithubUrl, String.class);
            JsonNode filesArray = objectMapper.readTree(response);

            List<WeaknessMapper.RawWeakness> weaknesses = new ArrayList<>();

            for (JsonNode fileNode : filesArray) {
                if (!"file".equals(fileNode.path("type").asText())) continue;
                if (!fileNode.path("name").asText().endsWith(".json")) continue;

                String singleUrl = fileNode.get("download_url").asText();

                try {
                    String weaknessJson = restTemplate.getForObject(singleUrl, String.class);
                    JavaType weaknessType = objectMapper.getTypeFactory().constructType(WeaknessMapper.RawWeakness.class);

                    WeaknessMapper.RawWeakness rawWeakness = objectMapper.readValue(weaknessJson, weaknessType);
                    weaknesses.add(rawWeakness);
                } catch (RestClientException e) {
                    log.warn("Skipping Weakness due to fetch error", e);
                } catch (JsonProcessingException e) {
                    log.warn("Skipping Weakness due to parse error", e);
                }
            }

            return weaknesses;

        } catch (RestClientException e) {
            throw new DAOExceptions.FetchException("Failed to fetch Weakness filesArray JSON", e);
        } catch (JsonProcessingException e) {
            throw new DAOExceptions.ParseException("Failed to parse Weakness filesArray JSON", e);
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to print weaknesses in Json", e);
        }
    }

    public static void main(String[] args) {
        WeaknessDAO dao = new WeaknessDAO();

        dao.printMappedWeaknesses(
                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/weaknesses/"
        );
    }
}
