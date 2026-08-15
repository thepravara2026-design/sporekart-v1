package com.sporekart.modules.shipment.domain;

public class InvalidShipmentStateTransitionException extends RuntimeException {
    public InvalidShipmentStateTransitionException(ShipmentStatus from, ShipmentStatus to, String reason) {
        super(String.format("Cannot transition shipment state from %s to %s: %s", from, to, reason));
    }
}
