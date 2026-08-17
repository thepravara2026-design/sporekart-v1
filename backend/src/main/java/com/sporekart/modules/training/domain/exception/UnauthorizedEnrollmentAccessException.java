package com.sporekart.modules.training.domain.exception;

public class UnauthorizedEnrollmentAccessException extends RuntimeException {
    public UnauthorizedEnrollmentAccessException(String message) {
        super(message);
    }
}
