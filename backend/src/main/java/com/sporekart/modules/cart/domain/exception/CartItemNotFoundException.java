package com.sporekart.modules.cart.domain.exception;

import java.util.UUID;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(UUID itemId) {
        super("Cart item not found with ID: " + itemId);
    }
}
