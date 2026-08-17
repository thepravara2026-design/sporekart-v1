package com.sporekart.modules.training.domain.exception;

public class BatchFullException extends RuntimeException {
    public BatchFullException(String message) {
        super(message);
    }
}
