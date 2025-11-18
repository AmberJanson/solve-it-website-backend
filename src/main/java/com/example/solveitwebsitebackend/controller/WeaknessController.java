package com.example.solveitwebsitebackend.controller;

import com.example.solveitwebsitebackend.mapper.WeaknessMapper;
import com.example.solveitwebsitebackend.service.WeaknessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/weaknesses")
public class WeaknessController {

    private final WeaknessService service;

    public WeaknessController(WeaknessService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String, WeaknessMapper.NewWeakness>> getAllWeaknesses() {
        return ResponseEntity.ok(service.getAllWeaknesses());
    }

    @GetMapping(params = "weaknessId")
    public ResponseEntity<WeaknessMapper.NewWeakness> getWeaknessById(@RequestParam String weaknessId) {
        return ResponseEntity.ok(service.getWeaknessById(weaknessId));
    }
}
