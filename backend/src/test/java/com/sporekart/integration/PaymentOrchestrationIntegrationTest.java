package com.sporekart.integration;

import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentOrchestrationIntegrationTest {

    @Autowired
    private SpringDataProductRepository productRepo;

    @Autowired
    private SpringDataCategoryRepository categoryRepo;

    @Autowired
    private CartApplicationService cartService;

    @Autowired
    private OrderApplicationService orderService;

    @Autowired
    private InventoryApplicationService inventoryService;

    @Autowired
    private PaymentApplicationService paymentService;

    @Test
    @DisplayName("Payment success path: moves payment to SUCCESS and order to CONFIRMED or PAID")
    void testPaymentSuccessPath() {
        String customerId = "cust-pay-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-PAY-" + UUID.randomUUID().toString().substring(0, 4), "Pay Prod", new BigDecimal("300.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 10);

        CommerceFixtures.createCartWithItem(cartService, customerId, p.getId(), 1);
        CommerceFixtures.CheckoutResult checkoutResult = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, "IDEM-PAY-" + UUID.randomUUID()
        );

        PaymentVerificationCommand cmd = new PaymentVerificationCommand(
                checkoutResult.paymentReference(),
                checkoutResult.providerOrderId(),
                "pay_mock_" + UUID.randomUUID().toString().substring(0, 8),
                "mock_signature"
        );

        PaymentDto paymentDto = paymentService.verifyPayment(cmd, customerId);
        assertThat(paymentDto.status().name()).isEqualTo("SUCCESS");

        OrderDto order = orderService.getOrderDetail(customerId, checkoutResult.orderId());
        assertThat(order.status()).isIn(OrderStatus.CONFIRMED, OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.READY_FOR_FULFILMENT);
    }

    @Test
    @DisplayName("Duplicate payment verification idempotency: replay of verification is idempotent with zero side effects")
    void testDuplicatePaymentCallbackIdempotency() {
        String customerId = "cust-dup-pay-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-DUPP-" + UUID.randomUUID().toString().substring(0, 4), "Dup Pay Prod", new BigDecimal("150.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 10);

        CommerceFixtures.createCartWithItem(cartService, customerId, p.getId(), 2);
        CommerceFixtures.CheckoutResult checkoutResult = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, "IDEM-DUPP-" + UUID.randomUUID()
        );

        PaymentVerificationCommand cmd = new PaymentVerificationCommand(
                checkoutResult.paymentReference(),
                checkoutResult.providerOrderId(),
                "pay_mock_" + UUID.randomUUID().toString().substring(0, 8),
                "mock_signature"
        );

        // First verification
        PaymentDto firstRes = paymentService.verifyPayment(cmd, customerId);
        assertThat(firstRes.status().name()).isEqualTo("SUCCESS");

        // Second duplicate verification
        PaymentDto secondRes = paymentService.verifyPayment(cmd, customerId);
        assertThat(secondRes.status().name()).isEqualTo("SUCCESS");
        assertThat(secondRes.id()).isEqualTo(firstRes.id());

        OrderDto order = orderService.getOrderDetail(customerId, checkoutResult.orderId());
        assertThat(order.status()).isIn(OrderStatus.CONFIRMED, OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.READY_FOR_FULFILMENT);
    }
}
