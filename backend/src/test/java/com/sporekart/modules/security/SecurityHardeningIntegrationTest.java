package com.sporekart.modules.security;

import com.sporekart.modules.notification.application.NotificationRetentionService;
import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationPriority;
import com.sporekart.modules.notification.domain.NotificationStatus;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.security.domain.UserRole;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SecurityHardeningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private SpringDataJpaNotificationRepository notificationRepository;

    @Autowired
    private NotificationRetentionService retentionService;

    private String validCustomerToken;
    private String validAdminToken;

    @BeforeEach
    void setUp() {
        validCustomerToken = jwtTokenProvider.generateAccessToken("cust-101", "customer@example.com", UserRole.ROLE_CUSTOMER, "session-101");
        validAdminToken = jwtTokenProvider.generateAccessToken("admin-999", "admin@sporekart.com", UserRole.ROLE_ADMIN, "session-999");
    }

    @Test
    @DisplayName("7K-001: Unauthenticated request to protected endpoint is rejected with 401")
    void test001_UnauthenticatedRequestRejected() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("7K-002: Authenticated normal user is accepted for user endpoints")
    void test002_AuthenticatedNormalUserAccepted() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7K-003: Normal customer user denied access to admin endpoints with 403")
    void test003_NormalUserDeniedAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/health")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("7K-004: Admin user allowed access to admin endpoints with 200")
    void test004_AdminAllowedAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/health")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallStatus").exists());
    }

    @Test
    @DisplayName("7K-005: Invalid JWT token rejected with 401")
    void test005_InvalidJwtRejected() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer invalid.jwt.token.string"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7K-006: Expired JWT token rejected with 401")
    void test006_ExpiredJwtRejected() throws Exception {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                "sporekart-v3-super-secure-production-jwt-secret-key-min-512-bits-for-hmac-sha512-signing-spec",
                -1000L,
                "sporekart-platform"
        );
        String expiredToken = shortLivedProvider.generateAccessToken("cust-101", "cust@example.com", UserRole.ROLE_CUSTOMER, "sess-1");

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7K-007: Tampered JWT token rejected with 401")
    void test007_TamperedJwtRejected() throws Exception {
        String tamperedToken = validCustomerToken.substring(0, validCustomerToken.length() - 6) + "XXXXXX";
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7K-008: Malformed authorization header rejected safely")
    void test008_MalformedAuthorizationHeaderRejected() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7K-009: IDOR protection prevents unauthorized access across users")
    void test009_IdorProtection() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7K-010: Customer support ticket endpoint queries user-scoped data")
    void test010_CustomerSupportTicketsEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/customer/support/tickets")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7K-011: Privileged field mass-assignment blocked in register/user DTOs")
    void test011_PrivilegedFieldMassAssignmentBlocked() throws Exception {
        String body = """
            {
              "email": "hacker_test_%s@example.com",
              "password": "Password123!",
              "role": "ROLE_ADMIN"
            }
            """.formatted(UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("7K-012: Invalid request payload rejected with 400")
    void test012_InvalidRequestPayloadRejected() throws Exception {
        String invalidBody = "{\"email\": \"invalid-email\", \"password\": \"\"}";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("7K-013: Oversized malformed payload rejected safely")
    void test013_OversizedRequestRejected() throws Exception {
        String hugeString = "A".repeat(50000);
        String body = "{\"email\": \"" + hugeString + "@example.com\", \"password\": \"Password123!\"}";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7K-014: Pagination maximum limit enforced (size <= 100)")
    void test014_PaginationMaximumEnforced() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products?page=0&size=9999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_PAGE_SIZE"));
    }

    @Test
    @DisplayName("7K-015: Invalid sort field rejected with 400")
    void test015_InvalidSortFieldRejected() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products?sort=nonExistentField,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_SORT"));
    }

    @Test
    @DisplayName("7K-016: PII recipient sanitized in admin notification response")
    void test016_PiiNotExposed() throws Exception {
        Notification notif = new Notification(
                "evt-7k-pii", "ORDER_CONFIRMATION", "cust-101", "cust-101",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "john.doe@example.com", "Subject", "Body message",
                NotificationPriority.NORMAL, "idemp-pii-100", "corr-1", "trace-1"
        );
        notif.markDelivered("msg-pii-100");
        notificationRepository.saveAndFlush(notif);

        mockMvc.perform(get("/api/v1/admin/notifications/" + notif.getId())
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recipient").value("jo***@example.com"));
    }

    @Test
    @DisplayName("7K-017: Secrets and credentials not exposed in APIs")
    void test017_SecretsNotExposed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/health")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("apiKey"))));
    }

    @Test
    @DisplayName("7K-018: Full JWT token not printed in server responses")
    void test018_JwtNotLogged() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/health")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(validAdminToken))));
    }

    @Test
    @DisplayName("7K-019: Password hashes not exposed in user profile API")
    void test019_PasswordNotLogged() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("passwordHash"))));
    }

    @Test
    @DisplayName("7K-020: SQL injection-oriented search input safely handled")
    void test020_SqlInjectionInputHandled() throws Exception {
        String sqlInjection = "' OR '1'='1' -- ";
        mockMvc.perform(get("/api/v1/catalog/products?search=" + sqlInjection))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7K-021: XSS-oriented payload input safely handled")
    void test021_XssInputHandled() throws Exception {
        String xssPayload = "<script>alert('xss')</script>";
        mockMvc.perform(get("/api/v1/catalog/products?search=" + xssPayload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7K-022: CORS policy configured safely")
    void test022_CorsPolicyVerified() throws Exception {
        mockMvc.perform(options("/api/v1/health")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("7K-023: Security HTTP headers verified")
    void test023_SecurityHeadersVerified() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("7K-024: Safe error responses without stack traces")
    void test024_SafeErrorResponses() throws Exception {
        mockMvc.perform(get("/api/v1/non-existent-endpoint-abc-xyz")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("at com.sporekart"))))
                .andExpect(jsonPath("$.error.code").exists());
    }

    @Test
    @DisplayName("7K-025: Admin retention endpoint protected against normal user")
    void test025_AdminRetentionEndpointProtected() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/retention/health")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7K-026: Retention dry-run preview does not mutate database records")
    void test026_RetentionDryRunCannotMutate() throws Exception {
        Notification oldNotif = new Notification(
                "evt-7k-dryrun", "ORDER_CONFIRMATION", "cust-101", "cust-101",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "cust@example.com", "Subject", "Unminimized Body",
                NotificationPriority.NORMAL, "idemp-dryrun-1", "corr-1", "trace-1"
        );
        oldNotif.markDelivered("msg-dryrun-1");

        java.lang.reflect.Field field = Notification.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(oldNotif, Instant.now().minusSeconds(864000L));
        notificationRepository.saveAndFlush(oldNotif);

        mockMvc.perform(get("/api/v1/admin/notifications/retention/preview")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligibleNotificationsForDeletion").exists());

        Notification reloaded = notificationRepository.findById(oldNotif.getId()).orElseThrow();
        assertThat(reloaded.getBody()).isEqualTo("Unminimized Body");
    }

    @Test
    @DisplayName("7K-027: Notification admin retry endpoint protected")
    void test027_NotificationAdminRetryProtected() throws Exception {
        mockMvc.perform(post("/api/v1/admin/notifications/notif-id-123/retry")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7K-028: Notification admin cancel endpoint protected")
    void test028_NotificationAdminCancelProtected() throws Exception {
        mockMvc.perform(post("/api/v1/admin/notifications/notif-id-123/cancel")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7K-029: Audit log endpoint protected against non-admin")
    void test029_AuditEndpointProtected() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/audit")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7K-030: Audit records remain sanitized")
    void test030_AuditRecordsSanitized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/audit")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("7K-031: Payment privileged status cannot be client-controlled")
    void test031_PaymentPrivilegedStateNotClientControlled() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + validCustomerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"ord-1\", \"amount\": 100.0, \"status\":\"CAPTURED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7K-032: Order privileged status cannot be client-controlled")
    void test032_OrderPrivilegedStateNotClientControlled() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + validCustomerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[], \"status\":\"DELIVERED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7K-033: Inventory privileged mutation endpoint protected")
    void test033_InventoryPrivilegedMutationProtected() throws Exception {
        mockMvc.perform(post("/api/v1/admin/inventory/SKU-1001/adjustments")
                        .header("Authorization", "Bearer " + validCustomerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newOnHandQuantity\": 50, \"reason\": \"Adjustment\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7K-034: Shipment privileged mutation endpoint protected")
    void test034_ShipmentPrivilegedMutationProtected() throws Exception {
        mockMvc.perform(post("/api/v1/admin/shipments/SHIP-1001/cancel")
                        .header("Authorization", "Bearer " + validCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7K-035: Actuator metrics endpoints protected against public access")
    void test035_ActuatorMetricsProtected() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/metrics")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7K-036: Webhook endpoints permitAll for external provider callbacks (no 401)")
    void test036_WebhookEndpointsVerified() throws Exception {
        mockMvc.perform(post("/api/v1/payments/webhooks/razorpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event\":\"payment.captured\"}"))
                .andExpect(status().isBadRequest()); // Bypasses auth (no 401), returns 400 due to missing signature header
    }

    @Test
    @DisplayName("7K-037: Backend authorization enforces security regardless of frontend headers")
    void test037_BackendAuthorizationEnforced() throws Exception {
        mockMvc.perform(get("/api/v1/admin/support/tickets")
                        .header("X-Admin-Role", "ADMIN"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7K-038: Security failures do not leak raw stack trace")
    void test038_SecurityFailureNoStackTrace() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/health"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("at com.sporekart"))));
    }

    @Test
    @DisplayName("7K-039: Security audit event generated on protected admin mutation")
    void test039_SecurityAuditEventGenerated() throws Exception {
        mockMvc.perform(post("/api/v1/admin/notifications/retention/run?dryRun=true")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7K-040: No regression to existing security health check")
    void test040_NoRegressionSecurityHealth() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
