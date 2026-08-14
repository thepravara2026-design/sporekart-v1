package com.sporekart.common.controller;

import com.sporekart.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/version")
public class VersionController {

    @Value("${spring.application.name:sporekart-backend}")
    private String appName;

    @Value("${app.version:0.1.0-SNAPSHOT}")
    private String appVersion;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getVersionInfo() {
        Map<String, String> versionMap = Map.of(
                "appName", appName,
                "version", appVersion
        );
        return ResponseEntity.ok(ApiResponse.success(versionMap));
    }
}
