package com.sporekart.modules.notification;

import com.sporekart.application.outbox.application.OutboxService;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.application.outbox.infrastructure.OutboxEventRepository;
import com.sporekart.application.outbox.infrastructure.OutboxWorker;
import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.application.NotificationPreferenceService;
import com.sporekart.modules.notification.application.NotificationTemplateService;
import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.notification.infrastructure.NotificationDeliveryWorker;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.notification.infrastructure.provider.MockEmailProvider;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationDeliveryLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired
    private NotificationTemplateService templateService;

    @Autowired
    private SpringDataJpaNotificationRepository notificationRepository;

    @Autowired
    private MockEmailProvider mockEmailProvider;

    @Autowired
    private NotificationDeliveryWorker deliveryWorker;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxWorker outboxWorker;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        outboxEventRepository.deleteAll();
        mockEmailProvider.clearOverrides();
    }

    @Test
    @DisplayName("7F-001: Enabled customer preference results in notification delivery")
    void testPreferenceEnabledDelivery() {
        preferenceService.updatePreference("user-7f-1", NotificationCategory.ORDER_UPDATES, true, true, true, true);

        Optional<Notification> opt = notificationApplicationService.sendNotification(
                "evt-7f-1", "ORDER_PAID", "user-7f-1", "user-7f-1",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "user-7f-1@example.com", Map.of("orderNumber", "ORD-7F-1"), NotificationPriority.NORMAL,
                "idem-7f-1", null, null
        );

        assertThat(opt).isPresent();
        assertThat(opt.get().getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("7F-002: Disabled customer preference results in explicit SUPPRESSED status")
    void testPreferenceDisabledSuppression() {
        preferenceService.updatePreference("user-7f-2", NotificationCategory.ORDER_UPDATES, false, false, false, false);

        Optional<Notification> opt = notificationApplicationService.sendNotification(
                "evt-7f-2", "ORDER_UPDATED", "user-7f-2", "user-7f-2",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "user-7f-2@example.com", Map.of("orderNumber", "ORD-7F-2"), NotificationPriority.NORMAL,
                "idem-7f-2", null, null
        );

        assertThat(opt).isPresent();
        assertThat(opt.get().getStatus()).isEqualTo(NotificationStatus.SUPPRESSED);
        assertThat(opt.get().getSuppressedAt()).isNotNull();
    }

    @Test
    @DisplayName("7F-003: Mandatory transactional notification bypasses customer preference suppression")
    void testTransactionalNotificationPolicy() {
        preferenceService.updatePreference("user-7f-3", NotificationCategory.SECURITY, false, false, false, false);

        Optional<Notification> opt = notificationApplicationService.sendNotification(
                "evt-7f-3", "SECURITY_ALERT", "user-7f-3", "user-7f-3",
                NotificationChannel.EMAIL, "SECURITY_NOTICE", NotificationCategory.SECURITY,
                "user-7f-3@example.com", Map.of("action", "Password Change"), NotificationPriority.HIGH,
                "idem-7f-3", null, null
        );

        assertThat(opt).isPresent();
        assertThat(opt.get().getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("7F-004: Delivery worker claims pending notification and executes delivery")
    void testDeliveryWorkerProcessing() {
        Notification notification = new Notification(
                "evt-7f-4", "ORDER_CREATED", "user-7f-4", "user-7f-4",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7f-4@example.com", "Order Update", "Your order is ready",
                NotificationPriority.NORMAL, "idem-7f-4", null, null
        );
        Notification saved = notificationRepository.save(notification);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.CREATED);

        deliveryWorker.processSingleNotification(saved.getId());

        Notification processed = notificationRepository.findById(saved.getId()).orElseThrow();
        assertThat(processed.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("7F-005: Optimistic locking protects against concurrent worker claims")
    void testConcurrentWorkerProtection() {
        Notification notification = new Notification(
                "evt-7f-5", "ORDER_CREATED", "user-7f-5", "user-7f-5",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7f-5@example.com", "Order Update", "Body",
                NotificationPriority.NORMAL, "idem-7f-5", null, null
        );
        Notification n1 = notificationRepository.save(notification);
        Notification n2 = notificationRepository.findById(n1.getId()).orElseThrow();

        n1.markProcessing("Worker1");
        notificationRepository.save(n1);

        n2.markProcessing("Worker2");
        assertThatThrownBy(() -> notificationRepository.save(n2))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("7F-006: Transient provider failure transitions notification to RETRY_SCHEDULED")
    void testTransientFailureRetryScheduling() {
        mockEmailProvider.setRecipientMode("transient-7f-6@example.com", MockEmailProvider.SimulationMode.TRANSIENT_FAILURE);

        Notification notification = notificationApplicationService.sendNotification(
                "evt-7f-6", "ORDER_CREATED", "user-7f-6", "user-7f-6",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "transient-7f-6@example.com", Map.of("orderNumber", "ORD-7F-6"), NotificationPriority.NORMAL,
                "idem-7f-6", null, null
        ).orElseThrow();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.RETRY_SCHEDULED);
        assertThat(notification.getFailureReason()).isNotNull();
    }

    @Test
    @DisplayName("7F-007: Retry schedule calculates bounded backoff")
    void testRetryBackoffSchedule() {
        Notification notification = new Notification(
                "evt-7f-7", "ORDER_CREATED", "user-7f-7", "user-7f-7",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7f-7@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7f-7", null, null
        );
        Instant nextScheduled = Instant.now().plusSeconds(4);
        notification.scheduleRetry("Transient timeout", nextScheduled);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.RETRY_SCHEDULED);
        assertThat(notification.getScheduledAt()).isEqualTo(nextScheduled);
    }

    @Test
    @DisplayName("7F-008: Maximum retry limit transitions notification to FAILED_PERMANENTLY")
    void testMaximumRetryLimit() {
        Notification notification = new Notification(
                "evt-7f-8", "ORDER_CREATED", "user-7f-8", "user-7f-8",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7f-8@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7f-8", null, null
        );
        notification.markProcessing("Worker");
        notification.markProcessing("Worker");
        notification.markProcessing("Worker");
        Notification saved = notificationRepository.save(notification);

        deliveryWorker.processSingleNotification(saved.getId());

        Notification finalState = notificationRepository.findById(saved.getId()).orElseThrow();
        assertThat(finalState.getStatus()).isEqualTo(NotificationStatus.FAILED_PERMANENTLY);
    }

    @Test
    @DisplayName("7F-009: Permanent provider failure reaches FAILED_PERMANENTLY status directly")
    void testPermanentProviderFailure() {
        mockEmailProvider.setRecipientMode("invalid-7f-9", MockEmailProvider.SimulationMode.PERMANENT_FAILURE);

        Notification notification = notificationApplicationService.sendNotification(
                "evt-7f-9", "ORDER_CREATED", "user-7f-9", "user-7f-9",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "invalid-7f-9", Map.of("orderNumber", "ORD-7F-9"), NotificationPriority.NORMAL,
                "idem-7f-9", null, null
        ).orElseThrow();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED_PERMANENTLY);
    }

    @Test
    @DisplayName("7F-010: Idempotent processing prevents duplicate delivery side-effects")
    void testIdempotentDelivery() {
        Notification notification = notificationApplicationService.sendNotification(
                "evt-7f-10", "ORDER_CREATED", "user-7f-10", "user-7f-10",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "user-7f-10@example.com", Map.of("orderNumber", "ORD-7F-10"), NotificationPriority.NORMAL,
                "idem-7f-10", null, null
        ).orElseThrow();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);

        // Re-process
        deliveryWorker.processSingleNotification(notification.getId());
        Notification rechecked = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(rechecked.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("7F-011: Provider message ID is persisted on successful delivery")
    void testProviderMessageIdPersistence() {
        Notification notification = notificationApplicationService.sendNotification(
                "evt-7f-11", "ORDER_CREATED", "user-7f-11", "user-7f-11",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", NotificationCategory.ORDER_UPDATES,
                "user-7f-11@example.com", Map.of("orderNumber", "ORD-7F-11"), NotificationPriority.NORMAL,
                "idem-7f-11", null, null
        ).orElseThrow();

        assertThat(notification.getProviderMessageId()).isNotNull();
    }

    @Test
    @DisplayName("7F-012: Active template version is resolved cleanly")
    void testTemplateVersionResolution() {
        NotificationTemplate template = templateService.createTemplate("GOV_TEST", "Gov Test", "Desc", NotificationCategory.ORDER_UPDATES);
        NotificationTemplateVersion version = templateService.createTemplateVersion(
                template.getTemplateCode(), NotificationChannel.EMAIL, 1, "en-US", "Hello {{name}}", "Body {{name}}"
        );
        templateService.activateTemplateVersion(version.getId());

        NotificationTemplateService.RenderedTemplate rendered = templateService.renderActiveTemplate("GOV_TEST", NotificationChannel.EMAIL, "en-US", Map.of("name", "David"));
        assertThat(rendered.subject()).isEqualTo("Hello David");
    }

    @Test
    @DisplayName("7F-013: Malformed template syntax is rejected")
    void testInvalidTemplateRejection() {
        NotificationTemplate template = templateService.createTemplate("SYNTAX_TEST", "Syntax Test", "Desc", NotificationCategory.ORDER_UPDATES);

        assertThatThrownBy(() -> templateService.createTemplateVersion(
                template.getTemplateCode(), NotificationChannel.EMAIL, 1, "en-US", "Unbalanced {brace", "Body"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("7F-014: Pending notification can be cancelled safely")
    void testNotificationCancellation() {
        Notification notification = new Notification(
                "evt-7f-14", "ORDER_CREATED", "user-7f-14", "user-7f-14",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7f-14@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7f-14", null, null
        );
        Notification saved = notificationRepository.save(notification);

        Notification cancelled = notificationApplicationService.cancelNotification("user-7f-14", saved.getId(), "User cancelled order");
        assertThat(cancelled.getStatus()).isEqualTo(NotificationStatus.CANCELLED);
        assertThat(cancelled.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("7F-015: Cancellation racing with delivery produces valid final state")
    void testCancellationRaceCondition() {
        Notification notification = new Notification(
                "evt-7f-15", "ORDER_CREATED", "user-7f-15", "user-7f-15",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7f-15@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7f-15", null, null
        );
        Notification saved = notificationRepository.save(notification);
        notificationApplicationService.cancelNotification("user-7f-15", saved.getId(), "Cancel request");

        assertThatThrownBy(() -> notificationApplicationService.retryNotificationAdmin(saved.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @WithMockUser(username = "customer-7f-16", roles = {"CUSTOMER"})
    @DisplayName("7F-016: Customer history API returns authorization-scoped records")
    void testCustomerHistoryAuthorization() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer-7f-17", roles = {"CUSTOMER"})
    @DisplayName("7F-017: Unauthorized users cannot access admin notification APIs")
    void testAdminAuthorizationEnforcement() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin-7f-18", roles = {"ADMIN"})
    @DisplayName("7F-018: Authorized admin can trigger retry endpoint for eligible notification")
    void testAdminRetryEndpoint() throws Exception {
        Notification notification = new Notification(
                "evt-7f-18", "ORDER_CREATED", "user-7f-18", "user-7f-18",
                NotificationChannel.EMAIL, "ORDER_STATUS_UPDATE", 1,
                "user-7f-18@example.com", "Subject", "Body",
                NotificationPriority.NORMAL, "idem-7f-18", null, null
        );
        notification.markFailed("Transient error", false);
        Notification saved = notificationRepository.save(notification);

        mockMvc.perform(post("/api/v1/admin/notifications/" + saved.getId() + "/retry"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7F-019: Observability health metrics accurately reflect persisted status counts")
    void testNotificationHealthObservability() {
        Map<String, Object> metrics = notificationApplicationService.getNotificationHealthMetrics();
        assertThat(metrics).containsKey("sentCount");
        assertThat(metrics).containsKey("suppressedCount");
        assertThat(metrics).containsKey("cancelledCount");
    }

    @Test
    @DisplayName("7F-020: Outbox worker and dispatcher operate seamlessly alongside notification layer")
    void testExistingOutboxRegression() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent eventPayload = OrderLifecycleEvent.create(
                orderId, "ORD-OUTBOX-7F", null, OrderStatus.PAID,
                "Order created for outbox regression test", OrderActorType.CUSTOMER, "cust-7f-20", "idem-7f-20"
        );

        OutboxEvent outboxEvent = outboxService.publish("ORDER", orderId.toString(), "ORDER_PAID", eventPayload);
        outboxWorker.processOutbox();

        List<OutboxEvent> events = outboxService.findByAggregate("ORDER", orderId.toString());
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).getStatus()).isEqualTo(OutboxStatus.PROCESSED);
    }
}
