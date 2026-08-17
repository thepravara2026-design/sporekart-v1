package com.sporekart.modules.training.domain.exception;

public class RescheduleWindowExpiredException extends RuntimeException {
    public RescheduleWindowExpiredException(String message) {
        super(message);
    }
}
