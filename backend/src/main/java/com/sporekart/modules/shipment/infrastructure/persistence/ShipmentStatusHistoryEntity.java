package com.sporekart.modules.shipment.infrastructure.persistence;

import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShipmentStatusHistory;
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
@Table(name = "shipment_status_history")
public class ShipmentStatusHistoryEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private ShipmentEntity shipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private ShipmentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private ShipmentStatus newStatus;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false)
    private OrderActorType actorType;

    @Column(name = "actor_id")
    private String actorId;

    @Column(name = "provider_event_id")
    private String providerEventId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ShipmentStatusHistoryEntity() {}

    public static ShipmentStatusHistoryEntity fromDomain(ShipmentStatusHistory history, ShipmentEntity shipment) {
        ShipmentStatusHistoryEntity entity = new ShipmentStatusHistoryEntity();
        entity.id = history.getId();
        entity.shipment = shipment;
        entity.previousStatus = history.getPreviousStatus();
        entity.newStatus = history.getNewStatus();
        entity.reason = history.getReason();
        entity.actorType = history.getActorType();
        entity.actorId = history.getActorId();
        entity.providerEventId = history.getProviderEventId();
        entity.correlationId = history.getCorrelationId();
        entity.createdAt = history.getCreatedAt();
        return entity;
    }

    public ShipmentStatusHistory toDomain() {
        return new ShipmentStatusHistory(
                id,
                shipment != null ? shipment.getId() : null,
                previousStatus,
                newStatus,
                reason,
                actorType,
                actorId,
                providerEventId,
                correlationId,
                createdAt
        );
    }
}
