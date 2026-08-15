package com.sporekart.modules.inventory.application.dto;

import com.sporekart.modules.inventory.domain.MovementType;
import com.sporekart.modules.inventory.domain.StockMovement;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StockMovementDto(
        UUID id,
        UUID inventoryItemId,
        MovementType movementType,
        int quantity,
        String referenceType,
        String referenceId,
        int previousOnHand,
        int resultingOnHand,
        int previousReserved,
        int resultingReserved,
        OffsetDateTime createdAt
) {
    public static StockMovementDto fromDomain(StockMovement movement) {
        return new StockMovementDto(
                movement.getId(),
                movement.getInventoryItemId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getPreviousOnHand(),
                movement.getResultingOnHand(),
                movement.getPreviousReserved(),
                movement.getResultingReserved(),
                movement.getCreatedAt()
        );
    }
}
