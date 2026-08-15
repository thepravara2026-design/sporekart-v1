package com.sporekart.modules.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.application.idempotency.IdempotencyRecord;
import com.sporekart.application.idempotency.IdempotencyService;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.AddressDto;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.WebhookResponseDto;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIdempotencyAndConcurrencyIntegrationTest {

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private PaymentApplicationService paymentApplicationService;

    @Test
    @DisplayName("Should validate idempotency key max length and reject blank keys")
    void shouldValidateIdempotencyKeyFormat() {
        String longKey = "K".repeat(150);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.sporekart.application.exception.InvalidIdempotencyKeyException.class,
                () -> idempotencyService.validateKey(longKey)
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                com.sporekart.application.exception.InvalidIdempotencyKeyException.class,
                () -> idempotencyService.validateKey("   ")
        );
    }

    @Test
    @DisplayName("Should detect payload mismatch for duplicate idempotency key with different payload")
    void shouldDetectIdempotencyPayloadMismatch() {
        String actorId = "cust-idem-1";
        String key = "IDEM-KEY-999";
        String path = "/api/v1/orders";
        String payload1 = "{\"item\":\"SKU-1\"}";
        String payload2 = "{\"item\":\"SKU-2\"}";

        idempotencyService.checkOrStartProcessing(actorId, key, path, payload1);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.sporekart.application.exception.InvalidIdempotencyKeyException.class,
                () -> idempotencyService.checkOrStartProcessing(actorId, key, path, payload2)
        );
    }

    @Test
    @DisplayName("Should deduplicate payment webhooks with identical provider event ID")
    void shouldDeduplicatePaymentWebhooks() {
        PaymentProviderType provider = PaymentProviderType.MOCK;
        String payload = "{\"event_id\":\"evt_mock_dup_1001\",\"event\":\"payment.authorized\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_1001\",\"order_id\":\"order_1001\",\"status\":\"authorized\",\"amount\":50000,\"currency\":\"INR\"}}}}";

        WebhookResponseDto result1 = paymentApplicationService.processWebhook(provider, payload, "mock_signature");
        WebhookResponseDto result2 = paymentApplicationService.processWebhook(provider, payload, "mock_signature");

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result2.message()).contains("already processed");
    }
}
