package com.sporekart.modules.payment;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentConcurrencyTest {

    @Test
    @DisplayName("Concurrent payment verification requests must safely handle state transitions idempotently")
    void testConcurrentPaymentVerification() throws InterruptedException {
        Payment payment = Payment.createNewPayment("PAY-CONCUR-01", UUID.randomUUID(), "cust-1", new BigDecimal("1500.00"), "INR", PaymentProviderType.MOCK);
        PaymentAttempt attempt = payment.createAttempt("PAY-CONCUR-01-ATT-1");

        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCalls = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    synchronized (payment) {
                        if (!payment.isSuccessful()) {
                            payment.markSuccess(attempt.getId(), "pay_mock_123", "sig_valid");
                            successCalls.incrementAndGet();
                        }
                    }
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

        assertEquals(1, successCalls.get(), "Exactly 1 thread should perform the SUCCESS transition");
        assertTrue(payment.isSuccessful());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
    }
}
