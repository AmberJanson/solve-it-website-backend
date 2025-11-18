package com.example.solveitwebsitebackend.controller;

import com.example.solveitwebsitebackend.mapper.ObjectiveMapper;
import com.example.solveitwebsitebackend.service.ObjectiveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/objectives")
public class ObjectiveController {

    private final ObjectiveService service;

    public ObjectiveController(ObjectiveService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String ,ObjectiveMapper.NewObjective>> getAllObjectives() {
        return ResponseEntity.ok(service.getAllObjectives());
    }

    @GetMapping(params = "objectiveId")
    public ResponseEntity<ObjectiveMapper.NewObjective> getObjectiveById(@RequestParam String objectiveId) {
        return ResponseEntity.ok(service.getObjectiveById(objectiveId));
    }
}
