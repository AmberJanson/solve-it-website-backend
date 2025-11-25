package com.example.solveitwebsitebackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StartupDataService {

    @Autowired ObjectiveService objectiveService;
    @Autowired TechniqueService techniqueService;
    @Autowired WeaknessService weaknessService;
    @Autowired MitigationService mitigationService;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 0 * * *")
    public void loadAllData() {
        System.out.println("Loading all entity caches...");

        objectiveService.refreshCache("https://raw.githubusercontent.com/SOLVE-IT-DF/solve-it/refs/heads/main/data/solve-it.json");
        techniqueService.refreshCache("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/techniques/");
        weaknessService.refreshCache("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/weaknesses/");
        mitigationService.refreshCache("https://api.github.com/repos/SOLVE-IT-DF/solve-it/contents/data/mitigations/");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        System.out.println("Finished loading all entity caches.");
    }
}
