package com.sporekart.modules.order;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.exception.OrderNotCancellableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderDomainTest {

    @Test
    @DisplayName("Should create Order aggregate with CREATED initial status and immutable items")
    void testOrderCreation() {
        AddressSnapshot address = new AddressSnapshot(
                "John Doe", "+919876543210", "123 Green Street", "Apt 4B",
                "Bengaluru", "Karnataka", "560001", "India"
        );

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderItem item = new OrderItem(
                UUID.randomUUID(), orderId, productId, null, "SKU-001",
                "Oyster Mushroom Spawn", null, new BigDecimal("499.00"), 2,
                BigDecimal.ZERO, new BigDecimal("179.64"), new BigDecimal("998.00"),
                new BigDecimal("1177.64"), OffsetDateTime.now()
        );

        Order order = new Order(
                orderId, "SPK-20260815-100001", "cust-123", OrderStatus.CREATED,
                "INR", new BigDecimal("998.00"), BigDecimal.ZERO, new BigDecimal("179.64"),
                new BigDecimal("50.00"), new BigDecimal("1227.64"), "KEY-123",
                address, "Handle with care", List.of(item), OffsetDateTime.now(), OffsetDateTime.now()
        );

        assertEquals(orderId, order.getId());
        assertEquals("SPK-20260815-100001", order.getOrderNumber());
        assertEquals("cust-123", order.getCustomerId());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals("INR", order.getCurrency());
        assertEquals(new BigDecimal("1227.64"), order.getGrandTotal());
        assertEquals(1, order.getItems().size());
        assertTrue(order.isCancellable());
    }

    @Test
    @DisplayName("Should allow cancelling CREATED order and transition to CANCELLED status")
    void testCancelOrderSuccess() {
        AddressSnapshot address = new AddressSnapshot("John", "9999999999", "Line 1", null, "City", "State", "123456", "India");
        Order order = Order.createNewOrder(
                "SPK-001", "cust-1", "INR", new BigDecimal("100.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100.00"), null, address, null, List.of()
        );

        assertTrue(order.isCancellable());
        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertFalse(order.isCancellable());
    }

    @Test
    @DisplayName("Should reject cancelling non-cancellable order status (e.g. SHIPPED)")
    void testCancelOrderFailure() {
        AddressSnapshot address = new AddressSnapshot("John", "9999999999", "Line 1", null, "City", "State", "123456", "India");
        Order order = new Order(
                UUID.randomUUID(), "SPK-002", "cust-1", OrderStatus.SHIPPED, "INR",
                new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("100.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        assertFalse(order.isCancellable());
        assertThrows(OrderNotCancellableException.class, order::cancel);
    }
}
