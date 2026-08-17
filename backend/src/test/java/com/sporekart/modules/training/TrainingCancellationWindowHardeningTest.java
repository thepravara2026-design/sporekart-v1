package com.sporekart.modules.training;

import com.sporekart.modules.training.application.CancellationEligibilityService;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.exception.CancellationWindowExpiredException;
import com.sporekart.modules.training.domain.exception.RescheduleWindowExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * TRAINING 14 — Cancellation & Rescheduling Window Hardening Tests.
 *
 * Tests exact boundary conditions for both TRAINEE (2-day) and ADMIN (7-day) windows.
 *
 * Business Rules (must not change):
 *  - Trainee may cancel/reschedule up to 2 days before the scheduled training date.
 *  - Admin may cancel/reschedule up to 7 days before the scheduled training date.
 *
 * Boundary cases tested:
 *  - Exactly at window boundary (T-2d or T-7d): ALLOWED
 *  - Just past window boundary (T-2d + 1 second or T-7d + 1 second): REJECTED
 *  - Well within window (T-10d): ALLOWED
 *  - Past training date: REJECTED
 */
@DisplayName("Training 14 — Cancellation & Rescheduling Window Hardening")
class TrainingCancellationWindowHardeningTest {

    private CancellationEligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        eligibilityService = new CancellationEligibilityService();
    }

    private TrainingBatch batchStartingAt(Instant startDate) {
        return new TrainingBatch(
                "batch-window-test",
                "program-1",
                "BATCH-WINDOW-" + System.nanoTime(),
                startDate,
                startDate.plus(1, ChronoUnit.DAYS),
                new Capacity(20, 0),
                BatchStatus.ACTIVE,
                List.of(),
                Instant.now(),
                Instant.now()
        );
    }

    // ─── Trainee Cancellation ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Trainee cancellation: 2-day window")
    class TraineeCancellation {

        @Test
        @DisplayName("ALLOWED: training starts in 10 days (well within 2-day window)")
        void traineeCanCancelWellBeforeWindow() {
            TrainingBatch batch = batchStartingAt(Instant.now().plus(10, ChronoUnit.DAYS));
            assertThatNoException()
                    .isThrownBy(() -> eligibilityService.validateTraineeCancellationEligibility(batch));
        }

        @Test
        @DisplayName("ALLOWED: training starts in exactly 2 days + 1 hour (just inside window)")
        void traineeCanCancelJustInsideWindow() {
            // 2 days + 1 hour from now → deadline is now + 2d, so this is still within window
            TrainingBatch batch = batchStartingAt(Instant.now().plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS));
            assertThatNoException()
                    .isThrownBy(() -> eligibilityService.validateTraineeCancellationEligibility(batch));
        }

        @Test
        @DisplayName("REJECTED: training starts in exactly 1 day 23 hours (just past 2-day deadline)")
        void traineeCannotCancelJustPastWindow() {
            // 1 day 23 hours from now → deadline was 2 days before start, which is now - 1h
            TrainingBatch batch = batchStartingAt(
                    Instant.now().plus(1, ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS));
            assertThatThrownBy(() -> eligibilityService.validateTraineeCancellationEligibility(batch))
                    .isInstanceOf(CancellationWindowExpiredException.class)
                    .hasMessageContaining("2");
        }

        @Test
        @DisplayName("REJECTED: training is tomorrow (1 day away — past the 2-day window)")
        void traineeCannotCancelOneDayBefore() {
            TrainingBatch batch = batchStartingAt(Instant.now().plus(1, ChronoUnit.DAYS));
            assertThatThrownBy(() -> eligibilityService.validateTraineeCancellationEligibility(batch))
                    .isInstanceOf(CancellationWindowExpiredException.class);
        }

        @Test
        @DisplayName("REJECTED: training has already started (past date)")
        void traineeCannotCancelPastTraining() {
            TrainingBatch batch = batchStartingAt(Instant.now().minus(1, ChronoUnit.HOURS));
            assertThatThrownBy(() -> eligibilityService.validateTraineeCancellationEligibility(batch))
                    .isInstanceOf(CancellationWindowExpiredException.class);
        }
    }

    // ─── Admin Cancellation ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Admin cancellation: 7-day window")
    class AdminCancellation {

        @Test
        @DisplayName("ALLOWED: training starts in 30 days (well within 7-day window)")
        void adminCanCancelWellBeforeWindow() {
            TrainingBatch batch = batchStartingAt(Instant.now().plus(30, ChronoUnit.DAYS));
            assertThatNoException()
                    .isThrownBy(() -> eligibilityService.validateAdminCancellationEligibility(batch));
        }

        @Test
        @DisplayName("ALLOWED: training starts in exactly 7 days + 1 hour (just inside window)")
        void adminCanCancelJustInsideWindow() {
            TrainingBatch batch = batchStartingAt(Instant.now().plus(7, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS));
            assertThatNoException()
                    .isThrownBy(() -> eligibilityService.validateAdminCancellationEligibility(batch));
        }

        @Test
        @DisplayName("REJECTED: training starts in 6 days 23 hours (just past 7-day deadline)")
        void adminCannotCancelJustPastWindow() {
            TrainingBatch batch = batchStartingAt(
                    Instant.now().plus(6, ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS));
            assertThatThrownBy(() -> eligibilityService.validateAdminCancellationEligibility(batch))
                    .isInstanceOf(CancellationWindowExpiredException.class)
                    .hasMessageContaining("7");
        }

        @Test
        @DisplayName("REJECTED: training starts in 3 days (well past 7-day window)")
        void adminCannotCancelThreeDaysBefore() {
            TrainingBatch batch = batchStartingAt(Instant.now().plus(3, ChronoUnit.DAYS));
            assertThatThrownBy(() -> eligibilityService.validateAdminCancellationEligibility(batch))
                    .isInstanceOf(CancellationWindowExpiredException.class);
        }

        @Test
        @DisplayName("REJECTED: training has already started (past date)")
        void adminCannotCancelPastTraining() {
            TrainingBatch batch = batchStartingAt(Instant.now().minus(1, ChronoUnit.HOURS));
            assertThatThrownBy(() -> eligibilityService.validateAdminCancellationEligibility(batch))
                    .isInstanceOf(CancellationWindowExpiredException.class);
        }
    }

    // ─── Trainee Reschedule ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Trainee reschedule: 2-day window")
    class TraineeReschedule {

        @Test
        @DisplayName("ALLOWED: training starts in 10 days")
        void traineeCanRescheduleWellBeforeWindow() {
            TrainingBatch batch = batchStartingAt(Instant.now().plus(10, ChronoUnit.DAYS));
            assertThatNoException()
                    .isThrownBy(() -> eligibilityService.validateTraineeRescheduleEligibility(batch));
        }

        @Test
        @DisplayName("REJECTED: training starts tomorrow (past 2-day window)")
        void traineeCannotRescheduleTooLate() {
            TrainingBatch batch = batchStartingAt(Instant.now().plus(1, ChronoUnit.DAYS));
            assertThatThrownBy(() -> eligibilityService.validateTraineeRescheduleEligibility(batch))
                    .isInstanceOf(RescheduleWindowExpiredException.class);
        }
    }

    // ─── Admin Reschedule ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Admin reschedule: 7-day window")
    class AdminReschedule {

        @Test
        @DisplayName("ALLOWED: training starts in 14 days")
        void adminCanRescheduleWellBeforeWindow() {
            TrainingBatch batch = batchStartingAt(Instant.now().plus(14, ChronoUnit.DAYS));
            assertThatNoException()
                    .isThrownBy(() -> eligibilityService.validateAdminRescheduleEligibility(batch));
        }

        @Test
        @DisplayName("REJECTED: training starts in 5 days (past 7-day window)")
        void adminCannotRescheduleTooLate() {
            TrainingBatch batch = batchStartingAt(Instant.now().plus(5, ChronoUnit.DAYS));
            assertThatThrownBy(() -> eligibilityService.validateAdminRescheduleEligibility(batch))
                    .isInstanceOf(RescheduleWindowExpiredException.class);
        }
    }
}
