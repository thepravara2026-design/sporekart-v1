package com.sporekart.modules.training.domain.exception;

public class DuplicateDemandException extends RuntimeException {
    public DuplicateDemandException(String message) {
        super(message);
    }
}
