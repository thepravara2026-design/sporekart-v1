package com.sporekart.modules.security.domain.exception;

public class SecurityAccessDeniedException extends RuntimeException {
    public SecurityAccessDeniedException(String message) {
        super(message);
    }
}
