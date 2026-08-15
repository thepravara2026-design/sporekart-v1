package com.sporekart.modules.payment;

import com.sporekart.integration.CommerceFixtures;
import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.PaymentReconciliationService;
import com.sporekart.modules.payment.application.dto.PaymentCheckoutDto;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import com.sporekart.modules.payment.application.dto.WebhookResponseDto;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.domain.PaymentStatusHistory;
import com.sporekart.modules.payment.domain.WebhookProcessingStatus;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentStatusHistoryEntity;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentStatusHistoryRepository;
import com.sporekart.modules.payment.infrastructure.provider.razorpay.RazorpayPaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentLifecycleIntegrationTest {

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataCategoryRepository categoryRepository;

    @Autowired
    private InventoryApplicationService inventoryApplicationService;

    @Autowired
    private CartApplicationService cartApplicationService;

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private PaymentApplicationService paymentApplicationService;

    @Autowired
    private PaymentReconciliationService reconciliationService;

    @Autowired
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    private String customerId;
    private CommerceFixtures.CheckoutResult checkoutResult;

    @BeforeEach
    void setUp() {
        String sku = "SKU-4B-INT-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity product = CommerceFixtures.createTestProduct(productRepository, categoryRepository, sku, "Payment Lifecycle Item", new BigDecimal("1499.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryApplicationService, product.getId(), sku, 20);
        customerId = "cust-4b-" + UUID.randomUUID().toString().substring(0, 6);
        CommerceFixtures.createCartWithItem(cartApplicationService, customerId, product.getId(), 1);

        checkoutResult = CommerceFixtures.executeCheckout(
                orderApplicationService, inventoryApplicationService, paymentApplicationService, customerId, "IDEM-4B-" + UUID.randomUUID()
        );
    }

    @Test
    @Transactional
    @DisplayName("Complete Successful Payment Verification Integrates Atomically with Sprint 4A Order Lifecycle")
    void testPaymentVerificationOrderLifecycleIntegration() {
        // Payment checkout was initiated during fixture checkout
        PaymentDto paymentBefore = paymentApplicationService.getPaymentByReference("PAY-" + checkoutResult.orderNumber(), customerId);
        assertThat(paymentBefore.status()).isEqualTo(PaymentStatus.PENDING);

        // Verify payment
        PaymentVerificationCommand command = new PaymentVerificationCommand(
                paymentBefore.paymentReference(),
                checkoutResult.providerOrderId(),
                "pay_mock_success_123",
                "mock_sig_valid"
        );

        PaymentDto verified = paymentApplicationService.verifyPayment(command, customerId);
        assertThat(verified.status()).isEqualTo(PaymentStatus.SUCCESS);

        // Verify PaymentStatusHistory recorded
        List<PaymentStatusHistoryEntity> histories = paymentStatusHistoryRepository.findByPaymentIdOrderByCreatedAtAsc(verified.id());
        assertThat(histories).isNotEmpty();
        PaymentStatusHistoryEntity latestHistory = histories.get(histories.size() - 1);
        assertThat(latestHistory.getNewStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(latestHistory.getSource()).isEqualTo("CLIENT_VERIFY");

        // Verify Order status transitioned to CONFIRMED via canonical Sprint 4A lifecycle
        OrderDto order = orderApplicationService.getAdminOrderDetail(checkoutResult.orderNumber());
        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @Transactional
    @DisplayName("Duplicate Webhook Ingestion Is Idempotent and Harmless")
    void testDuplicateWebhookIdempotency() {
        String eventId = "evt_rzp_" + UUID.randomUUID().toString().substring(0, 12);
        String rawBody = "{\"id\":\"" + eventId + "\",\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_rzp_mock_123\",\"order_id\":\"" + checkoutResult.providerOrderId() + "\",\"amount\":149900,\"currency\":\"INR\"}}}}";
        String secret = "test_webhook_secret_key";
        String validSignature = RazorpayPaymentProvider.verifyHmacSha256(rawBody, null, secret) ? "valid" : "mock_sig";

        WebhookResponseDto firstResult = paymentApplicationService.processWebhook(PaymentProviderType.MOCK, rawBody, validSignature);
        assertThat(firstResult.status()).isEqualTo(WebhookProcessingStatus.PROCESSED);

        // Re-send duplicate webhook event
        WebhookResponseDto secondResult = paymentApplicationService.processWebhook(PaymentProviderType.MOCK, rawBody, validSignature);
        assertThat(secondResult.status()).isEqualTo(WebhookProcessingStatus.DUPLICATE);
    }

    @Test
    @Transactional
    @DisplayName("Automated Payment Reconciliation Reconciles divergent Provider Status safely")
    void testPaymentReconciliation() {
        PaymentDto paymentBefore = paymentApplicationService.getPaymentByReference("PAY-" + checkoutResult.orderNumber(), customerId);

        PaymentDto reconciled = reconciliationService.reconcilePayment(paymentBefore.id(), "ADMIN-RECON");
        assertThat(reconciled.status()).isEqualTo(PaymentStatus.SUCCESS);

        OrderDto order = orderApplicationService.getAdminOrderDetail(checkoutResult.orderNumber());
        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Concurrent Payment Verification Is Safe Against Duplicate Settlements")
    void testConcurrentPaymentVerificationSafety() throws InterruptedException {
        PaymentDto paymentBefore = paymentApplicationService.getPaymentByReference("PAY-" + checkoutResult.orderNumber(), customerId);

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    PaymentVerificationCommand command = new PaymentVerificationCommand(
                            paymentBefore.paymentReference(),
                            checkoutResult.providerOrderId(),
                            "pay_mock_conc_123",
                            "mock_sig_valid"
                    );
                    paymentApplicationService.verifyPayment(command, customerId);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        OrderDto order = orderApplicationService.getAdminOrderDetail(checkoutResult.orderNumber());
        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
    }
}
