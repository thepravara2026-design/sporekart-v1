package com.sporekart.modules.training;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.EnrollmentLifecycleService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.InvalidEnrollmentStateException;
import com.sporekart.modules.training.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EnrollmentLifecycleServiceTest {

    private TrainingEnrollmentRepository enrollmentRepository;
    private TrainingEnrollmentHistoryRepository historyRepository;
    private TrainingBatchRepository batchRepository;
    private TrainingEnrollmentPaymentRepository paymentRepository;
    private TrainingDemandRepository demandRepository;
    private ApplicationEventPublisher eventPublisher;
    private SecurityAuditService auditService;
    private EnrollmentLifecycleService lifecycleService;

    @BeforeEach
    void setUp() {
        enrollmentRepository = mock(TrainingEnrollmentRepository.class);
        historyRepository = mock(TrainingEnrollmentHistoryRepository.class);
        batchRepository = mock(TrainingBatchRepository.class);
        paymentRepository = mock(TrainingEnrollmentPaymentRepository.class);
        demandRepository = mock(TrainingDemandRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        auditService = mock(SecurityAuditService.class);

        lifecycleService = new EnrollmentLifecycleService(
                enrollmentRepository,
                historyRepository,
                batchRepository,
                paymentRepository,
                demandRepository,
                eventPublisher,
                auditService
        );
    }

    @Test
    @DisplayName("Should successfully transition enrollment from PAYMENT_VERIFIED to CONFIRMED and allocate capacity")
    void testTransitionToConfirmedSuccess() {
        String batchId = "batch-100";
        String traineeId = "trainee-1";
        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId, new BigDecimal("5000.00"), "INR", null, traineeId);
        enrollment.markPaymentPending();
        enrollment.markPaymentVerified("TRN-PAY-100");

        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));
        when(batchRepository.tryAllocateSeatAtomic(batchId)).thenReturn(true);
        when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenAnswer(i -> i.getArgument(0));

        TrainingBatch batch = new TrainingBatch(batchId, "prog-1", "TRN-B1", Instant.now(), Instant.now().plusSeconds(86400), new Capacity(10, 1), BatchStatus.ACTIVE, java.util.List.of(), Instant.now(), Instant.now());
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        TrainingEnrollment result = lifecycleService.transitionStatus(enrollment.getId(), EnrollmentStatus.CONFIRMED, "PAYMENT_SUCCESS", traineeId);

        assertEquals(EnrollmentStatus.CONFIRMED, result.getStatus());
        verify(batchRepository).tryAllocateSeatAtomic(batchId);
        verify(historyRepository).save(any(TrainingEnrollmentHistory.class));
        verify(eventPublisher, atLeastOnce()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Should throw BatchFullException when transitioning to CONFIRMED on full batch")
    void testTransitionToConfirmedBatchFull() {
        String batchId = "batch-full";
        String traineeId = "trainee-1";
        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId, new BigDecimal("5000.00"), "INR", null, traineeId);
        enrollment.markPaymentPending();
        enrollment.markPaymentVerified("TRN-PAY-100");

        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));
        when(batchRepository.tryAllocateSeatAtomic(batchId)).thenReturn(false);

        assertThrows(BatchFullException.class, () ->
                lifecycleService.transitionStatus(enrollment.getId(), EnrollmentStatus.CONFIRMED, "PAYMENT_SUCCESS", traineeId)
        );
    }

    @Test
    @DisplayName("Should successfully recover confirmed enrollment when payment is VERIFIED and seat is allocated")
    void testRecoverySuccess() {
        String batchId = "batch-rec";
        String traineeId = "trainee-rec";
        String payRef = "TRN-PAY-REC";

        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId, new BigDecimal("5000.00"), "INR", null, traineeId);
        enrollment.markPaymentPending();
        enrollment.markPaymentVerified(payRef);

        TrainingEnrollmentPayment trnPay = TrainingEnrollmentPayment.create(UUID.randomUUID().toString(), batchId, traineeId, new BigDecimal("5000.00"), "INR", traineeId);
        trnPay.markVerified(traineeId);

        when(enrollmentRepository.findByBatchIdAndTraineeId(batchId, traineeId)).thenReturn(Optional.of(enrollment));
        when(paymentRepository.findByBatchIdAndTraineeId(batchId, traineeId)).thenReturn(Optional.of(trnPay));
        when(batchRepository.tryAllocateSeatAtomic(batchId)).thenReturn(true);
        when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenAnswer(i -> i.getArgument(0));

        TrainingEnrollment recovered = lifecycleService.recoverEnrollmentConfirmation(batchId, traineeId, payRef, "ADMIN");

        assertEquals(EnrollmentStatus.CONFIRMED, recovered.getStatus());
        assertEquals(TrainingPaymentStatus.ENROLLMENT_CONFIRMED, trnPay.getStatus());
        verify(batchRepository).tryAllocateSeatAtomic(batchId);
    }

    @Test
    @DisplayName("Should return existing confirmed enrollment idempotently during recovery retry")
    void testRecoveryIdempotentReplay() {
        String batchId = "batch-rec-idem";
        String traineeId = "trainee-rec-idem";
        String payRef = "TRN-PAY-IDEM";

        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId, new BigDecimal("5000.00"), "INR", null, traineeId);
        enrollment.markPaymentPending();
        enrollment.markPaymentVerified(payRef);
        enrollment.confirm(payRef);

        when(enrollmentRepository.findByBatchIdAndTraineeId(batchId, traineeId)).thenReturn(Optional.of(enrollment));

        TrainingEnrollment recovered = lifecycleService.recoverEnrollmentConfirmation(batchId, traineeId, payRef, "ADMIN");

        assertEquals(EnrollmentStatus.CONFIRMED, recovered.getStatus());
        verify(batchRepository, never()).tryAllocateSeatAtomic(anyString());
    }
}
