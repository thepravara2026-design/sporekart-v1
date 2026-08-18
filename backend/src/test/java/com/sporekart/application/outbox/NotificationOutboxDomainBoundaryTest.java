package com.sporekart.application.outbox;

import com.sporekart.application.outbox.application.OutboxService;
import com.sporekart.application.outbox.domain.OutboxEvent;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.modules.notification.application.TemplatePlaceholderSubstitutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notification & Outbox Domain Boundary Protection Suite")
class NotificationOutboxDomainBoundaryTest {

    @Test
    @DisplayName("OutboxEvent validates initial state and handles retry exhaustion to DEAD status")
    void testOutboxEventStateTransitionsAndTerminalFailure() {
        OutboxEvent event = OutboxEvent.create("ORDER", UUID.randomUUID().toString(), "ORDER_CREATED", "{\"orderId\":\"123\"}");

        assertEquals(OutboxStatus.PENDING, event.getStatus());
        assertEquals(0, event.getRetryCount());
        assertEquals(5, event.getMaxRetries());

        event.markProcessing();
        assertEquals(OutboxStatus.PROCESSING, event.getStatus());

        // Simulate 4 failures (retryCount becomes 4, status FAILED)
        for (int i = 0; i < 4; i++) {
            event.recordFailure("Transient error attempt " + (i + 1), 2);
            assertEquals(OutboxStatus.FAILED, event.getStatus());
        }

        // 5th failure exceeds maxRetries (status becomes DEAD)
        event.recordFailure("Final failure attempt 5", 2);
        assertEquals(OutboxStatus.DEAD, event.getStatus());
        assertEquals(5, event.getRetryCount());
        assertEquals("Final failure attempt 5", event.getLastError());
    }

    @Test
    @DisplayName("OutboxService rejects null mandatory attributes during event publication")
    void testOutboxServiceNullParameterValidation() {
        OutboxService service = new OutboxService(null);

        assertThrows(NullPointerException.class, () ->
                service.publish(null, "id-1", "EVENT_TYPE", "payload")
        );

        assertThrows(NullPointerException.class, () ->
                service.publish("ORDER", null, "EVENT_TYPE", "payload")
        );

        assertThrows(NullPointerException.class, () ->
                service.publish("ORDER", "id-1", null, "payload")
        );
    }

    @Test
    @DisplayName("TemplatePlaceholderSubstitutor handles missing variables and sanitizes HTML tags safely")
    void testTemplatePlaceholderSubstitution() {
        TemplatePlaceholderSubstitutor substitutor = new TemplatePlaceholderSubstitutor();

        String template = "Hello {{name}}, your order {{orderRef}} status is {{status}}.";
        Map<String, String> variables = Map.of(
                "name", "<John & Doe>",
                "orderRef", "ORD-999"
                // "status" is missing
        );

        String result = substitutor.substitute(template, variables);

        assertTrue(result.contains("&lt;John & Doe&gt;"));
        assertTrue(result.contains("ORD-999"));
        assertTrue(result.contains("status is ."));
        assertFalse(result.contains("<John & Doe>"));
    }

    @Test
    @DisplayName("OutboxEvent idempotency replay resets state to PENDING and clears processedAt")
    void testOutboxEventReplayReset() {
        OutboxEvent event = OutboxEvent.create("PAYMENT", "pay-100", "PAYMENT_FAILED", "{}");
        event.markProcessing();
        event.markProcessed();
        assertEquals(OutboxStatus.PROCESSED, event.getStatus());
        assertNotNull(event.getProcessedAt());

        event.markReplayed("ADMIN", "Manual replay testing");
        assertEquals(OutboxStatus.PENDING, event.getStatus());
        assertEquals(0, event.getRetryCount());
        assertNull(event.getProcessedAt());
        assertTrue(event.getLastError().contains("Replayed by ADMIN"));
    }
}
