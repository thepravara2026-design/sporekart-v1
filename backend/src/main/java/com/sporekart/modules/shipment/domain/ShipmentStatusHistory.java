package com.sporekart.modules.shipment.domain;

import com.sporekart.modules.order.domain.OrderActorType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ShipmentStatusHistory {
    private final UUID id;
    private final UUID shipmentId;
    private final ShipmentStatus previousStatus;
    private final ShipmentStatus newStatus;
    private final String reason;
    private final OrderActorType actorType;
    private final String actorId;
    private final String providerEventId;
    private final String correlationId;
    private final Instant createdAt;

    public ShipmentStatusHistory(
            UUID id,
            UUID shipmentId,
            ShipmentStatus previousStatus,
            ShipmentStatus newStatus,
            String reason,
            OrderActorType actorType,
            String actorId,
            String providerEventId,
            String correlationId,
            Instant createdAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.shipmentId = Objects.requireNonNull(shipmentId, "shipmentId required");
        this.previousStatus = previousStatus;
        this.newStatus = Objects.requireNonNull(newStatus, "newStatus required");
        this.reason = reason;
        this.actorType = actorType != null ? actorType : OrderActorType.SYSTEM;
        this.actorId = actorId != null ? actorId : "SYSTEM";
        this.providerEventId = providerEventId;
        this.correlationId = correlationId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getShipmentId() { return shipmentId; }
    public ShipmentStatus getPreviousStatus() { return previousStatus; }
    public ShipmentStatus getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
    public OrderActorType getActorType() { return actorType; }
    public String getActorId() { return actorId; }
    public String getProviderEventId() { return providerEventId; }
    public String getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
}
