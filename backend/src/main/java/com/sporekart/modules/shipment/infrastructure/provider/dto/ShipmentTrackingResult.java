package com.sporekart.modules.shipment.infrastructure.provider.dto;

import com.sporekart.modules.shipment.domain.ShipmentStatus;

import java.time.Instant;
import java.util.List;

public record ShipmentTrackingResult(
        boolean success,
        String providerShipmentId,
        String awb,
        ShipmentStatus currentStatus,
        List<TrackingCheckpoint> checkpoints,
        String errorMessage
) {
    public record TrackingCheckpoint(
            String eventId,
            String providerStatus,
            ShipmentStatus normalizedStatus,
            String description,
            String location,
            Instant timestamp
    ) {}
}
