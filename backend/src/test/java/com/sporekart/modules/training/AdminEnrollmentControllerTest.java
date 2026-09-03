package com.sporekart.modules.training;

import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.controller.AdminEnrollmentController;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminEnrollmentController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminEnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollmentApplicationService enrollmentService;

    @MockitoBean
    private com.sporekart.modules.training.application.AdminTrainingOperationsService operationsService;

    @MockitoBean
    private com.sporekart.modules.training.application.TrainingCancellationService cancellationService;

    @MockitoBean
    private com.sporekart.modules.training.application.TrainingRescheduleService rescheduleService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private TrainingEnrollment mockEnrollment;

    @BeforeEach
    void setUp() {
        mockEnrollment = TrainingEnrollment.create("batch-101", "trainee-1", "key-1", "admin");
    }

    @Test
    @WithMockUser(username = "adminUser", roles = "ADMIN")
    @DisplayName("GET /api/v1/admin/batches/{batchId}/enrollments returns 200 OK for ADMIN role")
    void testGetBatchEnrollmentsAdminSuccess() throws Exception {
        when(enrollmentService.getBatchEnrollmentsForAdmin(eq("batch-101"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockEnrollment)));

        mockMvc.perform(get("/api/v1/admin/batches/batch-101/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].batchId").value("batch-101"));
    }
}
