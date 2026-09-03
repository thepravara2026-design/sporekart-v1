package com.sporekart.modules.training;

import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.DemandApplicationService;
import com.sporekart.modules.training.controller.TraineeDemandController;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import com.sporekart.modules.training.domain.exception.DuplicateDemandException;
import com.sporekart.modules.training.domain.exception.InvalidDemandStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeDemandController.class)
@Import(GlobalExceptionHandler.class)
class TraineeDemandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DemandApplicationService demandService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(username = "trainee-john@sporekart.com", roles = {"USER"})
    @DisplayName("POST /api/v1/batches/{batchId}/demand - Should create demand request cleanly (201 Created)")
    void testCreateDemandSuccess() throws Exception {
        TrainingDemandRequest demand = TrainingDemandRequest.create("batch-101", "trainee-john@sporekart.com", "trainee-john@sporekart.com");
        when(demandService.createDemand("batch-101", "trainee-john@sporekart.com")).thenReturn(demand);

        mockMvc.perform(post("/api/v1/batches/batch-101/demand")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.batchId").value("batch-101"))
                .andExpect(jsonPath("$.data.traineeId").value("trainee-john@sporekart.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "trainee-john@sporekart.com", roles = {"USER"})
    @DisplayName("POST /api/v1/batches/{batchId}/demand - Should return 409 Conflict when duplicate active demand exists")
    void testCreateDemandDuplicateConflict() throws Exception {
        when(demandService.createDemand("batch-101", "trainee-john@sporekart.com"))
                .thenThrow(new DuplicateDemandException("Active demand request already exists"));

        mockMvc.perform(post("/api/v1/batches/batch-101/demand")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_DEMAND"));
    }

    @Test
    @WithMockUser(username = "trainee-john@sporekart.com", roles = {"USER"})
    @DisplayName("POST /api/v1/batches/{batchId}/demand - Should return 400 Bad Request when batch has available capacity")
    void testCreateDemandOpenBatchBadRequest() throws Exception {
        when(demandService.createDemand("batch-open", "trainee-john@sporekart.com"))
                .thenThrow(new InvalidDemandStateException("Batch has available capacity"));

        mockMvc.perform(post("/api/v1/batches/batch-open/demand")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_DEMAND_STATE"));
    }

    @Test
    @WithMockUser(username = "trainee-john@sporekart.com", roles = {"USER"})
    @DisplayName("GET /api/v1/me/demands - Should return paginated demands for session trainee (200 OK)")
    void testGetMyDemandsSuccess() throws Exception {
        TrainingDemandRequest demand = TrainingDemandRequest.create("batch-101", "trainee-john@sporekart.com", "trainee-john@sporekart.com");
        when(demandService.getTraineeDemands(eq("trainee-john@sporekart.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(demand)));

        mockMvc.perform(get("/api/v1/me/demands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].batchId").value("batch-101"))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "trainee-john@sporekart.com", roles = {"USER"})
    @DisplayName("DELETE /api/v1/demands/{id} - Should withdraw demand cleanly (200 OK)")
    void testWithdrawDemandSuccess() throws Exception {
        TrainingDemandRequest demand = TrainingDemandRequest.create("batch-101", "trainee-john@sporekart.com", "trainee-john@sporekart.com");
        demand.withdraw("trainee-john@sporekart.com");
        when(demandService.withdrawDemand(eq(demand.getId()), eq("trainee-john@sporekart.com"), eq(false)))
                .thenReturn(demand);

        mockMvc.perform(delete("/api/v1/demands/" + demand.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"));
    }
}
