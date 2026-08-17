package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.application.EnrollmentLifecycleService;
import com.sporekart.modules.training.controller.dto.EnrollmentHistoryResponseDto;
import com.sporekart.modules.training.controller.dto.EnrollmentLifecycleResponse;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.TrainingEnrollmentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/training/my-enrollments")
public class TraineeEnrollmentLifecycleController {

    private final EnrollmentApplicationService enrollmentService;
    private final EnrollmentLifecycleService lifecycleService;

    public TraineeEnrollmentLifecycleController(EnrollmentApplicationService enrollmentService, EnrollmentLifecycleService lifecycleService) {
        this.enrollmentService = enrollmentService;
        this.lifecycleService = lifecycleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EnrollmentLifecycleResponse>>> getMyEnrollments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        String traineeId = getAuthenticatedTraineeId();
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortParts[0]));

        Page<TrainingEnrollment> enrollments = enrollmentService.getTraineeEnrollments(traineeId, pageable);
        Page<EnrollmentLifecycleResponse> responsePage = enrollments.map(EnrollmentLifecycleResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/{enrollmentId}")
    public ResponseEntity<ApiResponse<EnrollmentLifecycleResponse>> getMyEnrollmentDetails(@PathVariable("enrollmentId") String enrollmentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String requesterId = getAuthenticatedTraineeId();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        TrainingEnrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(EnrollmentLifecycleResponse.fromDomain(enrollment)));
    }

    @GetMapping("/{enrollmentId}/history")
    public ResponseEntity<ApiResponse<List<EnrollmentHistoryResponseDto>>> getMyEnrollmentHistory(@PathVariable("enrollmentId") String enrollmentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String requesterId = getAuthenticatedTraineeId();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        List<TrainingEnrollmentHistory> historyList = lifecycleService.getEnrollmentHistory(enrollmentId, requesterId, isAdmin);
        List<EnrollmentHistoryResponseDto> responseList = historyList.stream().map(EnrollmentHistoryResponseDto::fromDomain).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    private String getAuthenticatedTraineeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ANONYMOUS_TRAINEE";
    }
}
