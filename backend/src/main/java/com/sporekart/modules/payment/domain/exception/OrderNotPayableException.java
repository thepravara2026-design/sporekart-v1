package com.sporekart.modules.payment.domain.exception;

import java.util.UUID;

public class OrderNotPayableException extends RuntimeException {

    public OrderNotPayableException(UUID orderId, String reason) {
        super("Order " + orderId + " is not eligible for payment: " + reason);
    }
}
