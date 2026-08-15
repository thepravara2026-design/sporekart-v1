package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.InventoryItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
public class InventoryItemEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "on_hand_quantity", nullable = false)
    private int onHandQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "damaged_quantity", nullable = false)
    private int damagedQuantity;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public InventoryItemEntity() {}

    public InventoryItemEntity(
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
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.sku = sku;
        this.onHandQuantity = onHandQuantity;
        this.reservedQuantity = reservedQuantity;
        this.damagedQuantity = damagedQuantity;
        this.lowStockThreshold = lowStockThreshold;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static InventoryItemEntity fromDomain(InventoryItem item) {
        return new InventoryItemEntity(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getSku(),
                item.getOnHandQuantity(),
                item.getReservedQuantity(),
                item.getDamagedQuantity(),
                item.getLowStockThreshold(),
                item.getStatus(),
                item.getVersion(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    public InventoryItem toDomain() {
        return new InventoryItem(
                this.id,
                this.productId,
                this.variantId,
                this.sku,
                this.onHandQuantity,
                this.reservedQuantity,
                this.damagedQuantity,
                this.lowStockThreshold,
                this.status,
                this.version,
                this.createdAt,
                this.updatedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getOnHandQuantity() { return onHandQuantity; }
    public void setOnHandQuantity(int onHandQuantity) { this.onHandQuantity = onHandQuantity; }

    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public int getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(int damagedQuantity) { this.damagedQuantity = damagedQuantity; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItemEntity that = (InventoryItemEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
