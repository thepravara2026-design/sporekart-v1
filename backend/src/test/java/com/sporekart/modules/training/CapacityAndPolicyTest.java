package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.CancellationWindowExpiredException;
import com.sporekart.modules.training.domain.exception.CapacityExceededException;
import com.sporekart.modules.training.domain.exception.RescheduleWindowExpiredException;
import com.sporekart.modules.training.domain.policy.CancellationPolicy;
import com.sporekart.modules.training.domain.policy.CapacityPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CapacityAndPolicyTest {

    @Test
    @DisplayName("Capacity should enforce invariants and correct available seat calculations")
    void testCapacityInvariants() {
        Capacity capacity = Capacity.of(10);
        assertEquals(10, capacity.getTotalCapacity());
        assertEquals(0, capacity.getOccupiedSeats());
        assertEquals(10, capacity.getAvailableSeats());
        assertFalse(capacity.isFull());

        Capacity allocated = capacity.allocateSeat();
        assertEquals(1, allocated.getOccupiedSeats());
        assertEquals(9, allocated.getAvailableSeats());

        assertThrows(IllegalArgumentException.class, () -> new Capacity(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Capacity(10, -1));
        assertThrows(CapacityExceededException.class, () -> new Capacity(5, 6));
    }

    @Test
    @DisplayName("Capacity full condition should prevent further allocation")
    void testCapacityFull() {
        Capacity capacity = new Capacity(2, 2);
        assertTrue(capacity.isFull());

        assertThrows(CapacityExceededException.class, capacity::allocateSeat);
    }

    @Test
    @DisplayName("CancellationPolicy should correctly enforce Admin 7-day and Trainee 2-day rules")
    void testCancellationPolicyWindows() {
        CancellationPolicy policy = CancellationPolicy.defaultConfig();
        assertEquals(7, policy.getAdminWindowDays());
        assertEquals(2, policy.getTraineeWindowDays());

        Instant scheduledAt = Instant.parse("2026-09-01T10:00:00Z");

        // 10 days before: both admin and trainee can cancel
        Instant tenDaysBefore = scheduledAt.minus(Duration.ofDays(10));
        assertTrue(policy.canAdminCancelOrReschedule(scheduledAt, tenDaysBefore));
        assertTrue(policy.canTraineeCancelOrReschedule(scheduledAt, tenDaysBefore));
        assertDoesNotThrow(() -> policy.validateAdminCancellation(scheduledAt, tenDaysBefore));
        assertDoesNotThrow(() -> policy.validateTraineeCancellation(scheduledAt, tenDaysBefore));

        // 5 days before: admin can cancel (>= 7 days? 5 days before is past 7-day deadline, so admin CANNOT cancel!), trainee can cancel (5 days >= 2 days)
        Instant fiveDaysBefore = scheduledAt.minus(Duration.ofDays(5));
        assertFalse(policy.canAdminCancelOrReschedule(scheduledAt, fiveDaysBefore));
        assertTrue(policy.canTraineeCancelOrReschedule(scheduledAt, fiveDaysBefore));
        assertThrows(CancellationWindowExpiredException.class, () -> policy.validateAdminCancellation(scheduledAt, fiveDaysBefore));
        assertDoesNotThrow(() -> policy.validateTraineeCancellation(scheduledAt, fiveDaysBefore));

        // 1 day before: neither can cancel
        Instant oneDayBefore = scheduledAt.minus(Duration.ofDays(1));
        assertFalse(policy.canAdminCancelOrReschedule(scheduledAt, oneDayBefore));
        assertFalse(policy.canTraineeCancelOrReschedule(scheduledAt, oneDayBefore));
        assertThrows(CancellationWindowExpiredException.class, () -> policy.validateAdminCancellation(scheduledAt, oneDayBefore));
        assertThrows(CancellationWindowExpiredException.class, () -> policy.validateTraineeCancellation(scheduledAt, oneDayBefore));
        assertThrows(RescheduleWindowExpiredException.class, () -> policy.validateTraineeReschedule(scheduledAt, oneDayBefore));
    }

    @Test
    @DisplayName("CapacityPolicy should govern seat allocation and batch status transitions")
    void testCapacityPolicyTransitions() {
        CapacityPolicy capacityPolicy = new CapacityPolicy();
        Capacity initial = Capacity.of(1);

        Capacity allocated = capacityPolicy.allocateSeat(initial, BatchStatus.PLANNED);
        assertTrue(allocated.isFull());

        BatchStatus status = capacityPolicy.evaluateBatchStatus(BatchStatus.PLANNED, allocated);
        assertEquals(BatchStatus.FULL, status);

        assertThrows(BatchFullException.class, () -> capacityPolicy.allocateSeat(allocated, status));

        Capacity expanded = capacityPolicy.updateCapacity(allocated, 5);
        assertFalse(expanded.isFull());
        BatchStatus newStatus = capacityPolicy.evaluateBatchStatus(BatchStatus.FULL, expanded);
        assertEquals(BatchStatus.ACTIVE, newStatus);
    }
}
