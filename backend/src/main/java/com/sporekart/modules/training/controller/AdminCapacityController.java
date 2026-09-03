package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.controller.dto.CapacityResponse;
import com.sporekart.modules.training.controller.dto.UpdateCapacityRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/batches")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminCapacityController {

    private final CapacityApplicationService capacityService;

    public AdminCapacityController(CapacityApplicationService capacityService) {
        this.capacityService = capacityService;
    }

    @PatchMapping("/{id}/capacity")
    public ResponseEntity<ApiResponse<CapacityResponse>> updateCapacity(
            @PathVariable("id") String batchId,
            @Valid @RequestBody UpdateCapacityRequest request) {

        String actor = "ADMIN";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            actor = auth.getName();
        }

        CapacityResponse response = capacityService.updateBatchCapacity(batchId, request.getCapacity(), actor);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/capacity")
    public ResponseEntity<ApiResponse<CapacityResponse>> getCapacity(
            @PathVariable("id") String batchId) {
        CapacityResponse response = capacityService.getBatchCapacity(batchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
