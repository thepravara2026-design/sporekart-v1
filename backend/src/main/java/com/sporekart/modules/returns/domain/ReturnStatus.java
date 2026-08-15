package com.sporekart.modules.returns.domain;

public enum ReturnStatus {
    REQUESTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED,
    REVERSE_SHIPMENT_CREATED,
    PICKUP_SCHEDULED,
    PICKUP_FAILED,
    PICKED_UP,
    IN_TRANSIT,
    RECEIVED,
    INSPECTION_PENDING,
    INSPECTED,
    ACCEPTED,
    PARTIALLY_ACCEPTED,
    RETURN_REJECTED,
    REFUND_PENDING,
    REFUNDED,
    EXCEPTION;

    public boolean isTerminal() {
        return this == REJECTED || this == CANCELLED || this == RETURN_REJECTED || this == REFUNDED;
    }
}
