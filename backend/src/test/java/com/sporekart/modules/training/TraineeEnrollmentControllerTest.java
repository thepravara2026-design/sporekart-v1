package com.sporekart.modules.training;

import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.controller.TraineeEnrollmentController;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.DuplicateEnrollmentException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TraineeEnrollmentController.class)
@ContextConfiguration(classes = {TraineeEnrollmentController.class, GlobalExceptionHandler.class})
class TraineeEnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollmentApplicationService enrollmentService;

    private TrainingEnrollment mockEnrollment;

    @BeforeEach
    void setUp() {
        mockEnrollment = TrainingEnrollment.create("batch-101", "trainee-user", "idempotency-123", "trainee-user");
    }

    @Test
    @WithMockUser(username = "trainee-user", roles = "USER")
    @DisplayName("POST /api/v1/batches/{batchId}/enrollments creates enrollment and returns 201 Created")
    void testEnrollSuccess() throws Exception {
        when(enrollmentService.enrollTrainee(eq("batch-101"), eq("trainee-user"), any())).thenReturn(mockEnrollment);

        mockMvc.perform(post("/api/v1/batches/batch-101/enrollments")
                        .with(csrf())
                        .header("X-Idempotency-Key", "idempotency-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"idempotency-123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.batchId").value("batch-101"))
                .andExpect(jsonPath("$.data.traineeId").value("trainee-user"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "trainee-user", roles = "USER")
    @DisplayName("POST /api/v1/batches/{batchId}/enrollments returns 409 Conflict when duplicate enrollment exists")
    void testEnrollDuplicateConflict() throws Exception {
        when(enrollmentService.enrollTrainee(eq("batch-101"), eq("trainee-user"), any()))
                .thenThrow(new DuplicateEnrollmentException("Trainee is already enrolled in batch batch-101"));

        mockMvc.perform(post("/api/v1/batches/batch-101/enrollments")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_ENROLLMENT"));
    }

    @Test
    @WithMockUser(username = "trainee-user", roles = "USER")
    @DisplayName("POST /api/v1/batches/{batchId}/enrollments returns 409 Conflict when batch is FULL")
    void testEnrollBatchFullConflict() throws Exception {
        when(enrollmentService.enrollTrainee(eq("batch-101"), eq("trainee-user"), any()))
                .thenThrow(new BatchFullException("Batch batch-101 is FULL"));

        mockMvc.perform(post("/api/v1/batches/batch-101/enrollments")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("BATCH_FULL"));
    }

    @Test
    @WithMockUser(username = "trainee-user", roles = "USER")
    @DisplayName("GET /api/v1/me/enrollments returns 200 OK with paginated user enrollments")
    void testGetMyEnrollmentsSuccess() throws Exception {
        when(enrollmentService.getTraineeEnrollments(eq("trainee-user"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockEnrollment)));

        mockMvc.perform(get("/api/v1/me/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].batchId").value("batch-101"));
    }

    @Test
    @WithMockUser(username = "trainee-user", roles = "USER")
    @DisplayName("GET /api/v1/enrollments/{id} returns 403 Forbidden on IDOR violation")
    void testGetEnrollmentByIdIdorForbidden() throws Exception {
        when(enrollmentService.getEnrollmentById(eq("enr-secret"), eq("trainee-user"), eq(false)))
                .thenThrow(new UnauthorizedEnrollmentAccessException("Access denied to enrollment enr-secret"));

        mockMvc.perform(get("/api/v1/enrollments/enr-secret"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED_ENROLLMENT_ACCESS"));
    }
}
