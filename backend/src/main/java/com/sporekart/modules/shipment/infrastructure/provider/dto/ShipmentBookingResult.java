package com.sporekart.modules.shipment.infrastructure.provider.dto;

import java.time.Instant;

public record ShipmentBookingResult(
        boolean success,
        String providerShipmentId,
        String awb,
        String trackingNumber,
        String courierName,
        String courierCode,
        Instant estimatedDeliveryAt,
        String errorMessage
) {
    public static ShipmentBookingResult success(
            String providerShipmentId,
            String awb,
            String trackingNumber,
            String courierName,
            String courierCode,
            Instant estimatedDeliveryAt
    ) {
        return new ShipmentBookingResult(true, providerShipmentId, awb, trackingNumber, courierName, courierCode, estimatedDeliveryAt, null);
    }

    public static ShipmentBookingResult failure(String errorMessage) {
        return new ShipmentBookingResult(false, null, null, null, null, null, null, errorMessage);
    }
}
