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

        objectiveService.refreshCache();
        techniqueService.refreshCache();
        weaknessService.refreshCache();
        mitigationService.refreshCache();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        System.out.println("Finished loading all entity caches.");
    }
}
