package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.dao.WeaknessDAO;
import com.example.solveitwebsitebackend.mapper.WeaknessMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeaknessService {

    private final WeaknessDAO dao;

    private Map<String, WeaknessMapper.NewWeakness> weaknessCache = new HashMap<>();

    public WeaknessService(WeaknessDAO dao) {
        this.dao = dao;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshCache() {
        System.out.println("Refreshing cache...");

        List<WeaknessMapper.NewWeakness> weaknesses = dao.mapFetchedWeaknesses("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/weaknesses/");
        weaknessCache.clear();

        for (WeaknessMapper.NewWeakness weakness : weaknesses) {
            weaknessCache.put(weakness.id, weakness);
        }

        System.out.println("Successfully updated cache!");
    }

    public WeaknessMapper.NewWeakness getWeaknessById(String id) {
        return weaknessCache.get(id);
    }

    public Map<String, WeaknessMapper.NewWeakness> getAllWeaknesses() {
        return weaknessCache;
    }
}
