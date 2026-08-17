package com.sporekart.modules.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.training.application.TrainingPaymentApplicationService;
import com.sporekart.modules.training.controller.TrainingPaymentController;
import com.sporekart.modules.training.controller.dto.TrainingPaymentOrderResponse;
import com.sporekart.modules.training.controller.dto.TrainingPaymentStatusResponse;
import com.sporekart.modules.training.controller.dto.TrainingPaymentVerificationRequestDto;
import com.sporekart.modules.training.domain.TrainingPaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingPaymentController.class)
class TrainingPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainingPaymentApplicationService paymentService;

    @BeforeEach
    void setUp() {}

    @Test
    @WithMockUser(username = "trainee-1", roles = {"TRAINEE"})
    @DisplayName("POST /api/v1/batches/{batchId}/enrollment/payment-order returns 201 Created with checkout info")
    void testCreatePaymentOrderSuccess() throws Exception {
        TrainingPaymentOrderResponse response = new TrainingPaymentOrderResponse(
                "trn-pay-1", "pay-1", "TRN-PAY-001", "batch-101",
                new BigDecimal("5000.00"), "INR", PaymentProviderType.MOCK, "order_mock_123", "key_123"
        );

        when(paymentService.initiatePaymentOrder(eq("batch-101"), eq("trainee-1"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/batches/batch-101/enrollment/payment-order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trainingPaymentId").value("trn-pay-1"))
                .andExpect(jsonPath("$.data.providerOrderId").value("order_mock_123"));
    }

    @Test
    @WithMockUser(username = "trainee-1", roles = {"TRAINEE"})
    @DisplayName("POST /api/v1/batches/{batchId}/enrollment/payment-verify returns 200 OK on successful verification")
    void testVerifyPaymentSuccess() throws Exception {
        TrainingPaymentStatusResponse response = new TrainingPaymentStatusResponse(
                "trn-pay-1", "pay-1", "batch-101", "trainee-1", "enroll-1",
                new BigDecimal("5000.00"), "INR", TrainingPaymentStatus.ENROLLMENT_CONFIRMED,
                Instant.now(), Instant.now()
        );

        when(paymentService.verifyPayment(eq("batch-101"), any(), eq("trainee-1"))).thenReturn(response);

        TrainingPaymentVerificationRequestDto requestDto = new TrainingPaymentVerificationRequestDto(
                "TRN-PAY-001", "order_mock_123", "pay_mock_999", "sig_valid"
        );

        mockMvc.perform(post("/api/v1/batches/batch-101/enrollment/payment-verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ENROLLMENT_CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "trainee-1", roles = {"TRAINEE"})
    @DisplayName("GET /api/v1/batches/{batchId}/enrollment/payment-status returns 200 OK")
    void testGetPaymentStatusSuccess() throws Exception {
        TrainingPaymentStatusResponse response = new TrainingPaymentStatusResponse(
                "trn-pay-1", "pay-1", "batch-101", "trainee-1", "enroll-1",
                new BigDecimal("5000.00"), "INR", TrainingPaymentStatus.ENROLLMENT_CONFIRMED,
                Instant.now(), Instant.now()
        );

        when(paymentService.getPaymentStatus("batch-101", "trainee-1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/batches/batch-101/enrollment/payment-status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ENROLLMENT_CONFIRMED"));
    }
}
