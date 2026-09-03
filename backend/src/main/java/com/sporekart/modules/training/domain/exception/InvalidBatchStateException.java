package com.sporekart.modules.training.domain.exception;

public class InvalidBatchStateException extends RuntimeException {
    public InvalidBatchStateException(String message) {
        super(message);
    }
}
