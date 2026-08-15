package com.sporekart.modules.order;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderDomainTest {

    @Test
    @DisplayName("Order aggregate should initialize with CREATED status and enforce invariants")
    void testOrderInitialization() {
        AddressSnapshot address = new AddressSnapshot("John", "123", "Line1", null, "City", "State", "100", "India");
        Order order = Order.createNewOrder(
                "SPK-10001", "cust-100", "INR", new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1000.00"),
                "idem-100", address, "Notes", List.of()
        );

        assertEquals("SPK-10001", order.getOrderNumber());
        assertEquals("cust-100", order.getCustomerId());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertTrue(order.isCancellable());
        assertFalse(order.isTerminal());
    }

    @Test
    @DisplayName("Order state transition methods should delegate to state machine correctly")
    void testStateTransitions() {
        AddressSnapshot address = new AddressSnapshot("John", "123", "Line1", null, "City", "State", "100", "India");
        Order order = Order.createNewOrder(
                "SPK-10002", "cust-100", "INR", new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1000.00"),
                null, address, null, List.of()
        );

        order.transitionToPaymentPending();
        assertEquals(OrderStatus.PAYMENT_PENDING, order.getStatus());

        order.markPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());

        order.startProcessing();
        assertEquals(OrderStatus.PROCESSING, order.getStatus());

        order.markReadyForFulfilment();
        assertEquals(OrderStatus.READY_FOR_FULFILMENT, order.getStatus());

        order.markShipped();
        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        order.markOutForDelivery();
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());

        order.markDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());

        order.markCompleted();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertTrue(order.isTerminal());

        // Reviving COMPLETED order must fail
        assertThrows(InvalidOrderStateTransitionException.class, order::cancel);
    }
}
