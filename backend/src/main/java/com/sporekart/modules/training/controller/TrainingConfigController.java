package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.TrainingApplicationService;
import com.sporekart.modules.training.domain.policy.CancellationPolicy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/training")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class TrainingConfigController {

    private final TrainingApplicationService trainingService;

    public TrainingConfigController(TrainingApplicationService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrainingPolicyConfig() {
        CancellationPolicy policy = trainingService.getCancellationPolicy();
        Map<String, Object> config = Map.of(
                "cancellationAdminDays", policy.getAdminWindowDays(),
                "cancellationTraineeDays", policy.getTraineeWindowDays(),
                "capacityStrategy", "ATOMIC_ROW_LOCK",
                "concurrencyProtection", true,
                "status", "ACTIVE"
        );
        return ResponseEntity.ok(ApiResponse.success(config));
    }
}
