package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/training")
public class TrainingHealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrainingModuleHealth() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "module", "training",
                "version", "3.0",
                "phase", "Training 0 — Foundation & Architecture"
        );
        return ResponseEntity.ok(ApiResponse.success(health));
    }
}
