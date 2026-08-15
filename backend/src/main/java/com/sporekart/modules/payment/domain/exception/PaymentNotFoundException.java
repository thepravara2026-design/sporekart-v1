package com.sporekart.modules.payment.domain.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(UUID paymentId) {
        super("Payment record not found with ID: " + paymentId);
    }

    public PaymentNotFoundException(String reference) {
        super("Payment record not found with reference: " + reference);
    }
}
