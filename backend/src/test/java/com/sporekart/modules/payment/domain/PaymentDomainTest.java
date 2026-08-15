package com.sporekart.modules.payment.domain;

import com.sporekart.modules.payment.domain.exception.PaymentInvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentDomainTest {

    private Payment createTestPayment() {
        return Payment.createNewPayment(
                "PAY-4B-100", UUID.randomUUID(), "cust-4b-user", new BigDecimal("1499.00"), "INR", PaymentProviderType.RAZORPAY
        );
    }

    @Test
    @DisplayName("Valid Payment Creation Sets CREATED Status")
    void testPaymentCreationInitialState() {
        Payment payment = createTestPayment();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("1499.00"));
        assertThat(payment.getCurrency()).isEqualTo("INR");
    }

    @Test
    @DisplayName("Creating Payment Attempt Transitions Status to PENDING")
    void testCreateAttemptTransitionsToPending() {
        Payment payment = createTestPayment();
        PaymentAttempt attempt = payment.createAttempt("PAY-4B-100-ATT-1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(attempt.getAttemptReference()).isEqualTo("PAY-4B-100-ATT-1");
        assertThat(payment.getActiveAttemptId()).isEqualTo(attempt.getId());
    }

    @Test
    @DisplayName("Marking Payment Success Sets SUCCESS Status and Is Immutable")
    void testPaymentSuccessStateAndImmutability() {
        Payment payment = createTestPayment();
        PaymentAttempt attempt = payment.createAttempt("PAY-4B-100-ATT-1");

        payment.markSuccess(attempt.getId(), "pay_rzp_123456", "sig_rzp_mock");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.isSuccessful()).isTrue();

        // Idempotent success replay does not throw
        payment.markSuccess(attempt.getId(), "pay_rzp_123456", "sig_rzp_mock");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        // Subsequent markFailed throws PaymentInvalidStateException
        assertThatThrownBy(() -> payment.markFailed(attempt.getId(), "FAILED", "Attempting failure on success"))
                .isInstanceOf(PaymentInvalidStateException.class);
    }

    @Test
    @DisplayName("Marking Payment Failed Updates Status and Attempt")
    void testPaymentFailedState() {
        Payment payment = createTestPayment();
        PaymentAttempt attempt = payment.createAttempt("PAY-4B-100-ATT-1");

        payment.markFailed(attempt.getId(), "DECLINED", "Card issuer declined transaction");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(attempt.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}
