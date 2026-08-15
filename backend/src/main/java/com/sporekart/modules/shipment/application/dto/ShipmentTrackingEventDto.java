package com.sporekart.modules.shipment.application.dto;

import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShipmentTrackingEvent;

import java.time.Instant;
import java.util.UUID;

public record ShipmentTrackingEventDto(
        UUID id,
        String providerEventId,
        String providerStatus,
        ShipmentStatus normalizedStatus,
        String description,
        String location,
        Instant occurredAt
) {
    public static ShipmentTrackingEventDto fromDomain(ShipmentTrackingEvent event) {
        return new ShipmentTrackingEventDto(
                event.getId(),
                event.getProviderEventId(),
                event.getProviderStatus(),
                event.getNormalizedStatus(),
                event.getDescription(),
                event.getLocation(),
                event.getOccurredAt()
        );
    }
}
