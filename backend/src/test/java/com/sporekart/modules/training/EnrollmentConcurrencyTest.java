package com.sporekart.modules.training;

import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.DuplicateEnrollmentException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EnrollmentConcurrencyTest {

    @Autowired
    private EnrollmentApplicationService enrollmentService;

    @Autowired
    private TrainingBatchRepository batchRepository;

    @Autowired
    private TrainingProgramRepository programRepository;

    @Autowired
    private TrainingEnrollmentRepository enrollmentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void seedUserIfMissing(String userId) {
        try {
            jdbcTemplate.update("INSERT INTO users (id, email, password_hash, role, status) VALUES (?, ?, 'hash', 'ROLE_CUSTOMER', 'ACTIVE')", userId, userId);
        } catch (Exception ex) {
            // User already exists
        }
    }

    @Test
    @Disabled("Requires PostgreSQL: relies on atomic UPDATE-WHERE for overbooking prevention. " +
              "H2 (used in CI) does not replicate PostgreSQL's row-level locking semantics. " +
              "Run manually against postgres profile: mvn test -Dspring.profiles.active=postgres -Dtest=EnrollmentConcurrencyTest")
    @DisplayName("FAANG Concurrency Test 1: 100 concurrent trainees attempting enrollment on capacity=10 batch yield exactly 10 successes, 90 failures, and ZERO overbooking")
    void testConcurrentTraineeEnrollmentNoOverbooking() throws InterruptedException {
        TrainingProgram program = TrainingProgram.create("Concurrency Course 100", "Desc", "CULTIVATION", 10, new BigDecimal("100.00"), "INR", "admin");
        program.activate();
        TrainingProgram savedProgram = programRepository.save(program);

        String batchCode = "ENR-CONC-" + UUID.randomUUID().toString().substring(0, 8);
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        TrainingBatch batch = TrainingBatch.create(savedProgram.getId(), batchCode, start, end, 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.activate();
        TrainingBatch savedBatch = batchRepository.save(batch);
        String batchId = savedBatch.getId();

        int numberOfThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final String traineeId = "trainee-worker-" + i + "@sporekart.com";
            seedUserIfMissing(traineeId);

            executor.submit(() -> {
                try {
                    startLatch.await();
                    enrollmentService.enrollTrainee(batchId, traineeId, null);
                    successCount.incrementAndGet();
                } catch (BatchFullException ex) {
                    failureCount.incrementAndGet();
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // release all 100 threads simultaneously
        boolean finishedInTime = finishLatch.await(20, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finishedInTime, "Concurrent enrollment threads did not complete in time");

        assertEquals(10, successCount.get(), "Expected exactly 10 successful enrollments for capacity=10 batch");
        assertEquals(90, failureCount.get(), "Expected exactly 90 rejected requests due to capacity limit");

        TrainingBatch reloaded = batchRepository.findById(batchId).orElseThrow();
        assertEquals(10, reloaded.getCapacity().getTotalCapacity());
        assertEquals(10, reloaded.getCapacity().getOccupiedSeats(), "Final occupied seats must equal total capacity");
        assertEquals(0, reloaded.getCapacity().getAvailableSeats());
        assertEquals(BatchStatus.FULL, reloaded.getStatus(), "Batch status must be FULL");
        assertTrue(reloaded.getCapacity().getOccupiedSeats() <= reloaded.getCapacity().getTotalCapacity(), "OVERBOOKING VIOLATION: occupiedSeats > totalCapacity!");
    }

    @Test
    @Disabled("Requires PostgreSQL: relies on atomic UPDATE-WHERE and row-level locking for duplicate protection. " +
              "H2 (used in CI) does not replicate PostgreSQL's concurrent transaction semantics. " +
              "Run manually against postgres profile: mvn test -Dspring.profiles.active=postgres -Dtest=EnrollmentConcurrencyTest")
    @DisplayName("FAANG Concurrency Test 2: 50 concurrent enrollment requests by the SAME trainee yield exactly 1 success, 49 duplicate rejections, occupied=1")
    void testConcurrentSameTraineeDuplicateProtection() throws InterruptedException {
        TrainingProgram program = TrainingProgram.create("Single Trainee Course", "Desc", "CULTIVATION", 10, new BigDecimal("100.00"), "INR", "admin");
        program.activate();
        TrainingProgram savedProgram = programRepository.save(program);

        String batchCode = "SAME-TRAINEE-" + UUID.randomUUID().toString().substring(0, 8);
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        TrainingBatch batch = TrainingBatch.create(savedProgram.getId(), batchCode, start, end, 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.activate();
        TrainingBatch savedBatch = batchRepository.save(batch);
        String batchId = savedBatch.getId();

        String sameTraineeId = "trainee-bob-unique@sporekart.com";
        seedUserIfMissing(sameTraineeId);

        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    enrollmentService.enrollTrainee(batchId, sameTraineeId, null);
                    successCount.incrementAndGet();
                } catch (DuplicateEnrollmentException ex) {
                    duplicateCount.incrementAndGet();
                } catch (Exception ex) {
                    duplicateCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finishedInTime = finishLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finishedInTime, "Threads did not finish in time");

        assertEquals(1, successCount.get(), "Expected exactly 1 successful enrollment for same trainee");
        assertEquals(49, duplicateCount.get(), "Expected exactly 49 duplicate enrollment rejections");

        TrainingBatch reloaded = batchRepository.findById(batchId).orElseThrow();
        assertEquals(1, reloaded.getCapacity().getOccupiedSeats(), "Occupied seats must be exactly 1 despite 50 requests");
        assertTrue(enrollmentRepository.existsByBatchIdAndTraineeId(batchId, sameTraineeId));
    }
}
