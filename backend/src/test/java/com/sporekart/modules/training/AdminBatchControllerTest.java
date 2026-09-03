package com.sporekart.modules.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.BatchApplicationService;
import com.sporekart.modules.training.controller.AdminBatchController;
import com.sporekart.modules.training.controller.dto.CreateBatchRequest;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.exception.BatchAlreadyExistsException;
import com.sporekart.modules.training.domain.exception.BatchNotFoundException;
import com.sporekart.modules.training.domain.exception.InvalidScheduleException;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminBatchController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BatchApplicationService batchService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/batches should create batch and return 201 Created")
    void testCreateBatchSuccess() throws Exception {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        CreateBatchRequest request = new CreateBatchRequest(
                "prog-123",
                "TRN-2026-001",
                start,
                end,
                20,
                DeliveryMode.ONLINE,
                null,
                "https://meet.google.com/test",
                "Asia/Kolkata"
        );

        TrainingBatch batch = TrainingBatch.create("prog-123", "TRN-2026-001", start, end, 20, DeliveryMode.ONLINE, null, "https://meet.google.com/test", "Asia/Kolkata", "admin");

        when(batchService.createBatch(eq("prog-123"), eq("TRN-2026-001"), any(), any(), eq(20), eq(DeliveryMode.ONLINE), any(), any(), any(), any()))
                .thenReturn(batch);

        mockMvc.perform(post("/api/v1/admin/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.batchCode").value("TRN-2026-001"))
                .andExpect(jsonPath("$.data.status").value("PLANNED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/batches duplicate batch code should return 409 Conflict")
    void testCreateBatchDuplicateCode() throws Exception {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        CreateBatchRequest request = new CreateBatchRequest(
                "prog-123", "TRN-EXISTING", start, end, 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata"
        );

        when(batchService.createBatch(any(), any(), any(), any(), anyInt(), any(), any(), any(), any(), any()))
                .thenThrow(new BatchAlreadyExistsException("Batch with code 'TRN-EXISTING' already exists"));

        mockMvc.perform(post("/api/v1/admin/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BATCH_ALREADY_EXISTS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/batches with start >= end should return 400 Bad Request")
    void testCreateBatchInvalidSchedule() throws Exception {
        Instant now = Instant.now();
        CreateBatchRequest request = new CreateBatchRequest(
                "prog-123", "TRN-INVALID", now.plus(5, ChronoUnit.DAYS), now.plus(2, ChronoUnit.DAYS), 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata"
        );

        when(batchService.createBatch(any(), any(), any(), any(), anyInt(), any(), any(), any(), any(), any()))
                .thenThrow(new InvalidScheduleException("Scheduled start date must be before end date"));

        mockMvc.perform(post("/api/v1/admin/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_SCHEDULE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/batches/{id}/activate should activate batch")
    void testActivateBatch() throws Exception {
        TrainingBatch batch = TrainingBatch.create("prog-123", "TRN-001", Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(5, ChronoUnit.DAYS), 10);
        batch.activate();

        when(batchService.activateBatch(eq("batch-123"), any())).thenReturn(batch);

        mockMvc.perform(post("/api/v1/admin/batches/batch-123/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/admin/batches/{id} not found should return 404 Not Found")
    void testGetBatchNotFound() throws Exception {
        when(batchService.getBatchById("non-existent"))
                .thenThrow(new BatchNotFoundException("TrainingBatch not found for id: non-existent"));

        mockMvc.perform(get("/api/v1/admin/batches/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BATCH_NOT_FOUND"));
    }
}
