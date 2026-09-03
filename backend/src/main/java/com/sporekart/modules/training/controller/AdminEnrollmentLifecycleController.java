package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.application.EnrollmentLifecycleService;
import com.sporekart.modules.training.controller.dto.EnrollmentLifecycleResponse;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/training/enrollments")
public class AdminEnrollmentLifecycleController {

    private final EnrollmentApplicationService enrollmentService;
    private final EnrollmentLifecycleService lifecycleService;

    public AdminEnrollmentLifecycleController(EnrollmentApplicationService enrollmentService, EnrollmentLifecycleService lifecycleService) {
        this.enrollmentService = enrollmentService;
        this.lifecycleService = lifecycleService;
    }

    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF') or hasAuthority('training:read')")
    public ResponseEntity<ApiResponse<EnrollmentLifecycleResponse>> getAdminEnrollmentDetails(@PathVariable("enrollmentId") String enrollmentId) {
        enforceAdminRole();
        String actor = getAuthenticatedActor();
        TrainingEnrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId, actor, true);
        return ResponseEntity.ok(ApiResponse.success(EnrollmentLifecycleResponse.fromDomain(enrollment)));
    }

    @PostMapping("/{enrollmentId}/recover")
    @PreAuthorize("hasAnyRole('ADMIN') or hasAuthority('training:admin')")
    public ResponseEntity<ApiResponse<EnrollmentLifecycleResponse>> recoverEnrollmentConfirmation(@PathVariable("enrollmentId") String enrollmentId) {
        enforceAdminRole();
        String actor = getAuthenticatedActor();
        TrainingEnrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId, actor, true);

        TrainingEnrollment recovered = lifecycleService.recoverEnrollmentConfirmation(
                enrollment.getBatchId(),
                enrollment.getTraineeId(),
                enrollment.getPaymentReference(),
                actor
        );
        return ResponseEntity.ok(ApiResponse.success(EnrollmentLifecycleResponse.fromDomain(recovered)));
    }

    private void enforceAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedEnrollmentAccessException("Authentication required for admin enrollment operations");
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") ||
                a.getAuthority().equalsIgnoreCase("ADMIN") ||
                a.getAuthority().equalsIgnoreCase("ROLE_STAFF") ||
                a.getAuthority().equalsIgnoreCase("training:admin") ||
                a.getAuthority().equalsIgnoreCase("training:read")
        );
        if (!isAdmin) {
            throw new UnauthorizedEnrollmentAccessException("Access denied: Admin role required");
        }
    }

    private String getAuthenticatedActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ADMIN_SYSTEM";
    }
}
