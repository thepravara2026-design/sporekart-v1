package com.sporekart.modules.training;

import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.application.DemandApplicationService;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import com.sporekart.modules.training.domain.exception.DuplicateDemandException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingDemandRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class DemandConcurrencyTest {

    @Autowired
    private DemandApplicationService demandService;

    @Autowired
    private EnrollmentApplicationService enrollmentService;

    @Autowired
    private CapacityApplicationService capacityService;

    @Autowired
    private TrainingBatchRepository batchRepository;

    @Autowired
    private TrainingDemandRepository demandRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void seedUserIfMissing(String userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO users (id, email, password_hash, role, first_name, last_name, status, created_at, updated_at) " +
                            "VALUES (?, ?, 'hash', 'ROLE_TRAINEE', 'Test', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    userId, userId);
        }
    }

    private void seedProgramIfMissing(String programId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM training_programs WHERE id = ?", Integer.class, programId);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO training_programs (id, title, description, status, price_amount, currency, slug, category, duration_hours, created_by, updated_by, created_at, updated_at) " +
                            "VALUES (?, 'Demand Program', 'Description', 'PUBLISHED', 0.00, 'INR', ?, 'GENERAL', 0, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    programId, "demand-program-" + programId);
        }
    }

    private TrainingBatch createFullBatch(String batchId, int capacity) {
        seedProgramIfMissing("program-demand-1");
        TrainingBatch batch = new TrainingBatch(
                batchId,
                "program-demand-1",
                "BATCH-DEMAND-" + UUID.randomUUID().toString().substring(0, 5),
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800),
                new Capacity(capacity, 0),
                BatchStatus.ACTIVE,
                List.of(),
                Instant.now(),
                Instant.now()
        );

        batchRepository.save(batch);

        // Fill all capacity slots via direct SQL — safe without JPA transaction context
        jdbcTemplate.update(
                "UPDATE training_batches SET occupied_seats = ?, status = 'FULL', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                capacity, batchId);

        return batchRepository.findById(batchId).orElseThrow();
    }

    @Test
    @DisplayName("FAANG CONCURRENCY TEST A: 100 concurrent demand requests from SAME trainee on full batch -> Exactly 1 active demand, 0 capacity consumed")
    void testConcurrentDuplicateDemandDefense() throws Exception {
        String batchId = UUID.randomUUID().toString();
        createFullBatch(batchId, 10);

        String traineeId = "trainee-single-dup@sporekart.com";
        seedUserIfMissing(traineeId);

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    demandService.createDemand(batchId, traineeId);
                    successCount.incrementAndGet();
                } catch (DuplicateDemandException ex) {
                    duplicateCount.incrementAndGet();
                } catch (Exception ex) {
                    if (ex.getCause() instanceof DuplicateDemandException || (ex.getMessage() != null && ex.getMessage().contains("Duplicate"))) {
                        duplicateCount.incrementAndGet();
                    } else {
                        System.err.println("Unexpected exception in demand concurrency test: " + ex.getClass().getName() + " - " + ex.getMessage());
                    }
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly 1 demand creation must succeed for single trainee");
        assertEquals(99, duplicateCount.get(), "99 concurrent requests must be rejected as duplicate demand");

        TrainingBatch batchAfter = batchRepository.findById(batchId).orElseThrow();
        assertEquals(10, batchAfter.getCapacity().getOccupiedSeats(), "Demand MUST NOT consume batch capacity");

        long activeDemandCount = demandRepository.countByBatchIdAndStatus(batchId, DemandStatus.ACTIVE);
        assertEquals(1, activeDemandCount, "Exactly 1 active demand record must exist in database");
    }

    @Test
    @DisplayName("FAANG CONCURRENCY TEST B: 100 distinct trainees submit demand on full batch -> 100 demands created, 0 capacity consumed, admin capacity expansion flow verified")
    void testHighVolumeDemandAndCapacityExpansion() throws Exception {
        String batchId = UUID.randomUUID().toString();
        createFullBatch(batchId, 10);

        int traineeCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(traineeCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(traineeCount);

        AtomicInteger demandSuccessCount = new AtomicInteger(0);

        for (int i = 0; i < traineeCount; i++) {
            String traineeId = "trainee-demand-worker-" + i + "@sporekart.com";
            seedUserIfMissing(traineeId);

            executor.submit(() -> {
                try {
                    startLatch.await();
                    demandService.createDemand(batchId, traineeId);
                    demandSuccessCount.incrementAndGet();
                } catch (Exception ex) {
                    System.err.println("Demand submission error: " + ex.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertEquals(100, demandSuccessCount.get(), "All 100 distinct trainees must create demand cleanly");

        TrainingBatch batchBeforeExpand = batchRepository.findById(batchId).orElseThrow();
        assertEquals(10, batchBeforeExpand.getCapacity().getOccupiedSeats(), "Occupied capacity MUST remain 10 (Demand does not consume capacity)");

        long demandCountBefore = demandRepository.countByBatchIdAndStatus(batchId, DemandStatus.ACTIVE);
        assertEquals(100, demandCountBefore, "Database must store 100 active demand requests");

        // Admin expands capacity: 10 -> 20
        capacityService.updateBatchCapacity(batchId, 20, "admin@sporekart.com");

        TrainingBatch batchAfterExpand = batchRepository.findById(batchId).orElseThrow();
        assertEquals(20, batchAfterExpand.getCapacity().getTotalCapacity());
        assertEquals(10, batchAfterExpand.getCapacity().getOccupiedSeats(), "Occupied seats MUST remain 10 (Capacity expansion NEVER creates automatic/phantom enrollments!)");
        assertEquals(10, batchAfterExpand.getCapacity().getAvailableSeats(), "Available seats MUST equal 10");

        // Trainee with active demand now enrolls in expanded batch
        String firstTrainee = "trainee-demand-worker-0@sporekart.com";
        enrollmentService.enrollTrainee(batchId, firstTrainee, "idempotency-key-demand-enroll-0");

        TrainingBatch batchAfterEnroll = batchRepository.findById(batchId).orElseThrow();
        assertEquals(11, batchAfterEnroll.getCapacity().getOccupiedSeats());

        // Verify active demand for firstTrainee was resolved upon enrollment
        assertTrue(demandRepository.findByBatchIdAndTraineeIdAndStatus(batchId, firstTrainee, DemandStatus.ACTIVE).isEmpty(),
                "Active demand for trainee must be RESOLVED upon enrollment");
    }
}
