package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.MovementType;
import com.sporekart.modules.inventory.domain.StockMovement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovementEntity {

    @Id
    private UUID id;

    @Column(name = "inventory_item_id", nullable = false)
    private UUID inventoryItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "previous_on_hand", nullable = false)
    private int previousOnHand;

    @Column(name = "resulting_on_hand", nullable = false)
    private int resultingOnHand;

    @Column(name = "previous_reserved", nullable = false)
    private int previousReserved;

    @Column(name = "resulting_reserved", nullable = false)
    private int resultingReserved;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public StockMovementEntity() {}

    public StockMovementEntity(
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
        this.id = id;
        this.inventoryItemId = inventoryItemId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.previousOnHand = previousOnHand;
        this.resultingOnHand = resultingOnHand;
        this.previousReserved = previousReserved;
        this.resultingReserved = resultingReserved;
        this.createdAt = createdAt;
    }

    public static StockMovementEntity fromDomain(StockMovement movement) {
        return new StockMovementEntity(
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

    public StockMovement toDomain() {
        return new StockMovement(
                this.id,
                this.inventoryItemId,
                this.movementType,
                this.quantity,
                this.referenceType,
                this.referenceId,
                this.previousOnHand,
                this.resultingOnHand,
                this.previousReserved,
                this.resultingReserved,
                this.createdAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public int getPreviousOnHand() { return previousOnHand; }
    public void setPreviousOnHand(int previousOnHand) { this.previousOnHand = previousOnHand; }

    public int getResultingOnHand() { return resultingOnHand; }
    public void setResultingOnHand(int resultingOnHand) { this.resultingOnHand = resultingOnHand; }

    public int getPreviousReserved() { return previousReserved; }
    public void setPreviousReserved(int previousReserved) { this.previousReserved = previousReserved; }

    public int getResultingReserved() { return resultingReserved; }
    public void setResultingReserved(int resultingReserved) { this.resultingReserved = resultingReserved; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockMovementEntity that = (StockMovementEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
