package com.sporekart.modules.support.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupportStateMachineTest {

    @Test
    @DisplayName("Should allow valid support ticket state transitions")
    void shouldAllowValidTransitions() {
        assertThat(SupportStateMachine.isValidTransition(TicketStatus.OPEN, TicketStatus.ASSIGNED)).isTrue();
        assertThat(SupportStateMachine.isValidTransition(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS)).isTrue();
        assertThat(SupportStateMachine.isValidTransition(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED)).isTrue();
        assertThat(SupportStateMachine.isValidTransition(TicketStatus.RESOLVED, TicketStatus.CLOSED)).isTrue();
        assertThat(SupportStateMachine.isValidTransition(TicketStatus.RESOLVED, TicketStatus.REOPENED)).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for invalid support ticket state transitions")
    void shouldRejectInvalidTransitions() {
        assertThatThrownBy(() -> SupportStateMachine.validateTransition(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid support ticket status transition");
    }
}
