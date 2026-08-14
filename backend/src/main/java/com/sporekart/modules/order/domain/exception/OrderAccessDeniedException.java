package com.sporekart.modules.order.domain.exception;

public class OrderAccessDeniedException extends RuntimeException {
    public OrderAccessDeniedException(String message) {
        super(message);
    }
}
