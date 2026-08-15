package com.sporekart.modules.inventory;

import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.domain.exception.InsufficientStockException;
import com.sporekart.modules.inventory.infrastructure.persistence.InventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class InventoryConcurrencyResilienceTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("Concurrent reservation requests for the last available item must result in exactly 1 winner and zero negative stock")
    void shouldHandleConcurrentReservationsWithoutNegativeStock() throws Exception {
        String testSku = "SKU-RACE-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        transactionTemplate.executeWithoutResult(status -> {
            InventoryItem item = InventoryItem.createNew(UUID.randomUUID(), UUID.randomUUID(), testSku, 1);
            inventoryRepository.save(item);
        });

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                readyLatch.await();
                try {
                    Boolean res = transactionTemplate.execute(status -> {
                        InventoryItem locked = inventoryRepository.findBySkuForUpdate(testSku).orElseThrow();
                        if (locked.getAvailableQuantity() >= 1) {
                            locked.reserve(1);
                            inventoryRepository.save(locked);
                            return true;
                        }
                        return false;
                    });
                    if (Boolean.TRUE.equals(res)) {
                        successCount.incrementAndGet();
                        return true;
                    } else {
                        failureCount.incrementAndGet();
                        return false;
                    }
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                    return false;
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        for (Callable<Boolean> task : tasks) {
            futures.add(executor.submit(task));
        }

        // Release all threads simultaneously
        readyLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(9);

        InventoryItem finalItem = inventoryRepository.findBySku(testSku).orElseThrow();
        assertThat(finalItem.getAvailableQuantity()).isEqualTo(0);
        assertThat(finalItem.getOnHandQuantity()).isEqualTo(1);
        assertThat(finalItem.getReservedQuantity()).isEqualTo(1);
    }
}
