package com.sporekart.modules.training;

import com.sporekart.modules.training.application.AdminTrainingOperationsService;
import com.sporekart.modules.training.controller.AdminTrainingDashboardController;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AdminTrainingOperationsSecurityTest {

    private AdminTrainingOperationsService operationsService;
    private AdminTrainingDashboardController dashboardController;

    @BeforeEach
    void setUp() {
        operationsService = mock(AdminTrainingOperationsService.class);
        dashboardController = new AdminTrainingDashboardController(operationsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("ROLE AUTHORIZATION: Unauthenticated user accessing dashboard must throw UnauthorizedEnrollmentAccessException")
    void testUnauthenticatedDashboardAccess() {
        assertThrows(UnauthorizedEnrollmentAccessException.class, () -> dashboardController.getDashboard());
    }

    @Test
    @DisplayName("ROLE AUTHORIZATION: Trainee (ROLE_USER) accessing admin dashboard must throw UnauthorizedEnrollmentAccessException")
    void testTraineeDashboardAccessDenied() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("trainee@sporekart.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        assertThrows(UnauthorizedEnrollmentAccessException.class, () -> dashboardController.getDashboard());
    }

    @Test
    @DisplayName("ROLE AUTHORIZATION: Admin (ROLE_ADMIN) accessing dashboard must be permitted")
    void testAdminDashboardAccessAllowed() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@sporekart.com", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        assertDoesNotThrow(() -> dashboardController.getDashboard());
    }
}
