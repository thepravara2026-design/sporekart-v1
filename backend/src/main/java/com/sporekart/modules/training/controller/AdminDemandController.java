package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.AdminTrainingOperationsService;
import com.sporekart.modules.training.application.DemandApplicationService;
import com.sporekart.modules.training.controller.dto.BatchDemandSummaryResponse;
import com.sporekart.modules.training.controller.dto.DemandResponse;
import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN') or hasAuthority('training:admin')")
public class AdminDemandController {

    private final DemandApplicationService demandService;
    private final AdminTrainingOperationsService operationsService;

    public AdminDemandController(DemandApplicationService demandService, AdminTrainingOperationsService operationsService) {
        this.demandService = demandService;
        this.operationsService = operationsService;
    }

    @GetMapping("/api/v1/admin/batches/{batchId}/demand")
    public ResponseEntity<ApiResponse<Page<DemandResponse>>> getBatchDemands(
            @PathVariable String batchId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Pageable pageable) {
        Page<DemandResponse> responsePage = demandService.getBatchDemandsForAdmin(batchId, pageable)
                .map(DemandResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/api/v1/admin/batches/{batchId}/demand/summary")
    public ResponseEntity<ApiResponse<BatchDemandSummaryResponse>> getBatchDemandSummary(@PathVariable String batchId) {
        long count = demandService.getBatchDemandCount(batchId);
        return ResponseEntity.ok(ApiResponse.success(new BatchDemandSummaryResponse(batchId, count)));
    }

    @GetMapping("/api/v1/admin/training/demand")
    public ResponseEntity<ApiResponse<Page<DemandResponse>>> searchGlobalDemands(
            @RequestParam(name = "batchId", required = false) String batchId,
            @RequestParam(name = "status", required = false) DemandStatus status,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortParts[0]));

        Page<TrainingDemandRequest> demands = operationsService.searchDemands(batchId, status, search, pageable);
        Page<DemandResponse> responsePage = demands.map(DemandResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }
}

