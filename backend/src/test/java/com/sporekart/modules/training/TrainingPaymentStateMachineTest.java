package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingPaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TRAINING 14 — Payment State Machine Hardening Tests.
 *
 * Exhaustively verifies every legal and illegal payment state transition.
 * A payment MUST NOT move backwards or jump states. Terminal states are final.
 */
@DisplayName("Training Payment State Machine — Full Transition Matrix")
class TrainingPaymentStateMachineTest {

    // ─── Legal Transitions ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Legal forward transitions")
    class LegalTransitions {

        @Test
        void pending_to_verified() {
            assertThat(TrainingPaymentStatus.PENDING.isValidTransitionTo(TrainingPaymentStatus.VERIFIED)).isTrue();
        }

        @Test
        void pending_to_failed() {
            assertThat(TrainingPaymentStatus.PENDING.isValidTransitionTo(TrainingPaymentStatus.FAILED)).isTrue();
        }

        @Test
        void pending_to_expired() {
            assertThat(TrainingPaymentStatus.PENDING.isValidTransitionTo(TrainingPaymentStatus.EXPIRED)).isTrue();
        }

        @Test
        void verified_to_enrollment_confirmed() {
            assertThat(TrainingPaymentStatus.VERIFIED.isValidTransitionTo(TrainingPaymentStatus.ENROLLMENT_CONFIRMED)).isTrue();
        }

        @Test
        void verified_to_enrollment_pending() {
            assertThat(TrainingPaymentStatus.VERIFIED.isValidTransitionTo(TrainingPaymentStatus.ENROLLMENT_PENDING)).isTrue();
        }

        @Test
        void verified_to_failed() {
            assertThat(TrainingPaymentStatus.VERIFIED.isValidTransitionTo(TrainingPaymentStatus.FAILED)).isTrue();
        }

        @Test
        void enrollment_pending_to_confirmed() {
            assertThat(TrainingPaymentStatus.ENROLLMENT_PENDING.isValidTransitionTo(TrainingPaymentStatus.ENROLLMENT_CONFIRMED)).isTrue();
        }

        @Test
        void enrollment_pending_to_failed() {
            assertThat(TrainingPaymentStatus.ENROLLMENT_PENDING.isValidTransitionTo(TrainingPaymentStatus.FAILED)).isTrue();
        }

        @Test
        void any_state_to_itself_is_idempotent() {
            for (TrainingPaymentStatus s : TrainingPaymentStatus.values()) {
                assertThat(s.isValidTransitionTo(s))
                        .as("Self-transition for %s must be idempotent", s)
                        .isTrue();
            }
        }
    }

    // ─── Illegal Transitions ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Illegal / backward transitions")
    class IllegalTransitions {

        @Test
        void terminal_confirmed_cannot_transition_to_anything() {
            for (TrainingPaymentStatus t : TrainingPaymentStatus.values()) {
                if (t == TrainingPaymentStatus.ENROLLMENT_CONFIRMED) continue; // self-transition is ok
                assertThat(TrainingPaymentStatus.ENROLLMENT_CONFIRMED.isValidTransitionTo(t))
                        .as("ENROLLMENT_CONFIRMED → %s must be rejected", t)
                        .isFalse();
            }
        }

        @Test
        void terminal_failed_cannot_transition_to_anything() {
            for (TrainingPaymentStatus t : TrainingPaymentStatus.values()) {
                if (t == TrainingPaymentStatus.FAILED) continue;
                assertThat(TrainingPaymentStatus.FAILED.isValidTransitionTo(t))
                        .as("FAILED → %s must be rejected", t)
                        .isFalse();
            }
        }

        @Test
        void terminal_expired_cannot_transition_to_anything() {
            for (TrainingPaymentStatus t : TrainingPaymentStatus.values()) {
                if (t == TrainingPaymentStatus.EXPIRED) continue;
                assertThat(TrainingPaymentStatus.EXPIRED.isValidTransitionTo(t))
                        .as("EXPIRED → %s must be rejected", t)
                        .isFalse();
            }
        }

        @Test
        void pending_cannot_jump_to_enrollment_confirmed() {
            assertThat(TrainingPaymentStatus.PENDING.isValidTransitionTo(TrainingPaymentStatus.ENROLLMENT_CONFIRMED)).isFalse();
        }

        @Test
        void pending_cannot_jump_to_enrollment_pending() {
            assertThat(TrainingPaymentStatus.PENDING.isValidTransitionTo(TrainingPaymentStatus.ENROLLMENT_PENDING)).isFalse();
        }

        @Test
        void verified_cannot_go_back_to_pending() {
            assertThat(TrainingPaymentStatus.VERIFIED.isValidTransitionTo(TrainingPaymentStatus.PENDING)).isFalse();
        }

        @Test
        void verified_cannot_go_to_expired() {
            assertThat(TrainingPaymentStatus.VERIFIED.isValidTransitionTo(TrainingPaymentStatus.EXPIRED)).isFalse();
        }

        @Test
        void enrollment_pending_cannot_go_back_to_pending() {
            assertThat(TrainingPaymentStatus.ENROLLMENT_PENDING.isValidTransitionTo(TrainingPaymentStatus.PENDING)).isFalse();
        }

        @Test
        void enrollment_pending_cannot_go_back_to_verified() {
            assertThat(TrainingPaymentStatus.ENROLLMENT_PENDING.isValidTransitionTo(TrainingPaymentStatus.VERIFIED)).isFalse();
        }
    }

    // ─── Terminal State Verification ──────────────────────────────────────────

    @Nested
    @DisplayName("Terminal state classification")
    class TerminalStates {

        @Test
        void enrollment_confirmed_is_terminal() {
            assertThat(TrainingPaymentStatus.ENROLLMENT_CONFIRMED.isTerminal()).isTrue();
        }

        @Test
        void failed_is_terminal() {
            assertThat(TrainingPaymentStatus.FAILED.isTerminal()).isTrue();
        }

        @Test
        void expired_is_terminal() {
            assertThat(TrainingPaymentStatus.EXPIRED.isTerminal()).isTrue();
        }

        @Test
        void pending_is_not_terminal() {
            assertThat(TrainingPaymentStatus.PENDING.isTerminal()).isFalse();
        }

        @Test
        void verified_is_not_terminal() {
            assertThat(TrainingPaymentStatus.VERIFIED.isTerminal()).isFalse();
        }

        @Test
        void enrollment_pending_is_not_terminal() {
            assertThat(TrainingPaymentStatus.ENROLLMENT_PENDING.isTerminal()).isFalse();
        }
    }

    // ─── Enrollment State Machine cross-check ────────────────────────────────

    @Nested
    @DisplayName("Enrollment state machine — terminal guard")
    class EnrollmentStateMachineTerminalGuard {

        @Test
        void completed_enrollment_cannot_transition() {
            for (EnrollmentStatus t : EnrollmentStatus.values()) {
                if (t == EnrollmentStatus.COMPLETED) continue;
                assertThat(EnrollmentStatus.COMPLETED.isValidTransitionTo(t))
                        .as("COMPLETED → %s must be rejected", t)
                        .isFalse();
            }
        }

        @Test
        void cancelled_enrollment_cannot_transition() {
            for (EnrollmentStatus t : EnrollmentStatus.values()) {
                if (t == EnrollmentStatus.CANCELLED) continue;
                assertThat(EnrollmentStatus.CANCELLED.isValidTransitionTo(t))
                        .as("CANCELLED → %s must be rejected", t)
                        .isFalse();
            }
        }

        @Test
        void rescheduled_enrollment_cannot_transition() {
            for (EnrollmentStatus t : EnrollmentStatus.values()) {
                if (t == EnrollmentStatus.RESCHEDULED) continue;
                assertThat(EnrollmentStatus.RESCHEDULED.isValidTransitionTo(t))
                        .as("RESCHEDULED → %s must be rejected", t)
                        .isFalse();
            }
        }

        @Test
        void pending_can_reach_confirmed_without_payment_for_free_programs() {
            assertThat(EnrollmentStatus.PENDING.isValidTransitionTo(EnrollmentStatus.CONFIRMED)).isTrue();
        }

        @Test
        void confirmed_can_be_cancelled() {
            assertThat(EnrollmentStatus.CONFIRMED.isValidTransitionTo(EnrollmentStatus.CANCELLED)).isTrue();
        }

        @Test
        void confirmed_can_be_rescheduled() {
            assertThat(EnrollmentStatus.CONFIRMED.isValidTransitionTo(EnrollmentStatus.RESCHEDULED)).isTrue();
        }

        @Test
        void active_can_be_completed() {
            assertThat(EnrollmentStatus.ACTIVE.isValidTransitionTo(EnrollmentStatus.COMPLETED)).isTrue();
        }
    }
}
