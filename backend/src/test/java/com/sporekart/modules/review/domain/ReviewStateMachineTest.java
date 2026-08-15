package com.sporekart.modules.review.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewStateMachineTest {

    @Test
    @DisplayName("Should allow valid review moderation state transitions")
    void shouldAllowValidTransitions() {
        assertThat(ReviewStateMachine.isValidTransition(ReviewStatus.PENDING_MODERATION, ReviewStatus.APPROVED)).isTrue();
        assertThat(ReviewStateMachine.isValidTransition(ReviewStatus.PENDING_MODERATION, ReviewStatus.REJECTED)).isTrue();
        assertThat(ReviewStateMachine.isValidTransition(ReviewStatus.APPROVED, ReviewStatus.FLAGGED)).isTrue();
        assertThat(ReviewStateMachine.isValidTransition(ReviewStatus.FLAGGED, ReviewStatus.APPROVED)).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for invalid review status transitions")
    void shouldRejectInvalidTransitions() {
        assertThatThrownBy(() -> ReviewStateMachine.validateTransition(ReviewStatus.REJECTED, ReviewStatus.APPROVED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid review status transition");
    }
}
