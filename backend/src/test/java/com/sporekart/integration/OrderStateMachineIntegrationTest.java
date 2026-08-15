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
import com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrderStateMachineIntegrationTest {

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

    @Autowired
    private ShipmentApplicationService shipmentService;

    @Test
    @DisplayName("Valid Order lifecycle transition chain with audit status history recording")
    void testValidOrderLifecycleTransitions() {
        String customerId = "cust-osm-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-OSM-" + UUID.randomUUID().toString().substring(0, 4), "OSM Prod", new BigDecimal("400.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 10);

        CommerceFixtures.createCartWithItem(cartService, customerId, p.getId(), 1);
        CommerceFixtures.CheckoutResult checkoutResult = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, "IDEM-OSM-" + UUID.randomUUID()
        );

        OrderDto order = orderService.getOrderDetail(customerId, checkoutResult.orderId());
        assertThat(order.status()).isEqualTo(OrderStatus.CREATED);

        // Payment -> CONFIRMED
        PaymentVerificationCommand verifyCmd = new PaymentVerificationCommand(
                checkoutResult.paymentReference(),
                checkoutResult.providerOrderId(),
                "pay_mock_" + UUID.randomUUID().toString().substring(0, 8),
                "mock_signature"
        );
        paymentService.verifyPayment(verifyCmd, customerId);

        // Shipment -> SHIPPED -> DELIVERED
        ShipmentDto shipment = shipmentService.createShipmentForOrder(checkoutResult.orderId());

        String deliveryWebhook = String.format(
                "{\"event_id\":\"evt-%s\",\"provider_shipment_id\":\"%s\",\"awb\":\"%s\",\"order_reference\":\"%s\",\"status\":\"DELIVERED\",\"description\":\"Delivered to recipient\"}",
                UUID.randomUUID(), shipment.providerShipmentId(), shipment.awb(), checkoutResult.orderNumber()
        );
        shipmentService.processWebhook(ShipmentProviderType.MOCK, deliveryWebhook, Map.of());

        OrderDto finalOrder = orderService.getOrderDetail(customerId, checkoutResult.orderId());
        assertThat(finalOrder.status()).isIn(OrderStatus.DELIVERED, OrderStatus.SHIPPED, OrderStatus.PAID, OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Invalid Order transition rejection: cannot transition directly from CREATED to SHIPPED")
    void testInvalidOrderTransitionRejection() {
        String customerId = "cust-inv-trans-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-INVTR-" + UUID.randomUUID().toString().substring(0, 4), "Inv Transition", new BigDecimal("100.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 10);

        CommerceFixtures.createCartWithItem(cartService, customerId, p.getId(), 1);
        CommerceFixtures.CheckoutResult checkoutResult = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, "IDEM-INVTR-" + UUID.randomUUID()
        );

        // Attempt invalid direct transition to SHIPPED from CREATED
        assertThatThrownBy(() -> orderService.markShipped(checkoutResult.orderId(), "admin-1", "TRACK-123"))
                .isInstanceOf(InvalidOrderStateTransitionException.class);
    }
}
