package com.sporekart.modules.shipment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ShipmentTrackingEvent {
    private final UUID id;
    private final UUID shipmentId;
    private final String providerEventId;
    private final String providerStatus;
    private final ShipmentStatus normalizedStatus;
    private final String description;
    private final String location;
    private final Instant occurredAt;
    private final Instant receivedAt;
    private final Instant createdAt;

    public ShipmentTrackingEvent(
            UUID id,
            UUID shipmentId,
            String providerEventId,
            String providerStatus,
            ShipmentStatus normalizedStatus,
            String description,
            String location,
            Instant occurredAt,
            Instant receivedAt,
            Instant createdAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.shipmentId = Objects.requireNonNull(shipmentId, "shipmentId required");
        this.providerEventId = Objects.requireNonNull(providerEventId, "providerEventId required");
        this.providerStatus = Objects.requireNonNull(providerStatus, "providerStatus required");
        this.normalizedStatus = Objects.requireNonNull(normalizedStatus, "normalizedStatus required");
        this.description = description;
        this.location = location;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.receivedAt = receivedAt != null ? receivedAt : Instant.now();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getShipmentId() { return shipmentId; }
    public String getProviderEventId() { return providerEventId; }
    public String getProviderStatus() { return providerStatus; }
    public ShipmentStatus getNormalizedStatus() { return normalizedStatus; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
