package com.sporekart.modules.training;

import com.sporekart.modules.training.application.CancellationEligibilityService;
import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.application.EnrollmentLifecycleService;
import com.sporekart.modules.training.application.TrainingCancellationService;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.TrainingPaymentStatus;
import com.sporekart.modules.training.domain.exception.CapacityExceededException;
import com.sporekart.modules.training.domain.exception.InvalidEnrollmentStateException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * TRAINING 14 — Security Acceptance Tests (Unit Level).
 *
 * Verifies IDOR protection, ownership enforcement, and authorization rules
 * at the service layer — without requiring full Spring context.
 *
 * Key invariants:
 *  1. Trainee A cannot access enrollment belonging to Trainee B (IDOR).
 *  2. Invalid/anonymous trainee ID cannot cancel an enrollment.
 *  3. Trainee ID always comes from the authenticated security context, not the request.
 *  4. Duplicate payment confirmation is idempotent (no second confirmation).
 */
@DisplayName("Training 14 — Security Acceptance Tests")
class TrainingSecurityAcceptanceTest {

    // ─── IDOR: Trainee cannot access another trainee's enrollment ─────────────

    @Nested
    @DisplayName("IDOR Protection — Cancellation")
    class IDORCancellationProtection {

        @Test
        @DisplayName("Trainee A cannot cancel enrollment owned by Trainee B")
        void traineeCannotCancelOtherTraineesEnrollment() {
            TrainingEnrollmentRepository enrollmentRepo = mock(TrainingEnrollmentRepository.class);
            TrainingBatchRepository batchRepo = mock(TrainingBatchRepository.class);
            EnrollmentLifecycleService lifecycleService = mock(EnrollmentLifecycleService.class);
            CapacityApplicationService capacityService = mock(CapacityApplicationService.class);
            CancellationEligibilityService eligibilityService = new CancellationEligibilityService();
            org.springframework.context.ApplicationEventPublisher eventPublisher = mock(org.springframework.context.ApplicationEventPublisher.class);

            TrainingCancellationService sut = new TrainingCancellationService(
                    enrollmentRepo, batchRepo, lifecycleService, capacityService, eligibilityService, eventPublisher);

            // Enrollment belongs to trainee-B
            TrainingEnrollment enrollmentB = TrainingEnrollment.create(
                    "batch-1", "trainee-B", "idem-key-1", "trainee-B");

            when(enrollmentRepo.findById("enr-B-id"))
                    .thenReturn(Optional.of(enrollmentB));

            // Trainee A attempts to cancel trainee B's enrollment
            assertThatThrownBy(() -> sut.cancelEnrollmentByTrainee("enr-B-id", "Cancelling", "trainee-A"))
                    .isInstanceOf(UnauthorizedEnrollmentAccessException.class)
                    .hasMessageContaining("do not own");
        }

        @Test
        @DisplayName("Anonymous / unauthenticated caller cannot cancel any enrollment")
        void anonymousCannotCancelEnrollment() {
            TrainingEnrollmentRepository enrollmentRepo = mock(TrainingEnrollmentRepository.class);
            TrainingBatchRepository batchRepo = mock(TrainingBatchRepository.class);
            EnrollmentLifecycleService lifecycleService = mock(EnrollmentLifecycleService.class);
            CapacityApplicationService capacityService = mock(CapacityApplicationService.class);
            CancellationEligibilityService eligibilityService = new CancellationEligibilityService();
            org.springframework.context.ApplicationEventPublisher eventPublisher = mock(org.springframework.context.ApplicationEventPublisher.class);

            TrainingCancellationService sut = new TrainingCancellationService(
                    enrollmentRepo, batchRepo, lifecycleService, capacityService, eligibilityService, eventPublisher);

            // null trainee ID (unauthenticated)
            assertThatThrownBy(() -> sut.cancelEnrollmentByTrainee("enr-1", "Reason", null))
                    .isInstanceOf(UnauthorizedEnrollmentAccessException.class);

            // blank trainee ID
            assertThatThrownBy(() -> sut.cancelEnrollmentByTrainee("enr-1", "Reason", "   "))
                    .isInstanceOf(UnauthorizedEnrollmentAccessException.class);

            // ANONYMOUS_TRAINEE sentinel value
            assertThatThrownBy(() -> sut.cancelEnrollmentByTrainee("enr-1", "Reason", "ANONYMOUS_TRAINEE"))
                    .isInstanceOf(UnauthorizedEnrollmentAccessException.class);
        }
    }

    // ─── Idempotent Cancellation ──────────────────────────────────────────────

    @Nested
    @DisplayName("Idempotent Cancellation")
    class IdempotentCancellation {

        @Test
        @DisplayName("Cancelling an already-CANCELLED enrollment is idempotent (no exception)")
        void cancellingAlreadyCancelledIsIdempotent() {
            TrainingEnrollmentRepository enrollmentRepo = mock(TrainingEnrollmentRepository.class);
            TrainingBatchRepository batchRepo = mock(TrainingBatchRepository.class);
            EnrollmentLifecycleService lifecycleService = mock(EnrollmentLifecycleService.class);
            CapacityApplicationService capacityService = mock(CapacityApplicationService.class);
            CancellationEligibilityService eligibilityService = new CancellationEligibilityService();
            org.springframework.context.ApplicationEventPublisher eventPublisher = mock(org.springframework.context.ApplicationEventPublisher.class);

            TrainingCancellationService sut = new TrainingCancellationService(
                    enrollmentRepo, batchRepo, lifecycleService, capacityService, eligibilityService, eventPublisher);

            // Create an already-cancelled enrollment
            TrainingEnrollment alreadyCancelled = new TrainingEnrollment(
                    "enr-already-cancelled", "ENR-2026-TEST001",
                    "batch-1", "trainee-A", EnrollmentStatus.CANCELLED,
                    null, java.math.BigDecimal.ZERO, "INR",
                    Instant.now(), null, null, null,
                    Instant.now(), Instant.now(), "trainee-A", "trainee-A", null
            );
            when(enrollmentRepo.findById("enr-already-cancelled"))
                    .thenReturn(Optional.of(alreadyCancelled));

            TrainingEnrollment result = sut.cancelEnrollmentByTrainee("enr-already-cancelled", "Duplicate cancel", "trainee-A");

            // Should return the existing cancelled enrollment, no state change
            org.junit.jupiter.api.Assertions.assertEquals(EnrollmentStatus.CANCELLED, result.getStatus());
            verify(lifecycleService, never()).transitionStatus(any(), any(), any(), any());
        }
    }

    // ─── Capacity Domain Security ─────────────────────────────────────────────

    @Nested
    @DisplayName("Capacity domain — overbooking prevention")
    class CapacityOverbookingPrevention {

        @Test
        @DisplayName("Capacity.allocateSeat() on full batch throws — overbooking is impossible")
        void allocateSeatOnFullBatchThrows() {
            Capacity fullCapacity = new Capacity(5, 5);
            assertThatThrownBy(fullCapacity::allocateSeat)
                    .isInstanceOf(CapacityExceededException.class)
                    .hasMessageContaining("FULL");
        }

        @Test
        @DisplayName("Capacity domain enforces occupied > total is always rejected at construction")
        void occupiedCannotExceedTotalAtConstruction() {
            assertThatThrownBy(() -> new Capacity(10, 11))
                    .isInstanceOf(CapacityExceededException.class);
        }

        @Test
        @DisplayName("Capacity.releaseSeat() on 0-occupied batch is safe (no underflow)")
        void releaseSeatOnEmptyBatchIsSafe() {
            Capacity empty = new Capacity(10, 0);
            Capacity result = empty.releaseSeat();
            org.junit.jupiter.api.Assertions.assertEquals(0, result.getOccupiedSeats());
        }
    }

    // ─── Payment State Machine Security ──────────────────────────────────────

    @Nested
    @DisplayName("Payment state machine — illegal transitions rejected")
    class PaymentStateMachineSecurity {

        @Test
        @DisplayName("ENROLLMENT_CONFIRMED payment cannot be rolled back to PENDING")
        void confirmedPaymentCannotRollBackToPending() {
            assertThatThrownBy(() -> {
                boolean valid = TrainingPaymentStatus.ENROLLMENT_CONFIRMED.isValidTransitionTo(TrainingPaymentStatus.PENDING);
                if (!valid) throw new IllegalStateException("Illegal payment state transition rejected");
            }).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("FAILED payment cannot be reactivated to VERIFIED")
        void failedPaymentCannotBeReactivated() {
            boolean valid = TrainingPaymentStatus.FAILED.isValidTransitionTo(TrainingPaymentStatus.VERIFIED);
            org.junit.jupiter.api.Assertions.assertFalse(valid, "FAILED payment must not transition to VERIFIED");
        }

        @Test
        @DisplayName("PENDING payment cannot jump directly to ENROLLMENT_CONFIRMED")
        void pendingPaymentCannotJumpToConfirmed() {
            boolean valid = TrainingPaymentStatus.PENDING.isValidTransitionTo(TrainingPaymentStatus.ENROLLMENT_CONFIRMED);
            org.junit.jupiter.api.Assertions.assertFalse(valid, "PENDING must go through VERIFIED first");
        }
    }

    // ─── Enrollment State Machine Security ───────────────────────────────────

    @Nested
    @DisplayName("Enrollment state machine — terminal guard")
    class EnrollmentStateMachineSecurity {

        @Test
        @DisplayName("COMPLETED enrollment cannot be cancelled (certificate already issued)")
        void completedEnrollmentCannotBeCancelled() {
            TrainingEnrollment completed = new TrainingEnrollment(
                    "enr-completed", "ENR-2026-COMP",
                    "batch-1", "trainee-A", EnrollmentStatus.COMPLETED,
                    "payment-ref-1", java.math.BigDecimal.TEN, "INR",
                    Instant.now(), Instant.now(), Instant.now(), Instant.now(),
                    Instant.now(), Instant.now(), "trainee-A", "admin", null
            );
            assertThatThrownBy(completed::cancel)
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("CANCELLED enrollment cannot be confirmed (terminal state is final)")
        void cancelledEnrollmentCannotBeConfirmed() {
            TrainingEnrollment cancelled = new TrainingEnrollment(
                    "enr-cancelled", "ENR-2026-CANC",
                    "batch-1", "trainee-A", EnrollmentStatus.CANCELLED,
                    null, java.math.BigDecimal.ZERO, "INR",
                    Instant.now(), null, null, null,
                    Instant.now(), Instant.now(), "trainee-A", "trainee-A", null
            );
            // Attempting to confirm a CANCELLED enrollment must be rejected
            assertThatThrownBy(() -> cancelled.confirm("pay-ref"))
                    .isInstanceOf(InvalidEnrollmentStateException.class);
        }
    }
}
