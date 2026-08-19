package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.domain.product.ProductVariant;
import com.sporekart.modules.catalog.domain.product.QuantityUnit;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
public class ProductVariantEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "quantity_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_unit", nullable = false, length = 10)
    private QuantityUnit quantityUnit;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "strike_out_price", precision = 12, scale = 2)
    private BigDecimal strikeOutPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductVariantEntity() {}

    public ProductVariantEntity(
            UUID id,
            ProductEntity product,
            String sku,
            BigDecimal quantityValue,
            QuantityUnit quantityUnit,
            BigDecimal sellingPrice,
            BigDecimal strikeOutPrice,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.product = product;
        this.sku = sku;
        this.quantityValue = quantityValue;
        this.quantityUnit = quantityUnit;
        this.sellingPrice = sellingPrice;
        this.strikeOutPrice = strikeOutPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductVariantEntity fromDomain(ProductVariant variant, ProductEntity parentEntity) {
        if (variant == null) return null;
        return new ProductVariantEntity(
                variant.getId(),
                parentEntity,
                variant.getSku(),
                variant.getQuantityValue(),
                variant.getQuantityUnit(),
                variant.getSellingPrice(),
                variant.getStrikeOutPrice(),
                variant.getStatus(),
                variant.getCreatedAt(),
                variant.getUpdatedAt()
        );
    }

    public ProductVariant toDomain() {
        return new ProductVariant(
                id,
                product != null ? product.getId() : null,
                sku,
                quantityValue,
                quantityUnit,
                sellingPrice,
                strikeOutPrice,
                status,
                createdAt,
                updatedAt
        );
    }

    public UUID getId() { return id; }
    public ProductEntity getProduct() { return product; }
    public void setProduct(ProductEntity product) { this.product = product; }
    public String getSku() { return sku; }
    public BigDecimal getQuantityValue() { return quantityValue; }
    public QuantityUnit getQuantityUnit() { return quantityUnit; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public BigDecimal getStrikeOutPrice() { return strikeOutPrice; }
    public ProductStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
