package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.CapacityExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class TrainingBatchCapacityTest {

    @Test
    @DisplayName("Should initialize capacity with zero occupied seats")
    void testCapacityInitialization() {
        Capacity cap = Capacity.of(15);
        assertEquals(15, cap.getTotalCapacity());
        assertEquals(0, cap.getOccupiedSeats());
        assertEquals(15, cap.getAvailableSeats());
        assertFalse(cap.isFull());
    }

    @Test
    @DisplayName("Should reject negative capacity or occupied > totalCapacity")
    void testInvalidCapacityInvariants() {
        assertThrows(IllegalArgumentException.class, () -> new Capacity(-5, 0));
        assertThrows(IllegalArgumentException.class, () -> new Capacity(10, -1));
        assertThrows(CapacityExceededException.class, () -> new Capacity(10, 11));
    }

    @Test
    @DisplayName("Should allocate seats up to total capacity and transition status to FULL")
    void testBatchSeatAllocation() {
        Instant now = Instant.now();
        TrainingBatch batch = TrainingBatch.create("prog-1", "TRN-001", now.plus(1, ChronoUnit.DAYS), now.plus(5, ChronoUnit.DAYS), 2);
        batch.activate();

        assertEquals(2, batch.getCapacity().getAvailableSeats());

        batch.allocateSeat();
        assertEquals(1, batch.getCapacity().getOccupiedSeats());
        assertEquals(1, batch.getCapacity().getAvailableSeats());
        assertFalse(batch.getCapacity().isFull());
        assertEquals(BatchStatus.ACTIVE, batch.getStatus());

        batch.allocateSeat();
        assertEquals(2, batch.getCapacity().getOccupiedSeats());
        assertEquals(0, batch.getCapacity().getAvailableSeats());
        assertTrue(batch.getCapacity().isFull());
        assertEquals(BatchStatus.FULL, batch.getStatus());

        assertThrows(BatchFullException.class, batch::allocateSeat);
    }

    @Test
    @DisplayName("Should release seat and transition from FULL to ACTIVE")
    void testBatchSeatRelease() {
        Instant now = Instant.now();
        TrainingBatch batch = TrainingBatch.create("prog-1", "TRN-001", now.plus(1, ChronoUnit.DAYS), now.plus(5, ChronoUnit.DAYS), 1);
        batch.activate();

        batch.allocateSeat();
        assertTrue(batch.getCapacity().isFull());
        assertEquals(BatchStatus.FULL, batch.getStatus());

        batch.releaseSeat();
        assertFalse(batch.getCapacity().isFull());
        assertEquals(1, batch.getCapacity().getAvailableSeats());
        assertEquals(BatchStatus.ACTIVE, batch.getStatus());

        // Releasing when occupied == 0 is safe no-op
        batch.releaseSeat();
        assertEquals(0, batch.getCapacity().getOccupiedSeats());
    }

    @Test
    @DisplayName("Should update capacity and reject newCapacity < occupiedSeats")
    void testUpdateCapacity() {
        Instant now = Instant.now();
        TrainingBatch batch = TrainingBatch.create("prog-1", "TRN-001", now.plus(1, ChronoUnit.DAYS), now.plus(5, ChronoUnit.DAYS), 10);
        batch.activate();

        for (int i = 0; i < 5; i++) {
            batch.allocateSeat();
        }
        assertEquals(5, batch.getCapacity().getOccupiedSeats());

        // Increase capacity
        batch.updateTotalCapacity(15);
        assertEquals(15, batch.getCapacity().getTotalCapacity());
        assertEquals(10, batch.getCapacity().getAvailableSeats());

        // Decrease capacity safely
        batch.updateTotalCapacity(6);
        assertEquals(6, batch.getCapacity().getTotalCapacity());
        assertEquals(1, batch.getCapacity().getAvailableSeats());

        // Reject new capacity < occupied (5)
        assertThrows(CapacityExceededException.class, () -> batch.updateTotalCapacity(4));
    }
}
