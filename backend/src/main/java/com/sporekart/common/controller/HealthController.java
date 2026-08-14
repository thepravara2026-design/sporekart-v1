package com.sporekart.common.controller;

import com.sporekart.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getHealthStatus() {
        Map<String, String> statusMap = Map.of("status", "UP");
        return ResponseEntity.ok(ApiResponse.success(statusMap));
    }
}
