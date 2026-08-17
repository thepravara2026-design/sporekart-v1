package com.sporekart.modules.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.AdminTrainingOperationsService;
import com.sporekart.modules.training.controller.AdminTrainingDashboardController;
import com.sporekart.modules.training.controller.dto.AdminTrainingDashboardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTrainingDashboardController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminTrainingDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminTrainingOperationsService operationsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/admin/training/dashboard should return dashboard metrics with 200 OK")
    void testGetDashboardSuccess() throws Exception {
        AdminTrainingDashboardResponse dashboardResponse = new AdminTrainingDashboardResponse(
                5L, 10L, 200L, 150L, 50L, 8L, 160L, 10L, 2L, 1L, 3L
        );
        when(operationsService.getDashboardMetrics()).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/v1/admin/training/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeProgramsCount").value(5))
                .andExpect(jsonPath("$.data.upcomingBatchesCount").value(10))
                .andExpect(jsonPath("$.data.totalConfiguredCapacity").value(200))
                .andExpect(jsonPath("$.data.totalOccupiedSeats").value(150))
                .andExpect(jsonPath("$.data.totalRemainingSeats").value(50))
                .andExpect(jsonPath("$.data.activeDemandCount").value(8))
                .andExpect(jsonPath("$.data.totalEnrollmentsCount").value(160))
                .andExpect(jsonPath("$.data.paymentPendingEnrollmentsCount").value(10))
                .andExpect(jsonPath("$.data.paymentFailedEnrollmentsCount").value(2))
                .andExpect(jsonPath("$.data.paymentVerifiedExceptionsCount").value(1))
                .andExpect(jsonPath("$.data.batchesApproachingFullCount").value(3));
    }
}
