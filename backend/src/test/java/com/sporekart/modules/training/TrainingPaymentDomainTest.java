package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.TrainingEnrollmentPayment;
import com.sporekart.modules.training.domain.TrainingPaymentStatus;
import com.sporekart.modules.training.domain.exception.InvalidPaymentStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TrainingPaymentDomainTest {

    @Test
    @DisplayName("Should create TrainingEnrollmentPayment with PENDING status")
    void testCreatePayment() {
        TrainingEnrollmentPayment payment = TrainingEnrollmentPayment.create(
                "pay-100",
                "batch-101",
                "trainee-1",
                new BigDecimal("5000.00"),
                "INR",
                "trainee-1"
        );

        assertNotNull(payment.getId());
        assertEquals("pay-100", payment.getPaymentId());
        assertEquals("batch-101", payment.getBatchId());
        assertEquals("trainee-1", payment.getTraineeId());
        assertEquals(new BigDecimal("5000.00"), payment.getAmount());
        assertEquals("INR", payment.getCurrency());
        assertEquals(TrainingPaymentStatus.PENDING, payment.getStatus());
        assertNull(payment.getEnrollmentId());
    }

    @Test
    @DisplayName("Should transition status cleanly: PENDING -> VERIFIED -> ENROLLMENT_CONFIRMED")
    void testStatusTransitions() {
        TrainingEnrollmentPayment payment = TrainingEnrollmentPayment.create(
                "pay-100", "batch-101", "trainee-1", new BigDecimal("5000.00"), "INR", "trainee-1"
        );

        payment.markVerified("trainee-1");
        assertEquals(TrainingPaymentStatus.VERIFIED, payment.getStatus());

        payment.markEnrollmentConfirmed("enroll-999", "trainee-1");
        assertEquals(TrainingPaymentStatus.ENROLLMENT_CONFIRMED, payment.getStatus());
        assertEquals("enroll-999", payment.getEnrollmentId());
    }

    @Test
    @DisplayName("Should handle capacity race status: PENDING -> VERIFIED -> ENROLLMENT_PENDING")
    void testCapacityRaceStatusTransition() {
        TrainingEnrollmentPayment payment = TrainingEnrollmentPayment.create(
                "pay-100", "batch-101", "trainee-1", new BigDecimal("5000.00"), "INR", "trainee-1"
        );

        payment.markVerified("trainee-1");
        payment.markEnrollmentPending("trainee-1");
        assertEquals(TrainingPaymentStatus.ENROLLMENT_PENDING, payment.getStatus());
    }

    @Test
    @DisplayName("Should reject invalid transition on already confirmed payment")
    void testRejectInvalidTransition() {
        TrainingEnrollmentPayment payment = TrainingEnrollmentPayment.create(
                "pay-100", "batch-101", "trainee-1", new BigDecimal("5000.00"), "INR", "trainee-1"
        );

        payment.markEnrollmentConfirmed("enroll-999", "trainee-1");
        assertThrows(InvalidPaymentStateException.class, () -> payment.markFailed("trainee-1"));
        assertThrows(InvalidPaymentStateException.class, () -> payment.markEnrollmentPending("trainee-1"));
    }
}
