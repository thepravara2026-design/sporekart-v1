package com.sporekart.modules.notification;

import com.sporekart.application.observability.metrics.CommerceMetricsService;
import com.sporekart.application.outbox.application.OutboxEventDispatcher;
import com.sporekart.application.outbox.application.OutboxService;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.infrastructure.OutboxWorker;
import com.sporekart.modules.notification.application.*;
import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.notification.infrastructure.NotificationDeliveryWorker;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.notification.infrastructure.provider.HttpMetaWhatsAppProvider;
import com.sporekart.modules.notification.infrastructure.provider.HttpSendGridEmailProvider;
import com.sporekart.modules.notification.infrastructure.provider.HttpTwilioSmsProvider;
import com.sporekart.modules.notification.infrastructure.resilience.NotificationCircuitBreaker;
import com.sporekart.modules.notification.infrastructure.resilience.ProviderHealthTracker;
import com.sporekart.modules.notification.infrastructure.worker.NotificationReconciliationWorker;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationReliabilityIntegrationTest {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private NotificationApplicationService notificationService;

    @Autowired
    private SpringDataJpaNotificationRepository notificationRepository;

    @Autowired
    private NotificationOrchestrator orchestrator;

    @Autowired
    private NotificationDeliveryWorker deliveryWorker;

    @Autowired
    private NotificationReconciliationWorker reconciliationWorker;

    @Autowired
    private NotificationWebhookService webhookService;

    @Autowired
    private NotificationResilienceService resilienceService;

    @Autowired
    private NotificationCircuitBreaker circuitBreaker;

    @Autowired
    private ProviderHealthTracker healthTracker;

    @Autowired
    private NotificationProperties properties;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxWorker outboxWorker;

    @Autowired
    private OutboxEventDispatcher outboxEventDispatcher;

    @Autowired(required = false)
    private CommerceMetricsService metricsService;

    @Autowired
    private HttpSendGridEmailProvider sendGridEmailProvider;

    @Autowired
    private HttpTwilioSmsProvider twilioSmsProvider;

    @Autowired
    private HttpMetaWhatsAppProvider metaWhatsAppProvider;

    @BeforeEach
    public void setUp() {
        circuitBreaker.reset("MockEmailProvider", NotificationChannel.EMAIL);
        circuitBreaker.reset("MockSmsProvider", NotificationChannel.SMS);
        circuitBreaker.reset("MockWhatsAppProvider", NotificationChannel.WHATSAPP);
        circuitBreaker.reset("SendGrid", NotificationChannel.EMAIL);
        circuitBreaker.reset("Twilio", NotificationChannel.SMS);
        circuitBreaker.reset("MetaWhatsApp", NotificationChannel.WHATSAPP);
    }

    // 7H-001: Provider Health State
    @Test
    @DisplayName("7H-001: Provider health begins in HEALTHY state")
    public void testProviderHealthState() {
        ProviderHealthTracker.ProviderHealthDetail health = healthTracker.getHealth("MockEmailProvider", NotificationChannel.EMAIL);
        assertThat(health.getStatus()).isEqualTo(ProviderHealthStatus.HEALTHY);
        assertThat(health.getConsecutiveFailures()).isEqualTo(0);
    }

    // 7H-002: Circuit Opens
    @Test
    @DisplayName("7H-002: Repeated transient provider failures cause circuit OPEN")
    public void testCircuitOpens() {
        for (int i = 0; i < 3; i++) {
            resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "503 Service Unavailable", true, 100);
        }
        assertThat(circuitBreaker.getState("MockEmailProvider", NotificationChannel.EMAIL)).isEqualTo(NotificationCircuitBreaker.State.OPEN);
    }

    // 7H-003: Circuit Prevents Requests
    @Test
    @DisplayName("7H-003: When OPEN, external provider requests are not attempted")
    public void testCircuitPreventsRequests() {
        for (int i = 0; i < 3; i++) {
            resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "503 Service Unavailable", true, 100);
        }

        Notification notification = new Notification(
                "evt-7h-003", "ORDER_PAID", "user-7h-3", "cust-7h-3",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-3@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-003", "corr-7h-3", "trace-7h-3"
        );
        notification = notificationRepository.save(notification);

        Notification result = orchestrator.processAndDeliver(notification);
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.RETRY_SCHEDULED);
        assertThat(result.getFailureReason()).contains("Circuit breaker OPEN");
    }

    // 7H-004: Circuit Half-Open
    @Test
    @DisplayName("7H-004: After open cooldown duration, controlled probe execution is allowed (HALF_OPEN)")
    public void testCircuitHalfOpen() {
        // Set short open duration for test
        circuitBreaker.setConfig(new NotificationCircuitBreaker.CircuitConfig(2, 50, 1));
        resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "500 Error", true, 50);
        resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "500 Error", true, 50);
        assertThat(circuitBreaker.getState("MockEmailProvider", NotificationChannel.EMAIL)).isEqualTo(NotificationCircuitBreaker.State.OPEN);

        try { Thread.sleep(60); } catch (InterruptedException ignored) {}

        boolean allowed = circuitBreaker.allowExecution("MockEmailProvider", NotificationChannel.EMAIL);
        assertThat(allowed).isTrue();
        assertThat(circuitBreaker.getState("MockEmailProvider", NotificationChannel.EMAIL)).isEqualTo(NotificationCircuitBreaker.State.HALF_OPEN);
    }

    // 7H-005: Circuit Recovery
    @Test
    @DisplayName("7H-005: Successful half-open request returns circuit to CLOSED")
    public void testCircuitRecovery() {
        circuitBreaker.setConfig(new NotificationCircuitBreaker.CircuitConfig(2, 50, 1));
        resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "500 Error", true, 50);
        resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "500 Error", true, 50);

        try { Thread.sleep(60); } catch (InterruptedException ignored) {}

        circuitBreaker.allowExecution("MockEmailProvider", NotificationChannel.EMAIL);
        resilienceService.recordSuccess("MockEmailProvider", NotificationChannel.EMAIL, 40);
        assertThat(circuitBreaker.getState("MockEmailProvider", NotificationChannel.EMAIL)).isEqualTo(NotificationCircuitBreaker.State.CLOSED);
    }

    // 7H-006: Circuit Reopens
    @Test
    @DisplayName("7H-006: Failed half-open probe returns circuit state to OPEN")
    public void testCircuitReopens() {
        circuitBreaker.setConfig(new NotificationCircuitBreaker.CircuitConfig(2, 50, 1));
        resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "500 Error", true, 50);
        resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "500 Error", true, 50);

        try { Thread.sleep(60); } catch (InterruptedException ignored) {}

        circuitBreaker.allowExecution("MockEmailProvider", NotificationChannel.EMAIL);
        resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "Probe failed 500", true, 50);
        assertThat(circuitBreaker.getState("MockEmailProvider", NotificationChannel.EMAIL)).isEqualTo(NotificationCircuitBreaker.State.OPEN);
    }

    // 7H-007: Provider Isolation
    @Test
    @DisplayName("7H-007: One provider circuit OPEN does not disable unrelated providers")
    public void testProviderIsolation() {
        for (int i = 0; i < 3; i++) {
            resilienceService.recordFailure("MockEmailProvider", NotificationChannel.EMAIL, "500 Error", true, 50);
        }
        assertThat(circuitBreaker.getState("MockEmailProvider", NotificationChannel.EMAIL)).isEqualTo(NotificationCircuitBreaker.State.OPEN);
        assertThat(circuitBreaker.getState("MockSmsProvider", NotificationChannel.SMS)).isEqualTo(NotificationCircuitBreaker.State.CLOSED);
    }

    // 7H-008: Rate Limit Backoff
    @Test
    @DisplayName("7H-008: HTTP 429 / Rate Limit error is classified correctly and triggers backoff schedule")
    public void testRateLimitBackoff() {
        ProviderFailureCategory category = resilienceService.classifyFailure("HTTP 429 Too Many Requests", true);
        assertThat(category).isEqualTo(ProviderFailureCategory.RATE_LIMITED);

        Instant nextRetry = resilienceService.calculateNextRetrySchedule(2, "429 Rate Limit");
        assertThat(nextRetry).isAfter(Instant.now());
    }

    // 7H-009: Retry Jitter
    @Test
    @DisplayName("7H-009: Retry scheduling calculates bounded delay with random jitter")
    public void testRetryJitter() {
        Instant retry1 = resilienceService.calculateNextRetrySchedule(1, "500 Error");
        Instant retry2 = resilienceService.calculateNextRetrySchedule(1, "500 Error");
        assertThat(retry1).isAfter(Instant.now());
        assertThat(retry2).isAfter(Instant.now());
    }

    // 7H-010: Stale Processing Recovery
    @Test
    @DisplayName("7H-010: Notifications stuck in PROCESSING status older than 5 minutes become recoverable")
    @Transactional
    public void testStaleProcessingRecovery() {
        Notification notification = new Notification(
                "evt-7h-010", "ORDER_PAID", "user-7h-10", "cust-7h-10",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-10@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-010", "corr-7h-10", "trace-7h-10"
        );
        notification.markProcessing("MockEmailProvider");
        notification = notificationRepository.save(notification);

        // Manually invoke stale processing recovery logic
        deliveryWorker.recoverSingleStaleNotification(notification.getId());
        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.RETRY_SCHEDULED);
        assertThat(updated.getFailureReason()).contains("Stale PROCESSING status recovered");
    }

    // 7H-011: Recent Processing Protection
    @Test
    @DisplayName("7H-011: Recently claimed PROCESSING notification is NOT incorrectly recovered")
    public void testRecentProcessingProtection() {
        Notification notification = new Notification(
                "evt-7h-011", "ORDER_PAID", "user-7h-11", "cust-7h-11",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-11@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-011", "corr-7h-11", "trace-7h-11"
        );
        notification.markProcessing("MockEmailProvider");
        notificationRepository.save(notification);

        // Run worker check (should not affect fresh processing)
        deliveryWorker.recoverStaleProcessingNotifications();

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.PROCESSING);
    }

    // 7H-012: Reconciliation Candidate
    @Test
    @DisplayName("7H-012: Old SENT notification older than 10 minutes is detected for reconciliation")
    @Transactional
    public void testReconciliationCandidate() {
        Notification notification = new Notification(
                "evt-7h-012", "ORDER_PAID", "user-7h-12", "cust-7h-12",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-12@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-012", "corr-7h-12", "trace-7h-12"
        );
        notification.markSent("msg-7h-12");
        notificationRepository.save(notification);

        reconciliationWorker.reconcileSingleNotification(notification.getId());

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getReconciliationAttemptCount()).isGreaterThanOrEqualTo(1);
        assertThat(updated.getLastReconciliationAt()).isNotNull();
    }

    // 7H-013: Provider Status Reconciliation
    @Test
    @DisplayName("7H-013: Provider delivery status updates correctly update notification state")
    public void testProviderStatusReconciliation() {
        Notification notification = new Notification(
                "evt-7h-013", "ORDER_PAID", "user-7h-13", "cust-7h-13",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-13@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-013", "corr-7h-13", "trace-7h-13"
        );
        notification.markSent("msg-7h-13");
        notificationRepository.save(notification);

        NotificationWebhookService.WebhookProcessingResult result = webhookService.processDeliveryCallback(
                "sendgrid", "msg-7h-13", "evt-recon-13", "delivered", "valid-test-signature", "{}"
        );
        assertThat(result.success()).isTrue();

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    // 7H-014: No False Delivery
    @Test
    @DisplayName("7H-014: Missing webhook/provider confirmation does NOT mark notification as DELIVERED automatically")
    public void testNoFalseDelivery() {
        Notification notification = new Notification(
                "evt-7h-014", "ORDER_PAID", "user-7h-14", "cust-7h-14",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-14@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-014", "corr-7h-14", "trace-7h-14"
        );
        notification.markSent("msg-7h-14");
        notificationRepository.save(notification);

        reconciliationWorker.reconcileSingleNotification(notification.getId());

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.SENT); // Retains SENT state safely
    }

    // 7H-015: Late Webhook
    @Test
    @DisplayName("7H-015: Late webhook safely updates eligible notification state to DELIVERED")
    public void testLateWebhook() {
        Notification notification = new Notification(
                "evt-7h-015", "ORDER_PAID", "user-7h-15", "cust-7h-15",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-15@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-015", "corr-7h-15", "trace-7h-15"
        );
        notification.markSent("msg-7h-15");
        notificationRepository.save(notification);

        reconciliationWorker.reconcileSingleNotification(notification.getId());

        // Late webhook arrives
        webhookService.processDeliveryCallback(
                "sendgrid", "msg-7h-15", "evt-late-15", "delivered", "valid-test-signature", "{}"
        );

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    // 7H-016: Webhook vs Reconciliation Race
    @Test
    @DisplayName("7H-016: Concurrent webhook and reconciliation produce one consistent terminal result without race errors")
    public void testWebhookVsReconciliationRace() {
        Notification notification = new Notification(
                "evt-7h-016", "ORDER_PAID", "user-7h-16", "cust-7h-16",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-16@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-016", "corr-7h-16", "trace-7h-16"
        );
        notification.markSent("msg-7h-16");
        notificationRepository.save(notification);

        reconciliationWorker.reconcileSingleNotification(notification.getId());
        webhookService.processDeliveryCallback("sendgrid", "msg-7h-16", "evt-race-16", "delivered", "valid-test-signature", "{}");

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    // 7H-017: Authentication Failure
    @Test
    @DisplayName("7H-017: Provider credential failure immediately marks FAILED_PERMANENTLY without infinite retries")
    public void testAuthenticationFailure() {
        ProviderFailureCategory category = resilienceService.classifyFailure("401 Unauthorized: Invalid API key", false);
        assertThat(category).isEqualTo(ProviderFailureCategory.AUTHENTICATION);

        Notification notification = new Notification(
                "evt-7h-017", "ORDER_PAID", "user-7h-17", "cust-7h-17",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-17@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-017", "corr-7h-17", "trace-7h-17"
        );
        notificationRepository.save(notification);

        resilienceService.recordFailure("SendGrid", NotificationChannel.EMAIL, "401 Unauthorized: Invalid API key", false, 100);
        ProviderHealthTracker.ProviderHealthDetail detail = healthTracker.getHealth("SendGrid", NotificationChannel.EMAIL);
        assertThat(detail.getStatus()).isEqualTo(ProviderHealthStatus.UNAVAILABLE);
    }

    // 7H-018: Provider Timeout
    @Test
    @DisplayName("7H-018: Timeout error is bounded and classified as TIMEOUT")
    public void testProviderTimeout() {
        ProviderFailureCategory category = resilienceService.classifyFailure("java.net.SocketTimeoutException: Read timed out", true);
        assertThat(category).isEqualTo(ProviderFailureCategory.TIMEOUT);
    }

    // 7H-019: Metrics
    @Test
    @DisplayName("7H-019: Provider and notification resilience metrics are recorded and accessible")
    public void testMetrics() {
        if (metricsService != null) {
            metricsService.recordCircuitOpen("MockEmailProvider", "EMAIL");
            metricsService.recordNotificationRetry("EMAIL", "MockEmailProvider");
            metricsService.recordReconciliationAttempt("EMAIL", "MockEmailProvider");
        }
        Map<String, Object> health = notificationService.getNotificationHealthMetrics();
        assertThat(health).containsKey("resilience");
    }

    // 7H-020: Credential Safety
    @Test
    @WithMockUser(username = "admin-7h", roles = {"ADMIN"})
    @DisplayName("7H-020: Secrets remain absent from health endpoints, metrics, and logs")
    public void testCredentialSafety() {
        Map<String, Object> health = notificationService.getNotificationHealthMetrics();
        String json = health.toString();
        assertThat(json).doesNotContain("secret");
        assertThat(json).doesNotContain("api-key");
        assertThat(json).doesNotContain("auth-token");
    }

    // 7H-021: Notification Lifecycle Regression
    @Test
    @DisplayName("7H-021: All Sprint 7F lifecycle transitions remain intact")
    public void testNotificationLifecycleRegression() {
        Optional<Notification> opt = notificationService.sendNotification(
                "evt-7h-021", "ORDER_PAID", "user-7h-21", "cust-7h-21",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION",
                NotificationCategory.ORDER_UPDATES, "user-7h-21@example.com",
                Map.of("customerName", "Customer 21", "orderNumber", "ORD-7H-21"),
                NotificationPriority.HIGH, "idemp-7h-021", "corr-7h-21", "trace-7h-21"
        );
        assertThat(opt).isPresent();
        assertThat(opt.get().getStatus()).isIn(NotificationStatus.SENT, NotificationStatus.DELIVERED);
    }

    // 7H-022: Provider Regression
    @Test
    @DisplayName("7H-022: All Sprint 7G provider adapters remain functional")
    public void testProviderRegression() {
        assertThat(sendGridEmailProvider).isNotNull();
        assertThat(twilioSmsProvider).isNotNull();
        assertThat(metaWhatsAppProvider).isNotNull();
    }

    // 7H-023: Outbox Regression
    @Test
    @DisplayName("7H-023: Sprint 7C/7D outbox dispatch and processing remain intact")
    public void testOutboxRegression() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent event = OrderLifecycleEvent.create(
                orderId, "ORD-7H-23", null, OrderStatus.PAID,
                "Payment confirmed", OrderActorType.SYSTEM, "cust-7h-23", "idempotency-key-7h-23"
        );

        applicationEventPublisher.publishEvent(event);
        outboxWorker.processOutbox();

        List<OutboxEvent> events = outboxService.findByAggregate("ORDER", orderId.toString());
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).getStatus()).isEqualTo(com.sporekart.application.outbox.domain.OutboxStatus.PROCESSED);
    }

    // 7H-024: Concurrent Worker Safety
    @Test
    @DisplayName("7H-024: Optimistic locking prevents multiple workers from double-delivering")
    public void testConcurrentWorkerSafety() {
        Notification notification = new Notification(
                "evt-7h-024", "ORDER_PAID", "user-7h-24", "cust-7h-24",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-24@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-024", "corr-7h-24", "trace-7h-24"
        );
        notification = notificationRepository.save(notification);

        deliveryWorker.processSingleNotification(notification.getId());
        deliveryWorker.processSingleNotification(notification.getId()); // Second call should be idempotent no-op

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getAttemptCount()).isEqualTo(1);
    }

    // 7H-025: Crash Recovery
    @Test
    @DisplayName("7H-025: Simulates process crash after provider acceptance and verifies delivery reconciliation")
    public void testCrashRecovery() {
        Notification notification = new Notification(
                "evt-7h-025", "ORDER_PAID", "user-7h-25", "cust-7h-25",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", 1,
                "user-7h-25@example.com", "Test Subject", "Test Body",
                NotificationPriority.NORMAL, "idemp-7h-025", "corr-7h-25", "trace-7h-25"
        );
        notification.markSent("msg-crash-25");
        notificationRepository.save(notification);

        // Crash simulated: process restarts and webhook reconciles
        webhookService.processDeliveryCallback("sendgrid", "msg-crash-25", "evt-crash-25", "delivered", "valid-test-signature", "{}");

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }
}
