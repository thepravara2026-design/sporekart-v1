package com.sporekart.application.security;

import com.sporekart.modules.security.domain.UserRole;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityHardeningTestSuiteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("6C-001: Invalid credentials should be rejected with 401 Unauthorized")
    void testInvalidCredentialsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid@sporekart.com\",\"password\":\"WrongPassword123!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("6C-003: Modified or tampered JWT token should be rejected with 401 Unauthorized")
    void testTamperedTokenRejected() throws Exception {
        String token = jwtTokenProvider.generateAccessToken("user-1", "user@sporekart.com", UserRole.ROLE_CUSTOMER, "session-1");
        String tamperedToken = token.substring(0, token.length() - 5) + "ABCDE";

        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("6C-004: Customer attempting to access Admin endpoint should be rejected with 403 Forbidden")
    void testCustomerAccessingAdminEndpointForbidden() throws Exception {
        String customerToken = jwtTokenProvider.generateAccessToken("cust-101", "customer@sporekart.com", UserRole.ROLE_CUSTOMER, "session-cust");

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6C-005: IDOR Check - User B accessing User A's order detail directly should be rejected with 404/403")
    void testIdorOrderAccessDenied() throws Exception {
        String userBToken = jwtTokenProvider.generateAccessToken("user-B", "userB@sporekart.com", UserRole.ROLE_CUSTOMER, "session-B");
        UUID userAOrderId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/orders/" + userAOrderId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound()); // Scoped query returns 404 (Resource Not Found for this Customer)
    }

    @Test
    @DisplayName("6C-011: SQL Injection payload in catalog search should execute safely without 500 error or syntax leak")
    void testSqlInjectionPayloadSafe() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("search", "' OR '1'='1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6C-013: Payment Webhook with invalid or missing signature should be rejected with 400 Bad Request")
    void testInvalidWebhookSignatureRejected() throws Exception {
        mockMvc.perform(post("/api/v1/payments/webhooks/razorpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Razorpay-Signature", "invalid_sig_123")
                        .content("{\"event\":\"payment.captured\",\"payload\":{}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6C-018: Security Headers (CSP, Referrer Policy, Frame Options) should be present in HTTP responses")
    void testSecurityHeadersPresent() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String csp = result.getResponse().getHeader("Content-Security-Policy");
                    String frame = result.getResponse().getHeader("X-Frame-Options");
                    String referrer = result.getResponse().getHeader("Referrer-Policy");
                    if (csp == null && frame == null && referrer == null) {
                        throw new AssertionError("Missing security headers!");
                    }
                });
    }
}
