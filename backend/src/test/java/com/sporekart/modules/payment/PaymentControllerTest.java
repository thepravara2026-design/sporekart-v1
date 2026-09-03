package com.sporekart.modules.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentCheckoutDto;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import com.sporekart.modules.payment.application.dto.WebhookResponseDto;
import com.sporekart.modules.payment.controller.PaymentController;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.domain.WebhookProcessingStatus;
import com.sporekart.modules.payment.domain.exception.PaymentNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentApplicationService paymentApplicationService;

    @Test
    @DisplayName("POST /api/v1/payments should create payment attempt and return checkout information")
    @WithMockUser(username = "cust-100")
    void testCreatePayment() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentCheckoutDto checkout = new PaymentCheckoutDto(
                UUID.randomUUID(), "PAY-SPK-10001", UUID.randomUUID(), "PAY-SPK-10001-ATT-1",
                orderId, new BigDecimal("1500.00"), "INR", PaymentProviderType.MOCK,
                "order_mock_123", "mock_key"
        );

        when(paymentApplicationService.createPayment(eq(orderId), eq("cust-100"))).thenReturn(checkout);

        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("orderId", orderId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentReference").value("PAY-SPK-10001"))
                .andExpect(jsonPath("$.data.providerOrderId").value("order_mock_123"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/verify should verify payment callback")
    @WithMockUser(username = "cust-100")
    void testVerifyPayment() throws Exception {
        PaymentVerificationCommand command = new PaymentVerificationCommand("PAY-SPK-10001", "order_mock_123", "pay_mock_999", "sig_valid");
        PaymentDto dto = new PaymentDto(
                UUID.randomUUID(), "PAY-SPK-10001", UUID.randomUUID(), "cust-100",
                new BigDecimal("1500.00"), "INR", PaymentStatus.SUCCESS, PaymentProviderType.MOCK,
                UUID.randomUUID(), List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(paymentApplicationService.verifyPayment(any(), eq("cust-100"))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/payments/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/{paymentReference} should return 404 when payment does not exist")
    @WithMockUser(username = "cust-100")
    void testGetPaymentNotFound() throws Exception {
        String payRef = "PAY-UNKNOWN";
        when(paymentApplicationService.getPaymentByReference(payRef, "cust-100"))
                .thenThrow(new PaymentNotFoundException(payRef));

        mockMvc.perform(get("/api/v1/payments/{paymentReference}", payRef))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("PAYMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/webhooks/razorpay should allow unauthenticated access and process webhook")
    @WithMockUser
    void testPublicRazorpayWebhook() throws Exception {
        WebhookResponseDto response = new WebhookResponseDto(WebhookProcessingStatus.PROCESSED, "Webhook processed successfully");
        when(paymentApplicationService.processWebhook(eq(PaymentProviderType.RAZORPAY), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/webhooks/razorpay")
                        .with(csrf())
                        .header("X-Razorpay-Signature", "test_sig_123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event_id\":\"evt_123\",\"event\":\"payment.captured\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSED"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/webhooks/razorpay missing X-Razorpay-Signature returns 400 Bad Request")
    @WithMockUser
    void testMissingRazorpaySignatureHeaderReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/payments/webhooks/razorpay")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event_id\":\"evt_123\",\"event\":\"payment.captured\"}"))
                .andExpect(status().isBadRequest());
    }
}
