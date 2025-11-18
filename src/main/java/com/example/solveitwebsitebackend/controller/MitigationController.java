package com.example.solveitwebsitebackend.controller;

import com.example.solveitwebsitebackend.mapper.MitigationMapper;
import com.example.solveitwebsitebackend.service.MitigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/mitigations")
public class MitigationController {

    private final MitigationService service;

    public MitigationController(MitigationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String, MitigationMapper.NewMitigation>> getAllMitigations() {
        return ResponseEntity.ok(service.getAllMitigations());
    }

    @GetMapping(params = "mitigationId")
    public ResponseEntity<MitigationMapper.NewMitigation> getMitigationById(@RequestParam String mitigationId) {
        return ResponseEntity.ok(service.getMitigationById(mitigationId));
    }
}
