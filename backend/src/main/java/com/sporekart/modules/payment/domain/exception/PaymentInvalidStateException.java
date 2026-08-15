package com.sporekart.modules.payment.domain.exception;

import com.sporekart.modules.payment.domain.PaymentStatus;

import java.util.UUID;

public class PaymentInvalidStateException extends RuntimeException {

    public PaymentInvalidStateException(UUID paymentId, PaymentStatus currentStatus, PaymentStatus targetStatus) {
        super("Cannot transition payment " + paymentId + " from status " + currentStatus + " to " + targetStatus);
    }

    public PaymentInvalidStateException(String message) {
        super(message);
    }
}
