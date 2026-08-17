package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.DemandApplicationService;
import com.sporekart.modules.training.controller.dto.BatchDemandSummaryResponse;
import com.sporekart.modules.training.controller.dto.DemandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/batches")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminDemandController {

    private final DemandApplicationService demandService;

    public AdminDemandController(DemandApplicationService demandService) {
        this.demandService = demandService;
    }

    @GetMapping("/{batchId}/demand")
    public ResponseEntity<ApiResponse<Page<DemandResponse>>> getBatchDemands(
            @PathVariable String batchId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Pageable pageable) {
        Page<DemandResponse> responsePage = demandService.getBatchDemandsForAdmin(batchId, pageable)
                .map(DemandResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/{batchId}/demand/summary")
    public ResponseEntity<ApiResponse<BatchDemandSummaryResponse>> getBatchDemandSummary(@PathVariable String batchId) {
        long count = demandService.getBatchDemandCount(batchId);
        return ResponseEntity.ok(ApiResponse.success(new BatchDemandSummaryResponse(batchId, count)));
    }
}
