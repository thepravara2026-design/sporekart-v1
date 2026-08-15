package com.sporekart.modules.inventory.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class StockReservationItem {

    private final UUID id;
    private final UUID reservationId;
    private final UUID inventoryItemId;
    private final UUID productId;
    private final UUID variantId;
    private final String sku;
    private final int quantity;
    private final OffsetDateTime createdAt;

    public StockReservationItem(
            UUID id,
            UUID reservationId,
            UUID inventoryItemId,
            UUID productId,
            UUID variantId,
            String sku,
            int quantity,
            OffsetDateTime createdAt
    ) {
        this.id = Objects.requireNonNull(id, "Reservation Item ID cannot be null");
        this.reservationId = Objects.requireNonNull(reservationId, "Reservation ID cannot be null");
        this.inventoryItemId = Objects.requireNonNull(inventoryItemId, "Inventory Item ID cannot be null");
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        this.variantId = variantId;
        this.sku = Objects.requireNonNull(sku, "SKU cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getReservationId() { return reservationId; }
    public UUID getInventoryItemId() { return inventoryItemId; }
    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockReservationItem that = (StockReservationItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
