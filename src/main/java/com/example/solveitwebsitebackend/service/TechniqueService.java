package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.dao.TechniqueDAO;
import com.example.solveitwebsitebackend.mapper.TechniqueMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshCache() {
        System.out.println("Refreshing cache...");

        List<TechniqueMapper.NewTechnique> techniques = dao.mapFetchedTechniques("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/techniques/");
        techniqueCache.clear();

        for (TechniqueMapper.NewTechnique technique : techniques) {
            techniqueCache.put(technique.id, technique);
        }

        System.out.println("Successfully updated cache!");
    }

    public TechniqueMapper.NewTechnique getTechniqueById(String id) {
        return techniqueCache.get(id);
    }

    public Map<String, TechniqueMapper.NewTechnique> getAllTechniques() {
        return techniqueCache;
    }
}
