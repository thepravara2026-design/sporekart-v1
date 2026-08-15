package com.sporekart.modules.shipment.infrastructure.persistence;

import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShipmentTrackingEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipment_tracking_events")
public class ShipmentTrackingEventEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private ShipmentEntity shipment;

    @Column(name = "provider_event_id", nullable = false)
    private String providerEventId;

    @Column(name = "provider_status", nullable = false)
    private String providerStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "normalized_status", nullable = false)
    private ShipmentStatus normalizedStatus;

    @Column(name = "description")
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ShipmentTrackingEventEntity() {}

    public static ShipmentTrackingEventEntity fromDomain(ShipmentTrackingEvent event, ShipmentEntity shipment) {
        ShipmentTrackingEventEntity entity = new ShipmentTrackingEventEntity();
        entity.id = event.getId();
        entity.shipment = shipment;
        entity.providerEventId = event.getProviderEventId();
        entity.providerStatus = event.getProviderStatus();
        entity.normalizedStatus = event.getNormalizedStatus();
        entity.description = event.getDescription();
        entity.location = event.getLocation();
        entity.occurredAt = event.getOccurredAt();
        entity.receivedAt = event.getReceivedAt();
        entity.createdAt = event.getCreatedAt();
        return entity;
    }

    public ShipmentTrackingEvent toDomain() {
        return new ShipmentTrackingEvent(
                id,
                shipment != null ? shipment.getId() : null,
                providerEventId,
                providerStatus,
                normalizedStatus,
                description,
                location,
                occurredAt,
                receivedAt,
                createdAt
        );
    }
}
