package com.sporekart.modules.training.domain.exception;

public class InvalidEnrollmentStateException extends RuntimeException {
    public InvalidEnrollmentStateException(String message) {
        super(message);
    }
}
