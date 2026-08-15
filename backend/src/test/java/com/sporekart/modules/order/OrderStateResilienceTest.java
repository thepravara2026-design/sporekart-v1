package com.sporekart.modules.order;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateResilienceTest {

    @Test
    @DisplayName("Terminal order states (CANCELLED, COMPLETED, EXPIRED) must reject regressive out-of-order status transitions")
    void shouldRejectTransitionsOnTerminalOrders() {
        AddressSnapshot addr = new AddressSnapshot("John Doe", "9999999999", "123 Main St", "Suite 4", "Bangalore", "Karnataka", "560001", "IN");
        OffsetDateTime now = OffsetDateTime.now();

        Order order = new Order(
                UUID.randomUUID(),
                "ORD-TERM-101",
                "CUST-101",
                OrderStatus.CREATED,
                "INR",
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                new BigDecimal("180.00"),
                new BigDecimal("50.00"),
                new BigDecimal("1230.00"),
                "IDEMP-101",
                addr,
                "Test notes",
                List.of(),
                0L,
                now,
                now
        );

        order.transitionToPaymentPending();
        order.markPaid();
        order.cancel(); // Transition to CANCELLED (Terminal)

        assertThat(order.isTerminal()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        // Attempt out-of-order transition after cancellation must throw InvalidOrderStateTransitionException
        assertThatThrownBy(order::markShipped)
                .isInstanceOf(InvalidOrderStateTransitionException.class);

        assertThatThrownBy(order::markDelivered)
                .isInstanceOf(InvalidOrderStateTransitionException.class);

        // Status remains CANCELLED
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
