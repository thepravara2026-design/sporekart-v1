package com.sporekart.modules.shipment.domain;

public enum ShipmentStatus {
    CREATED,
    READY_FOR_BOOKING,
    BOOKING_PENDING,
    BOOKED,
    PICKUP_SCHEDULED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELIVERY_FAILED,
    CANCELLED,
    RTO_INITIATED,
    RTO_IN_TRANSIT,
    RTO_DELIVERED,
    EXCEPTION;

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED || this == RTO_DELIVERED;
    }
}
