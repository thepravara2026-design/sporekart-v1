package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.BatchApplicationService;
import com.sporekart.modules.training.controller.dto.AddScheduleRequest;
import com.sporekart.modules.training.controller.dto.BatchResponse;
import com.sporekart.modules.training.controller.dto.CreateBatchRequest;
import com.sporekart.modules.training.controller.dto.ScheduleResponse;
import com.sporekart.modules.training.controller.dto.UpdateBatchRequest;
import com.sporekart.modules.training.domain.BatchSchedule;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.TrainingBatch;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminBatchController {

    private final BatchApplicationService batchService;

    public AdminBatchController(BatchApplicationService batchService) {
        this.batchService = batchService;
    }

    @PostMapping("/api/v1/admin/training-programs/{programId}/batches")
    public ResponseEntity<ApiResponse<BatchResponse>> createBatchForProgram(
            @PathVariable("programId") String programId,
            @Valid @RequestBody CreateBatchRequest request) {
        request.setProgramId(programId);
        return createBatchInternal(request);
    }

    @PostMapping("/api/v1/admin/batches")
    public ResponseEntity<ApiResponse<BatchResponse>> createBatch(@Valid @RequestBody CreateBatchRequest request) {
        return createBatchInternal(request);
    }

    private ResponseEntity<ApiResponse<BatchResponse>> createBatchInternal(CreateBatchRequest request) {
        String actorId = getAuthenticatedActor();
        TrainingBatch batch = batchService.createBatch(
                request.getProgramId(),
                request.getBatchCode(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTotalCapacity(),
                request.getDeliveryMode(),
                request.getVenueInfo(),
                request.getMeetingUrl(),
                request.getTimezone(),
                actorId
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(BatchResponse.fromDomain(batch)));
    }

    @PutMapping("/api/v1/admin/batches/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatch(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateBatchRequest request) {
        String actorId = getAuthenticatedActor();
        TrainingBatch batch = batchService.updateBatch(
                id,
                request.getProgramId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTimezone(),
                request.getDeliveryMode(),
                request.getVenueInfo(),
                request.getMeetingUrl(),
                actorId
        );
        return ResponseEntity.ok(ApiResponse.success(BatchResponse.fromDomain(batch)));
    }

    @PostMapping("/api/v1/admin/batches/{id}/activate")
    public ResponseEntity<ApiResponse<BatchResponse>> activateBatch(@PathVariable("id") String id) {
        String actorId = getAuthenticatedActor();
        TrainingBatch batch = batchService.activateBatch(id, actorId);
        return ResponseEntity.ok(ApiResponse.success(BatchResponse.fromDomain(batch)));
    }

    @PostMapping("/api/v1/admin/batches/{id}/deactivate")
    public ResponseEntity<ApiResponse<BatchResponse>> deactivateBatch(@PathVariable("id") String id) {
        String actorId = getAuthenticatedActor();
        TrainingBatch batch = batchService.deactivateBatch(id, actorId);
        return ResponseEntity.ok(ApiResponse.success(BatchResponse.fromDomain(batch)));
    }

    @PostMapping("/api/v1/admin/batches/{id}/cancel")
    public ResponseEntity<ApiResponse<BatchResponse>> cancelBatch(@PathVariable("id") String id) {
        String actorId = getAuthenticatedActor();
        TrainingBatch batch = batchService.cancelBatch(id, actorId);
        return ResponseEntity.ok(ApiResponse.success(BatchResponse.fromDomain(batch)));
    }

    @PostMapping("/api/v1/admin/batches/{id}/schedules")
    public ResponseEntity<ApiResponse<ScheduleResponse>> addScheduleSession(
            @PathVariable("id") String id,
            @Valid @RequestBody AddScheduleRequest request) {
        String actorId = getAuthenticatedActor();
        BatchSchedule schedule = batchService.addScheduleSession(
                id,
                request.getTitle(),
                request.getScheduledAt(),
                request.getDurationMinutes(),
                request.getLocation(),
                actorId
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ScheduleResponse.fromDomain(schedule)));
    }

    @GetMapping("/api/v1/admin/batches/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatchById(@PathVariable("id") String id) {
        TrainingBatch batch = batchService.getBatchById(id);
        return ResponseEntity.ok(ApiResponse.success(BatchResponse.fromDomain(batch)));
    }

    @GetMapping("/api/v1/admin/batches")
    public ResponseEntity<ApiResponse<Page<BatchResponse>>> searchBatches(
            @RequestParam(name = "programId", required = false) String programId,
            @RequestParam(name = "status", required = false) BatchStatus status,
            @RequestParam(name = "deliveryMode", required = false) DeliveryMode deliveryMode,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "startDate,asc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortParts[0]));

        Page<TrainingBatch> batches = batchService.searchBatches(programId, status, deliveryMode, fromDate, toDate, pageable);
        Page<BatchResponse> responsePage = batches.map(BatchResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    private String getAuthenticatedActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ADMIN_USER";
    }
}
