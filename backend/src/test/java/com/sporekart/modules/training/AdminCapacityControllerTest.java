package com.sporekart.modules.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.controller.AdminCapacityController;
import com.sporekart.modules.training.controller.dto.CapacityResponse;
import com.sporekart.modules.training.controller.dto.UpdateCapacityRequest;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.exception.CapacityBelowOccupancyException;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCapacityController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCapacityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CapacityApplicationService capacityService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/admin/batches/{id}/capacity should update capacity and return 200 OK")
    void testUpdateCapacitySuccess() throws Exception {
        UpdateCapacityRequest request = new UpdateCapacityRequest(30);
        CapacityResponse response = new CapacityResponse("batch-123", "TRN-001", 30, 5, 25, false, BatchStatus.ACTIVE);

        when(capacityService.updateBatchCapacity(eq("batch-123"), eq(30), anyString()))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/batches/batch-123/capacity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCapacity").value(30))
                .andExpect(jsonPath("$.data.availableSeats").value(25));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/admin/batches/{id}/capacity below occupancy should return 400 Bad Request")
    void testUpdateCapacityBelowOccupancy() throws Exception {
        UpdateCapacityRequest request = new UpdateCapacityRequest(4);

        when(capacityService.updateBatchCapacity(eq("batch-123"), eq(4), anyString()))
                .thenThrow(new CapacityBelowOccupancyException("New capacity (4) cannot be less than currently occupied seats (6)"));

        mockMvc.perform(patch("/api/v1/admin/batches/batch-123/capacity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CAPACITY_BELOW_OCCUPANCY"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/admin/batches/{id}/capacity should return capacity state")
    void testGetCapacity() throws Exception {
        CapacityResponse response = new CapacityResponse("batch-123", "TRN-001", 20, 5, 15, false, BatchStatus.ACTIVE);

        when(capacityService.getBatchCapacity("batch-123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/batches/batch-123/capacity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCapacity").value(20))
                .andExpect(jsonPath("$.data.occupiedSeats").value(5))
                .andExpect(jsonPath("$.data.availableSeats").value(15));
    }
}
