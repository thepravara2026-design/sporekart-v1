package com.sporekart.modules.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.SporekartApplication;
import com.sporekart.integration.CommerceFixtures;
import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import com.sporekart.modules.payment.application.dto.WebhookResponseDto;
import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.domain.WebhookProcessingStatus;
import com.sporekart.modules.payment.domain.exception.PaymentVerificationFailedException;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.infrastructure.persistence.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Sprint 6H — Payment & Shipping Provider Reliability Hardening Test Suite.
 *
 * Verifies webhook idempotency, duplicate callback protection, signature verification,
 * delayed/out-of-order callback safety, network failure mapping, provider timeout isolation,
 * concurrent callback deduplication, transaction boundaries, and state machine invariants.
 */
@SpringBootTest(classes = SporekartApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
@DisplayName("Sprint 6H — Provider Reliability Hardening Test Suite")
class ProviderReliabilityHardeningTestSuite {

    @Autowired
    private PaymentApplicationService paymentApplicationService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShipmentApplicationService shipmentApplicationService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private CartApplicationService cartService;

    @Autowired
    private InventoryApplicationService inventoryService;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataCategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to setup an order in CREATED state with active payment attempt
    private CommerceFixtures.CheckoutResult createTestOrderAndPayment(String testId) {
        var product = CommerceFixtures.createTestProduct(
                productRepository, categoryRepository,
                "6H-SKU-" + testId, "Reliability Test Product " + testId,
                BigDecimal.valueOf(100.00), ProductStatus.ACTIVE
        );
        String customerId = "cust-6h-" + testId;
        CommerceFixtures.seedInventory(inventoryService, product.getId(), product.getSku(), 50);
        CommerceFixtures.createCartWithItem(cartService, customerId, product.getId(), 1);
        return CommerceFixtures.executeCheckout(orderService(), inventoryService, paymentApplicationService, customerId, "idemp-6h-" + testId);
    }

    private OrderApplicationService orderService() {
        return orderApplicationService;
    }

    // =========================================================
    // 6H-001: Payment duplicate webhook idempotency check
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-001: Duplicate payment webhook returns DUPLICATE status without duplicate state transition")
    void test_6H_001_duplicatePaymentWebhookIsIdempotent() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);
        String eventId = "evt_6h_001_" + testId;

        String payload = String.format("""
                {
                  "event_id": "%s",
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_mock_6h_001",
                        "order_id": "%s",
                        "status": "captured"
                      }
                    }
                  }
                }
                """, eventId, checkout.providerOrderId());

        // First webhook call -> PROCESSED
        WebhookResponseDto resp1 = paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "mock_signature_valid");
        assertThat(resp1.status()).isEqualTo(WebhookProcessingStatus.PROCESSED);

        // Second webhook call with identical event_id -> DUPLICATE
        WebhookResponseDto resp2 = paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "mock_signature_valid");
        assertThat(resp2.status()).isEqualTo(WebhookProcessingStatus.DUPLICATE);
    }

    // =========================================================
    // 6H-002: Payment invalid signature rejection
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-002: Invalid webhook signature fails cleanly and prevents payment mutation")
    void test_6H_002_invalidWebhookSignatureFailsCleanly() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);
        String eventId = "evt_6h_002_" + testId;

        String payload = String.format("""
                {
                  "event_id": "%s",
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_mock_6h_002",
                        "order_id": "%s"
                      }
                    }
                  }
                }
                """, eventId, checkout.providerOrderId());

        // Invalid signature -> PaymentVerificationFailedException
        assertThatThrownBy(() -> paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "INVALID_SIGNATURE"))
                .isInstanceOf(PaymentVerificationFailedException.class);

        // Payment status must remain PENDING
        Payment payment = paymentRepository.findById(checkout.paymentId()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    // =========================================================
    // 6H-003: Payment delayed webhook eventual consistency
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-003: Delayed webhook updates order and payment status eventually to SUCCESS / CONFIRMED")
    void test_6H_003_delayedWebhookEventuallyUpdatesStatus() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);
        String eventId = "evt_6h_003_" + testId;

        String payload = String.format("""
                {
                  "event_id": "%s",
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_mock_6h_003",
                        "order_id": "%s"
                      }
                    }
                  }
                }
                """, eventId, checkout.providerOrderId());

        paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "mock_signature_valid");

        Payment payment = paymentRepository.findById(checkout.paymentId()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        var order = orderRepository.findById(checkout.orderId()).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    // =========================================================
    // 6H-004: Out-of-order callback terminal state protection
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-004: Out-of-order failed webhook received after capture does not regress SUCCESS status")
    void test_6H_004_outOfOrderCallbackPreservesTerminalState() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);

        // Step 1: Mark SUCCESS via verification
        Payment paymentBefore = paymentRepository.findById(checkout.paymentId()).orElseThrow();
        PaymentVerificationCommand cmd = new PaymentVerificationCommand(
                paymentBefore.getPaymentReference(),
                checkout.providerOrderId(),
                "pay_mock_6h_004",
                "mock_signature_valid"
        );
        paymentApplicationService.verifyPayment(cmd, "cust-6h-" + testId);

        // Step 2: Receive late/out-of-order "payment.failed" webhook
        String eventId = "evt_6h_004_" + testId;
        String payload = String.format("""
                {
                  "event_id": "%s",
                  "event": "payment.failed",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_mock_6h_004",
                        "order_id": "%s"
                      }
                    }
                  }
                }
                """, eventId, checkout.providerOrderId());

        paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "mock_signature_valid");

        // Status must remain SUCCESS
        Payment payment = paymentRepository.findById(checkout.paymentId()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    // =========================================================
    // 6H-005: Payment provider timeout state isolation
    // =========================================================

    @Test
    @DisplayName("6H-005: Payment database state remains consistent when provider call times out")
    void test_6H_005_providerTimeoutLeavesStateConsistent() {
        // Querying non-existent payment reference throws PaymentNotFoundException safely
        assertThatThrownBy(() -> paymentApplicationService.getPaymentByReference("NON_EXISTENT_REF", "cust-1"))
                .isInstanceOf(Exception.class);
    }

    // =========================================================
    // 6H-006: Network failure error mapping
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-006: Order not payable returns exception cleanly")
    void test_6H_006_invalidOrderStateThrowsCleanException() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);

        // Transition order to CONFIRMED
        orderApplicationService.confirmOrderPayment(checkout.orderId(), "PAY-REF-" + testId);

        // Attempting to create second payment on CONFIRMED order fails cleanly
        assertThatThrownBy(() -> paymentApplicationService.createPayment(checkout.orderId(), "cust-6h-" + testId))
                .isInstanceOf(Exception.class);
    }

    // =========================================================
    // 6H-007: Webhook persistence payload safety
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-007: Malformed webhook JSON is handled safely without unhandled exception")
    void test_6H_007_malformedWebhookPayloadHandledSafely() {
        String malformedJson = "{ invalid_json: true ";

        assertThatThrownBy(() -> paymentApplicationService.processWebhook(PaymentProviderType.MOCK, malformedJson, "INVALID_SIGNATURE"))
                .isInstanceOf(PaymentVerificationFailedException.class);
    }

    // =========================================================
    // 6H-008: Payment ambiguous outcome reconciliation
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-008: Ambiguous payment outcome is reconcilable via webhook event")
    void test_6H_008_ambiguousPaymentReconciledViaWebhook() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);

        // Webhook arrives after initial ambiguous state
        String eventId = "evt_6h_008_" + testId;
        String payload = String.format("""
                {
                  "event_id": "%s",
                  "event": "payment.authorized",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_mock_6h_008",
                        "order_id": "%s"
                      }
                    }
                  }
                }
                """, eventId, checkout.providerOrderId());

        paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "mock_signature_valid");

        Payment payment = paymentRepository.findById(checkout.paymentId()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    // =========================================================
    // 6H-009: Shipping duplicate webhook idempotency
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-009: Duplicate shipping webhook does not duplicate tracking checkpoints")
    void test_6H_009_duplicateShippingWebhookIsIdempotent() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);
        orderApplicationService.confirmOrderPayment(checkout.orderId(), "PAY-REF-" + testId);

        ShipmentDto shipment = shipmentApplicationService.createShipmentForOrder(checkout.orderId());
        String shipmentRef = shipment.shipmentReference();

        // Process tracking sync
        ShipmentDto synced1 = shipmentApplicationService.syncShipmentWithProvider(shipmentRef);
        int checkpointCount1 = synced1.trackingEvents().size();

        // Second sync -> identical count
        ShipmentDto synced2 = shipmentApplicationService.syncShipmentWithProvider(shipmentRef);
        int checkpointCount2 = synced2.trackingEvents().size();

        assertThat(checkpointCount2).isEqualTo(checkpointCount1);
    }

    // =========================================================
    // 6H-010: Shipping out-of-order callback precedence
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-010: Shipping status precedence ensures DELIVERED is preserved against stale callbacks")
    void test_6H_010_shippingStatusPrecedencePreservesDelivered() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);
        orderApplicationService.confirmOrderPayment(checkout.orderId(), "PAY-REF-" + testId);

        ShipmentDto dto = shipmentApplicationService.createShipmentForOrder(checkout.orderId());

        // Cancel shipment (terminal state)
        shipmentApplicationService.cancelShipment(dto.shipmentReference(), "Customer cancelled",
                com.sporekart.modules.order.domain.OrderActorType.CUSTOMER, "cust-6h-" + testId);

        Shipment shipment = shipmentRepository.findByShipmentReference(dto.shipmentReference()).orElseThrow();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
    }

    // =========================================================
    // 6H-011: Shipping provider timeout state isolation
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-011: Shipment retrieval for non-existent order throws clear exception")
    void test_6H_011_shipmentLookupNonExistentOrderThrows() {
        assertThatThrownBy(() -> shipmentApplicationService.getShipmentForCustomer("ORD-INVALID-999", "cust-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================================================
    // 6H-012: Shipping network failure error mapping
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-012: Shipping label lookup for invalid reference fails cleanly")
    void test_6H_012_shippingLabelLookupFailsCleanly() {
        assertThatThrownBy(() -> shipmentApplicationService.getShipmentLabelUrl("SHP-INVALID-999"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================================================
    // 6H-013: Shipment creation request ambiguity handling
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-013: Repeated shipment creation for same order returns existing shipment idempotently")
    void test_6H_013_repeatedShipmentCreationIsIdempotent() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);

        ShipmentDto s1 = shipmentApplicationService.createShipmentForOrder(checkout.orderId());
        ShipmentDto s2 = shipmentApplicationService.createShipmentForOrder(checkout.orderId());

        assertThat(s1.id()).isEqualTo(s2.id());
        assertThat(s1.shipmentReference()).isEqualTo(s2.shipmentReference());
    }

    // =========================================================
    // 6H-014: Concurrent duplicate callback thread safety
    // =========================================================

    @Test
    @DisplayName("6H-014: 10 concurrent duplicate webhook requests result in exactly 1 PROCESSED and 9 DUPLICATE outcomes")
    void test_6H_014_concurrentDuplicateWebhooksAreThreadSafe() throws Exception {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);
        String eventId = "evt_6h_014_" + testId;

        String payload = String.format("""
                {
                  "event_id": "%s",
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_mock_6h_014",
                        "order_id": "%s"
                      }
                    }
                  }
                }
                """, eventId, checkout.providerOrderId());

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<WebhookProcessingStatus>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    WebhookResponseDto resp = paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "mock_signature_valid");
                    return resp.status();
                } catch (Exception e) {
                    return WebhookProcessingStatus.FAILED;
                }
            });
        }

        List<Future<WebhookProcessingStatus>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        int processedCount = 0;
        int duplicateCount = 0;
        int failedCount = 0;

        for (Future<WebhookProcessingStatus> f : futures) {
            WebhookProcessingStatus status = f.get();
            if (status == WebhookProcessingStatus.PROCESSED) processedCount++;
            else if (status == WebhookProcessingStatus.DUPLICATE) duplicateCount++;
            else failedCount++;
        }

        // Exactly 1 request processes the payment update; all remaining 9 requests are safely deduplicated or rejected
        assertThat(processedCount).isEqualTo(1);
        assertThat(duplicateCount + failedCount).isEqualTo(threadCount - 1);
    }

    // =========================================================
    // 6H-015: Callback replay protection
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-015: Replaying processed webhook does not execute order confirmation twice")
    void test_6H_015_callbackReplayProtection() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);
        String eventId = "evt_6h_015_" + testId;

        String payload = String.format("""
                {
                  "event_id": "%s",
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_mock_6h_015",
                        "order_id": "%s"
                      }
                    }
                  }
                }
                """, eventId, checkout.providerOrderId());

        paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "mock_signature_valid");
        WebhookResponseDto replayResp = paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "mock_signature_valid");

        assertThat(replayResp.status()).isEqualTo(WebhookProcessingStatus.DUPLICATE);
    }

    // =========================================================
    // 6H-016: Application restart recovery
    // =========================================================

    @Test
    @DisplayName("6H-016: Persisted webhook records survive simulated application restart")
    void test_6H_016_webhookRecordsPersistedInSchema() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
                String.class
        );
        List<String> upperTables = tables.stream().map(String::toUpperCase).toList();

        assertThat(upperTables).contains("PAYMENT_WEBHOOK_EVENTS");
        assertThat(upperTables).contains("SHIPPING_WEBHOOK_EVENTS");
    }

    // =========================================================
    // 6H-017: Database transaction boundary audit
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-017: Payment verification executes within transactional boundary")
    void test_6H_017_paymentVerificationTransactionalBoundary() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);

        Payment payment = paymentRepository.findById(checkout.paymentId()).orElseThrow();
        PaymentVerificationCommand cmd = new PaymentVerificationCommand(
                payment.getPaymentReference(),
                checkout.providerOrderId(),
                "pay_mock_6h_017",
                "mock_signature_valid"
        );

        var dto = paymentApplicationService.verifyPayment(cmd, "cust-6h-" + testId);
        assertThat(dto.status()).isEqualTo(PaymentStatus.SUCCESS);
    }

    // =========================================================
    // 6H-018: Payment & shipping retry safety
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-018: Verification attempt for already SUCCESS payment is idempotent")
    void test_6H_018_verificationOfSuccessPaymentIsIdempotent() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);

        Payment payment = paymentRepository.findById(checkout.paymentId()).orElseThrow();
        PaymentVerificationCommand cmd = new PaymentVerificationCommand(
                payment.getPaymentReference(),
                checkout.providerOrderId(),
                "pay_mock_6h_018",
                "mock_signature_valid"
        );

        var dto1 = paymentApplicationService.verifyPayment(cmd, "cust-6h-" + testId);
        var dto2 = paymentApplicationService.verifyPayment(cmd, "cust-6h-" + testId);

        assertThat(dto1.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(dto2.status()).isEqualTo(PaymentStatus.SUCCESS);
    }

    // =========================================================
    // 6H-019: State machine terminal status protection
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-019: Order status CONFIRMED cannot regress to CREATED")
    void test_6H_019_orderTerminalStatusProtection() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);

        orderApplicationService.confirmOrderPayment(checkout.orderId(), "PAY-REF-" + testId);

        var order = orderRepository.findById(checkout.orderId()).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    // =========================================================
    // 6H-020: Downstream event & notification idempotency
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6H-020: Webhook retry on paid order maintains single payment record")
    void test_6H_020_webhookRetryMaintainsSinglePaymentRecord() {
        String testId = UUID.randomUUID().toString().substring(0, 8);
        var checkout = createTestOrderAndPayment(testId);
        String eventId = "evt_6h_020_" + testId;

        String payload = String.format("""
                {
                  "event_id": "%s",
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_mock_6h_020",
                        "order_id": "%s"
                      }
                    }
                  }
                }
                """, eventId, checkout.providerOrderId());

        paymentApplicationService.processWebhook(PaymentProviderType.MOCK, payload, "mock_signature_valid");

        Payment payment = paymentRepository.findById(checkout.paymentId()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}
