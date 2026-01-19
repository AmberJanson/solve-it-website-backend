package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StartupDataService {

    @Autowired CategoryViewService categoryViewService;
    @Autowired CategoryService categoryService;
    @Autowired TechniqueService techniqueService;
    @Autowired WeaknessService weaknessService;
    @Autowired MitigationService mitigationService;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 0 * * *")
    public void loadAllData() {
        System.out.println("Loading all entity caches...");

        Map<String, CategoryMapper.RawCategory> combinedCache = new HashMap<>();
        List<String> categoryUrls = Arrays.asList(
                "https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it/refs/heads/main/data/solve-it.json",
                "https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it-examples/refs/heads/main/reorganization_of_techniques/dfrws.json"
        );

        categoryViewService.refreshCache("json/CategoryView.json");
        for (String categoryUrl : categoryUrls) {
            Map<String, CategoryMapper.RawCategory> rawCache =  categoryService.setRawCache(categoryUrl);
            if (rawCache != null) {
                combinedCache.putAll(rawCache);
            }
        }
        categoryService.refreshCache(combinedCache);
        techniqueService.refreshCache("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/techniques/");
        weaknessService.refreshCache("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/weaknesses/");
        mitigationService.refreshCache("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/mitigations/");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        System.out.println("Finished loading all entity caches.");
    }
}
