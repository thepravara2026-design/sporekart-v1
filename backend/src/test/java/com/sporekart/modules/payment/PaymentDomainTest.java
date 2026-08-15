package com.sporekart.modules.payment;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.domain.exception.PaymentInvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDomainTest {

    @Test
    @DisplayName("Payment aggregate should initialize with CREATED status and enforce invariants")
    void testPaymentInitialization() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-100";
        BigDecimal amount = new BigDecimal("1500.00");

        Payment payment = Payment.createNewPayment("PAY-SPK-001", orderId, customerId, amount, "INR", PaymentProviderType.MOCK);

        assertEquals("PAY-SPK-001", payment.getPaymentReference());
        assertEquals(orderId, payment.getOrderId());
        assertEquals(customerId, payment.getCustomerId());
        assertEquals(amount, payment.getAmount());
        assertEquals(PaymentStatus.CREATED, payment.getStatus());
        assertFalse(payment.isSuccessful());
    }

    @Test
    @DisplayName("Should create payment attempt and transition payment status to PENDING")
    void testCreateAttempt() {
        Payment payment = Payment.createNewPayment("PAY-SPK-002", UUID.randomUUID(), "cust-100", new BigDecimal("500.00"), "INR", PaymentProviderType.MOCK);
        PaymentAttempt attempt = payment.createAttempt("PAY-SPK-002-ATT-1");

        assertNotNull(attempt);
        assertEquals(attempt.getId(), payment.getActiveAttemptId());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(1, payment.getAttempts().size());
    }

    @Test
    @DisplayName("Should mark payment SUCCESS and enforce terminal immutability")
    void testPaymentSuccessAndImmutability() {
        Payment payment = Payment.createNewPayment("PAY-SPK-003", UUID.randomUUID(), "cust-100", new BigDecimal("500.00"), "INR", PaymentProviderType.MOCK);
        PaymentAttempt attempt = payment.createAttempt("PAY-SPK-003-ATT-1");

        payment.markSuccess(attempt.getId(), "pay_mock_123", "sig_mock_456");

        assertTrue(payment.isSuccessful());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());

        // Attempting to downgrade SUCCESS to FAILED must throw PaymentInvalidStateException
        assertThrows(PaymentInvalidStateException.class, () -> payment.markFailed(attempt.getId(), "ERR", "Late failure"));
        assertThrows(PaymentInvalidStateException.class, () -> payment.markCancelled("Late cancel"));
    }
}
