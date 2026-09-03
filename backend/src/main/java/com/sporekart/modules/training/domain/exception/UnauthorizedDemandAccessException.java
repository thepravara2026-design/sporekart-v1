package com.sporekart.modules.training.domain.exception;

public class UnauthorizedDemandAccessException extends RuntimeException {
    public UnauthorizedDemandAccessException(String message) {
        super(message);
    }
}
