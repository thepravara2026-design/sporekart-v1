package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.controller.dto.CreateEnrollmentRequest;
import com.sporekart.modules.training.controller.dto.EnrollmentResponse;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class TraineeEnrollmentController {

    private final EnrollmentApplicationService enrollmentService;

    public TraineeEnrollmentController(EnrollmentApplicationService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/api/v1/batches/{batchId}/enrollments")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @PathVariable("batchId") String batchId,
            @RequestHeader(name = "X-Idempotency-Key", required = false) String headerIdempotencyKey,
            @Valid @RequestBody(required = false) CreateEnrollmentRequest request) {

        String traineeId = getAuthenticatedTraineeId();
        String idempotencyKey = headerIdempotencyKey != null ? headerIdempotencyKey : (request != null ? request.getIdempotencyKey() : null);

        TrainingEnrollment enrollment = enrollmentService.enrollTrainee(batchId, traineeId, idempotencyKey);
        EnrollmentResponse response = EnrollmentResponse.fromDomain(enrollment);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/me/enrollments")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getMyEnrollments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        String traineeId = getAuthenticatedTraineeId();
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortParts[0]));

        Page<TrainingEnrollment> enrollments = enrollmentService.getTraineeEnrollments(traineeId, pageable);
        Page<EnrollmentResponse> responsePage = enrollments.map(EnrollmentResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/api/v1/enrollments/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentById(@PathVariable("id") String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String requesterId = getAuthenticatedTraineeId();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        TrainingEnrollment enrollment = enrollmentService.getEnrollmentById(id, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(EnrollmentResponse.fromDomain(enrollment)));
    }

    private String getAuthenticatedTraineeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ANONYMOUS_TRAINEE";
    }
}
