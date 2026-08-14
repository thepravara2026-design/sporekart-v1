package com.sporekart.modules.cart.domain.exception;

import java.util.UUID;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(UUID id) {
        super("Cart not found with ID: " + id);
    }

    public CartNotFoundException(String customerId) {
        super("Active cart not found for customer: " + customerId);
    }
}
