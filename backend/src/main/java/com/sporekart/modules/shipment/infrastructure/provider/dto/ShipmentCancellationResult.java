package com.sporekart.modules.shipment.infrastructure.provider.dto;

public record ShipmentCancellationResult(
        boolean success,
        String message
) {
    public static ShipmentCancellationResult success(String message) {
        return new ShipmentCancellationResult(true, message);
    }
    public static ShipmentCancellationResult failure(String message) {
        return new ShipmentCancellationResult(false, message);
    }
}
