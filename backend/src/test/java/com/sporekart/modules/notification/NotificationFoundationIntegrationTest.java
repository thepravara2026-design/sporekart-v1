package com.sporekart.modules.notification;

import com.sporekart.application.outbox.application.OutboxEventDispatcher;
import com.sporekart.application.outbox.application.OutboxService;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.application.outbox.infrastructure.OutboxEventRepository;
import com.sporekart.application.outbox.infrastructure.OutboxWorker;
import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.application.NotificationEventProcessor;
import com.sporekart.modules.notification.application.NotificationPolicy;
import com.sporekart.modules.notification.application.TemplatePlaceholderSubstitutor;
import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationStatus;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationRepository;
import com.sporekart.modules.notification.infrastructure.provider.MockEmailProvider;
import com.sporekart.modules.notification.infrastructure.provider.MockPushProvider;
import com.sporekart.modules.notification.infrastructure.provider.MockSmsProvider;
import com.sporekart.modules.notification.infrastructure.provider.NotificationProvider;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationFoundationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationEventProcessor notificationEventProcessor;

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @Autowired
    private NotificationPolicy notificationPolicy;

    @Autowired
    private TemplatePlaceholderSubstitutor templateSubstitutor;

    @Autowired
    private SpringDataJpaNotificationRepository notificationRepository;

    @Autowired
    private List<NotificationProvider> providers;

    @Autowired
    private MockEmailProvider mockEmailProvider;

    @Autowired
    private MockSmsProvider mockSmsProvider;

    @Autowired
    private MockPushProvider mockPushProvider;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxWorker outboxWorker;

    @Autowired
    private OutboxEventDispatcher outboxEventDispatcher;

    @BeforeEach
    void setUp() {
        outboxWorker.setEnabled(true);
        notificationRepository.deleteAll();
        outboxEventRepository.deleteAll();
        mockEmailProvider.clearOverrides();
        mockSmsProvider.clearOverrides();
        mockPushProvider.clearOverrides();
    }

    @Test
    @DisplayName("7E-001: Supported domain event produces expected notification records")
    void testEventToNotification() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent event = OrderLifecycleEvent.create(
                orderId, "ORD-7E-001", null, OrderStatus.PAID,
                "Payment received", OrderActorType.CUSTOMER, "cust-7e-1", "idem-7e-1"
        );

        notificationEventProcessor.handleOrderLifecycleEvent(event);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).isNotEmpty();
        assertThat(notifications).extracting(Notification::getEventId).contains(event.eventId().toString());
    }

    @Test
    @DisplayName("7E-002: Policy selects appropriate channels for domain event")
    void testChannelSelection() {
        NotificationPolicy.PolicyDecision decision = notificationPolicy.evaluatePolicy("ORDER_CREATED");

        assertThat(decision.category()).isEqualTo(NotificationCategory.ORDER_UPDATES);
        assertThat(decision.channels()).contains(NotificationChannel.EMAIL, NotificationChannel.IN_APP, NotificationChannel.SMS);
        assertThat(decision.templateCode()).isEqualTo("ORDER_STATUS_UPDATE");
    }

    @Test
    @DisplayName("7E-003: Selected channels resolve to registered notification providers")
    void testProviderResolution() {
        boolean hasEmailProvider = providers.stream().anyMatch(p -> p.supports(NotificationChannel.EMAIL));
        boolean hasSmsProvider = providers.stream().anyMatch(p -> p.supports(NotificationChannel.SMS));
        boolean hasPushProvider = providers.stream().anyMatch(p -> p.supports(NotificationChannel.PUSH));

        assertThat(hasEmailProvider).isTrue();
        assertThat(hasSmsProvider).isTrue();
        assertThat(hasPushProvider).isTrue();
    }

    @Test
    @DisplayName("7E-004: Template placeholder substitution renders variables cleanly")
    void testTemplateRendering() {
        String template = "Hello {{customerName}}, your order {{orderNumber}} has status {{status}}.";
        Map<String, String> variables = Map.of(
                "customerName", "Alice",
                "orderNumber", "ORD-100",
                "status", "SHIPPED"
        );

        String rendered = templateSubstitutor.substitute(template, variables);
        assertThat(rendered).isEqualTo("Hello Alice, your order ORD-100 has status SHIPPED.");
    }

    @Test
    @DisplayName("7E-005: Missing template variables are handled safely without exceptions")
    void testMissingTemplateVariableHandling() {
        String template = "Hello {{customerName}}, your tracking is {{trackingNumber}}.";
        Map<String, String> variables = Map.of("customerName", "Bob");

        String rendered = templateSubstitutor.substitute(template, variables);
        assertThat(rendered).contains("Bob");
    }

    @Test
    @DisplayName("7E-006: Successful mock provider delivery transitions notification status")
    void testSuccessfulDelivery() {
        mockEmailProvider.setDefaultMode(MockEmailProvider.SimulationMode.SUCCESS);

        Notification notification = notificationApplicationService.sendNotification(
                "evt-7e-6",
                "ORDER_CREATED",
                "user-7e-6",
                "user-7e-6@example.com",
                NotificationChannel.EMAIL,
                "ORDER_STATUS_UPDATE",
                Map.of("orderNumber", "ORD-7E-6")
        );

        assertThat(notification).isNotNull();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getProviderMessageId()).isNotNull();
    }

    @Test
    @DisplayName("7E-007: Transient provider failure is marked retryable with attempt tracking")
    void testTransientProviderFailure() {
        mockEmailProvider.setRecipientMode("transient@example.com", MockEmailProvider.SimulationMode.TRANSIENT_FAILURE);

        Notification notification = notificationApplicationService.sendNotification(
                "evt-7e-7",
                "ORDER_CREATED",
                "user-7e-7",
                "transient@example.com",
                NotificationChannel.EMAIL,
                "ORDER_STATUS_UPDATE",
                Map.of("orderNumber", "ORD-7E-7")
        );

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.RETRY_SCHEDULED);
        assertThat(notification.getAttemptCount()).isGreaterThan(0);
        assertThat(notification.getFailureReason()).isNotNull();
    }

    @Test
    @DisplayName("7E-008: Permanent provider failure updates status to FAILED")
    void testPermanentProviderFailure() {
        mockEmailProvider.setRecipientMode("invalid-email", MockEmailProvider.SimulationMode.PERMANENT_FAILURE);

        Notification notification = notificationApplicationService.sendNotification(
                "evt-7e-8",
                "ORDER_CREATED",
                "user-7e-8",
                "invalid-email",
                NotificationChannel.EMAIL,
                "ORDER_STATUS_UPDATE",
                Map.of("orderNumber", "ORD-7E-8")
        );

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED_PERMANENTLY);
        assertThat(notification.getFailureReason()).isNotNull();
    }

    @Test
    @DisplayName("7E-009: Idempotent processing prevents duplicate notification records")
    void testNotificationIdempotency() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent event = OrderLifecycleEvent.create(
                orderId, "ORD-IDEM-7E", null, OrderStatus.PAID,
                "Payment cleared", OrderActorType.CUSTOMER, "cust-7e-9", "idem-7e-9"
        );

        // Process 1
        notificationEventProcessor.handleOrderLifecycleEvent(event);
        long countAfterFirst = notificationRepository.count();

        // Process 2 (Duplicate)
        notificationEventProcessor.handleOrderLifecycleEvent(event);
        long countAfterSecond = notificationRepository.count();

        assertThat(countAfterSecond).isEqualTo(countAfterFirst);
    }

    @Test
    @DisplayName("7E-010: Provider abstraction allows adding providers without business logic changes")
    void testProviderIndependence() {
        Notification pushNotification = notificationApplicationService.sendNotification(
                "evt-7e-10",
                "ORDER_SHIPPED",
                "user-push-1",
                "fcm-device-token-123",
                NotificationChannel.PUSH,
                "SHIPMENT_STATUS_UPDATE",
                Map.of("trackingNumber", "TRK-999")
        );

        assertThat(pushNotification.getChannel()).isEqualTo(NotificationChannel.PUSH);
        assertThat(pushNotification.getProviderName()).isEqualTo("MockPushProvider");
        assertThat(pushNotification.getStatus()).isIn(NotificationStatus.SENT, NotificationStatus.DELIVERED);
    }

    @Test
    @DisplayName("7E-011: Notification REST endpoints enforce security authentication")
    void testNotificationSecurityEnforcement() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7E-012: Outbox worker and dispatcher operate seamlessly with notification layer")
    void testExistingOutboxRegression() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent eventPayload = OrderLifecycleEvent.create(
                orderId, "ORD-OUTBOX-7E", null, OrderStatus.PAID,
                "Order created for outbox regression test", OrderActorType.CUSTOMER, "cust-7e-12", "idem-7e-12"
        );

        outboxService.publish("ORDER", orderId.toString(), "ORDER_PAID", eventPayload);

        outboxWorker.processOutbox();

        List<OutboxEvent> events = outboxService.findByAggregate("ORDER", orderId.toString());
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).getStatus()).isEqualTo(OutboxStatus.PROCESSED);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).isNotEmpty();
    }
}
