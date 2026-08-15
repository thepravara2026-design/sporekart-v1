package com.sporekart.modules.shipment.application.dto;

import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShipmentStatusHistory;

import java.time.Instant;
import java.util.UUID;

public record ShipmentStatusHistoryDto(
        UUID id,
        ShipmentStatus previousStatus,
        ShipmentStatus newStatus,
        String reason,
        OrderActorType actorType,
        String actorId,
        String providerEventId,
        Instant createdAt
) {
    public static ShipmentStatusHistoryDto fromDomain(ShipmentStatusHistory history) {
        return new ShipmentStatusHistoryDto(
                history.getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getReason(),
                history.getActorType(),
                history.getActorId(),
                history.getProviderEventId(),
                history.getCreatedAt()
        );
    }
}
