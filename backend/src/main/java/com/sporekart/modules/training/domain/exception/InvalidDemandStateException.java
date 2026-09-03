package com.sporekart.modules.training.domain.exception;

public class InvalidDemandStateException extends RuntimeException {
    public InvalidDemandStateException(String message) {
        super(message);
    }
}
