package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.dao.TechniqueDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.TechniqueMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TechniqueService {

    private final TechniqueDAO dao;

    private Map<String, TechniqueMapper.NewTechnique> techniqueCache = new HashMap<>();

    public TechniqueService(TechniqueDAO dao) {
        this.dao = dao;
    }

    public void refreshCache() {

        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            attempts++;

            try {
                System.out.println("Attempt " + attempts + " to refresh techniques cache...");

                List<TechniqueMapper.NewTechnique> techniques = dao.mapFetchedTechniques("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/techniques/");

                Map<String, TechniqueMapper.NewTechnique> newCache  = new HashMap<>();
                for (TechniqueMapper.NewTechnique technique : techniques) {
                    newCache.put(technique.id, technique);
                }

                techniqueCache = newCache;

                System.out.println("Successfully updated techniques cache!");
                return;
            } catch (DAOExceptions.FetchException | DAOExceptions.ParseException e) {
                System.err.println("Refresh failed on attempt " + attempts + ": " + e.getMessage());

                if (attempts >= maxRetries) {
                    System.err.println("All retries failed. Keeping existing techniques cache.");
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    public TechniqueMapper.NewTechnique getTechniqueById(String id) {
        TechniqueMapper.NewTechnique technique = techniqueCache.get(id);
        if (technique == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Couldn't find a Technique with id " + id);
        }
        return technique;
    }

    public Map<String, TechniqueMapper.NewTechnique> getAllTechniques() {
        return techniqueCache;
    }
}
