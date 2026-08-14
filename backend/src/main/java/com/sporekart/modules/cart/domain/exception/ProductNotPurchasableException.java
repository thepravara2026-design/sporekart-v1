package com.sporekart.modules.cart.domain.exception;

import java.util.UUID;

public class ProductNotPurchasableException extends RuntimeException {
    public ProductNotPurchasableException(UUID productId, String reason) {
        super("Product " + productId + " is not purchasable: " + reason);
    }
}
