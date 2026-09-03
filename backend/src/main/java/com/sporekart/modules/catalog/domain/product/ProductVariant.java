package com.sporekart.modules.catalog.domain.product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ProductVariant {
    private final UUID id;
    private final UUID productId;
    private String sku;
    private BigDecimal quantityValue;
    private QuantityUnit quantityUnit;
    private BigDecimal sellingPrice;
    private BigDecimal strikeOutPrice;
    private ProductStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public ProductVariant(
            UUID id,
            UUID productId,
            String sku,
            BigDecimal quantityValue,
            QuantityUnit quantityUnit,
            BigDecimal sellingPrice,
            BigDecimal strikeOutPrice,
            ProductStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Variant ID cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("Variant SKU cannot be blank");
        }
        if (quantityValue == null || quantityValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Variant quantity value must be strictly positive");
        }
        if (quantityUnit == null) {
            throw new IllegalArgumentException("Variant quantity unit cannot be null");
        }
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Variant selling price must be non-negative");
        }

        this.id = id;
        this.productId = productId;
        this.sku = Product.normalizeSku(sku);
        this.quantityValue = quantityValue.setScale(2, RoundingMode.HALF_UP);
        this.quantityUnit = quantityUnit;
        this.sellingPrice = sellingPrice.setScale(2, RoundingMode.HALF_UP);

        if (strikeOutPrice != null) {
            BigDecimal scaledStrikeOut = strikeOutPrice.setScale(2, RoundingMode.HALF_UP);
            if (scaledStrikeOut.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Strike-out price must be positive");
            }
            if (scaledStrikeOut.compareTo(this.sellingPrice) <= 0) {
                throw new IllegalArgumentException("Strike-out price must be strictly greater than actual selling price");
            }
            this.strikeOutPrice = scaledStrikeOut;
        } else {
            this.strikeOutPrice = null;
        }

        this.status = status != null ? status : ProductStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static ProductVariant create(
            UUID productId,
            String sku,
            BigDecimal quantityValue,
            QuantityUnit quantityUnit,
            BigDecimal sellingPrice,
            BigDecimal strikeOutPrice
    ) {
        UUID newId = UUID.randomUUID();
        Instant now = Instant.now();
        return new ProductVariant(newId, productId, sku, quantityValue, quantityUnit, sellingPrice, strikeOutPrice, ProductStatus.ACTIVE, now, now);
    }

    public void update(
            BigDecimal quantityValue,
            QuantityUnit quantityUnit,
            BigDecimal sellingPrice,
            BigDecimal strikeOutPrice,
            ProductStatus status
    ) {
        if (quantityValue == null || quantityValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Variant quantity value must be strictly positive");
        }
        if (quantityUnit == null) {
            throw new IllegalArgumentException("Variant quantity unit cannot be null");
        }
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Variant selling price must be non-negative");
        }

        BigDecimal scaledSelling = sellingPrice.setScale(2, RoundingMode.HALF_UP);
        if (strikeOutPrice != null) {
            BigDecimal scaledStrikeOut = strikeOutPrice.setScale(2, RoundingMode.HALF_UP);
            if (scaledStrikeOut.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Strike-out price must be positive");
            }
            if (scaledStrikeOut.compareTo(scaledSelling) <= 0) {
                throw new IllegalArgumentException("Strike-out price must be strictly greater than actual selling price");
            }
            this.strikeOutPrice = scaledStrikeOut;
        } else {
            this.strikeOutPrice = null;
        }

        this.quantityValue = quantityValue.setScale(2, RoundingMode.HALF_UP);
        this.quantityUnit = quantityUnit;
        this.sellingPrice = scaledSelling;
        if (status != null) {
            this.status = status;
        }
        this.updatedAt = Instant.now();
    }

    public String getFormattedQuantity() {
        double val = quantityValue.doubleValue();
        String valStr = (val == Math.floor(val)) ? String.valueOf((long) val) : String.valueOf(val);
        return valStr + " " + quantityUnit.getSymbol();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getQuantityValue() {
        return quantityValue;
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public BigDecimal getStrikeOutPrice() {
        return strikeOutPrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
