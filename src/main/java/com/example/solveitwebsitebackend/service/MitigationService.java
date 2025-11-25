package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.dao.MitigationDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.MitigationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public void refreshCache(String githubUrl) {

        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            attempts++;

            try {
                System.out.println("Attempt " + attempts + " to refresh mitigations cache...");

                List<MitigationMapper.NewMitigation> mitigations = dao.mapFetchedMitigations(githubUrl);

                Map<String, MitigationMapper.NewMitigation> newCache = new HashMap<>();
                for (MitigationMapper.NewMitigation mitigation : mitigations) {
                    newCache.put(mitigation.id, mitigation);
                }

                mitigationCache = newCache;

                System.out.println("Successfully updated mitigations cache!");
                return;
            } catch (DAOExceptions.FetchException | DAOExceptions.ParseException e) {
                System.err.println("Refresh failed on attempt " + attempts + ": " + e.getMessage());

                if (attempts >= maxRetries) {
                    System.err.println("All retries failed. Keeping existing mitigations cache.");
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    public MitigationMapper.NewMitigation getMitigationById(String id) {
        MitigationMapper.NewMitigation mitigation = mitigationCache.get(id);
        if (mitigation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Couldn't find a Mitigation with id: " + id);
        }
        return mitigation;
    }

    public Map<String, MitigationMapper.NewMitigation> getAllMitigations() {
        return mitigationCache;
    }
}
