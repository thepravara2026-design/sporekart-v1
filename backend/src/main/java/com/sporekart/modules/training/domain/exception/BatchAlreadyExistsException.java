package com.sporekart.modules.training.domain.exception;

public class BatchAlreadyExistsException extends RuntimeException {
    public BatchAlreadyExistsException(String message) {
        super(message);
    }
}
