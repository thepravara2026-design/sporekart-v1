package com.sporekart.modules.order.domain.exception;

import com.sporekart.modules.order.domain.OrderStatus;

import java.util.UUID;

public class InvalidOrderStateTransitionException extends RuntimeException {

    public InvalidOrderStateTransitionException(UUID orderId, OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Invalid order state transition for order " + orderId + ": cannot transition from " + currentStatus + " to " + targetStatus);
    }

    public InvalidOrderStateTransitionException(String message) {
        super(message);
    }
}
