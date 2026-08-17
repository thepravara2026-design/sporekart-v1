package com.sporekart.modules.training;

import com.sporekart.modules.training.application.CapacityApplicationService;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DeliveryMode;
import com.sporekart.modules.training.domain.TrainingBatch;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.exception.InsufficientCapacityException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
class BatchCapacityConcurrencyTest {

    @Autowired
    private CapacityApplicationService capacityApplicationService;

    @Autowired
    private TrainingBatchRepository batchRepository;

    @Autowired
    private com.sporekart.modules.training.domain.port.TrainingProgramRepository programRepository;

    @Test
    @DisplayName("FAANG Concurrency Test: 50 concurrent allocation requests on capacity=10 batch must yield exactly 10 successes, 40 failures, and ZERO overbooking")
    void testConcurrentSlotAllocationNoOverbooking() throws InterruptedException {
        TrainingProgram program = TrainingProgram.create("Concurrency Test Program 1", "Desc", "CULTIVATION", 10, new java.math.BigDecimal("1000.00"), "INR", "ADMIN");
        program.activate();
        TrainingProgram savedProgram = programRepository.save(program);

        String batchCode = "CONC-" + UUID.randomUUID().toString().substring(0, 8);
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        TrainingBatch batch = TrainingBatch.create(savedProgram.getId(), batchCode, start, end, 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.activate();
        TrainingBatch saved = batchRepository.save(batch);
        String batchId = saved.getId();

        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // wait for start signal
                    capacityApplicationService.allocateSlot(batchId);
                    successCount.incrementAndGet();
                } catch (InsufficientCapacityException ex) {
                    failureCount.incrementAndGet();
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // release all 50 threads simultaneously
        boolean finishedInTime = finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finishedInTime, "Concurrent allocation threads did not finish in time");

        assertEquals(10, successCount.get(), "Expected exactly 10 successful slot allocations");
        assertEquals(40, failureCount.get(), "Expected exactly 40 failed slot allocations due to capacity limit");

        TrainingBatch reloaded = batchRepository.findById(batchId).orElseThrow();
        assertEquals(10, reloaded.getCapacity().getTotalCapacity());
        assertEquals(10, reloaded.getCapacity().getOccupiedSeats(), "Final occupied seats must equal maximum capacity");
        assertEquals(0, reloaded.getCapacity().getAvailableSeats(), "Final available seats must be zero");
        assertTrue(reloaded.getCapacity().isFull(), "Batch must be full");
        assertEquals(BatchStatus.FULL, reloaded.getStatus(), "Batch status must be FULL");

        assertTrue(reloaded.getCapacity().getOccupiedSeats() <= reloaded.getCapacity().getTotalCapacity(), "OVERBOOKING INVARIANT VIOLATION: occupiedSeats > totalCapacity!");
    }

    @Test
    @DisplayName("FAANG Concurrency Test: 20 concurrent allocation requests on capacity=1 batch must yield exactly 1 success, 19 failures")
    void testSingleSlotHighConcurrency() throws InterruptedException {
        TrainingProgram program = TrainingProgram.create("Concurrency Test Program 2", "Desc", "CULTIVATION", 10, new java.math.BigDecimal("1000.00"), "INR", "ADMIN");
        program.activate();
        TrainingProgram savedProgram = programRepository.save(program);

        String batchCode = "SINGLE-" + UUID.randomUUID().toString().substring(0, 8);
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        TrainingBatch batch = TrainingBatch.create(savedProgram.getId(), batchCode, start, end, 1, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.activate();
        TrainingBatch saved = batchRepository.save(batch);
        String batchId = saved.getId();

        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    capacityApplicationService.allocateSlot(batchId);
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finishedInTime = finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finishedInTime, "Threads did not finish in time");
        assertEquals(1, successCount.get(), "Expected exactly 1 successful slot allocation");
        assertEquals(19, failureCount.get(), "Expected exactly 19 failed slot allocations");

        TrainingBatch reloaded = batchRepository.findById(batchId).orElseThrow();
        assertEquals(1, reloaded.getCapacity().getOccupiedSeats());
        assertEquals(BatchStatus.FULL, reloaded.getStatus());
    }
}
