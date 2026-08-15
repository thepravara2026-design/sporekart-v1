package com.sporekart.modules.inventory.domain;

import com.sporekart.modules.inventory.domain.exception.InsufficientStockException;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class InventoryItem {

    private final UUID id;
    private final UUID productId;
    private final UUID variantId;
    private final String sku;
    private int onHandQuantity;
    private int reservedQuantity;
    private String status;
    private Long version;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public InventoryItem(
            UUID id,
            UUID productId,
            UUID variantId,
            String sku,
            int onHandQuantity,
            int reservedQuantity,
            String status,
            Long version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Inventory ID cannot be null");
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        this.variantId = variantId;
        this.sku = Objects.requireNonNull(sku, "SKU cannot be null");
        if (onHandQuantity < 0) {
            throw new IllegalArgumentException("On-hand quantity cannot be negative");
        }
        if (reservedQuantity < 0) {
            throw new IllegalArgumentException("Reserved quantity cannot be negative");
        }
        if (reservedQuantity > onHandQuantity) {
            throw new IllegalArgumentException("Reserved quantity cannot exceed on-hand quantity");
        }
        this.onHandQuantity = onHandQuantity;
        this.reservedQuantity = reservedQuantity;
        this.status = status != null ? status : "ACTIVE";
        this.version = version;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public static InventoryItem createNew(UUID productId, UUID variantId, String sku, int initialOnHand) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        return new InventoryItem(id, productId, variantId, sku, initialOnHand, 0, "ACTIVE", null, now, now);
    }

    public int getAvailableQuantity() {
        return this.onHandQuantity - this.reservedQuantity;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be greater than zero");
        }
        if (getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(this.sku, quantity, getAvailableQuantity());
        }
        this.reservedQuantity += quantity;
        this.updatedAt = OffsetDateTime.now();
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Release quantity must be greater than zero");
        }
        if (this.reservedQuantity < quantity) {
            throw new IllegalArgumentException("Cannot release quantity (" + quantity + ") exceeding reserved quantity (" + this.reservedQuantity + ")");
        }
        this.reservedQuantity -= quantity;
        this.updatedAt = OffsetDateTime.now();
    }

    public void adjustOnHand(int newOnHandQuantity) {
        if (newOnHandQuantity < 0) {
            throw new IllegalArgumentException("On-hand quantity cannot be negative");
        }
        if (newOnHandQuantity < this.reservedQuantity) {
            throw new IllegalArgumentException("Cannot reduce on-hand quantity below active reserved quantity (" + this.reservedQuantity + ")");
        }
        this.onHandQuantity = newOnHandQuantity;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public String getSku() { return sku; }
    public int getOnHandQuantity() { return onHandQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public String getStatus() { return status; }
    public Long getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem item = (InventoryItem) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
