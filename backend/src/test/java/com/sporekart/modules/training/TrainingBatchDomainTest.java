package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.exception.InvalidBatchStateException;
import com.sporekart.modules.training.domain.exception.InvalidScheduleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class TrainingBatchDomainTest {

    @Test
    @DisplayName("Should create TrainingBatch with default delivery mode ONLINE and PLANNED status")
    void testCreateBatch() {
        Instant now = Instant.now();
        Instant start = now.plus(1, ChronoUnit.DAYS);
        Instant end = now.plus(7, ChronoUnit.DAYS);

        TrainingBatch batch = TrainingBatch.create(
                "prog-123",
                "TRN-2026-001",
                start,
                end,
                25,
                DeliveryMode.HYBRID,
                "Lab 4, Science Block",
                "https://meet.google.com/test",
                "Asia/Kolkata",
                "ADMIN_USER"
        );

        assertNotNull(batch.getId());
        assertEquals("prog-123", batch.getProgramId());
        assertEquals("TRN-2026-001", batch.getBatchCode());
        assertEquals(start, batch.getStartDate());
        assertEquals(end, batch.getEndDate());
        assertEquals(25, batch.getCapacity().getTotalCapacity());
        assertEquals(0, batch.getCapacity().getOccupiedSeats());
        assertEquals(BatchStatus.PLANNED, batch.getStatus());
        assertEquals(DeliveryMode.HYBRID, batch.getDeliveryMode());
        assertEquals("Lab 4, Science Block", batch.getVenueInfo());
        assertEquals("https://meet.google.com/test", batch.getMeetingUrl());
        assertEquals("Asia/Kolkata", batch.getTimezone());
        assertEquals("ADMIN_USER", batch.getCreatedBy());
    }

    @Test
    @DisplayName("Should reject startDate >= endDate with InvalidScheduleException")
    void testInvalidScheduleDates() {
        Instant now = Instant.now();
        Instant start = now.plus(5, ChronoUnit.DAYS);
        Instant endBeforeStart = now.plus(2, ChronoUnit.DAYS);

        assertThrows(InvalidScheduleException.class, () ->
                TrainingBatch.create("prog-123", "TRN-001", start, endBeforeStart, 10)
        );

        assertThrows(InvalidScheduleException.class, () ->
                TrainingBatch.create("prog-123", "TRN-001", start, start, 10)
        );
    }

    @Test
    @DisplayName("Should update batch schedule and delivery metadata")
    void testUpdateSchedule() {
        Instant now = Instant.now();
        Instant start = now.plus(2, ChronoUnit.DAYS);
        Instant end = now.plus(5, ChronoUnit.DAYS);

        TrainingBatch batch = TrainingBatch.create("prog-123", "TRN-001", start, end, 10);

        Instant newStart = now.plus(3, ChronoUnit.DAYS);
        Instant newEnd = now.plus(6, ChronoUnit.DAYS);

        batch.updateSchedule(newStart, newEnd, "UTC", DeliveryMode.OFFLINE, "Auditorium A", null, "UPDATER_ADMIN");

        assertEquals(newStart, batch.getStartDate());
        assertEquals(newEnd, batch.getEndDate());
        assertEquals("UTC", batch.getTimezone());
        assertEquals(DeliveryMode.OFFLINE, batch.getDeliveryMode());
        assertEquals("Auditorium A", batch.getVenueInfo());
        assertEquals("UPDATER_ADMIN", batch.getUpdatedBy());
    }

    @Test
    @DisplayName("Should enforce state transitions correctly for batch lifecycle")
    void testBatchStateTransitions() {
        Instant now = Instant.now();
        TrainingBatch batch = TrainingBatch.create("prog-123", "TRN-001", now.plus(1, ChronoUnit.DAYS), now.plus(5, ChronoUnit.DAYS), 10);
        assertEquals(BatchStatus.PLANNED, batch.getStatus());

        batch.activate();
        assertEquals(BatchStatus.ACTIVE, batch.getStatus());

        batch.deactivate();
        assertEquals(BatchStatus.PLANNED, batch.getStatus());

        batch.cancel();
        assertEquals(BatchStatus.CANCELLED, batch.getStatus());

        assertThrows(InvalidBatchStateException.class, batch::activate);
        assertThrows(InvalidBatchStateException.class, batch::complete);
    }
}
