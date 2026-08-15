package com.sporekart.modules.order;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderConcurrencyTest {

    @Test
    @DisplayName("Concurrent state transition requests must safely yield single valid deterministic outcome")
    void testConcurrentStateTransitions() throws InterruptedException {
        AddressSnapshot address = new AddressSnapshot("John", "123", "Line1", null, "City", "State", "100", "India");
        Order order = Order.createNewOrder(
                "SPK-CONCUR-01", "cust-1", "INR", new BigDecimal("1500.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1500.00"),
                null, address, null, List.of()
        );
        order.cancel(); // Transition to terminal state CANCELLED

        int numberOfThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger rejectedTransitions = new AtomicInteger(0);

        // All 4 threads try to revive or change status of CANCELLED order to PAID
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    order.markPaid();
                } catch (InvalidOrderStateTransitionException e) {
                    rejectedTransitions.incrementAndGet();
                } catch (Exception e) {}
            });
        }

        latch.countDown();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(10);
        }

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(4, rejectedTransitions.get(), "All 4 attempts to revive CANCELLED order must be rejected");
    }
}
