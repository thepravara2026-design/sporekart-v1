package com.sporekart.modules.training;

import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.application.EnrollmentLifecycleService;
import com.sporekart.modules.training.controller.AdminEnrollmentLifecycleController;
import com.sporekart.modules.training.controller.TraineeEnrollmentLifecycleController;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainingEnrollmentSecurityTest {

    private EnrollmentApplicationService enrollmentService;
    private EnrollmentLifecycleService lifecycleService;
    private TraineeEnrollmentLifecycleController traineeController;
    private AdminEnrollmentLifecycleController adminController;

    @BeforeEach
    void setUp() {
        enrollmentService = mock(EnrollmentApplicationService.class);
        lifecycleService = mock(EnrollmentLifecycleService.class);
        traineeController = new TraineeEnrollmentLifecycleController(enrollmentService, lifecycleService);
        adminController = new AdminEnrollmentLifecycleController(enrollmentService, lifecycleService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("IDOR DEFENSE: Trainee A attempting to view Trainee B enrollment must throw UnauthorizedEnrollmentAccessException")
    void testTraineeIDORDefense() {
        String enrollmentId = UUID.randomUUID().toString();
        String traineeA = "trainee-A@sporekart.com";
        String traineeB = "trainee-B@sporekart.com";

        // Trainee A is authenticated
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(traineeA, "password", List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        when(enrollmentService.getEnrollmentById(enrollmentId, traineeA, false))
                .thenThrow(new UnauthorizedEnrollmentAccessException("Access denied: Trainee " + traineeA + " cannot view enrollment " + enrollmentId));

        assertThrows(UnauthorizedEnrollmentAccessException.class, () -> traineeController.getMyEnrollmentDetails(enrollmentId));
    }

    @Test
    @DisplayName("ADMIN AUTHORIZATION DEFENSE: Trainee attempting to trigger admin recovery endpoint must throw UnauthorizedEnrollmentAccessException")
    void testAdminRecoveryTraineeDefense() {
        String enrollmentId = UUID.randomUUID().toString();
        String traineeA = "trainee-A@sporekart.com";

        // Trainee A is authenticated with ROLE_USER (non-admin)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(traineeA, "password", List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        assertThrows(UnauthorizedEnrollmentAccessException.class, () -> adminController.recoverEnrollmentConfirmation(enrollmentId));
    }

    @Test
    @DisplayName("ADMIN AUTHORIZATION SUCCESS: Authenticated ADMIN can invoke recovery endpoint")
    void testAdminRecoverySuccess() {
        String enrollmentId = UUID.randomUUID().toString();
        String adminUser = "admin@sporekart.com";

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminUser, "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-100", "trainee-B", new BigDecimal("5000.00"), "INR", null, "trainee-B");
        enrollment.confirm("TRN-PAY-001");

        when(enrollmentService.getEnrollmentById(enrollmentId, adminUser, true)).thenReturn(enrollment);
        when(lifecycleService.recoverEnrollmentConfirmation("batch-100", "trainee-B", "TRN-PAY-001", adminUser)).thenReturn(enrollment);

        var response = adminController.recoverEnrollmentConfirmation(enrollmentId);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(EnrollmentStatus.CONFIRMED, response.getBody().getData().status());
    }
}
