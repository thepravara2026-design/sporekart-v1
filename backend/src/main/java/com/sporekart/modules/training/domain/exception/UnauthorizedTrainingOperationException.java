package com.sporekart.modules.training.domain.exception;

public class UnauthorizedTrainingOperationException extends RuntimeException {
    public UnauthorizedTrainingOperationException(String message) {
        super(message);
    }
}
