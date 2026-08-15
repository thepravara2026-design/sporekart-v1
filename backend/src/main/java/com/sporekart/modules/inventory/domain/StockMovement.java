package com.sporekart.modules.inventory.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class StockMovement {

    private final UUID id;
    private final UUID inventoryItemId;
    private final MovementType movementType;
    private final int quantity;
    private final String referenceType;
    private final String referenceId;
    private final int previousOnHand;
    private final int resultingOnHand;
    private final int previousReserved;
    private final int resultingReserved;
    private final OffsetDateTime createdAt;

    public StockMovement(
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
        this.id = Objects.requireNonNull(id, "Movement ID cannot be null");
        this.inventoryItemId = Objects.requireNonNull(inventoryItemId, "Inventory Item ID cannot be null");
        this.movementType = Objects.requireNonNull(movementType, "Movement Type cannot be null");
        this.quantity = quantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.previousOnHand = previousOnHand;
        this.resultingOnHand = resultingOnHand;
        this.previousReserved = previousReserved;
        this.resultingReserved = resultingReserved;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    public static StockMovement recordMovement(
            UUID inventoryItemId,
            MovementType type,
            int quantity,
            String referenceType,
            String referenceId,
            int prevOnHand,
            int newOnHand,
            int prevReserved,
            int newReserved
    ) {
        return new StockMovement(
                UUID.randomUUID(), inventoryItemId, type, quantity, referenceType, referenceId,
                prevOnHand, newOnHand, prevReserved, newReserved, OffsetDateTime.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getInventoryItemId() { return inventoryItemId; }
    public MovementType getMovementType() { return movementType; }
    public int getQuantity() { return quantity; }
    public String getReferenceType() { return referenceType; }
    public String getReferenceId() { return referenceId; }
    public int getPreviousOnHand() { return previousOnHand; }
    public int getResultingOnHand() { return resultingOnHand; }
    public int getPreviousReserved() { return previousReserved; }
    public int getResultingReserved() { return resultingReserved; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockMovement movement = (StockMovement) o;
        return Objects.equals(id, movement.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
