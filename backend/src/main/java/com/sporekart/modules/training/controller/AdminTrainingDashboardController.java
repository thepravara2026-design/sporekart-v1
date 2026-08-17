package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.AdminTrainingOperationsService;
import com.sporekart.modules.training.controller.dto.AdminTrainingDashboardResponse;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/training")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN') or hasAuthority('training:admin')")
public class AdminTrainingDashboardController {

    private final AdminTrainingOperationsService operationsService;

    public AdminTrainingDashboardController(AdminTrainingOperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminTrainingDashboardResponse>> getDashboard() {
        enforceAdminRole();
        AdminTrainingDashboardResponse response = operationsService.getDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private void enforceAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedEnrollmentAccessException("Authentication required for admin dashboard");
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") ||
                a.getAuthority().equalsIgnoreCase("ADMIN") ||
                a.getAuthority().equalsIgnoreCase("training:admin") ||
                a.getAuthority().equalsIgnoreCase("ROLE_STAFF")
        );
        if (!isAdmin) {
            throw new UnauthorizedEnrollmentAccessException("Access denied: Admin role required");
        }
    }
}
