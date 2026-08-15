package com.sporekart.modules.payment.domain.exception;

public class PaymentVerificationFailedException extends RuntimeException {

    public PaymentVerificationFailedException(String message) {
        super(message);
    }
}
