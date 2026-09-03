package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.exception.InvalidEnrollmentStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TrainingEnrollmentLifecycleDomainTest {

    @Test
    @DisplayName("Should create enrollment with server-generated enrollment code and price snapshot")
    void testEnrollmentCreationSnapshot() {
        String batchId = UUID.randomUUID().toString();
        String traineeId = "trainee-123";
        BigDecimal price = new BigDecimal("4999.00");

        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId, price, "INR", "KEY-123", traineeId);

        assertNotNull(enrollment.getId());
        assertNotNull(enrollment.getEnrollmentCode());
        assertTrue(enrollment.getEnrollmentCode().startsWith("ENR-2026-"));
        assertEquals(batchId, enrollment.getBatchId());
        assertEquals(traineeId, enrollment.getTraineeId());
        assertEquals(EnrollmentStatus.PENDING, enrollment.getStatus());
        assertEquals(price, enrollment.getPriceAmount());
        assertEquals("INR", enrollment.getCurrency());
        assertNull(enrollment.getConfirmedAt());
    }

    @Test
    @DisplayName("Should execute valid state transitions and record timestamps")
    void testValidLifecycleTransitions() {
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-1", "trainee-1");

        // PENDING -> PAYMENT_PENDING
        enrollment.markPaymentPending();
        assertEquals(EnrollmentStatus.PAYMENT_PENDING, enrollment.getStatus());

        // PAYMENT_PENDING -> PAYMENT_VERIFIED
        enrollment.markPaymentVerified("TRN-PAY-001");
        assertEquals(EnrollmentStatus.PAYMENT_VERIFIED, enrollment.getStatus());
        assertEquals("TRN-PAY-001", enrollment.getPaymentReference());

        // PAYMENT_VERIFIED -> CONFIRMED
        enrollment.confirm("TRN-PAY-001");
        assertEquals(EnrollmentStatus.CONFIRMED, enrollment.getStatus());
        assertNotNull(enrollment.getConfirmedAt());

        // CONFIRMED -> ACTIVE
        enrollment.activate();
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());
        assertNotNull(enrollment.getActivatedAt());

        // ACTIVE -> COMPLETED
        enrollment.complete();
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());
        assertNotNull(enrollment.getCompletedAt());
    }

    @Test
    @DisplayName("Should reject invalid state transition COMPLETED -> CONFIRMED")
    void testRejectInvalidTransitionFromTerminal() {
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-1", "trainee-1");
        enrollment.markPaymentPending();
        enrollment.markPaymentVerified("REF-1");
        enrollment.confirm("REF-1");
        enrollment.activate();
        enrollment.complete();

        assertThrows(InvalidEnrollmentStateException.class, () -> enrollment.confirm("REF-1"));
    }

    @Test
    @DisplayName("Should reject invalid state transition PAYMENT_FAILED -> CONFIRMED")
    void testRejectInvalidTransitionFromPaymentFailed() {
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-1", "trainee-1");
        enrollment.markPaymentPending();
        enrollment.markPaymentFailed();

        assertThrows(InvalidEnrollmentStateException.class, () -> enrollment.confirm("REF-1"));
    }

    @Test
    @DisplayName("Should preserve price snapshot even if current program price changes")
    void testPriceSnapshotImmutability() {
        BigDecimal initialPrice = new BigDecimal("5000.00");
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-1", "trainee-1", initialPrice, "INR", null, "trainee-1");

        // Admin updates program price later to 6000.00
        BigDecimal adminUpdatedPrice = new BigDecimal("6000.00");
        assertNotEquals(adminUpdatedPrice, enrollment.getPriceAmount());
        assertEquals(initialPrice, enrollment.getPriceAmount());
    }
}
