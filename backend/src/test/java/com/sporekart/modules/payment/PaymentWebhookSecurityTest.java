package com.sporekart.modules.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.WebhookResponseDto;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentWebhookEvent;
import com.sporekart.modules.payment.domain.WebhookProcessingStatus;
import com.sporekart.modules.payment.domain.exception.PaymentVerificationFailedException;
import com.sporekart.modules.payment.infrastructure.config.PaymentProperties;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentAttemptRepository;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.payment.infrastructure.persistence.WebhookEventRepository;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderRegistry;
import com.sporekart.modules.payment.infrastructure.provider.mock.MockPaymentProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentWebhookSecurityTest {

    @Test
    @DisplayName("Should process valid webhook notification successfully and record event")
    void testProcessValidWebhook() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentAttemptRepository paymentAttemptRepository = mock(PaymentAttemptRepository.class);
        WebhookEventRepository webhookEventRepository = mock(WebhookEventRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        InventoryApplicationService inventoryService = mock(InventoryApplicationService.class);
        PaymentProperties properties = new PaymentProperties();

        MockPaymentProvider mockProvider = new MockPaymentProvider();
        PaymentProviderRegistry registry = new PaymentProviderRegistry(List.of(mockProvider), properties);

        PaymentApplicationService service = new PaymentApplicationService(
                paymentRepository, paymentAttemptRepository, webhookEventRepository,
                orderRepository, reservationRepository, inventoryService, registry, properties, new ObjectMapper()
        );

        String jsonPayload = "{\"event_id\":\"evt_test_001\",\"event\":\"payment.captured\",\"payload\":{}}";

        when(webhookEventRepository.findByProviderAndProviderEventId(PaymentProviderType.MOCK, "evt_test_001")).thenReturn(Optional.empty());
        when(webhookEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookResponseDto response = service.processWebhook(PaymentProviderType.MOCK, jsonPayload, "valid_signature_123");

        assertNotNull(response);
        assertEquals(WebhookProcessingStatus.PROCESSED, response.status());
        verify(webhookEventRepository).save(any(PaymentWebhookEvent.class));
    }

    @Test
    @DisplayName("Should reject webhook with invalid signature and throw PaymentVerificationFailedException")
    void testRejectInvalidSignatureWebhook() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentAttemptRepository paymentAttemptRepository = mock(PaymentAttemptRepository.class);
        WebhookEventRepository webhookEventRepository = mock(WebhookEventRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        InventoryApplicationService inventoryService = mock(InventoryApplicationService.class);
        PaymentProperties properties = new PaymentProperties();

        MockPaymentProvider mockProvider = new MockPaymentProvider();
        PaymentProviderRegistry registry = new PaymentProviderRegistry(List.of(mockProvider), properties);

        PaymentApplicationService service = new PaymentApplicationService(
                paymentRepository, paymentAttemptRepository, webhookEventRepository,
                orderRepository, reservationRepository, inventoryService, registry, properties, new ObjectMapper()
        );

        String jsonPayload = "{\"event_id\":\"evt_test_002\",\"event\":\"payment.captured\",\"payload\":{}}";
        when(webhookEventRepository.findByProviderAndProviderEventId(PaymentProviderType.MOCK, "evt_test_002")).thenReturn(Optional.empty());
        when(webhookEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(PaymentVerificationFailedException.class, () ->
                service.processWebhook(PaymentProviderType.MOCK, jsonPayload, "INVALID_SIGNATURE")
        );
    }

    @Test
    @DisplayName("Should detect duplicate webhook and return DUPLICATE status without reprocessing side-effects")
    void testDuplicateWebhookIdempotency() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentAttemptRepository paymentAttemptRepository = mock(PaymentAttemptRepository.class);
        WebhookEventRepository webhookEventRepository = mock(WebhookEventRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        InventoryApplicationService inventoryService = mock(InventoryApplicationService.class);
        PaymentProperties properties = new PaymentProperties();

        MockPaymentProvider mockProvider = new MockPaymentProvider();
        PaymentProviderRegistry registry = new PaymentProviderRegistry(List.of(mockProvider), properties);

        PaymentApplicationService service = new PaymentApplicationService(
                paymentRepository, paymentAttemptRepository, webhookEventRepository,
                orderRepository, reservationRepository, inventoryService, registry, properties, new ObjectMapper()
        );

        PaymentWebhookEvent existingProcessedEvent = PaymentWebhookEvent.recordEvent(PaymentProviderType.MOCK, "evt_test_003", "payment.captured", true);
        existingProcessedEvent.markProcessed();

        String jsonPayload = "{\"event_id\":\"evt_test_003\",\"event\":\"payment.captured\",\"payload\":{}}";
        when(webhookEventRepository.findByProviderAndProviderEventId(PaymentProviderType.MOCK, "evt_test_003")).thenReturn(Optional.of(existingProcessedEvent));

        WebhookResponseDto response = service.processWebhook(PaymentProviderType.MOCK, jsonPayload, "valid_signature_123");

        assertNotNull(response);
        assertEquals(WebhookProcessingStatus.DUPLICATE, response.status());
    }
}
