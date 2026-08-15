package com.sporekart.modules.order.domain;

import com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderLifecycleDomainTest {

    private Order createTestOrder(OrderStatus initialStatus) {
        AddressSnapshot address = new AddressSnapshot("Jane Doe", "9876543210", "123 Main St", null, "Bengaluru", "Karnataka", "560001", "India");
        OrderItem item = new OrderItem(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, "SKU-4A-01", "Test Product", null,
                new BigDecimal("500.00"), 2, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1000.00"), new BigDecimal("1000.00"), OffsetDateTime.now()
        );
        return new Order(
                UUID.randomUUID(), "ORD-4A-100", "cust-4a-test", initialStatus, "INR",
                new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1000.00"),
                "IDEM-4A-KEY", address, "Test Notes", List.of(item), 0L, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("Valid Full Order Lifecycle Transitions Flow Smoothly")
    void testValidLifecycleTransitions() {
        Order order = createTestOrder(OrderStatus.CREATED);

        order.markPaid();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        order.startProcessing();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);

        order.markReadyForFulfilment();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY_FOR_FULFILMENT);

        order.markShipped();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        order.markOutForDelivery();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);

        order.markDelivered();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        order.markCompleted();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("Invalid State Transition Throws InvalidOrderStateTransitionException")
    void testInvalidTransitionThrowsException() {
        Order order = createTestOrder(OrderStatus.CREATED);

        assertThatThrownBy(order::markDelivered)
                .isInstanceOf(InvalidOrderStateTransitionException.class);
    }

    @Test
    @DisplayName("Terminal States Reject Outward Transitions")
    void testTerminalStateRejectsTransitions() {
        Order cancelledOrder = createTestOrder(OrderStatus.CANCELLED);
        assertThat(cancelledOrder.isTerminal()).isTrue();

        assertThatThrownBy(cancelledOrder::markPaid)
                .isInstanceOf(InvalidOrderStateTransitionException.class);

        Order completedOrder = createTestOrder(OrderStatus.COMPLETED);
        assertThat(completedOrder.isTerminal()).isTrue();

        assertThatThrownBy(completedOrder::cancel)
                .isInstanceOf(InvalidOrderStateTransitionException.class);
    }

    @Test
    @DisplayName("Idempotent Status Change Does Not Mutate State or Throw")
    void testIdempotentStatusChange() {
        Order order = createTestOrder(OrderStatus.PROCESSING);

        order.startProcessing(); // Same state
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }
}
