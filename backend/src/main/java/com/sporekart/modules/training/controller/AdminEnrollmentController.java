package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.AdminTrainingOperationsService;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.application.TrainingCancellationService;
import com.sporekart.modules.training.application.TrainingRescheduleService;
import com.sporekart.modules.training.controller.dto.CancelEnrollmentRequest;
import com.sporekart.modules.training.controller.dto.EnrollmentResponse;
import com.sporekart.modules.training.controller.dto.RescheduleEnrollmentRequest;
import com.sporekart.modules.training.controller.dto.TrainingPaymentStatusResponse;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.TrainingEnrollmentPayment;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN') or hasAuthority('training:admin')")
public class AdminEnrollmentController {

    private final EnrollmentApplicationService enrollmentService;
    private final AdminTrainingOperationsService operationsService;
    private final TrainingCancellationService cancellationService;
    private final TrainingRescheduleService rescheduleService;

    public AdminEnrollmentController(
            EnrollmentApplicationService enrollmentService,
            AdminTrainingOperationsService operationsService,
            TrainingCancellationService cancellationService,
            TrainingRescheduleService rescheduleService) {
        this.enrollmentService = enrollmentService;
        this.operationsService = operationsService;
        this.cancellationService = cancellationService;
        this.rescheduleService = rescheduleService;
    }

    @GetMapping("/api/v1/admin/batches/{batchId}/enrollments")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getBatchEnrollments(
            @PathVariable("batchId") String batchId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortParts[0]));

        Page<TrainingEnrollment> enrollments = enrollmentService.getBatchEnrollmentsForAdmin(batchId, pageable);
        Page<EnrollmentResponse> responsePage = enrollments.map(EnrollmentResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/api/v1/admin/training/enrollments")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> searchGlobalEnrollments(
            @RequestParam(name = "batchId", required = false) String batchId,
            @RequestParam(name = "status", required = false) EnrollmentStatus status,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortParts[0]));

        Page<TrainingEnrollment> enrollments = operationsService.searchEnrollments(batchId, status, search, pageable);
        Page<EnrollmentResponse> responsePage = enrollments.map(EnrollmentResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/api/v1/admin/training/exceptions")
    public ResponseEntity<ApiResponse<Page<TrainingPaymentStatusResponse>>> getPaymentVerifiedExceptions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<TrainingEnrollmentPayment> exceptions = operationsService.getPaymentVerifiedExceptions(pageable);
        Page<TrainingPaymentStatusResponse> responsePage = exceptions.map(p -> new TrainingPaymentStatusResponse(
                p.getId(), p.getPaymentId(), p.getBatchId(), p.getTraineeId(), p.getEnrollmentId(), p.getAmount(), p.getCurrency(), p.getStatus(), p.getCreatedAt(), p.getUpdatedAt()
        ));
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @PostMapping("/api/v1/admin/training/enrollments/{enrollmentId}/cancel")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> cancelEnrollmentByAdmin(
            @PathVariable("enrollmentId") String enrollmentId,
            @RequestBody(required = false) CancelEnrollmentRequest request) {

        String adminUser = getAuthenticatedAdminUser();
        String reason = request != null ? request.getReason() : null;

        TrainingEnrollment cancelled = cancellationService.cancelEnrollmentByAdmin(enrollmentId, reason, adminUser);
        return ResponseEntity.ok(ApiResponse.success(EnrollmentResponse.fromDomain(cancelled)));
    }

    @PostMapping("/api/v1/admin/training/enrollments/{enrollmentId}/reschedule")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> rescheduleEnrollmentByAdmin(
            @PathVariable("enrollmentId") String enrollmentId,
            @Valid @RequestBody RescheduleEnrollmentRequest request) {

        String adminUser = getAuthenticatedAdminUser();
        TrainingEnrollment rescheduled = rescheduleService.rescheduleEnrollmentByAdmin(
                enrollmentId, request.getTargetBatchId(), request.getReason(), adminUser
        );
        return ResponseEntity.ok(ApiResponse.success(EnrollmentResponse.fromDomain(rescheduled)));
    }

    private String getAuthenticatedAdminUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "ADMIN";
    }
}
