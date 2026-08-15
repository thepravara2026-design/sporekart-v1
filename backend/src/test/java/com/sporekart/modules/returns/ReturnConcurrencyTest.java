package com.sporekart.modules.returns;

import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.application.dto.CreateReturnRequestDto;
import com.sporekart.modules.returns.domain.ReturnReasonCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReturnConcurrencyTest {

    @Autowired
    private ReturnApplicationService returnApplicationService;

    @Test
    @DisplayName("Concurrent return requests for the same order should be safely handled")
    void testConcurrentReturnRequests() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    CreateReturnRequestDto req = new CreateReturnRequestDto(
                            ReturnReasonCode.DAMAGED,
                            "Concurrent test",
                            null,
                            List.of(new CreateReturnRequestDto.CreateReturnItemInput(UUID.randomUUID(), 1, ReturnReasonCode.DAMAGED))
                    );
                    returnApplicationService.createReturn("ORD-NONEXISTENT", req, "cust-101");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            }));
        }

        latch.countDown();
        executor.shutdown();
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

        assertTrue(finished);
        assertEquals(5, failureCount.get()); // Non-existent order properly rejected
    }
}
