package com.sporekart.modules.training;

import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.DemandApplicationService;
import com.sporekart.modules.training.controller.AdminDemandController;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
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

@WebMvcTest(AdminDemandController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDemandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DemandApplicationService demandService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(username = "admin@sporekart.com", roles = {"ADMIN"})
    @DisplayName("GET /api/v1/admin/batches/{batchId}/demand - Should return paginated batch demands for admin (200 OK)")
    void testGetBatchDemandsForAdminSuccess() throws Exception {
        TrainingDemandRequest demand = TrainingDemandRequest.create("batch-101", "trainee-john@sporekart.com", "admin@sporekart.com");
        when(demandService.getBatchDemandsForAdmin(eq("batch-101"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(demand)));

        mockMvc.perform(get("/api/v1/admin/batches/batch-101/demand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].batchId").value("batch-101"))
                .andExpect(jsonPath("$.data.content[0].traineeId").value("trainee-john@sporekart.com"));
    }

    @Test
    @WithMockUser(username = "admin@sporekart.com", roles = {"ADMIN"})
    @DisplayName("GET /api/v1/admin/batches/{batchId}/demand/summary - Should return active demand count for admin (200 OK)")
    void testGetBatchDemandSummarySuccess() throws Exception {
        when(demandService.getBatchDemandCount("batch-101")).thenReturn(25L);

        mockMvc.perform(get("/api/v1/admin/batches/batch-101/demand/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.batchId").value("batch-101"))
                .andExpect(jsonPath("$.data.activeDemandCount").value(25));
    }
}
