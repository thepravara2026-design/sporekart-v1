package com.sporekart.modules.order.domain;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    CONFIRMED,
    PROCESSING,
    READY_FOR_FULFILMENT,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    PAYMENT_FAILED,
    EXPIRED;

    public boolean isTerminal() {
        return this == CANCELLED || this == EXPIRED || this == COMPLETED;
    }

    public boolean isCancellable() {
        return this == CREATED || this == PAYMENT_PENDING || this == CONFIRMED || this == PAID || this == PROCESSING;
    }
}
