package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.dao.ObjectiveDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.ObjectiveMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ObjectiveService {

    private final ObjectiveDAO dao;

    private Map<String, ObjectiveMapper.NewObjective> objectiveCache = new HashMap<>();

    public ObjectiveService(ObjectiveDAO dao) {
        this.dao = dao;
    }

    public void refreshCache() {

        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            attempts++;

            try {
                System.out.println("Attempt " + attempts + " to refresh objectives cache...");

                List<ObjectiveMapper.NewObjective> objectives = dao.mapFetchedObjectives("https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it/refs/heads/main/data/solve-it.json");

                Map<String, ObjectiveMapper.NewObjective> newCache = new HashMap<>();
                for (ObjectiveMapper.NewObjective objective : objectives) {
                    newCache.put(objective.id, objective);
                }

                objectiveCache = newCache;

                System.out.println("Successfully updated objectives cache!");
                return;

            } catch (DAOExceptions.FetchException | DAOExceptions.ParseException e) {
                System.err.println("Refresh failed on attempt " + attempts + ": " + e.getMessage());

                if (attempts >= maxRetries) {
                    System.err.println("All retries failed. Keeping existing objectives cache.");
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    public ObjectiveMapper.NewObjective getObjectiveById(String id) {
        return objectiveCache.get(id);
    }

    public Map<String, ObjectiveMapper.NewObjective> getAllObjectives() {
        return objectiveCache;
    }
}
