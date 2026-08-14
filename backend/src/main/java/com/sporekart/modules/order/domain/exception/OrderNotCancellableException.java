package com.sporekart.modules.order.domain.exception;

import com.sporekart.modules.order.domain.OrderStatus;

import java.util.UUID;

public class OrderNotCancellableException extends RuntimeException {
    public OrderNotCancellableException(UUID orderId, OrderStatus status) {
        super("Order " + orderId + " cannot be cancelled because current status is: " + status);
    }
}
