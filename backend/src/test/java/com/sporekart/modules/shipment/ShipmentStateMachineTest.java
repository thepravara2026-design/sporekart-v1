package com.sporekart.modules.shipment;

import com.sporekart.modules.shipment.domain.InvalidShipmentStateTransitionException;
import com.sporekart.modules.shipment.domain.ShipmentStateMachine;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipmentStateMachineTest {

    @Test
    @DisplayName("Should allow valid forward transitions")
    void testValidTransitions() {
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.CREATED, ShipmentStatus.READY_FOR_BOOKING)).isTrue();
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.READY_FOR_BOOKING, ShipmentStatus.BOOKING_PENDING)).isTrue();
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.BOOKING_PENDING, ShipmentStatus.BOOKED)).isTrue();
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.BOOKED, ShipmentStatus.PICKED_UP)).isTrue();
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.PICKED_UP, ShipmentStatus.IN_TRANSIT)).isTrue();
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY)).isTrue();
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED)).isTrue();
    }

    @Test
    @DisplayName("Should allow same-state transition (idempotent)")
    void testSameStateTransition() {
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.BOOKED, ShipmentStatus.BOOKED)).isTrue();
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.DELIVERED, ShipmentStatus.DELIVERED)).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid state jumps")
    void testInvalidJumps() {
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.CREATED, ShipmentStatus.DELIVERED)).isFalse();
        assertThatThrownBy(() -> ShipmentStateMachine.validateTransition(ShipmentStatus.CREATED, ShipmentStatus.DELIVERED, "Direct jump"))
                .isInstanceOf(InvalidShipmentStateTransitionException.class);
    }

    @Test
    @DisplayName("Should correctly identify state regression")
    void testStateRegression() {
        assertThat(ShipmentStateMachine.isStateRegression(ShipmentStatus.DELIVERED, ShipmentStatus.IN_TRANSIT)).isTrue();
        assertThat(ShipmentStateMachine.isStateRegression(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.BOOKED)).isTrue();
        assertThat(ShipmentStateMachine.isStateRegression(ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY)).isFalse();
    }

    @Test
    @DisplayName("Terminal states should disallow further transitions")
    void testTerminalStates() {
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.DELIVERED, ShipmentStatus.IN_TRANSIT)).isFalse();
        assertThat(ShipmentStateMachine.isTransitionAllowed(ShipmentStatus.CANCELLED, ShipmentStatus.BOOKED)).isFalse();
    }
}
