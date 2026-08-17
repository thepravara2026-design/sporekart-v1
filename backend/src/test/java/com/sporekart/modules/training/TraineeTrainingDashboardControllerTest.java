package com.sporekart.modules.training;

import com.sporekart.modules.training.application.TraineeTrainingOperationsService;
import com.sporekart.modules.training.controller.TraineeTrainingDashboardController;
import com.sporekart.modules.training.controller.dto.TraineeEnrollmentDetailResponse;
import com.sporekart.modules.training.controller.dto.TraineeTrainingDashboardResponse;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraineeTrainingDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class TraineeTrainingDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TraineeTrainingOperationsService operationsService;

    @MockBean
    private com.sporekart.modules.training.application.TrainingCancellationService cancellationService;

    @MockBean
    private com.sporekart.modules.training.application.TrainingRescheduleService rescheduleService;

    @Test
    @WithMockUser(username = "trainee-123", roles = {"TRAINEE"})
    @DisplayName("GET /api/v1/trainee/training/dashboard should return HTTP 200 with metrics")
    void shouldReturnTraineeDashboard() throws Exception {
        TraineeTrainingDashboardResponse dashboardResponse = new TraineeTrainingDashboardResponse(
                2, 1, 3, 0, 1, "Spring Boot Deep Dive", "BATCH-001", Instant.now().plusSeconds(3600), "ONLINE", "https://zoom.us/j/123"
        );

        when(operationsService.getTraineeDashboardMetrics("trainee-123")).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/v1/trainee/training/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.upcomingEnrollmentsCount").value(2))
                .andExpect(jsonPath("$.data.nextUpcomingBatchCode").value("BATCH-001"));
    }

    @Test
    @WithMockUser(username = "trainee-123", roles = {"TRAINEE"})
    @DisplayName("GET /api/v1/trainee/training/upcoming should return HTTP 200 with paginated upcoming enrollments")
    void shouldReturnUpcomingTraining() throws Exception {
        TrainingEnrollment enrollment = new TrainingEnrollment(
                "enr-1", "ENR-001", "batch-1", "trainee-123", EnrollmentStatus.CONFIRMED,
                "pay-1", new BigDecimal("500.00"), "INR", Instant.now(), Instant.now(), null, null, Instant.now(), Instant.now(), "trainee-123", "trainee-123", null
        );

        when(operationsService.getUpcomingTrainingForTrainee(eq("trainee-123"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(enrollment)));

        mockMvc.perform(get("/api/v1/trainee/training/upcoming")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("enr-1"));
    }

    @Test
    @WithMockUser(username = "trainee-123", roles = {"TRAINEE"})
    @DisplayName("GET /api/v1/trainee/training/enrollments/{id} should return HTTP 200 for owned enrollment")
    void shouldReturnEnrollmentDetailForOwner() throws Exception {
        TraineeEnrollmentDetailResponse detailResponse = new TraineeEnrollmentDetailResponse(
                "enr-1", "ENR-001", "batch-1", "prog-1", "Spring Boot", "Backend", "BATCH-001",
                "ONLINE", null, "https://zoom.us", "UTC", List.of(), "CONFIRMED", "VERIFIED",
                new BigDecimal("500.00"), "INR", "pay-1", Instant.now(), Instant.now(), null, null, Instant.now()
        );

        when(operationsService.getTraineeEnrollmentDetail("enr-1", "trainee-123")).thenReturn(detailResponse);

        mockMvc.perform(get("/api/v1/trainee/training/enrollments/enr-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.programTitle").value("Spring Boot"))
                .andExpect(jsonPath("$.data.paymentStatusSummary").value("VERIFIED"));
    }

    @Test
    @WithMockUser(username = "attacker-trainee", roles = {"TRAINEE"})
    @DisplayName("GET /api/v1/trainee/training/enrollments/{id} should return HTTP 403 when requesting another trainee's enrollment")
    void shouldReturn403ForIdorAttempt() throws Exception {
        when(operationsService.getTraineeEnrollmentDetail("enr-victim", "attacker-trainee"))
                .thenThrow(new UnauthorizedEnrollmentAccessException("Access denied: You do not own enrollment enr-victim"));

        mockMvc.perform(get("/api/v1/trainee/training/enrollments/enr-victim")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
