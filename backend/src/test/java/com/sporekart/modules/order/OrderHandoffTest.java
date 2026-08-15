package com.sporekart.modules.order;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderHandoffTest {

    @Test
    @DisplayName("Complete Order lifecycle end-to-end handoff contract")
    void testFullOrderLifecycleHandoff() {
        AddressSnapshot address = new AddressSnapshot("Jane", "987", "Avenue", null, "Metropolis", "State", "500", "India");
        Order order = Order.createNewOrder(
                "SPK-HANDOFF-001", "cust-handoff", "INR", new BigDecimal("2500.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("2500.00"),
                "idem-handoff-1", address, "Deliver before 5 PM", List.of()
        );

        assertEquals(OrderStatus.CREATED, order.getStatus());

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
    }
}
