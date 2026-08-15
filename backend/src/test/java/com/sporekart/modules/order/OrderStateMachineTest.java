package com.sporekart.modules.order;

import com.sporekart.modules.order.domain.OrderStateMachine;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStateMachineTest {

    @Test
    @DisplayName("Valid transitions should be allowed by state machine")
    void testValidTransitions() {
        UUID orderId = UUID.randomUUID();

        // CREATED -> PAYMENT_PENDING -> CONFIRMED -> PROCESSING -> READY_FOR_FULFILMENT -> SHIPPED -> OUT_FOR_DELIVERY -> DELIVERED -> COMPLETED
        assertDoesNotThrow(() -> OrderStateMachine.validateTransition(orderId, OrderStatus.CREATED, OrderStatus.PAYMENT_PENDING));
        assertDoesNotThrow(() -> OrderStateMachine.validateTransition(orderId, OrderStatus.PAYMENT_PENDING, OrderStatus.CONFIRMED));
        assertDoesNotThrow(() -> OrderStateMachine.validateTransition(orderId, OrderStatus.CONFIRMED, OrderStatus.PROCESSING));
        assertDoesNotThrow(() -> OrderStateMachine.validateTransition(orderId, OrderStatus.PROCESSING, OrderStatus.READY_FOR_FULFILMENT));
        assertDoesNotThrow(() -> OrderStateMachine.validateTransition(orderId, OrderStatus.READY_FOR_FULFILMENT, OrderStatus.SHIPPED));
        assertDoesNotThrow(() -> OrderStateMachine.validateTransition(orderId, OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY));
        assertDoesNotThrow(() -> OrderStateMachine.validateTransition(orderId, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED));
        assertDoesNotThrow(() -> OrderStateMachine.validateTransition(orderId, OrderStatus.DELIVERED, OrderStatus.COMPLETED));
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    @DisplayName("Self-transitions (idempotency) should always be allowed")
    void testIdempotentSelfTransitions(OrderStatus status) {
        assertTrue(OrderStateMachine.isTransitionAllowed(status, status));
        assertDoesNotThrow(() -> OrderStateMachine.validateTransition(UUID.randomUUID(), status, status));
    }

    @Test
    @DisplayName("Terminal states (CANCELLED, EXPIRED, COMPLETED) must reject outward transitions")
    void testTerminalStateRejection() {
        UUID orderId = UUID.randomUUID();

        assertThrows(InvalidOrderStateTransitionException.class, () -> OrderStateMachine.validateTransition(orderId, OrderStatus.CANCELLED, OrderStatus.CONFIRMED));
        assertThrows(InvalidOrderStateTransitionException.class, () -> OrderStateMachine.validateTransition(orderId, OrderStatus.EXPIRED, OrderStatus.PAYMENT_PENDING));
        assertThrows(InvalidOrderStateTransitionException.class, () -> OrderStateMachine.validateTransition(orderId, OrderStatus.COMPLETED, OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("Illegal transition jumps (e.g. CREATED -> DELIVERED) must throw InvalidOrderStateTransitionException")
    void testIllegalTransitionJumps() {
        UUID orderId = UUID.randomUUID();

        assertThrows(InvalidOrderStateTransitionException.class, () -> OrderStateMachine.validateTransition(orderId, OrderStatus.CREATED, OrderStatus.DELIVERED));
        assertThrows(InvalidOrderStateTransitionException.class, () -> OrderStateMachine.validateTransition(orderId, OrderStatus.PAYMENT_PENDING, OrderStatus.SHIPPED));
    }
}
