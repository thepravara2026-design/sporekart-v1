package com.sporekart.modules.shipment.domain.event;

import com.sporekart.modules.shipment.domain.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

public record ShipmentLifecycleEvent(
        UUID shipmentId,
        String shipmentReference,
        UUID orderId,
        String orderReference,
        String customerId,
        ShipmentStatus previousStatus,
        ShipmentStatus newStatus,
        String awb,
        String courierName,
        String reason,
        Instant occurredAt
) {
    public static ShipmentLifecycleEvent of(
            UUID shipmentId,
            String shipmentReference,
            UUID orderId,
            String orderReference,
            String customerId,
            ShipmentStatus previousStatus,
            ShipmentStatus newStatus,
            String awb,
            String courierName,
            String reason
    ) {
        return new ShipmentLifecycleEvent(
                shipmentId,
                shipmentReference,
                orderId,
                orderReference,
                customerId,
                previousStatus,
                newStatus,
                awb,
                courierName,
                reason,
                Instant.now()
        );
    }
}
