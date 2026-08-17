package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDomainEntityTest {

    @Test
    @DisplayName("TrainingProgram entity creation and status transitions")
    void testTrainingProgram() {
        TrainingProgram program = TrainingProgram.create("Mushroom Cultivation Masterclass", "Complete guide", new BigDecimal("4999.00"), "INR");
        assertNotNull(program.getId());
        assertEquals("Mushroom Cultivation Masterclass", program.getTitle());
        assertEquals(ProgramStatus.DRAFT, program.getStatus());

        program.publish();
        assertEquals(ProgramStatus.ACTIVE, program.getStatus());

        program.archive();
        assertEquals(ProgramStatus.ARCHIVED, program.getStatus());
    }

    @Test
    @DisplayName("TrainingBatch state management and seat allocation")
    void testTrainingBatch() {
        Instant now = Instant.now();
        Instant end = now.plus(30, ChronoUnit.DAYS);
        TrainingBatch batch = TrainingBatch.create("prog-123", "BATCH-2026-01", now, end, 2);

        assertEquals(BatchStatus.PLANNED, batch.getStatus());
        assertEquals(2, batch.getCapacity().getAvailableSeats());

        batch.allocateSeat();
        assertEquals(1, batch.getCapacity().getOccupiedSeats());
        assertEquals(BatchStatus.PLANNED, batch.getStatus());

        batch.allocateSeat();
        assertEquals(2, batch.getCapacity().getOccupiedSeats());
        assertEquals(BatchStatus.FULL, batch.getStatus());

        assertThrows(BatchFullException.class, batch::allocateSeat);

        batch.releaseSeat();
        assertEquals(1, batch.getCapacity().getOccupiedSeats());
        assertEquals(BatchStatus.ACTIVE, batch.getStatus());
    }

    @Test
    @DisplayName("BatchSchedule and TrainingEnrollment creation")
    void testScheduleAndEnrollment() {
        Instant now = Instant.now();
        BatchSchedule schedule = BatchSchedule.create("batch-1", "Introduction to Mycelium", now, 90, "Online Lab Room 1");
        assertNotNull(schedule.getId());
        assertEquals("Introduction to Mycelium", schedule.getTitle());
        assertEquals(90, schedule.getDurationMinutes());

        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-1", "trainee-101");
        assertNotNull(enrollment.getId());
        assertEquals(EnrollmentStatus.PENDING, enrollment.getStatus());

        enrollment.confirm("PAY-REF-1001");
        assertEquals(EnrollmentStatus.CONFIRMED, enrollment.getStatus());
        assertEquals("PAY-REF-1001", enrollment.getPaymentReference());
    }
}
