package com.sporekart.integration;

import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.domain.exception.InsufficientStockException;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class InventoryReservationIntegrationTest {

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
    @DisplayName("Inventory accounting: Exact stock reservation reduces available and increases reserved")
    void testExactStockReservation() {
        String customerId = "cust-inv-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-EXACT-" + UUID.randomUUID().toString().substring(0, 4), "Exact Stock", new BigDecimal("150.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 5); // 5 on hand

        CommerceFixtures.createCartWithItem(cartService, customerId, p.getId(), 5);
        CommerceFixtures.CheckoutResult checkoutResult = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, "IDEM-EXACT-" + UUID.randomUUID()
        );

        assertThat(checkoutResult.orderId()).isNotNull();

        InventoryItemDto item = inventoryService.getInventoryBySku(p.getSku());
        assertThat(item.onHandQuantity()).isEqualTo(5);
        assertThat(item.reservedQuantity()).isEqualTo(5);
        assertThat(item.availableQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("Insufficient stock: checkout reservation rejected cleanly when requested quantity exceeds available stock")
    void testInsufficientStockRejection() {
        String customerId = "cust-insuff-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-INSUFF-" + UUID.randomUUID().toString().substring(0, 4), "Low Stock", new BigDecimal("80.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 2); // only 2 on hand

        CommerceFixtures.createCartWithItem(cartService, customerId, p.getId(), 5);

        assertThatThrownBy(() -> CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, "IDEM-INSUFF-" + UUID.randomUUID()
        )).isInstanceOf(InsufficientStockException.class);

        InventoryItemDto item = inventoryService.getInventoryBySku(p.getSku());
        assertThat(item.onHandQuantity()).isEqualTo(2);
        assertThat(item.reservedQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("Payment failure compensation: releases reserved inventory back to available stock")
    void testPaymentFailureReleasesReservedInventory() {
        String customerId = "cust-comp-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-COMP-" + UUID.randomUUID().toString().substring(0, 4), "Compensated Stock", new BigDecimal("200.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 10);

        CommerceFixtures.createCartWithItem(cartService, customerId, p.getId(), 4);
        CommerceFixtures.CheckoutResult checkoutResult = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, "IDEM-COMP-" + UUID.randomUUID()
        );

        InventoryItemDto reservedItem = inventoryService.getInventoryBySku(p.getSku());
        assertThat(reservedItem.reservedQuantity()).isEqualTo(4);

        // Payment webhook failure event -> triggers Sprint 3H payment failure compensation
        String rawWebhookBody = "{\"event\":\"payment.failed\",\"event_id\":\"evt_fail_comp\",\"payload\":{\"payment\":{\"entity\":{\"order_id\":\"" + checkoutResult.providerOrderId() + "\"}}}}";
        paymentService.processWebhook(PaymentProviderType.MOCK, rawWebhookBody, "mock_signature");

        InventoryItemDto releasedItem = inventoryService.getInventoryBySku(p.getSku());
        assertThat(releasedItem.reservedQuantity()).isEqualTo(0);
        assertThat(releasedItem.onHandQuantity()).isEqualTo(10);
        assertThat(releasedItem.availableQuantity()).isEqualTo(10);
    }
}
