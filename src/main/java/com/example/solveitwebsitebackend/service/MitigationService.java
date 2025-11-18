package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.dao.MitigationDAO;
import com.example.solveitwebsitebackend.mapper.MitigationMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MitigationService {

    private final MitigationDAO dao;

    private Map<String, MitigationMapper.NewMitigation> mitigationCache = new HashMap<>();

    public MitigationService(MitigationDAO dao) {
        this.dao = dao;
    }

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * *")
    public void refreshCache() {
        System.out.println("Refreshing cache...");

        List<MitigationMapper.NewMitigation> mitigations = dao.mapFetchedMitigations("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/mitigations/");
        mitigationCache.clear();

        for (MitigationMapper.NewMitigation mitigation : mitigations) {
            mitigationCache.put(mitigation.id, mitigation);
        }

        System.out.println("Successfully updated cache!");
    }

    public MitigationMapper.NewMitigation getMitigationById(String id) {
        return mitigationCache.get(id);
    }

    public Map<String, MitigationMapper.NewMitigation> getAllMitigations() {
        return mitigationCache;
    }
}
