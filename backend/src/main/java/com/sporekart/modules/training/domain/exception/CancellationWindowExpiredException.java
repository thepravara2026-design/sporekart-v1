package com.sporekart.modules.training.domain.exception;

public class CancellationWindowExpiredException extends RuntimeException {
    public CancellationWindowExpiredException(String message) {
        super(message);
    }
}
