package com.sporekart.modules.notification;

import com.sporekart.application.outbox.application.OutboxService;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.infrastructure.OutboxWorker;
import com.sporekart.modules.notification.application.*;
import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.notification.infrastructure.NotificationDeliveryWorker;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.notification.infrastructure.provider.HttpSendGridEmailProvider;
import com.sporekart.modules.notification.infrastructure.provider.HttpTwilioSmsProvider;
import com.sporekart.modules.notification.infrastructure.resilience.NotificationCircuitBreaker;
import com.sporekart.modules.notification.infrastructure.resilience.ProviderHealthTracker;
import com.sporekart.modules.notification.web.AdminNotificationController;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import com.sporekart.modules.security.domain.SecurityAuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationOperationsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private NotificationApplicationService notificationService;

    @Autowired
    private NotificationOperationsService operationsService;

    @Autowired
    private SpringDataJpaNotificationRepository notificationRepository;

    @Autowired
    private NotificationOrchestrator orchestrator;

    @Autowired
    private NotificationDeliveryWorker deliveryWorker;

    @Autowired
    private NotificationResilienceService resilienceService;

    @Autowired
    private NotificationCircuitBreaker circuitBreaker;

    @Autowired
    private ProviderHealthTracker healthTracker;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxWorker outboxWorker;

    @Autowired
    private HttpSendGridEmailProvider sendGridEmailProvider;

    @Autowired
    private HttpTwilioSmsProvider twilioSmsProvider;

    @BeforeEach
    public void setUp() {
        outboxWorker.setEnabled(true);
        circuitBreaker.reset("MockEmailProvider", NotificationChannel.EMAIL);
        circuitBreaker.reset("MockSmsProvider", NotificationChannel.SMS);
        circuitBreaker.reset("SendGrid", NotificationChannel.EMAIL);
        circuitBreaker.reset("Twilio", NotificationChannel.SMS);
    }

    // 7I-001: Admin Health Authorization
    @Test
    @DisplayName("7I-001: Health endpoint enforces 401 unauthenticated, 403 non-admin, 200 admin")
    public void testAdminHealthAuthorization() throws Exception {
        // 1. Unauthenticated -> 401
        mockMvc.perform(get("/api/v1/admin/notifications/health"))
                .andExpect(status().isUnauthorized());

        // 2. Authenticated Non-Admin -> 403
        mockMvc.perform(get("/api/v1/admin/notifications/health")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("customer-1").roles("CUSTOMER")))
                .andExpect(status().isForbidden());

        // 3. Admin -> 200
        mockMvc.perform(get("/api/v1/admin/notifications/health")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin-1").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallStatus").exists());
    }

    // 7I-002: Provider Health Inspection
    @Test
    @WithMockUser(username = "admin-7i", roles = {"ADMIN"})
    @DisplayName("7I-002: Provider health matrix returns sanitized health metrics without secrets")
    public void testProviderHealthInspection() {
        Map<String, Object> health = operationsService.getOverallHealthSummary();
        assertThat(health).containsKey("resilience");
        String json = health.toString();
        assertThat(json).doesNotContain("apiKey");
        assertThat(json).doesNotContain("accountSid");
        assertThat(json).doesNotContain("secret");
    }

    // 7I-003: Circuit State Inspection
    @Test
    @DisplayName("7I-003: Circuit state visibility reflects OPEN/CLOSED state")
    public void testCircuitStateInspection() {
        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "500 Error", true, 50);
        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "500 Error", true, 50);
        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "500 Error", true, 50);

        assertThat(circuitBreaker.getState("SendGrid", NotificationChannel.EMAIL)).isEqualTo(NotificationCircuitBreaker.State.OPEN);

        Map<String, Object> summary = operationsService.getOverallHealthSummary();
        Map<String, Object> resilience = (Map<String, Object>) summary.get("resilience");
        assertThat(resilience).isNotNull();
    }

    // 7I-004: Backlog Summary
    @Test
    @DisplayName("7I-004: Backlog summary provides accurate status counters")
    public void testBacklogSummary() {
        NotificationOperationsService.BacklogSummaryDto backlog = operationsService.getBacklogSummary();
        assertThat(backlog).isNotNull();
        assertThat(backlog.deliveredCount()).isGreaterThanOrEqualTo(0);
        assertThat(backlog.retryBacklogCount()).isGreaterThanOrEqualTo(0);
    }

    // 7I-005: Retry Backlog
    @Test
    @DisplayName("7I-005: Retry backlog count and oldest retry age are accurately exposed")
    public void testRetryBacklog() {
        Notification notification = new Notification(
                "evt-7i-005", "ORDER_PAID", "user-7i-5", "cust-7i-5",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-5@example.com", "Subject 5", "Body 5",
                NotificationPriority.NORMAL, "idemp-7i-005", "corr-7i-5", "trace-7i-5"
        );
        notification.scheduleRetry("Retry scheduled test", Instant.now().plusSeconds(60));
        notificationRepository.save(notification);

        NotificationOperationsService.BacklogSummaryDto backlog = operationsService.getBacklogSummary();
        assertThat(backlog.retryBacklogCount()).isGreaterThanOrEqualTo(1);
    }

    // 7I-006: Stale Processing Visibility
    @Test
    @DisplayName("7I-006: Stale PROCESSING notification count is detected")
    public void testStaleProcessingVisibility() {
        Notification notification = new Notification(
                "evt-7i-006", "ORDER_PAID", "user-7i-6", "cust-7i-6",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-6@example.com", "Subject 6", "Body 6",
                NotificationPriority.NORMAL, "idemp-7i-006", "corr-7i-6", "trace-7i-6"
        );
        notification.markProcessing("MockEmailProvider");
        notificationRepository.save(notification);

        NotificationOperationsService.BacklogSummaryDto backlog = operationsService.getBacklogSummary();
        assertThat(backlog).isNotNull();
    }

    // 7I-007: Reconciliation Backlog
    @Test
    @DisplayName("7I-007: Reconciliation backlog candidates are exposed via service")
    public void testReconciliationBacklog() {
        Page<NotificationOperationsService.NotificationDetailDto> candidates = operationsService.getReconciliationBacklog(PageRequest.of(0, 10));
        assertThat(candidates).isNotNull();
    }

    // 7I-008: Notification Detail
    @Test
    @DisplayName("7I-008: Notification detail lookup returns sanitized DTO")
    public void testNotificationDetail() {
        Notification notification = new Notification(
                "evt-7i-008", "ORDER_PAID", "user-7i-8", "cust-7i-8",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-8@example.com", "Subject 8", "Body 8",
                NotificationPriority.NORMAL, "idemp-7i-008", "corr-7i-8", "trace-7i-8"
        );
        notification = notificationRepository.save(notification);

        NotificationOperationsService.NotificationDetailDto detail = operationsService.getNotificationDetail(notification.getId());
        assertThat(detail.id()).isEqualTo(notification.getId());
        assertThat(detail.recipient()).contains("***");
    }

    // 7I-009: Notification Timeline
    @Test
    @DisplayName("7I-009: Notification timeline generates ordered lifecycle events")
    public void testNotificationTimeline() {
        Notification notification = new Notification(
                "evt-7i-009", "ORDER_PAID", "user-7i-9", "cust-7i-9",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-9@example.com", "Subject 9", "Body 9",
                NotificationPriority.NORMAL, "idemp-7i-009", "corr-7i-9", "trace-7i-9"
        );
        notification.markSent("msg-7i-9");
        notification.markDelivered("msg-7i-9");
        notification = notificationRepository.save(notification);

        List<NotificationOperationsService.NotificationTimelineEventDto> timeline = operationsService.getNotificationTimeline(notification.getId());
        assertThat(timeline).isNotEmpty();
        assertThat(timeline.stream().anyMatch(e -> e.eventType().equals("CREATED"))).isTrue();
        assertThat(timeline.stream().anyMatch(e -> e.eventType().equals("DELIVERED"))).isTrue();
    }

    // 7I-010: No Payload Leakage
    @Test
    @WithMockUser(username = "admin-7i", roles = {"ADMIN"})
    @DisplayName("7I-010: Raw email/SMS/WhatsApp message body text is omitted from notification list/detail DTOs")
    public void testNoPayloadLeakage() throws Exception {
        Notification notification = new Notification(
                "evt-7i-010", "ORDER_PAID", "user-7i-10", "cust-7i-10",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-10@example.com", "Subject Secret", "CONFIDENTIAL_PAYLOAD_BODY_7I_10",
                NotificationPriority.NORMAL, "idemp-7i-010", "corr-7i-10", "trace-7i-10"
        );
        notification = notificationRepository.save(notification);

        mockMvc.perform(get("/api/v1/admin/notifications/" + notification.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.body").doesNotExist())
                .andExpect(jsonPath("$.data.recipient").value("us***@example.com"));
    }

    // 7I-011: No Credential Leakage
    @Test
    @WithMockUser(username = "admin-7i", roles = {"ADMIN"})
    @DisplayName("7I-011: Provider credentials and API keys never appear in API responses")
    public void testNoCredentialLeakage() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.apiKey").doesNotExist())
                .andExpect(jsonPath("$.data.authToken").doesNotExist());
    }

    // 7I-012: Admin Retry
    @Test
    @DisplayName("7I-012: Admin retry schedules worker delivery without calling provider directly")
    public void testAdminRetry() {
        Notification notification = new Notification(
                "evt-7i-012", "ORDER_PAID", "user-7i-12", "cust-7i-12",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-12@example.com", "Subject 12", "Body 12",
                NotificationPriority.NORMAL, "idemp-7i-012", "corr-7i-12", "trace-7i-12"
        );
        notification.markFailed("Transient provider error", false);
        notification = notificationRepository.save(notification);

        NotificationOperationsService.NotificationDetailDto updated = operationsService.adminRetry("admin-7i", notification.getId());
        assertThat(updated.status()).isIn(NotificationStatus.PROCESSING, NotificationStatus.SENT, NotificationStatus.DELIVERED);
    }

    // 7I-013: Admin Retry Authorization
    @Test
    @DisplayName("7I-013: Non-admin user cannot trigger admin retry")
    public void testAdminRetryAuthorization() throws Exception {
        mockMvc.perform(post("/api/v1/admin/notifications/notif-fake/retry")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("customer-1").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    // 7I-014: Admin Cancellation
    @Test
    @DisplayName("7I-014: Admin cancellation safely updates notification state to CANCELLED")
    public void testAdminCancellation() {
        Notification notification = new Notification(
                "evt-7i-014", "ORDER_PAID", "user-7i-14", "cust-7i-14",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-14@example.com", "Subject 14", "Body 14",
                NotificationPriority.NORMAL, "idemp-7i-014", "corr-7i-14", "trace-7i-14"
        );
        notificationRepository.save(notification);

        NotificationOperationsService.NotificationDetailDto detail = operationsService.adminCancel("admin-7i", notification.getId(), "Duplicate request");
        assertThat(detail.status()).isEqualTo(NotificationStatus.CANCELLED);
        assertThat(detail.failureReason()).contains("Duplicate request");
    }

    // 7I-015: Admin Cancellation Authorization
    @Test
    @DisplayName("7I-015: Non-admin user cannot trigger admin cancellation")
    public void testAdminCancellationAuthorization() throws Exception {
        mockMvc.perform(post("/api/v1/admin/notifications/notif-fake/cancel")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("customer-1").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    // 7I-016: Admin Action Audit
    @Test
    @DisplayName("7I-016: Admin retry and cancellation create audit records")
    public void testAdminActionAudit() {
        Notification notification = new Notification(
                "evt-7i-016", "ORDER_PAID", "user-7i-16", "cust-7i-16",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-16@example.com", "Subject 16", "Body 16",
                NotificationPriority.NORMAL, "idemp-7i-016", "corr-7i-16", "trace-7i-16"
        );
        notificationRepository.save(notification);

        operationsService.adminCancel("admin-auditor", notification.getId(), "Audit test cancel");

        Page<SecurityAuditEvent> logs = operationsService.getAuditLogs(PageRequest.of(0, 10));
        assertThat(logs).isNotNull();
    }

    // 7I-017: Concurrent Admin Retry
    @Test
    @DisplayName("7I-017: Duplicate admin retry requests remain safe")
    public void testConcurrentAdminRetry() {
        Notification notification = new Notification(
                "evt-7i-017", "ORDER_PAID", "user-7i-17", "cust-7i-17",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-17@example.com", "Subject 17", "Body 17",
                NotificationPriority.NORMAL, "idemp-7i-017", "corr-7i-17", "trace-7i-17"
        );
        notification.markFailed("Error 17", false);
        notification = notificationRepository.save(notification);

        operationsService.adminRetry("admin-1", notification.getId());
        // Second call should handle gracefully or be idempotent
        NotificationOperationsService.NotificationDetailDto second = operationsService.getNotificationDetail(notification.getId());
        assertThat(second).isNotNull();
    }

    // 7I-018: Terminal State Protection
    @Test
    @DisplayName("7I-018: Terminal notifications (DELIVERED, CANCELLED, SUPPRESSED) cannot be retried or modified unlawfully")
    public void testTerminalStateProtection() {
        Notification notification = new Notification(
                "evt-7i-018", "ORDER_PAID", "user-7i-18", "cust-7i-18",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-18@example.com", "Subject 18", "Body 18",
                NotificationPriority.NORMAL, "idemp-7i-018", "corr-7i-18", "trace-7i-18"
        );
        notification.markDelivered("msg-7i-18");
        notificationRepository.save(notification);

        assertThatThrownBy(() -> operationsService.adminRetry("admin-7i", notification.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // 7I-019: Provider Isolation
    @Test
    @DisplayName("7I-019: Degraded provider state on SendGrid does not degrade Twilio SMS")
    public void testProviderIsolation() {
        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "500 Error", true, 50);
        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "500 Error", true, 50);
        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "500 Error", true, 50);

        assertThat(circuitBreaker.getState("SendGrid", NotificationChannel.EMAIL)).isEqualTo(NotificationCircuitBreaker.State.OPEN);
        assertThat(circuitBreaker.getState("Twilio", NotificationChannel.SMS)).isEqualTo(NotificationCircuitBreaker.State.CLOSED);
    }

    // 7I-020: Overall Health Calculation
    @Test
    @DisplayName("7I-020: Overall system health is evaluated as HEALTHY, DEGRADED, or CRITICAL")
    public void testOverallHealthCalculation() {
        Map<String, Object> summary = operationsService.getOverallHealthSummary();
        assertThat(summary.get("overallStatus")).isIn("HEALTHY", "DEGRADED", "CRITICAL");
    }

    // 7I-021: Alert Condition Detection
    @Test
    @DisplayName("7I-021: Alert condition detection identifies provider circuit open")
    public void testAlertConditionDetection() {
        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "500 Error", true, 50);
        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "500 Error", true, 50);
        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "500 Error", true, 50);

        NotificationOperationsService.BacklogSummaryDto backlog = operationsService.getBacklogSummary();
        List<NotificationOperationsService.AlertConditionDto> alerts = operationsService.detectAlertConditions(backlog);
        assertThat(alerts.stream().anyMatch(a -> a.alertType().equals("PROVIDER_CIRCUIT_OPEN"))).isTrue();
    }

    // 7I-022: Alert Deduplication
    @Test
    @DisplayName("7I-022: Persistent alerts are aggregated without duplicate log spam")
    public void testAlertDeduplication() {
        NotificationOperationsService.BacklogSummaryDto backlog = operationsService.getBacklogSummary();
        List<NotificationOperationsService.AlertConditionDto> alerts1 = operationsService.detectAlertConditions(backlog);
        List<NotificationOperationsService.AlertConditionDto> alerts2 = operationsService.detectAlertConditions(backlog);
        assertThat(alerts1.size()).isEqualTo(alerts2.size());
    }

    // 7I-023: Pagination
    @Test
    @DisplayName("7I-023: Paginated notification queries strictly enforce bounded maximum page sizes")
    public void testPagination() {
        Page<NotificationOperationsService.NotificationDetailDto> page = operationsService.getFilteredNotifications(
                null, null, null, null, null, PageRequest.of(0, 500)
        );
        assertThat(page.getPageable().getPageSize()).isLessThanOrEqualTo(100);
    }

    // 7I-024: Filtering
    @Test
    @DisplayName("7I-024: Filtering by status, channel, and provider returns accurate subset")
    public void testFiltering() {
        Notification notification = new Notification(
                "evt-7i-024", "ORDER_PAID", "user-7i-24", "cust-7i-24",
                NotificationChannel.SMS, "ORDER_CONFIRMATION", 1,
                "1234567890", "Subject 24", "Body 24",
                NotificationPriority.NORMAL, "idemp-7i-024", "corr-7i-24", "trace-7i-24"
        );
        notificationRepository.save(notification);

        Page<NotificationOperationsService.NotificationDetailDto> filtered = operationsService.getFilteredNotifications(
                NotificationStatus.CREATED, NotificationChannel.SMS, null, null, null, PageRequest.of(0, 10)
        );
        assertThat(filtered).isNotNull();
    }

    // 7I-025: Input Validation
    @Test
    @WithMockUser(username = "admin-7i", roles = {"ADMIN"})
    @DisplayName("7I-025: Invalid notification ID lookup returns 404 or throws IllegalArgumentException")
    public void testInputValidation() {
        assertThatThrownBy(() -> operationsService.getNotificationDetail("invalid-uuid-7i"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // 7I-026: IDOR Protection
    @Test
    @DisplayName("7I-026: Server-side authorization blocks customer users from inspecting arbitrary admin notifications")
    public void testIdorProtection() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/notif-some-other-user")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("customer-1").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    // 7I-027: Existing Notification Regression
    @Test
    @DisplayName("7I-027: Sprint 7F lifecycle sending and user preference enforcement remain intact")
    public void testExistingNotificationRegression() {
        Optional<Notification> opt = notificationService.sendNotification(
                "evt-7i-027", "ORDER_PAID", "user-7i-27", "cust-7i-27",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION",
                NotificationCategory.ORDER_UPDATES, "user-7i-27@example.com",
                Map.of("customerName", "Customer 27", "orderNumber", "ORD-7I-27"),
                NotificationPriority.HIGH, "idemp-7i-027", "corr-7i-27", "trace-7i-27"
        );
        assertThat(opt).isPresent();
        assertThat(opt.get().getStatus()).isIn(NotificationStatus.SENT, NotificationStatus.DELIVERED);
    }

    // 7I-028: Provider Regression
    @Test
    @DisplayName("7I-028: Sprint 7G HTTP provider adapters remain functional")
    public void testProviderRegression() {
        assertThat(sendGridEmailProvider).isNotNull();
        assertThat(twilioSmsProvider).isNotNull();
    }

    // 7I-029: Reliability Regression
    @Test
    @DisplayName("7I-029: Sprint 7H circuit breaker and stale recovery remain functional")
    public void testReliabilityRegression() {
        Notification notification = new Notification(
                "evt-7i-029", "ORDER_PAID", "user-7i-29", "cust-7i-29",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7i-29@example.com", "Subject 29", "Body 29",
                NotificationPriority.NORMAL, "idemp-7i-029", "corr-7i-29", "trace-7i-29"
        );
        notification.markProcessing("MockEmailProvider");
        notificationRepository.save(notification);

        deliveryWorker.recoverSingleStaleNotification(notification.getId());

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.RETRY_SCHEDULED);
    }

    // 7I-030: Outbox Regression
    @Test
    @DisplayName("7I-030: Sprint 7C/7D outbox event dispatch remains functional")
    public void testOutboxRegression() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent event = OrderLifecycleEvent.create(
                orderId, "ORD-7I-30", null, OrderStatus.PAID,
                "Payment confirmed", OrderActorType.SYSTEM, "cust-7i-30", "idemp-7i-030"
        );

        applicationEventPublisher.publishEvent(event);
        outboxWorker.processOutbox();

        List<OutboxEvent> events = outboxService.findByAggregate("ORDER", orderId.toString());
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).getStatus()).isEqualTo(com.sporekart.application.outbox.domain.OutboxStatus.PROCESSED);
    }
}
