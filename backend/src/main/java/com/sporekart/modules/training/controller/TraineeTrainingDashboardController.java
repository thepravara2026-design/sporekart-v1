package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.TraineeTrainingOperationsService;
import com.sporekart.modules.training.application.TrainingCancellationService;
import com.sporekart.modules.training.application.TrainingRescheduleService;
import com.sporekart.modules.training.controller.dto.CancelEnrollmentRequest;
import com.sporekart.modules.training.controller.dto.EnrollmentResponse;
import com.sporekart.modules.training.controller.dto.RescheduleEnrollmentRequest;
import com.sporekart.modules.training.controller.dto.TraineeEnrollmentDetailResponse;
import com.sporekart.modules.training.controller.dto.TraineeTrainingDashboardResponse;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainee/training")
public class TraineeTrainingDashboardController {

    private final TraineeTrainingOperationsService operationsService;
    private final TrainingCancellationService cancellationService;
    private final TrainingRescheduleService rescheduleService;

    public TraineeTrainingDashboardController(
            TraineeTrainingOperationsService operationsService,
            TrainingCancellationService cancellationService,
            TrainingRescheduleService rescheduleService) {
        this.operationsService = operationsService;
        this.cancellationService = cancellationService;
        this.rescheduleService = rescheduleService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<TraineeTrainingDashboardResponse>> getTraineeDashboard() {
        String traineeId = getAuthenticatedTraineeId();
        TraineeTrainingDashboardResponse dashboard = operationsService.getTraineeDashboardMetrics(traineeId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getUpcomingTraining(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        String traineeId = getAuthenticatedTraineeId();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));

        Page<TrainingEnrollment> upcomingPage = operationsService.getUpcomingTrainingForTrainee(traineeId, pageable);
        Page<EnrollmentResponse> responsePage = upcomingPage.map(EnrollmentResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<ApiResponse<TraineeEnrollmentDetailResponse>> getTraineeEnrollmentDetail(@PathVariable("enrollmentId") String enrollmentId) {
        String traineeId = getAuthenticatedTraineeId();
        TraineeEnrollmentDetailResponse detail = operationsService.getTraineeEnrollmentDetail(enrollmentId, traineeId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PostMapping("/enrollments/{enrollmentId}/cancel")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> cancelMyEnrollment(
            @PathVariable("enrollmentId") String enrollmentId,
            @RequestBody(required = false) CancelEnrollmentRequest request) {

        String traineeId = getAuthenticatedTraineeId();
        String reason = request != null ? request.getReason() : null;

        TrainingEnrollment cancelled = cancellationService.cancelEnrollmentByTrainee(enrollmentId, reason, traineeId);
        return ResponseEntity.ok(ApiResponse.success(EnrollmentResponse.fromDomain(cancelled)));
    }

    @PostMapping("/enrollments/{enrollmentId}/reschedule")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> rescheduleMyEnrollment(
            @PathVariable("enrollmentId") String enrollmentId,
            @Valid @RequestBody RescheduleEnrollmentRequest request) {

        String traineeId = getAuthenticatedTraineeId();
        TrainingEnrollment rescheduled = rescheduleService.rescheduleEnrollmentByTrainee(
                enrollmentId, request.getTargetBatchId(), request.getReason(), traineeId
        );
        return ResponseEntity.ok(ApiResponse.success(EnrollmentResponse.fromDomain(rescheduled)));
    }

    private String getAuthenticatedTraineeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        throw new UnauthorizedEnrollmentAccessException("Authentication required to access trainee training Portal");
    }
}
