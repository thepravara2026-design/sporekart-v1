package com.sporekart.modules.returns;

import com.sporekart.modules.returns.domain.InvalidReturnStateTransitionException;
import com.sporekart.modules.returns.domain.ReturnStatus;
import com.sporekart.modules.returns.domain.ReturnStateMachine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReturnStateMachineTest {

    @Test
    @DisplayName("Should allow valid transitions in normal flow")
    void testValidTransitions() {
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.REQUESTED, ReturnStatus.APPROVED));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.APPROVED, ReturnStatus.REVERSE_SHIPMENT_CREATED));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.REVERSE_SHIPMENT_CREATED, ReturnStatus.PICKUP_SCHEDULED));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.PICKUP_SCHEDULED, ReturnStatus.PICKUP_FAILED));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.PICKUP_FAILED, ReturnStatus.APPROVED));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.APPROVED, ReturnStatus.PICKUP_SCHEDULED));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.PICKUP_SCHEDULED, ReturnStatus.PICKED_UP));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.PICKED_UP, ReturnStatus.IN_TRANSIT));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.IN_TRANSIT, ReturnStatus.RECEIVED));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.RECEIVED, ReturnStatus.INSPECTION_PENDING));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.INSPECTION_PENDING, ReturnStatus.INSPECTED));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.INSPECTED, ReturnStatus.ACCEPTED));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.ACCEPTED, ReturnStatus.REFUND_PENDING));
        assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(ReturnStatus.REFUND_PENDING, ReturnStatus.REFUNDED));
    }

    @Test
    @DisplayName("Should allow same-state idempotency")
    void testSameStateIdempotency() {
        for (ReturnStatus status : ReturnStatus.values()) {
            assertDoesNotThrow(() -> ReturnStateMachine.validateTransition(status, status));
        }
    }

    @Test
    @DisplayName("Should reject illegal state transitions")
    void testIllegalTransitions() {
        assertThrows(InvalidReturnStateTransitionException.class,
                () -> ReturnStateMachine.validateTransition(ReturnStatus.REQUESTED, ReturnStatus.REFUNDED));
        assertThrows(InvalidReturnStateTransitionException.class,
                () -> ReturnStateMachine.validateTransition(ReturnStatus.CANCELLED, ReturnStatus.PICKED_UP));
        assertThrows(InvalidReturnStateTransitionException.class,
                () -> ReturnStateMachine.validateTransition(ReturnStatus.REJECTED, ReturnStatus.APPROVED));
        assertThrows(InvalidReturnStateTransitionException.class,
                () -> ReturnStateMachine.validateTransition(ReturnStatus.REFUNDED, ReturnStatus.REQUESTED));
    }
}
