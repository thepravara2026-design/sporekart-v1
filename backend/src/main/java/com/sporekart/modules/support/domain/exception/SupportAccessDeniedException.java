package com.sporekart.modules.support.domain.exception;

public class SupportAccessDeniedException extends RuntimeException {
    public SupportAccessDeniedException(String message) {
        super(message);
    }
}
