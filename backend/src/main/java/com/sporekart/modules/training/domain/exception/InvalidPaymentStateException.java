package com.sporekart.modules.training.domain.exception;

public class InvalidPaymentStateException extends TrainingPaymentException {
    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
