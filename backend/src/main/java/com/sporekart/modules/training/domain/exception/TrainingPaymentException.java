package com.sporekart.modules.training.domain.exception;

public class TrainingPaymentException extends RuntimeException {
    public TrainingPaymentException(String message) {
        super(message);
    }
    public TrainingPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
