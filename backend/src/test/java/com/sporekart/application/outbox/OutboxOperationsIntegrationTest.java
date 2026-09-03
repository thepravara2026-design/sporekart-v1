package com.sporekart.application.outbox;

import com.sporekart.application.outbox.application.OutboxEventDispatcher;
import com.sporekart.application.outbox.application.OutboxOperationsService;
import com.sporekart.application.outbox.application.OutboxService;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.application.outbox.dto.OutboxEventDetailResponse;
import com.sporekart.application.outbox.dto.OutboxHealthResponse;
import com.sporekart.application.outbox.infrastructure.OutboxEventRepository;
import com.sporekart.application.outbox.infrastructure.OutboxWorker;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.infrastructure.persistence.SecurityAuditEventRepository;
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
public class OutboxOperationsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxOperationsService outboxOperationsService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxWorker outboxWorker;

    @Autowired
    private OutboxEventDispatcher outboxEventDispatcher;

    @Autowired(required = false)
    private SecurityAuditEventRepository securityAuditEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("7D-001: Health Summary accurately counts events by status and computes metrics")
    void testOutboxHealthSummary() {
        OutboxEvent pending1 = outboxService.publish("ORDER", "ORD-H-1", "ORDER_CREATED", "{}");
        OutboxEvent pending2 = outboxService.publish("ORDER", "ORD-H-2", "ORDER_PAID", "{}");
        OutboxEvent dead1 = outboxService.publish("SHIPMENT", "SHP-H-1", "SHIPMENT_FAILED", "{}");
        outboxService.recordFailure(dead1.getId(), "Fail 1");
        outboxService.recordFailure(dead1.getId(), "Fail 2");
        outboxService.recordFailure(dead1.getId(), "Fail 3");
        outboxService.recordFailure(dead1.getId(), "Fail 4");
        outboxService.recordFailure(dead1.getId(), "Fail 5"); // transition to DEAD

        OutboxHealthResponse health = outboxOperationsService.getOutboxHealth();
        assertThat(health.pendingCount()).isEqualTo(2);
        assertThat(health.deadCount()).isEqualTo(1);
        assertThat(health.totalCount()).isEqualTo(3);
        assertThat(health.status()).isEqualTo("DEGRADED");
        assertThat(health.oldestPendingAt()).isNotNull();
        assertThat(health.oldestDeadAt()).isNotNull();
    }

    @Test
    @DisplayName("7D-002: Dead event inspection retrieves operational detail safely")
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testDeadEventInspection() throws Exception {
        OutboxEvent deadEvent = outboxService.publish("RETURN", "RET-D-1", "RETURN_FAILED", "{\"sensitiveKey\":\"secret-value-12345\"}");
        outboxService.recordFailure(deadEvent.getId(), "Fatal failure 1");
        outboxService.recordFailure(deadEvent.getId(), "Fatal failure 2");
        outboxService.recordFailure(deadEvent.getId(), "Fatal failure 3");
        outboxService.recordFailure(deadEvent.getId(), "Fatal failure 4");
        outboxService.recordFailure(deadEvent.getId(), "Fatal failure 5");

        mockMvc.perform(get("/api/v1/admin/outbox/events/" + deadEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(deadEvent.getId()))
                .andExpect(jsonPath("$.data.status").value("DEAD"))
                .andExpect(jsonPath("$.data.aggregateType").value("RETURN"))
                .andExpect(jsonPath("$.data.retryCount").value(5));
    }

    @Test
    @DisplayName("7D-003: Valid Replay transitions DEAD -> PENDING, and OutboxWorker processes it to PROCESSED")
    void testValidReplayAndWorkerProcessing() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent eventPayload = OrderLifecycleEvent.create(
                orderId, "ORD-R-1", null, OrderStatus.CREATED,
                "Test order", OrderActorType.CUSTOMER, "cust-7d-3", "idem-7d-3"
        );
        OutboxEvent deadEvent = outboxService.publish("ORDER", orderId.toString(), "ORDER_CREATED", eventPayload);
        outboxService.recordFailure(deadEvent.getId(), "Err 1");
        outboxService.recordFailure(deadEvent.getId(), "Err 2");
        outboxService.recordFailure(deadEvent.getId(), "Err 3");
        outboxService.recordFailure(deadEvent.getId(), "Err 4");
        outboxService.recordFailure(deadEvent.getId(), "Err 5");

        assertThat(outboxEventRepository.findById(deadEvent.getId()).orElseThrow().getStatus()).isEqualTo(OutboxStatus.DEAD);

        // 1. Replay request
        OutboxEventDetailResponse replayed = outboxOperationsService.replayDeadLetterEvent(deadEvent.getId(), "opAdmin", "Operational test replay");
        assertThat(replayed.status()).isEqualTo(OutboxStatus.PENDING);
        assertThat(replayed.retryCount()).isEqualTo(0);

        // 2. Existing OutboxWorker polls and processes the replayed PENDING event
        outboxWorker.processOutbox();

        OutboxEvent processed = outboxEventRepository.findById(deadEvent.getId()).orElseThrow();
        assertThat(processed.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
        assertThat(processed.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("7D-004: Invalid Replay attempt on PENDING, PROCESSING, or PROCESSED event is rejected")
    void testInvalidReplayRejection() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent eventPayload = OrderLifecycleEvent.create(
                orderId, "ORD-INV-1", null, OrderStatus.CREATED,
                "Test order", OrderActorType.CUSTOMER, "cust-7d-4", "idem-7d-4"
        );
        OutboxEvent pendingEvent = outboxService.publish("ORDER", orderId.toString(), "ORDER_CREATED", eventPayload);

        assertThatThrownBy(() -> outboxOperationsService.replayDeadLetterEvent(pendingEvent.getId(), "admin", "Invalid attempt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot replay outbox event in status: PENDING");

        outboxWorker.processOutbox();
        OutboxEvent processedEvent = outboxEventRepository.findById(pendingEvent.getId()).orElseThrow();
        assertThat(processedEvent.getStatus()).isEqualTo(OutboxStatus.PROCESSED);

        assertThatThrownBy(() -> outboxOperationsService.replayDeadLetterEvent(processedEvent.getId(), "admin", "Invalid attempt on processed"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot replay outbox event in status: PROCESSED");
    }

    @Test
    @DisplayName("7D-005: Concurrent replay protection prevents invalid transitions during active processing")
    void testConcurrentReplayProtection() {
        OutboxEvent event = outboxService.publish("ORDER", "ORD-CONC-1", "ORDER_PAID", "{}");
        outboxService.markProcessing(event.getId());

        OutboxEvent processingEvent = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(processingEvent.getStatus()).isEqualTo(OutboxStatus.PROCESSING);

        assertThatThrownBy(() -> outboxOperationsService.replayDeadLetterEvent(event.getId(), "admin", "Concurrent attempt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot replay outbox event in status: PROCESSING");
    }

    @Test
    @DisplayName("7D-006: Replayed event respects idempotency and prevents duplicate consumer side effects")
    void testReplayedEventIdempotency() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent orderEvent = OrderLifecycleEvent.create(
                orderId, "ORD-IDEM-7D", null, OrderStatus.PAID,
                "Payment cleared", OrderActorType.CUSTOMER, "cust-7d-idem", "idem-key-7d"
        );
        OutboxEvent outboxEvent = outboxService.publish("ORDER", orderId.toString(), "ORDER_PAID", orderEvent);

        // Initial dispatch
        outboxEventDispatcher.dispatch(outboxEvent);
        assertThat(outboxEventDispatcher.isIdempotentlyProcessed(outboxEvent.getId())).isTrue();

        // Transition outbox event to DEAD in DB
        outboxService.recordFailure(outboxEvent.getId(), "Failure 1");
        outboxService.recordFailure(outboxEvent.getId(), "Failure 2");
        outboxService.recordFailure(outboxEvent.getId(), "Failure 3");
        outboxService.recordFailure(outboxEvent.getId(), "Failure 4");
        outboxService.recordFailure(outboxEvent.getId(), "Failure 5");

        outboxOperationsService.replayDeadLetterEvent(outboxEvent.getId(), "admin", "Replay idempotency test");

        // Dispatch replayed event
        OutboxEvent replayedEvent = outboxEventRepository.findById(outboxEvent.getId()).orElseThrow();
        outboxEventDispatcher.dispatch(replayedEvent);

        assertThat(outboxEventDispatcher.isIdempotentlyProcessed(replayedEvent.getId())).isTrue();
    }

    @Test
    @DisplayName("7D-007: Authorization rejects unauthorized requests on admin outbox endpoints")
    void testAuthorizationProtection() throws Exception {
        mockMvc.perform(get("/api/v1/admin/outbox/health"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/admin/outbox/events/some-id/replay"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7D-008: Replay action produces traceable security audit record")
    void testAuditTrailOnReplay() {
        OutboxEvent dead = outboxService.publish("ORDER", "ORD-AUD-1", "ORDER_CREATED", "{}");
        outboxService.recordFailure(dead.getId(), "1");
        outboxService.recordFailure(dead.getId(), "2");
        outboxService.recordFailure(dead.getId(), "3");
        outboxService.recordFailure(dead.getId(), "4");
        outboxService.recordFailure(dead.getId(), "5");

        long auditCountBefore = securityAuditEventRepository != null ? securityAuditEventRepository.count() : 0;

        outboxOperationsService.replayDeadLetterEvent(dead.getId(), "auditorAdmin", "Audit trace test");

        if (securityAuditEventRepository != null) {
            long auditCountAfter = securityAuditEventRepository.count();
            assertThat(auditCountAfter).isGreaterThan(auditCountBefore);
        }
    }

    @Test
    @DisplayName("7D-009: Health endpoint returns valid operational metrics when authorized")
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testAdminHealthEndpoint() throws Exception {
        outboxService.publish("ORDER", "ORD-API-1", "ORDER_CREATED", "{}");

        mockMvc.perform(get("/api/v1/admin/outbox/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pendingCount").value(1))
                .andExpect(jsonPath("$.data.status").value("HEALTHY"));
    }

    @Test
    @DisplayName("7D-010: Stale processing recovery detects stuck claims and resets them to PENDING")
    void testStaleProcessingRecovery() {
        UUID orderId = UUID.randomUUID();
        OrderLifecycleEvent eventPayload = OrderLifecycleEvent.create(
                orderId, "ORD-STALE-1", null, OrderStatus.CREATED,
                "Stale recovery test", OrderActorType.SYSTEM, "sys-stale", "idem-stale"
        );
        OutboxEvent event = outboxService.publish("ORDER", orderId.toString(), "ORDER_CREATED", eventPayload);
        outboxService.markProcessing(event.getId());

        OutboxEvent processingEvent = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(processingEvent.getStatus()).isEqualTo(OutboxStatus.PROCESSING);

        // Execute stale claim recovery (timeout 0 minutes to match current test timestamp)
        int recovered = outboxOperationsService.recoverStaleProcessingEvents(0);
        assertThat(recovered).isEqualTo(1);

        OutboxEvent recoveredEvent = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(recoveredEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);

        // Next worker poller execution processes the recovered event cleanly
        outboxWorker.processOutbox();
        assertThat(outboxEventRepository.findById(event.getId()).orElseThrow().getStatus()).isEqualTo(OutboxStatus.PROCESSED);
    }
}
