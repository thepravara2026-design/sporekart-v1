package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainingEnrollmentDomainTest {

    @Test
    @DisplayName("Factory method create() initializes TrainingEnrollment with PENDING status and required timestamps")
    void testCreateEnrollmentSuccess() {
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-101", "trainee-202", "idempotency-key-001", "trainee-202");

        assertNotNull(enrollment.getId());
        assertEquals("batch-101", enrollment.getBatchId());
        assertEquals("trainee-202", enrollment.getTraineeId());
        assertEquals(EnrollmentStatus.PENDING, enrollment.getStatus());
        assertEquals("idempotency-key-001", enrollment.getIdempotencyKey());
        assertNotNull(enrollment.getEnrolledAt());
        assertNotNull(enrollment.getCreatedAt());
        assertNotNull(enrollment.getUpdatedAt());
        assertEquals("trainee-202", enrollment.getCreatedBy());
    }

    @Test
    @DisplayName("Creation with null batchId or traineeId throws NullPointerException")
    void testCreateNullValidation() {
        assertThrows(NullPointerException.class, () -> TrainingEnrollment.create(null, "trainee-1"));
        assertThrows(NullPointerException.class, () -> TrainingEnrollment.create("batch-1", null));
    }

    @Test
    @DisplayName("Status transitions confirm(), activate(), complete(), cancel() update status and timestamp")
    void testStatusTransitions() {
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-1", "trainee-1");

        enrollment.markPaymentPending();
        enrollment.markPaymentVerified("PAY-998877");
        enrollment.confirm("PAY-998877");
        assertEquals(EnrollmentStatus.CONFIRMED, enrollment.getStatus());
        assertEquals("PAY-998877", enrollment.getPaymentReference());

        enrollment.activate();
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());

        enrollment.complete();
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());
    }

    @Test
    @DisplayName("Value equality is based on ID")
    void testEnrollmentEquality() {
        TrainingEnrollment e1 = new TrainingEnrollment("id-123", "batch-1", "trainee-1", EnrollmentStatus.PENDING, null, null, null);
        TrainingEnrollment e2 = new TrainingEnrollment("id-123", "batch-1", "trainee-2", EnrollmentStatus.CONFIRMED, null, null, null);
        TrainingEnrollment e3 = new TrainingEnrollment("id-456", "batch-1", "trainee-1", EnrollmentStatus.PENDING, null, null, null);

        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
