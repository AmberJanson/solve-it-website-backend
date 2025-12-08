package com.example.solveitwebsitebackend.service;

import com.example.solveitwebsitebackend.dao.WeaknessDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.WeaknessMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public void refreshCache(String githubUrl) {

        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            attempts++;

            try {
                System.out.println("Attempt " + attempts + " to refresh weaknesses cache...");

                List<WeaknessMapper.NewWeakness> weaknesses = dao.mapFetchedWeaknesses(githubUrl);

                Map<String, WeaknessMapper.NewWeakness> newCache = new HashMap<>();
                for (WeaknessMapper.NewWeakness weakness : weaknesses) {
                    newCache.put(weakness.id, weakness);
                }

                weaknessCache = newCache;

                System.out.println("Successfully updated weaknesses cache!");
                return;
            } catch (DAOExceptions.FetchException | DAOExceptions.ParseException e) {
                System.err.println("Refresh failed on attempt " + attempts + ": " + e.getMessage());

                if (attempts >= maxRetries) {
                    System.err.println("All retries failed. Keeping existing weaknesses cache.");
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    public WeaknessMapper.NewWeakness getWeaknessById(String id) {
        WeaknessMapper.NewWeakness weakness = weaknessCache.get(id);
        if (weakness == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Couldn't find a Weakness with id: " + id);
        }
        return weakness;
    }

    public Map<String, WeaknessMapper.NewWeakness> getAllWeaknesses() {
        return weaknessCache;
    }
}
