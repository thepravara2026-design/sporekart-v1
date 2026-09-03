package com.sporekart.application.outbox;

import com.sporekart.application.outbox.application.OutboxEventDispatcher;
import com.sporekart.application.outbox.application.OutboxService;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.application.outbox.infrastructure.OutboxEventListener;
import com.sporekart.application.outbox.infrastructure.OutboxEventRepository;
import com.sporekart.application.outbox.infrastructure.OutboxWorker;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import com.sporekart.modules.order.domain.OrderActorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class OutboxReliabilityIntegrationTest {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

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
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("7C-001: Outbox event is persisted in database when domain event is published")
    void testOutboxEventPersistedOnDomainEvent() {
        UUID orderId = UUID.randomUUID();
        String orderNumber = "ORD-7C-001";
        String customerId = "cust-7c-101";

        OrderLifecycleEvent event = OrderLifecycleEvent.create(
                orderId, orderNumber, null, OrderStatus.CREATED,
                "Checkout completed", OrderActorType.CUSTOMER, customerId, "idempotency-key-7c-1"
        );

        applicationEventPublisher.publishEvent(event);

        List<OutboxEvent> events = outboxService.findByAggregate("ORDER", orderId.toString());
        assertThat(events).hasSize(1);

        OutboxEvent outboxEvent = events.get(0);
        assertThat(outboxEvent.getAggregateType()).isEqualTo("ORDER");
        assertThat(outboxEvent.getAggregateId()).isEqualTo(orderId.toString());
        assertThat(outboxEvent.getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEvent.getPayload()).contains(orderNumber);
    }

    @Test
    @DisplayName("7C-002: Outbox worker polls PENDING events and transitions status to PROCESSED")
    void testOutboxWorkerProcessing() {
        UUID orderId = UUID.randomUUID();
        String orderNumber = "ORD-7C-002";
        String customerId = "cust-7c-102";

        OrderLifecycleEvent event = OrderLifecycleEvent.create(
                orderId, orderNumber, null, OrderStatus.PAID,
                "Payment confirmed", OrderActorType.SYSTEM, customerId, "idempotency-key-7c-2"
        );

        applicationEventPublisher.publishEvent(event);

        List<OutboxEvent> pendingList = outboxService.fetchPendingEvents(10);
        assertThat(pendingList).isNotEmpty();

        outboxWorker.processOutbox();

        List<OutboxEvent> events = outboxService.findByAggregate("ORDER", orderId.toString());
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getStatus()).isEqualTo(OutboxStatus.PROCESSED);
        assertThat(events.get(0).getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("7C-003: Outbox failure records attempt, calculates backoff, and eventually marks event as DEAD")
    void testOutboxFailureAndDeadLettering() {
        OutboxEvent event = outboxService.publish("ORDER", "ORD-FAIL-1", "UNKNOWN_FAIL_EVENT", "{ \"bad\": \"json\" }");
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);

        // Record failure 1
        outboxService.recordFailure(event.getId(), "Simulated connection timeout");
        OutboxEvent afterFail1 = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(afterFail1.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(afterFail1.getRetryCount()).isEqualTo(1);
        assertThat(afterFail1.getLastError()).isEqualTo("Simulated connection timeout");

        // Record failures until maxRetries (5)
        outboxService.recordFailure(event.getId(), "Retry failure 2");
        outboxService.recordFailure(event.getId(), "Retry failure 3");
        outboxService.recordFailure(event.getId(), "Retry failure 4");
        outboxService.recordFailure(event.getId(), "Final failure");

        OutboxEvent deadEvent = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(deadEvent.getStatus()).isEqualTo(OutboxStatus.DEAD);
        assertThat(deadEvent.getRetryCount()).isEqualTo(5);
        assertThat(deadEvent.getLastError()).isEqualTo("Final failure");
    }

    @Test
    @DisplayName("7C-004: Outbox event dispatch enforces idempotency to prevent duplicate side effects")
    void testOutboxIdempotency() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent event = OrderLifecycleEvent.create(
                orderId, "ORD-7C-004", null, OrderStatus.SHIPPED,
                "Package dispatched", OrderActorType.SHIPPING, "SHIPPING_SYSTEM", "idempotency-key-7c-4"
        );

        OutboxEvent outboxEvent = outboxService.publish("ORDER", orderId.toString(), "ORDER_SHIPPED", event);

        // Dispatch 1
        outboxEventDispatcher.dispatch(outboxEvent);
        assertThat(outboxEventDispatcher.isIdempotentlyProcessed(outboxEvent.getId())).isTrue();

        // Dispatch 2 (Duplicate)
        outboxEventDispatcher.dispatch(outboxEvent);
        assertThat(outboxEventDispatcher.isIdempotentlyProcessed(outboxEvent.getId())).isTrue();
    }

    @Test
    @DisplayName("7C-005: Outbox recovery guarantees un-processed events remain in database after restart simulation")
    void testCrashRecoveryResilience() {
        OutboxEvent event1 = outboxService.publish("ORDER", "ORD-CRASH-1", "ORDER_CREATED", "{}");
        OutboxEvent event2 = outboxService.publish("SHIPMENT", "SHP-CRASH-1", "SHIPMENT_BOOKED", "{}");

        assertThat(outboxService.countByStatus(OutboxStatus.PENDING)).isEqualTo(2);

        // Simulate application restart: fetch pending events
        List<OutboxEvent> recovered = outboxService.fetchPendingEvents(10);
        assertThat(recovered).hasSize(2);
        assertThat(recovered).extracting(OutboxEvent::getId).contains(event1.getId(), event2.getId());
    }
}
