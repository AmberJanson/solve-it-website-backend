package com.example.solveitwebsitebackend.controller;

import com.example.solveitwebsitebackend.mapper.TechniqueMapper;
import com.example.solveitwebsitebackend.service.TechniqueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/techniques")
public class TechniqueController {

    private final TechniqueService service;

    public TechniqueController(TechniqueService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String, TechniqueMapper.NewTechnique>> getAllTechniques() {
        return ResponseEntity.ok(service.getAllTechniques());
    }

    @GetMapping(params = "techniqueId")
    public ResponseEntity<TechniqueMapper.NewTechnique> getTechniqueById(@RequestParam String techniqueId) {
        return ResponseEntity.ok(service.getTechniqueById(techniqueId));
    }
}
