package com.sporekart.modules.cart.domain.exception;

import com.sporekart.modules.cart.domain.CartStatus;

public class CartNotModifiableException extends RuntimeException {
    public CartNotModifiableException(CartStatus status) {
        super("Cart cannot be modified because its current status is: " + status);
    }
}
