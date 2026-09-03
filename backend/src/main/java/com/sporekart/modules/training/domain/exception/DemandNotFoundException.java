package com.sporekart.modules.training.domain.exception;

public class DemandNotFoundException extends RuntimeException {
    public DemandNotFoundException(String message) {
        super(message);
    }
}
