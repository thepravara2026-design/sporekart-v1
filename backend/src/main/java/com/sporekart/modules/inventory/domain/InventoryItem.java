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
    private int damagedQuantity;
    private int lowStockThreshold;
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
            int damagedQuantity,
            int lowStockThreshold,
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
        if (damagedQuantity < 0) {
            throw new IllegalArgumentException("Damaged quantity cannot be negative");
        }
        if (reservedQuantity > onHandQuantity) {
            throw new IllegalArgumentException("Reserved quantity cannot exceed on-hand quantity");
        }
        this.onHandQuantity = onHandQuantity;
        this.reservedQuantity = reservedQuantity;
        this.damagedQuantity = damagedQuantity;
        this.lowStockThreshold = lowStockThreshold >= 0 ? lowStockThreshold : 5;
        this.status = status != null ? status : "ACTIVE";
        this.version = version;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

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
        this(id, productId, variantId, sku, onHandQuantity, reservedQuantity, 0, 5, status, version, createdAt, updatedAt);
    }

    public static InventoryItem createNew(UUID productId, UUID variantId, String sku, int initialOnHand) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        return new InventoryItem(id, productId, variantId, sku, initialOnHand, 0, 0, 5, "ACTIVE", null, now, now);
    }

    public int getAvailableQuantity() {
        int avail = this.onHandQuantity - this.reservedQuantity - this.damagedQuantity;
        return Math.max(0, avail);
    }

    public boolean isLowStock() {
        int avail = getAvailableQuantity();
        return avail > 0 && avail <= this.lowStockThreshold;
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

    public void commit(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Commit quantity must be greater than zero");
        }
        if (this.reservedQuantity < quantity) {
            throw new IllegalArgumentException("Cannot commit quantity (" + quantity + ") exceeding reserved quantity (" + this.reservedQuantity + ")");
        }
        this.reservedQuantity -= quantity;
        this.onHandQuantity -= quantity;
        this.updatedAt = OffsetDateTime.now();
    }

    public void recordDamaged(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Damaged quantity must be greater than zero");
        }
        this.damagedQuantity += quantity;
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
    public int getDamagedQuantity() { return damagedQuantity; }
    public int getLowStockThreshold() { return lowStockThreshold; }
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
