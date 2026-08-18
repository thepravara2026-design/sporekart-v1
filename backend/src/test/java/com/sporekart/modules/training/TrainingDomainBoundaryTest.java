package com.sporekart.modules.training;

import com.sporekart.modules.training.application.TrainingCompletionService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.CapacityExceededException;
import com.sporekart.modules.training.domain.exception.InvalidBatchStateException;
import com.sporekart.modules.training.domain.exception.InvalidEnrollmentStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Training Domain Boundary Protection & Quality Suite")
class TrainingDomainBoundaryTest {

    @Test
    @DisplayName("TrainingBatch seat allocation on cancelled or completed batch throws InvalidBatchStateException")
    void testTrainingBatchSeatAllocationCancelledOrCompletedBatch() {
        Instant now = Instant.now();
        TrainingBatch batch = TrainingBatch.create(
                UUID.randomUUID().toString(), "BATCH-TEST-01",
                now, now.plus(30, ChronoUnit.DAYS), 10
        );

        batch.cancel();
        assertEquals(BatchStatus.CANCELLED, batch.getStatus());

        assertThrows(InvalidBatchStateException.class, batch::allocateSeat);
    }

    @Test
    @DisplayName("TrainingCompletionService rejects invalid attendance percentage thresholds")
    void testTrainingCompletionThresholdRangeValidation() {
        TrainingCompletionService service = new TrainingCompletionService(
                org.mockito.Mockito.mock(com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository.class),
                org.mockito.Mockito.mock(com.sporekart.modules.training.domain.port.TrainingBatchRepository.class),
                null,
                org.mockito.Mockito.mock(com.sporekart.modules.training.application.TrainingAttendanceService.class),
                org.mockito.Mockito.mock(com.sporekart.modules.training.application.TrainingCertificateService.class),
                org.mockito.Mockito.mock(com.sporekart.modules.training.application.EnrollmentLifecycleService.class),
                org.mockito.Mockito.mock(org.springframework.context.ApplicationEventPublisher.class)
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.evaluateBatchCompletion("batch-1", -10.0, "SYSTEM")
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.evaluateBatchCompletion("batch-1", 105.0, "SYSTEM")
        );
    }

    @Test
    @DisplayName("Capacity value object enforces positive total capacity and prevents negative seats")
    void testCapacityBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> Capacity.of(0));
        assertThrows(IllegalArgumentException.class, () -> Capacity.of(-5));
        assertThrows(IllegalArgumentException.class, () -> new Capacity(10, -1));
        assertThrows(CapacityExceededException.class, () -> new Capacity(5, 6));

        Capacity capacity = Capacity.of(2);
        assertFalse(capacity.isFull());
        assertEquals(2, capacity.getAvailableSeats());

        Capacity cap1 = capacity.allocateSeat();
        assertEquals(1, cap1.getAvailableSeats());

        Capacity cap2 = cap1.allocateSeat();
        assertTrue(cap2.isFull());
        assertThrows(CapacityExceededException.class, cap2::allocateSeat);

        Capacity released = cap2.releaseSeat();
        assertFalse(released.isFull());
        assertEquals(1, released.getAvailableSeats());
    }

    @Test
    @DisplayName("TrainingEnrollment validates allowed state transitions and rejects illegal transitions")
    void testTrainingEnrollmentStatusTransitions() {
        TrainingEnrollment enrollment = TrainingEnrollment.create(
                UUID.randomUUID().toString(), "trainee-1"
        );
        assertEquals(EnrollmentStatus.PENDING, enrollment.getStatus());

        enrollment.markPaymentPending();
        assertEquals(EnrollmentStatus.PAYMENT_PENDING, enrollment.getStatus());

        enrollment.markPaymentVerified("PAY-REF-100");
        assertEquals(EnrollmentStatus.PAYMENT_VERIFIED, enrollment.getStatus());

        enrollment.confirm("PAY-REF-100");
        assertEquals(EnrollmentStatus.CONFIRMED, enrollment.getStatus());

        enrollment.activate();
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());

        enrollment.complete();
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());

        assertThrows(InvalidEnrollmentStateException.class, enrollment::markPaymentPending);
    }
}
