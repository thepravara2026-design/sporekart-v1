package com.sporekart.modules.order;

import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderStateMachine;
import com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException;
import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders & Payments Domain Boundary Protection Suite")
class OrdersPaymentsDomainBoundaryTest {

    @Test
    @DisplayName("Order state machine permits valid transitions and self-transitions")
    void testOrderStateMachineValidTransitions() {
        assertTrue(OrderStateMachine.isTransitionAllowed(OrderStatus.CREATED, OrderStatus.CONFIRMED));
        assertTrue(OrderStateMachine.isTransitionAllowed(OrderStatus.CONFIRMED, OrderStatus.PROCESSING));
        assertTrue(OrderStateMachine.isTransitionAllowed(OrderStatus.PROCESSING, OrderStatus.READY_FOR_FULFILMENT));
        assertTrue(OrderStateMachine.isTransitionAllowed(OrderStatus.SHIPPED, OrderStatus.DELIVERED));
        assertTrue(OrderStateMachine.isTransitionAllowed(OrderStatus.DELIVERED, OrderStatus.DELIVERED)); // Idempotent
    }

    @Test
    @DisplayName("Order state machine rejects illegal state transitions")
    void testOrderStateMachineInvalidTransitions() {
        assertFalse(OrderStateMachine.isTransitionAllowed(OrderStatus.COMPLETED, OrderStatus.CREATED));
        assertFalse(OrderStateMachine.isTransitionAllowed(OrderStatus.CANCELLED, OrderStatus.CONFIRMED));

        assertThrows(InvalidOrderStateTransitionException.class, () ->
                OrderStateMachine.validateTransition(UUID.randomUUID(), OrderStatus.COMPLETED, OrderStatus.PROCESSING)
        );
    }

    @Test
    @DisplayName("Payment domain model enforces positive non-zero amounts")
    void testPaymentAmountValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                Payment.createNewPayment("PAY-100", UUID.randomUUID(), "cust-1", BigDecimal.ZERO, "INR", PaymentProviderType.RAZORPAY)
        );
        assertThrows(IllegalArgumentException.class, () ->
                Payment.createNewPayment("PAY-101", UUID.randomUUID(), "cust-1", new BigDecimal("-10.00"), "INR", PaymentProviderType.RAZORPAY)
        );
    }

    @Test
    @DisplayName("Payment domain model correctly transitions status and prevents duplicate success/failure conflicts")
    void testPaymentStatusTransitions() {
        Payment payment = Payment.createNewPayment("PAY-102", UUID.randomUUID(), "cust-1", new BigDecimal("100.00"), "INR", PaymentProviderType.RAZORPAY);
        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        payment.markSuccess(null, "pay_prov_123", "sig_xyz");
        assertTrue(payment.isSuccessful());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());

        // Idempotent re-mark
        payment.markSuccess(null, "pay_prov_123", "sig_xyz");
        assertTrue(payment.isSuccessful());
    }
}
