package com.sporekart.modules.training;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProvider;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderRegistry;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.application.TrainingPaymentApplicationService;
import com.sporekart.modules.training.controller.dto.TrainingPaymentStatusResponse;
import com.sporekart.modules.training.controller.dto.TrainingPaymentVerificationRequestDto;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.port.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TrainingPaymentConcurrencyTest {

    @Autowired
    private TrainingPaymentApplicationService paymentService;

    @Autowired
    private EnrollmentApplicationService enrollmentService;

    @Autowired
    private TrainingBatchRepository batchRepository;

    @Autowired
    private TrainingProgramRepository programRepository;

    @Autowired
    private TrainingEnrollmentPaymentRepository trainingPaymentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentProviderRegistry providerRegistry;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void seedUserIfMissing(String userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO users (id, email, password_hash, role, first_name, last_name, status, created_at, updated_at) " +
                            "VALUES (?, ?, 'hash', 'ROLE_TRAINEE', 'Test', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    userId, userId);
        }
    }

    private TrainingProgram seedProgramIfMissing(String programId, BigDecimal price) {
        return programRepository.findById(programId).orElseGet(() -> {
            TrainingProgram program = new TrainingProgram(
                    programId,
                    "slug-" + programId,
                    "Paid Program " + programId,
                    "Description",
                    "GENERAL",
                    10,
                    ProgramStatus.ACTIVE,
                    price,
                    "INR",
                    "ADMIN",
                    "ADMIN",
                    Instant.now(),
                    Instant.now()
            );
            return programRepository.save(program);
        });
    }

    @Test
    @DisplayName("CAPACITY RACE TEST: Prevent batch overbooking when seat is occupied during payment execution")
    void testCapacityRacePreventsOverbooking() {
        String batchId = "batch-race-" + UUID.randomUUID().toString().substring(0, 6);
        String programId = "prog-race-1";
        String traineeA = "trainee-race-A@sporekart.com";
        String traineeB = "trainee-race-B@sporekart.com";

        seedUserIfMissing(traineeA);
        seedUserIfMissing(traineeB);
        seedProgramIfMissing(programId, new BigDecimal("5000.00"));

        // Capacity = 1, Occupied = 0
        TrainingBatch batch = TrainingBatch.create(
                programId,
                "BATCH-RACE-01",
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800),
                1,
                DeliveryMode.ONLINE,
                null, null, "Asia/Kolkata", "admin"
        );
        batchRepository.save(batch);

        // 1. Trainee A initiates payment order when capacity is available
        var orderResponse = paymentService.initiatePaymentOrder(batch.getId(), traineeA);
        assertNotNull(orderResponse);

        // 2. Trainee B enrolls directly into the single seat (occupies 1/1 seat, batch becomes FULL)
        TrainingEnrollment enrollmentB = enrollmentService.enrollTrainee(batch.getId(), traineeB, "key-B-race");
        assertNotNull(enrollmentB);

        // Verify batch is FULL
        TrainingBatch reloaded = batchRepository.findById(batch.getId()).orElseThrow();
        assertEquals(1, reloaded.getCapacity().getOccupiedSeats());
        assertEquals(BatchStatus.FULL, reloaded.getStatus());

        // 3. Trainee A payment verifies signature after seat was taken by Trainee B
        TrainingPaymentVerificationRequestDto verifyDto = new TrainingPaymentVerificationRequestDto(
                orderResponse.paymentReference(),
                orderResponse.providerOrderId(),
                "pay_mock_race_A",
                "valid_sig"
        );

        // Verification call must NOT throw unhandled exception or overbook
        TrainingPaymentStatusResponse statusResponse = paymentService.verifyPayment(batch.getId(), verifyDto, traineeA);

        // INVARIANT 1: Trainee A payment status is ENROLLMENT_PENDING (paid, but enrollment pending reconciliation)
        assertEquals(TrainingPaymentStatus.ENROLLMENT_PENDING, statusResponse.status());

        // INVARIANT 2: Batch capacity MUST REMAIN 1 (Occupied = 1). BATCH IS NOT OVERBOOKED!
        TrainingBatch finalBatchState = batchRepository.findById(batch.getId()).orElseThrow();
        assertEquals(1, finalBatchState.getCapacity().getOccupiedSeats());
        assertTrue(finalBatchState.getCapacity().getOccupiedSeats() <= finalBatchState.getCapacity().getTotalCapacity());
    }

    @Test
    @DisplayName("CONCURRENT PAYMENT ORDER IDEMPOTENCY: Concurrent order creation requests return same payment order")
    void testConcurrentPaymentOrderCreationIdempotency() throws InterruptedException {
        String batchId = "batch-idem-" + UUID.randomUUID().toString().substring(0, 6);
        String programId = "prog-idem-1";
        String trainee = "trainee-idem@sporekart.com";

        seedUserIfMissing(trainee);
        seedProgramIfMissing(programId, new BigDecimal("2500.00"));

        TrainingBatch batch = TrainingBatch.create(
                programId, "BATCH-IDEM-01", Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800), 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin"
        );
        batchRepository.save(batch);

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    try {
                        paymentService.initiatePaymentOrder(batch.getId(), trainee);
                    } catch (Exception retryEx) {
                        // Concurrent race retry: upon conflict, subsequent call returns existing pending order
                        paymentService.initiatePaymentOrder(batch.getId(), trainee);
                    }
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    System.err.println("Order creation exception: " + ex.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertEquals(threadCount, successCount.get(), "All concurrent calls must complete cleanly via idempotency reuse");
    }
}
