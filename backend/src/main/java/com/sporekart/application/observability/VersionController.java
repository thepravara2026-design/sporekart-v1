package com.sporekart.application.observability;

import com.sporekart.application.exception.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/version")
public class VersionController {

    @Value("${spring.application.name:sporekart-backend}")
    private String appName;

    @Value("${app.version:3.0.0-RELEASE}")
    private String appVersion;

    @Value("${app.git.commit:f0339a4}")
    private String gitCommit;

    @Value("${app.build.timestamp:2026-08-15T17:55:00Z}")
    private String buildTimestamp;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getVersionInfo() {
        Map<String, String> versionMap = new LinkedHashMap<>();
        versionMap.put("appName", appName);
        versionMap.put("version", appVersion);
        versionMap.put("gitCommit", gitCommit);
        versionMap.put("buildTimestamp", buildTimestamp);
        versionMap.put("environment", activeProfile);

        return ResponseEntity.ok(ApiResponse.success(versionMap));
    }
}
