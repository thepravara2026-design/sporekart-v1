package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.StockReservationItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "stock_reservation_items")
public class StockReservationItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private StockReservationEntity reservation;

    @Column(name = "inventory_item_id", nullable = false)
    private UUID inventoryItemId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public StockReservationItemEntity() {}

    public StockReservationItemEntity(
            UUID id,
            StockReservationEntity reservation,
            UUID inventoryItemId,
            UUID productId,
            UUID variantId,
            String sku,
            int quantity,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.reservation = reservation;
        this.inventoryItemId = inventoryItemId;
        this.productId = productId;
        this.variantId = variantId;
        this.sku = sku;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public static StockReservationItemEntity fromDomain(StockReservationItem item, StockReservationEntity reservationEntity) {
        return new StockReservationItemEntity(
                item.getId(),
                reservationEntity,
                item.getInventoryItemId(),
                item.getProductId(),
                item.getVariantId(),
                item.getSku(),
                item.getQuantity(),
                item.getCreatedAt()
        );
    }

    public StockReservationItem toDomain() {
        return new StockReservationItem(
                this.id,
                this.reservation != null ? this.reservation.getId() : null,
                this.inventoryItemId,
                this.productId,
                this.variantId,
                this.sku,
                this.quantity,
                this.createdAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public StockReservationEntity getReservation() { return reservation; }
    public void setReservation(StockReservationEntity reservation) { this.reservation = reservation; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockReservationItemEntity that = (StockReservationItemEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
