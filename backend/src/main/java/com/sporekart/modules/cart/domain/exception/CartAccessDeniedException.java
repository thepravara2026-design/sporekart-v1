package com.sporekart.modules.cart.domain.exception;

public class CartAccessDeniedException extends RuntimeException {
    public CartAccessDeniedException(String message) {
        super(message);
    }
}
