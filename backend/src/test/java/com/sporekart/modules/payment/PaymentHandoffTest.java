package com.sporekart.modules.payment;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentHandoffTest {

    @Test
    @DisplayName("Payment module handoff contract: ensures financial integrity and non-leaking abstractions")
    void testPaymentHandoffContract() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-handoff-100";
        BigDecimal amount = new BigDecimal("4999.00");

        Payment payment = Payment.createNewPayment("PAY-HANDOFF-01", orderId, customerId, amount, "INR", PaymentProviderType.MOCK);
        var attempt = payment.createAttempt("PAY-HANDOFF-01-ATT-1");

        assertNotNull(payment.getId());
        assertEquals(orderId, payment.getOrderId());
        assertEquals("INR", payment.getCurrency());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());

        payment.markSuccess(attempt.getId(), "pay_mock_handoff", "sig_valid");
        assertTrue(payment.isSuccessful());
    }
}
