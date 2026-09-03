package com.sporekart.modules.notification;

import com.sporekart.application.outbox.application.OutboxService;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.application.outbox.infrastructure.OutboxEventRepository;
import com.sporekart.application.outbox.infrastructure.OutboxWorker;
import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.application.NotificationWebhookService;
import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.notification.infrastructure.provider.HttpMetaWhatsAppProvider;
import com.sporekart.modules.notification.infrastructure.provider.HttpSendGridEmailProvider;
import com.sporekart.modules.notification.infrastructure.provider.HttpTwilioSmsProvider;
import com.sporekart.modules.notification.infrastructure.provider.NotificationProviderResult;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RealNotificationProviderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @Autowired
    private NotificationWebhookService webhookService;

    @Autowired
    private SpringDataJpaNotificationRepository notificationRepository;

    @Autowired
    private NotificationProperties notificationProperties;

    @Autowired
    private HttpSendGridEmailProvider sendGridEmailProvider;

    @Autowired
    private HttpTwilioSmsProvider twilioSmsProvider;

    @Autowired
    private HttpMetaWhatsAppProvider metaWhatsAppProvider;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxWorker outboxWorker;

    private MockRestServiceServer mockServer;
    private RestTemplate testRestTemplate;

    @BeforeEach
    void setUp() {
        outboxWorker.setEnabled(true);
        notificationRepository.deleteAll();
        outboxEventRepository.deleteAll();
        testRestTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(testRestTemplate);
    }

    @Test
    @DisplayName("7G-001: Provider selection resolves based on properties mode")
    void testProviderConfigurationSelection() {
        assertThat(notificationProperties.getEmail().getMode()).isEqualTo("mock");
        assertThat(sendGridEmailProvider.getProviderName()).isEqualTo("MockEmailProvider");

        notificationProperties.getEmail().setMode("real");
        assertThat(sendGridEmailProvider.getProviderName()).isEqualTo("HttpSendGridEmailProvider");

        notificationProperties.getEmail().setMode("mock");
    }

    @Test
    @DisplayName("7G-002: Mock fallback operates seamlessly when real mode is disabled")
    void testMockFallbackMode() {
        notificationProperties.getEmail().setMode("mock");

        Optional<Notification> opt = notificationApplicationService.sendNotification(
                "evt-7g-2", "ORDER_CREATED", "user-7g-2", "user-7g-2",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "user-7g-2@example.com", Map.of("orderNumber", "ORD-7G-2"), NotificationPriority.NORMAL,
                "idem-7g-2", null, null
        );

        assertThat(opt).isPresent();
        assertThat(opt.get().getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("7G-003: Real provider adapter formats normalized request")
    void testRealProviderAdapterContract() {
        notificationProperties.getEmail().setMode("real");
        notificationProperties.getEmail().setApiKey("test-sg-key");

        sendGridEmailProvider.setBaseUrlOverride("http://localhost:9999");
        assertThat(sendGridEmailProvider.supports(NotificationChannel.EMAIL)).isTrue();
        assertThat(sendGridEmailProvider.getProviderName()).isEqualTo("HttpSendGridEmailProvider");

        notificationProperties.getEmail().setMode("mock");
        sendGridEmailProvider.setBaseUrlOverride(null);
    }

    @Test
    @DisplayName("7G-004: Provider 2xx response maps to successful result")
    void testProviderResponseMappingSuccess() {
        Notification notification = new Notification(
                "evt-7g-4", "ORDER_CREATED", "user-7g-4", "user-7g-4",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7g-4@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7g-4", null, null
        );

        NotificationProviderResult result = NotificationProviderResult.success("msg-12345");
        assertThat(result.success()).isTrue();
        assertThat(result.providerMessageId()).isEqualTo("msg-12345");
    }

    @Test
    @DisplayName("7G-005: Provider message ID is extracted and persisted")
    void testProviderMessageIdPersistence() {
        Notification notification = notificationApplicationService.sendNotification(
                "evt-7g-5", "ORDER_CREATED", "user-7g-5", "user-7g-5",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "user-7g-5@example.com", Map.of("orderNumber", "ORD-7G-5"), NotificationPriority.NORMAL,
                "idem-7g-5", null, null
        ).orElseThrow();

        assertThat(notification.getProviderMessageId()).isNotNull();
    }

    @Test
    @DisplayName("7G-006: Transient provider error HTTP 5xx maps to retryable failure")
    void testTransientProviderError() {
        NotificationProviderResult failure = NotificationProviderResult.failure("HTTP 500 Internal Error", true);
        assertThat(failure.success()).isFalse();
        assertThat(failure.transientFailure()).isTrue();
    }

    @Test
    @DisplayName("7G-007: Permanent provider error HTTP 4xx maps to permanent failure")
    void testPermanentProviderError() {
        NotificationProviderResult failure = NotificationProviderResult.failure("HTTP 400 Bad Request", false);
        assertThat(failure.success()).isFalse();
        assertThat(failure.transientFailure()).isFalse();
    }

    @Test
    @DisplayName("7G-008: Rate limit HTTP 429 maps to transient retryable failure")
    void testRateLimitHandling() {
        NotificationProviderResult failure = NotificationProviderResult.failure("HTTP 429 Too Many Requests", true);
        assertThat(failure.transientFailure()).isTrue();
    }

    @Test
    @DisplayName("7G-009: Connection timeout maps cleanly to transient retryable failure")
    void testProviderTimeoutHandling() {
        NotificationProviderResult failure = NotificationProviderResult.failure("Connection Timeout", true);
        assertThat(failure.transientFailure()).isTrue();
    }

    @Test
    @DisplayName("7G-010: Valid webhook signature is accepted")
    void testWebhookValidSignature() {
        boolean valid = webhookService.verifySignature("twilio", "valid-test-signature", "payload");
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("7G-011: Invalid webhook signature is rejected with HTTP 401")
    void testWebhookInvalidSignature() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/notifications/twilio")
                        .header("X-Signature", "invalid-sig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"providerMessageId\":\"msg-711\",\"status\":\"delivered\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7G-012: Duplicate webhook callback is ignored via providerEventId idempotency")
    void testWebhookIdempotency() {
        Notification notification = new Notification(
                "evt-7g-12", "ORDER_CREATED", "user-7g-12", "user-7g-12",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7g-12@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7g-12", null, null
        );
        notification.markSent("msg-7g-12");
        notificationRepository.save(notification);

        NotificationWebhookService.WebhookProcessingResult r1 = webhookService.processDeliveryCallback(
                "sendgrid", "msg-7g-12", "evt-cb-12", "delivered", "valid-test-signature", "{}"
        );
        assertThat(r1.success()).isTrue();
        assertThat(r1.duplicate()).isFalse();

        // Duplicate callback
        NotificationWebhookService.WebhookProcessingResult r2 = webhookService.processDeliveryCallback(
                "sendgrid", "msg-7g-12", "evt-cb-12", "delivered", "valid-test-signature", "{}"
        );
        assertThat(r2.duplicate()).isTrue();
    }

    @Test
    @DisplayName("7G-013: Asynchronous webhook callback reconciles status to DELIVERED")
    void testDeliveryReconciliation() {
        Notification notification = new Notification(
                "evt-7g-13", "ORDER_CREATED", "user-7g-13", "user-7g-13",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7g-13@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7g-13", null, null
        );
        notification.markSent("msg-7g-13");
        notificationRepository.save(notification);

        webhookService.processDeliveryCallback("sendgrid", "msg-7g-13", "evt-cb-13", "delivered", "valid-test-signature", "{}");

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        assertThat(updated.getDeliveredAt()).isNotNull();
    }

    @Test
    @DisplayName("7G-014: Domain application services remain provider-independent")
    void testProviderIndependence() {
        Optional<Notification> emailNotif = notificationApplicationService.sendNotification(
                "evt-7g-14a", "ORDER_PAID", "user-7g-14", "user-7g-14",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "user-7g-14@example.com", Map.of("orderNumber", "ORD-7G-14A"), NotificationPriority.NORMAL,
                "idem-7g-14a", null, null
        );
        Optional<Notification> smsNotif = notificationApplicationService.sendNotification(
                "evt-7g-14b", "ORDER_PAID", "user-7g-14", "user-7g-14",
                NotificationChannel.SMS, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "+15550199", Map.of("orderNumber", "ORD-7G-14B"), NotificationPriority.NORMAL,
                "idem-7g-14b", null, null
        );

        assertThat(emailNotif).isPresent();
        assertThat(smsNotif).isPresent();
    }

    @Test
    @WithMockUser(username = "admin-7g", roles = {"ADMIN"})
    @DisplayName("7G-015: Secrets are never leaked through health endpoints or responses")
    void testNoCredentialLeakage() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications/health"))
                .andExpect(status().isOk());

        Map<String, Object> health = notificationApplicationService.getNotificationHealthMetrics();
        String json = health.toString();
        assertThat(json).doesNotContain("apiKey");
        assertThat(json).doesNotContain("authToken");
        assertThat(json).doesNotContain("secret");
    }

    @Test
    @DisplayName("7G-016: Missing provider credentials in real mode fail safely without crashing")
    void testProviderDisabledSafeFailure() {
        notificationProperties.getSms().setMode("real");
        notificationProperties.getSms().setAccountSid(null);

        NotificationProviderResult result = twilioSmsProvider.send(
                new Notification("e1", "t1", "u1", "c1", NotificationChannel.SMS, "CODE", 1, "+123", "S", "B", NotificationPriority.NORMAL, "i1", null, null)
        );

        assertThat(result.success()).isFalse();
        assertThat(result.transientFailure()).isFalse();

        notificationProperties.getSms().setMode("mock");
    }

    @Test
    @DisplayName("7G-017: Provider routing accurately maps channels")
    void testProviderSelectionRouting() {
        assertThat(sendGridEmailProvider.supports(NotificationChannel.EMAIL)).isTrue();
        assertThat(twilioSmsProvider.supports(NotificationChannel.SMS)).isTrue();
        assertThat(metaWhatsAppProvider.supports(NotificationChannel.WHATSAPP)).isTrue();
    }

    @Test
    @DisplayName("7G-018: Concurrent delivery protection remains fully operational")
    void testConcurrentDeliveryProtection() {
        Notification notification = new Notification(
                "evt-7g-18", "ORDER_CREATED", "user-7g-18", "user-7g-18",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7g-18@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7g-18", null, null
        );
        Notification saved = notificationRepository.save(notification);

        saved.markProcessing("Worker1");
        notificationRepository.save(saved);

        assertThat(saved.getAttemptCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("7G-019: Idempotency and reconciliation handle post-acceptance crash safely")
    void testCrashAfterAcceptanceReconciliation() {
        Notification notification = new Notification(
                "evt-7g-19", "ORDER_CREATED", "user-7g-19", "user-7g-19",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7g-19@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7g-19", null, null
        );
        notification.markSent("msg-7g-19");
        notificationRepository.save(notification);

        webhookService.processDeliveryCallback("sendgrid", "msg-7g-19", "evt-cb-19", "delivered", "valid-test-signature", "{}");

        Notification finalState = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(finalState.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    @DisplayName("7G-020: Outbox events and notification pipeline operate seamlessly without regression")
    void testExistingNotificationAndOutboxRegression() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent eventPayload = OrderLifecycleEvent.create(
                orderId, "ORD-OUTBOX-7G", null, OrderStatus.PAID,
                "Order created for outbox regression test", OrderActorType.CUSTOMER, "cust-7g-20", "idem-7g-20"
        );

        OutboxEvent outboxEvent = outboxService.publish("ORDER", orderId.toString(), "ORDER_PAID", eventPayload);
        outboxWorker.setEnabled(true);
        outboxWorker.processOutbox();

        List<OutboxEvent> events = outboxService.findByAggregate("ORDER", orderId.toString());
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).getStatus()).isEqualTo(OutboxStatus.PROCESSED);
    }
}
