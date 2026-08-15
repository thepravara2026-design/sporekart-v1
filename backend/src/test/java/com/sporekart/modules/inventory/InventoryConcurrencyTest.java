package com.sporekart.modules.inventory;

import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryConcurrencyTest {

    @Test
    @DisplayName("Concurrent reservation requests must maintain non-negative available stock invariant")
    void testConcurrentReservationProtection() throws InterruptedException {
        InventoryItem item = InventoryItem.createNew(UUID.randomUUID(), null, "SKU-CONCUR-1", 10);

        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 5 concurrent threads each trying to reserve 3 items (Total requested = 15, Available = 10)
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    synchronized (item) {
                        item.reserve(3);
                    }
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    // unexpected exception
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(10);
        }

        // Exactly 3 threads can succeed (3 * 3 = 9 reserved, 1 left), 2 threads fail
        assertEquals(3, successCount.get(), "Exactly 3 reservations of 3 units should succeed on stock of 10");
        assertEquals(2, failureCount.get(), "2 reservations should fail with InsufficientStockException");
        assertEquals(9, item.getReservedQuantity());
        assertEquals(1, item.getAvailableQuantity());
        assertTrue(item.getAvailableQuantity() >= 0, "Available stock MUST NEVER become negative!");
    }
}
