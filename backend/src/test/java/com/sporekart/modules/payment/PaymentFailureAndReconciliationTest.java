package com.sporekart.modules.payment;

import com.sporekart.application.resilience.ResilienceProperties;
import com.sporekart.application.resilience.ResilientExecutor;
import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.payment.application.PaymentReconciliationService;
import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PaymentFailureAndReconciliationTest {

    @Autowired
    private ResilientExecutor resilientExecutor;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentReconciliationService reconciliationService;

    @Test
    @DisplayName("ResilientExecutor should retry transient failures using exponential backoff")
    void shouldRetryTransientFailures() {
        AtomicInteger attempts = new AtomicInteger(0);
        ResilienceProperties.Policy policy = new ResilienceProperties.Policy(100, 100, 3, 10, 1.5, 0.1);

        String result = resilientExecutor.executeWithRetry("test-payment-op", policy, () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new SocketTimeoutException("Simulated Razorpay gateway connection timeout");
            }
            return "SUCCESS";
        });

        assertThat(result).isEqualTo("SUCCESS");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("ResilientExecutor should fail fast without retrying permanent 400 bad request errors")
    void shouldFailFastOnPermanentErrors() {
        AtomicInteger attempts = new AtomicInteger(0);
        ResilienceProperties.Policy policy = new ResilienceProperties.Policy(100, 100, 3, 10, 1.5, 0.1);

        assertThatThrownBy(() -> resilientExecutor.executeWithRetry("test-invalid-op", policy, () -> {
            attempts.incrementAndGet();
            throw new IllegalArgumentException("Invalid payment credentials or payload");
        })).isInstanceOf(IllegalArgumentException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("PaymentReconciliationService should transition PENDING_RECONCILIATION payments to SUCCESS when attempt succeeds")
    void shouldReconcilePendingPayments() {
        AddressSnapshot addr = new AddressSnapshot("John Doe", "9999999999", "123 Main St", "Suite 4", "Bangalore", "Karnataka", "560001", "IN");
        OffsetDateTime now = OffsetDateTime.now();
        Order order = new Order(
                UUID.randomUUID(),
                "ORD-REC-" + UUID.randomUUID().toString().substring(0, 8),
                "CUST-REC-2",
                OrderStatus.CREATED,
                "INR",
                new BigDecimal("800.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("800.00"),
                "IDEMP-REC-1",
                addr,
                "Notes",
                List.of(),
                0L,
                now,
                now
        );
        orderRepository.save(order);

        String ref = "PAY-REC-" + UUID.randomUUID().toString().substring(0, 8);
        Payment pendingPayment = Payment.createNewPayment(ref, order.getId(), "CUST-REC-2", new BigDecimal("800.00"), "INR", PaymentProviderType.RAZORPAY);

        PaymentAttempt attempt = pendingPayment.createAttempt("ATT-REC-001");
        attempt.markSuccess("pay_rzp_confirmed_777", "sig_rzp_999");

        try {
            var field = Payment.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(pendingPayment, PaymentStatus.PENDING_RECONCILIATION);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        paymentRepository.save(pendingPayment);

        reconciliationService.reconcilePendingPayments();

        Payment reconciled = paymentRepository.findById(pendingPayment.getId()).orElseThrow();
        assertThat(reconciled.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}
