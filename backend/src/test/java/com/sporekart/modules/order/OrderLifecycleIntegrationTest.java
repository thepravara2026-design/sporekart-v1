package com.sporekart.modules.order;

import com.sporekart.integration.CommerceFixtures;
import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderStatusHistory;
import com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException;
import com.sporekart.modules.order.infrastructure.persistence.OrderStatusHistoryRepository;
import com.sporekart.modules.payment.application.PaymentApplicationService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class OrderLifecycleIntegrationTest {

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
    private OrderStatusHistoryRepository historyRepository;

    private CommerceFixtures.CheckoutResult testCheckout;

    @BeforeEach
    void setUp() {
        String sku = "SKU-4A-INT-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity product = CommerceFixtures.createTestProduct(productRepository, categoryRepository, sku, "Lifecycle Test Item", new BigDecimal("100.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryApplicationService, product.getId(), sku, 10);
        String customerId = "cust-4a-" + UUID.randomUUID().toString().substring(0, 6);
        CommerceFixtures.createCartWithItem(cartApplicationService, customerId, product.getId(), 2);
        testCheckout = CommerceFixtures.executeCheckout(orderApplicationService, inventoryApplicationService, paymentApplicationService, customerId, "IDEM-4A-" + UUID.randomUUID());
    }

    @Test
    @Transactional
    @DisplayName("Generic Transition API Atomically Updates Order Status & Persists History")
    void testGenericTransitionApiAtomicUpdate() {
        // CREATED -> CONFIRMED
        OrderDto confirmed = orderApplicationService.executeTransition(
                testCheckout.orderId(), OrderStatus.CONFIRMED, "Payment verified via webhook", OrderActorType.PAYMENT, "PAY-4A-001"
        );
        assertThat(confirmed.status()).isEqualTo(OrderStatus.CONFIRMED);

        List<OrderStatusHistory> history = historyRepository.findByOrderId(testCheckout.orderId());
        assertThat(history).hasSize(2); // CREATED, CONFIRMED
        OrderStatusHistory latest = history.get(history.size() - 1);
        assertThat(latest.getNewStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(latest.getActorType()).isEqualTo(OrderActorType.PAYMENT);
        assertThat(latest.getActorId()).isEqualTo("PAY-4A-001");
    }

    @Test
    @Transactional
    @DisplayName("Invalid State Transition Is Rejected Without History Mutation")
    void testInvalidTransitionRejected() {
        // CREATED -> DELIVERED is invalid
        assertThatThrownBy(() -> orderApplicationService.executeTransition(
                testCheckout.orderId(), OrderStatus.DELIVERED, "Direct delivery attempt", OrderActorType.ADMIN, "ADMIN-01"
        )).isInstanceOf(InvalidOrderStateTransitionException.class);

        List<OrderStatusHistory> history = historyRepository.findByOrderId(testCheckout.orderId());
        assertThat(history).hasSize(1); // Only CREATED
        assertThat(history.get(0).getNewStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @Transactional
    @DisplayName("Duplicate Transition Request Is Idempotent and Does Not Duplicate History")
    void testDuplicateTransitionIdempotency() {
        orderApplicationService.confirmOrderPayment(testCheckout.orderId(), "PAY-IDEM-01");

        OrderDto first = orderApplicationService.startProcessing(testCheckout.orderId(), "ADMIN-01");
        OrderDto second = orderApplicationService.startProcessing(testCheckout.orderId(), "ADMIN-01");

        assertThat(first.status()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(second.status()).isEqualTo(OrderStatus.PROCESSING);

        List<OrderStatusHistory> history = historyRepository.findByOrderId(testCheckout.orderId());
        assertThat(history).hasSize(3); // CREATED, CONFIRMED, PROCESSING (no duplicate)
    }

    @Test
    @DisplayName("Concurrent Transition Execution Handled Safely Without Corruption")
    void testConcurrentTransitionExecution() throws InterruptedException {
        // Prepare order in CONFIRMED status
        orderApplicationService.confirmOrderPayment(testCheckout.orderId(), "PAY-CONC-01");

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String adminId = "ADMIN-" + i;
            executor.submit(() -> {
                try {
                    latch.await();
                    orderApplicationService.startProcessing(testCheckout.orderId(), adminId);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        OrderDto finalOrder = orderApplicationService.getAdminOrderDetail(testCheckout.orderNumber());
        assertThat(finalOrder.status()).isEqualTo(OrderStatus.PROCESSING);
    }
}
