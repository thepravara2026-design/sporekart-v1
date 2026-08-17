package com.sporekart.modules.training.domain.exception;

public class InvalidTrainingStateException extends RuntimeException {
    public InvalidTrainingStateException(String message) {
        super(message);
    }
}
