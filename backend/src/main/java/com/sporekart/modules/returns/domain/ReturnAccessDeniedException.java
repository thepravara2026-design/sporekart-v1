package com.sporekart.modules.returns.domain;

public class ReturnAccessDeniedException extends RuntimeException {
    public ReturnAccessDeniedException(String message) {
        super(message);
    }
}
