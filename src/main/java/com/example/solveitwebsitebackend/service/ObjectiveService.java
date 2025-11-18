package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.dao.ObjectiveDAO;
import com.example.solveitwebsitebackend.mapper.ObjectiveMapper;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshCache() {
        System.out.println("Refreshing cache...");

        List<ObjectiveMapper.NewObjective> objectives = dao.mapFetchedObjectives("https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it/refs/heads/main/data/solve-it.json");
        objectiveCache.clear();

        for (ObjectiveMapper.NewObjective objective : objectives) {
            objectiveCache.put(objective.id, objective);
        }

        System.out.println("Successfully updated cache!");
    }

    public ObjectiveMapper.NewObjective getObjectiveById(String id) {
        return objectiveCache.get(id);
    }

    public Map<String, ObjectiveMapper.NewObjective> getAllObjectives() {
        return objectiveCache;
    }
}
