package com.sporekart.integration;

import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.cart.application.dto.CartDto;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentCheckoutDto;
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
public class CartCheckoutCurrencyConsistencyTest {

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
    @DisplayName("Cart -> Order -> Payment: persisted totals and currency match a manually computed expected value (INR, 18% tax, flat shipping)")
    void testCartToPaymentCurrencyAndTotalsConsistency() {
        String customerId = "cust-cc-" + UUID.randomUUID().toString().substring(0, 6);
        String sku = "SKU-CC-" + UUID.randomUUID().toString().substring(0, 4);

        ProductEntity product = CommerceFixtures.createTestProduct(
                productRepo, categoryRepo, sku, "Currency Consistency Mushroom", new BigDecimal("499.00"), ProductStatus.ACTIVE
        );
        assertThat(product.getCurrency()).isEqualTo("INR");

        InventoryItemDto inventory = CommerceFixtures.seedInventory(inventoryService, product.getId(), sku, 50);
        assertThat(inventory.onHandQuantity()).isEqualTo(50);

        // 1. Cart
        CartDto cart = CommerceFixtures.createCartWithItem(cartService, customerId, product.getId(), 2);
        assertThat(cart.currency()).isEqualTo("INR");
        assertThat(cart.subtotal()).isEqualByComparingTo(new BigDecimal("998.00"));

        // 2. Order creation (authoritative catalog price, 18% GST, flat shipping below 1000 threshold)
        CreateOrderCommand createCmd = new CreateOrderCommand(
                CommerceFixtures.createTestAddress(), "IDEM-CC-" + UUID.randomUUID(), "Currency consistency notes"
        );
        OrderDto order = orderService.createOrder(customerId, createCmd);

        BigDecimal expectedSubtotal = new BigDecimal("998.00");
        BigDecimal expectedTax = new BigDecimal("179.64");   // 998.00 * 0.18
        BigDecimal expectedShipping = new BigDecimal("50.00"); // below free shipping threshold of 1000
        BigDecimal expectedGrandTotal = new BigDecimal("1227.64"); // 998.00 + 179.64 + 50.00

        assertThat(order.currency()).isEqualTo("INR");
        assertThat(order.subtotal()).isEqualByComparingTo(expectedSubtotal);
        assertThat(order.taxTotal()).isEqualByComparingTo(expectedTax);
        assertThat(order.shippingFee()).isEqualByComparingTo(expectedShipping);
        assertThat(order.grandTotal()).isEqualByComparingTo(expectedGrandTotal);

        // 3. Inventory reservation (required before payment) then payment initiation mirrors the persisted order amount and currency
        inventoryService.reserveInventoryForOrder(order.id(), customerId);
        PaymentCheckoutDto paymentCheckout = paymentService.createPayment(order.id(), customerId);
        assertThat(paymentCheckout.currency()).isEqualTo("INR");
        assertThat(paymentCheckout.amount()).isEqualByComparingTo(expectedGrandTotal);

        // 4. Payment verification retains amount and currency
        PaymentVerificationCommand verifyCmd = new PaymentVerificationCommand(
                paymentCheckout.paymentReference(),
                paymentCheckout.providerOrderId(),
                "pay_cc_" + UUID.randomUUID().toString().substring(0, 8),
                "mock_signature"
        );
        PaymentDto payment = paymentService.verifyPayment(verifyCmd, customerId);
        assertThat(payment.currency()).isEqualTo("INR");
        assertThat(payment.amount()).isEqualByComparingTo(expectedGrandTotal);
        assertThat(payment.status().name()).isEqualTo("SUCCESS");

        // 5. Persisted order still matches
        OrderDto persistedOrder = orderService.getOrderDetail(customerId, order.id());
        assertThat(persistedOrder.currency()).isEqualTo("INR");
        assertThat(persistedOrder.grandTotal()).isEqualByComparingTo(expectedGrandTotal);
    }
}