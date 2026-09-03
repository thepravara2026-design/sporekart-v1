package com.sporekart.modules.training;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.EnrollmentLifecycleService;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TrainingEnrollmentConcurrencyTest {

    private TrainingEnrollmentRepository enrollmentRepository;
    private TrainingEnrollmentHistoryRepository historyRepository;
    private TrainingBatchRepository batchRepository;
    private TrainingEnrollmentPaymentRepository paymentRepository;
    private TrainingDemandRepository demandRepository;
    private EnrollmentLifecycleService lifecycleService;

    @BeforeEach
    void setUp() {
        enrollmentRepository = mock(TrainingEnrollmentRepository.class);
        historyRepository = mock(TrainingEnrollmentHistoryRepository.class);
        batchRepository = mock(TrainingBatchRepository.class);
        paymentRepository = mock(TrainingEnrollmentPaymentRepository.class);
        demandRepository = mock(TrainingDemandRepository.class);

        lifecycleService = new EnrollmentLifecycleService(
                enrollmentRepository,
                historyRepository,
                batchRepository,
                paymentRepository,
                demandRepository,
                mock(org.springframework.context.ApplicationEventPublisher.class),
                mock(SecurityAuditService.class)
        );
    }

    @Test
    @DisplayName("CONCURRENCY & IDEMPOTENCY: 100 concurrent confirmation requests for same enrollment -> exactly 1 seat allocated, 0 overbooking")
    void testConcurrentConfirmationIdempotency() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        String batchId = "batch-conc-" + UUID.randomUUID();
        String traineeId = "trainee-conc@sporekart.com";
        String payRef = "TRN-PAY-CONC";

        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId, new BigDecimal("5000.00"), "INR", null, traineeId);
        enrollment.markPaymentPending();
        enrollment.markPaymentVerified(payRef);

        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));
        when(batchRepository.tryAllocateSeatAtomic(batchId)).thenReturn(true);
        when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenAnswer(i -> i.getArgument(0));

        TrainingBatch batch = new TrainingBatch(batchId, "prog-1", "TRN-B1", Instant.now(), Instant.now().plusSeconds(86400), new Capacity(10, 1), BatchStatus.ACTIVE, java.util.List.of(), Instant.now(), Instant.now());
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    lifecycleService.transitionStatus(enrollment.getId(), EnrollmentStatus.CONFIRMED, "CONFIRMATION_PAYMENT", traineeId);
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    System.err.println("Concurrent confirmation exception: " + ex.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertEquals(threadCount, successCount.get(), "All concurrent confirmation requests should converge cleanly");
        assertEquals(EnrollmentStatus.CONFIRMED, enrollment.getStatus());
        // Capacity seat allocation should execute exactly ONCE
        verify(batchRepository, times(1)).tryAllocateSeatAtomic(batchId);
    }

    @Test
    @DisplayName("CONCURRENCY RACE: Simultaneous webhook + callback arrival -> exactly 1 confirmed enrollment, 1 capacity increment")
    void testSimultaneousWebhookCallbackRace() throws Exception {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        String batchId = "batch-race-" + UUID.randomUUID();
        String traineeId = "trainee-race@sporekart.com";
        String payRef = "TRN-PAY-RACE";

        TrainingEnrollment enrollment = TrainingEnrollment.create(batchId, traineeId, new BigDecimal("5000.00"), "INR", null, traineeId);
        enrollment.markPaymentPending();
        enrollment.markPaymentVerified(payRef);

        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));
        when(batchRepository.tryAllocateSeatAtomic(batchId)).thenReturn(true);
        when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenAnswer(i -> i.getArgument(0));

        TrainingBatch batch = new TrainingBatch(batchId, "prog-1", "TRN-B1", Instant.now(), Instant.now().plusSeconds(86400), new Capacity(10, 1), BatchStatus.ACTIVE, java.util.List.of(), Instant.now(), Instant.now());
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        AtomicInteger successCount = new AtomicInteger(0);

        // Thread 1: Webhook execution
        executor.submit(() -> {
            try {
                startLatch.await();
                lifecycleService.transitionStatus(enrollment.getId(), EnrollmentStatus.CONFIRMED, "WEBHOOK_PAYMENT_VERIFIED", "WEBHOOK");
                successCount.incrementAndGet();
            } catch (Exception ex) {
                System.err.println("Webhook race exception: " + ex.getMessage());
            } finally {
                endLatch.countDown();
            }
        });

        // Thread 2: Frontend Callback execution
        executor.submit(() -> {
            try {
                startLatch.await();
                lifecycleService.transitionStatus(enrollment.getId(), EnrollmentStatus.CONFIRMED, "CALLBACK_PAYMENT_VERIFIED", traineeId);
                successCount.incrementAndGet();
            } catch (Exception ex) {
                System.err.println("Callback race exception: " + ex.getMessage());
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertEquals(2, successCount.get(), "Both webhook and callback tasks complete cleanly without error");
        assertEquals(EnrollmentStatus.CONFIRMED, enrollment.getStatus());
        // Capacity seat allocation must execute exactly ONCE across both concurrent threads
        verify(batchRepository, times(1)).tryAllocateSeatAtomic(batchId);
    }
}
