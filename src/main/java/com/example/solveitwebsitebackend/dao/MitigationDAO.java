package com.example.solveitwebsitebackend.dao;

import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.MitigationMapper;
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
public class MitigationDAO {

    private static final Logger log = LoggerFactory.getLogger(MitigationDAO.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<MitigationMapper.RawMitigation> fetchRawMitigations(String mitigationGithubUrl) {
        try {
            String response = restTemplate.getForObject(mitigationGithubUrl, String.class);
            JsonNode filesArray = objectMapper.readTree(response);

            List<MitigationMapper.RawMitigation> mitigations = new ArrayList<>();

            for (JsonNode fileNode : filesArray) {
                if (!"file".equals(fileNode.path("type").asText())) continue;
                if (!fileNode.path("name").asText().endsWith(".json")) continue;

                String singleUrl = fileNode.get("download_url").asText();

                try {
                    String mitigationJson = restTemplate.getForObject(singleUrl, String.class);
                    JavaType mitigationType = objectMapper.getTypeFactory().constructType(MitigationMapper.RawMitigation.class);

                    MitigationMapper.RawMitigation rawMitigation = objectMapper.readValue(mitigationJson, mitigationType);
                    mitigations.add(rawMitigation);
                }catch (RestClientException e) {
                    log.warn("Skipping Mitigation due to fetch error", e);
                } catch (JsonProcessingException e) {
                    log.warn("Skipping Mitigation due to parse error", e);
                }
            }

            return mitigations;

        } catch (RestClientException e) {
            throw new DAOExceptions.FetchException("Failed to fetch Mitigation filesArray JSON", e);
        } catch (JsonProcessingException e) {
            throw new DAOExceptions.ParseException("Failed to parse Mitigation filesArray JSON", e);
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to print mitigations in Json", e);
        }
    }

    public static void main(String[] args) {
        MitigationDAO dao = new MitigationDAO();

        dao.printMappedMitigations(
                "https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/mitigations/"
        );
    }
}
