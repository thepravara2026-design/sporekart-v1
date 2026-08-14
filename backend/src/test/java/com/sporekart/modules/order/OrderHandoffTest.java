package com.sporekart.modules.order;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderHandoffTest {

    @Test
    @DisplayName("Sprint 3D Inventory Handoff: Order exposes product IDs, SKUs, and quantities for reservation")
    void testInventoryHandoffContract() {
        UUID orderId = UUID.randomUUID();
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        OrderItem item1 = new OrderItem(
                UUID.randomUUID(), orderId, productId1, null, "SKU-A", "Product A", null,
                new BigDecimal("100.00"), 3, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("300.00"),
                new BigDecimal("300.00"), OffsetDateTime.now()
        );
        OrderItem item2 = new OrderItem(
                UUID.randomUUID(), orderId, productId2, null, "SKU-B", "Product B", null,
                new BigDecimal("200.00"), 1, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("200.00"),
                new BigDecimal("200.00"), OffsetDateTime.now()
        );

        AddressSnapshot address = new AddressSnapshot("Test", "123", "Line", null, "City", "State", "100", "India");
        Order order = new Order(
                orderId, "SPK-100", "cust-1", OrderStatus.CREATED, "INR",
                new BigDecimal("500.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("500.00"), null, address, null, List.of(item1, item2),
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        // Verify Sprint 3D can extract reservation payload cleanly
        List<OrderItem> items = order.getItems();
        assertEquals(2, items.size());
        assertEquals("SKU-A", items.get(0).getSku());
        assertEquals(3, items.get(0).getQuantity());
        assertEquals("SKU-B", items.get(1).getSku());
        assertEquals(1, items.get(1).getQuantity());
    }

    @Test
    @DisplayName("Sprint 3E Payment Handoff: Order exposes orderId, orderNumber, grandTotal, currency, and customerId")
    void testPaymentHandoffContract() {
        UUID orderId = UUID.randomUUID();
        AddressSnapshot address = new AddressSnapshot("Test", "123", "Line", null, "City", "State", "100", "India");

        Order order = new Order(
                orderId, "SPK-PAY-001", "cust-88", OrderStatus.CREATED, "INR",
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("180.00"), new BigDecimal("50.00"),
                new BigDecimal("1230.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        // Verify Sprint 3E can read payment target values
        assertEquals(orderId, order.getId());
        assertEquals("SPK-PAY-001", order.getOrderNumber());
        assertEquals("cust-88", order.getCustomerId());
        assertEquals("INR", order.getCurrency());
        assertEquals(new BigDecimal("1230.00"), order.getGrandTotal());
    }

    @Test
    @DisplayName("Sprint 3F Order State Machine Handoff: Order status transitions and cancellability boundary are extensible")
    void testStateMachineHandoffContract() {
        AddressSnapshot address = new AddressSnapshot("Test", "123", "Line", null, "City", "State", "100", "India");
        Order order = Order.createNewOrder(
                "SPK-SM-001", "cust-1", "INR", new BigDecimal("100.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100.00"), null, address, null, List.of()
        );

        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertTrue(order.isCancellable());
    }
}
