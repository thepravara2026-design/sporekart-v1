package com.sporekart.modules.training;

import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.BatchApplicationService;
import com.sporekart.modules.training.controller.PublicBatchController;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.exception.BatchNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicBatchController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatchApplicationService batchService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /api/v1/batches should return public active batches page")
    void testListPublicBatches() throws Exception {
        TrainingBatch batch = TrainingBatch.create("prog-123", "TRN-ACTIVE-01", Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(5, ChronoUnit.DAYS), 20, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.activate();

        when(batchService.listPublicActiveBatches(any()))
                .thenReturn(new PageImpl<>(List.of(batch), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].batchCode").value("TRN-ACTIVE-01"))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/batches/{idOrCode} for non-existent batch should return 404 Not Found")
    void testGetPublicBatchNotFound() throws Exception {
        when(batchService.getPublicActiveBatch("unknown-batch"))
                .thenThrow(new BatchNotFoundException("Active TrainingBatch not found for id or code: unknown-batch"));

        mockMvc.perform(get("/api/v1/batches/unknown-batch"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BATCH_NOT_FOUND"));
    }
}
