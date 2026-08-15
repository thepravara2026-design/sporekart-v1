package com.sporekart.modules.shipment.infrastructure.provider.dto;

import com.sporekart.modules.shipment.domain.ShipmentStatus;

import java.time.Instant;

public record NormalizedWebhookEvent(
        String providerEventId,
        String providerShipmentId,
        String awb,
        String orderReference,
        String rawProviderStatus,
        ShipmentStatus normalizedStatus,
        String description,
        String location,
        Instant occurredAt
) {}
