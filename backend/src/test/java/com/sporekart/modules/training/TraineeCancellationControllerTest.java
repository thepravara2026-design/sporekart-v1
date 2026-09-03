package com.sporekart.modules.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.training.application.TraineeTrainingOperationsService;
import com.sporekart.modules.training.application.TrainingCancellationService;
import com.sporekart.modules.training.application.TrainingRescheduleService;
import com.sporekart.modules.training.controller.TraineeTrainingDashboardController;
import com.sporekart.modules.training.controller.dto.CancelEnrollmentRequest;
import com.sporekart.modules.training.controller.dto.RescheduleEnrollmentRequest;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.CancellationWindowExpiredException;
import com.sporekart.modules.training.domain.exception.RescheduleWindowExpiredException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TraineeTrainingDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class TraineeCancellationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TraineeTrainingOperationsService operationsService;
    @MockBean private TrainingCancellationService cancellationService;
    @MockBean private TrainingRescheduleService rescheduleService;

    private static final String TRAINEE_ID = "trainee-123";
    private static final String ENROLLMENT_ID = "enr-777";
    private static final String BATCH_ID = "batch-101";

    @Test
    @WithMockUser(username = TRAINEE_ID, roles = {"TRAINEE"})
    @DisplayName("POST /api/v1/trainee/training/enrollments/{enrollmentId}/cancel - Cancel enrollment success")
    void cancelEnrollmentSuccess() throws Exception {
        TrainingEnrollment cancelled = createTestEnrollment(ENROLLMENT_ID, BATCH_ID, TRAINEE_ID, EnrollmentStatus.CANCELLED);
        when(cancellationService.cancelEnrollmentByTrainee(eq(ENROLLMENT_ID), eq("Personal reason"), eq(TRAINEE_ID)))
                .thenReturn(cancelled);

        CancelEnrollmentRequest request = new CancelEnrollmentRequest("Personal reason");

        mockMvc.perform(post("/api/v1/trainee/training/enrollments/{enrollmentId}/cancel", ENROLLMENT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = TRAINEE_ID, roles = {"TRAINEE"})
    @DisplayName("POST /api/v1/trainee/training/enrollments/{enrollmentId}/cancel - Returns 400 when cancellation window expired")
    void cancelEnrollmentWindowExpiredReturns400() throws Exception {
        when(cancellationService.cancelEnrollmentByTrainee(eq(ENROLLMENT_ID), anyString(), eq(TRAINEE_ID)))
                .thenThrow(new CancellationWindowExpiredException("Cancellation window expired"));

        CancelEnrollmentRequest request = new CancelEnrollmentRequest("Too late");

        mockMvc.perform(post("/api/v1/trainee/training/enrollments/{enrollmentId}/cancel", ENROLLMENT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CANCELLATION_WINDOW_EXPIRED"));
    }

    @Test
    @WithMockUser(username = TRAINEE_ID, roles = {"TRAINEE"})
    @DisplayName("POST /api/v1/trainee/training/enrollments/{enrollmentId}/reschedule - Reschedule enrollment success")
    void rescheduleEnrollmentSuccess() throws Exception {
        TrainingEnrollment rescheduled = createTestEnrollment(ENROLLMENT_ID, "batch-target", TRAINEE_ID, EnrollmentStatus.CONFIRMED);
        when(rescheduleService.rescheduleEnrollmentByTrainee(eq(ENROLLMENT_ID), eq("batch-target"), eq("Schedule clash"), eq(TRAINEE_ID)))
                .thenReturn(rescheduled);

        RescheduleEnrollmentRequest request = new RescheduleEnrollmentRequest("batch-target", "Schedule clash");

        mockMvc.perform(post("/api/v1/trainee/training/enrollments/{enrollmentId}/reschedule", ENROLLMENT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.batchId").value("batch-target"));
    }

    private TrainingEnrollment createTestEnrollment(String id, String batchId, String traineeId, EnrollmentStatus status) {
        return new TrainingEnrollment(
                id, "ENR-001", batchId, traineeId, status, "PAY-REF-100",
                BigDecimal.valueOf(499), "INR", Instant.now(), Instant.now(), null, null,
                Instant.now(), Instant.now(), "TRAINEE", "TRAINEE", "IDEMP-1"
        );
    }
}
