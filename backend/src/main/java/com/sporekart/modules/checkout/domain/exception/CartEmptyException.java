package com.sporekart.modules.checkout.domain.exception;

import java.util.UUID;

public class CartEmptyException extends RuntimeException {
    private final UUID cartId;

    public CartEmptyException(UUID cartId) {
        super("Cart is empty and cannot be processed for checkout preview: " + cartId);
        this.cartId = cartId;
    }

    public UUID getCartId() {
        return cartId;
    }
}
