package com.sporekart.modules.catalog.application;

import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.domain.product.ProductVariant;
import com.sporekart.modules.catalog.domain.product.QuantityUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductVariantDto(
        UUID id,
        UUID productId,
        String sku,
        BigDecimal quantityValue,
        QuantityUnit quantityUnit,
        String formattedQuantity,
        BigDecimal sellingPrice,
        BigDecimal strikeOutPrice,
        ProductStatus status,
        int availableQuantity,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductVariantDto fromDomain(ProductVariant variant) {
        return fromDomain(variant, 50);
    }

    public static ProductVariantDto fromDomain(ProductVariant variant, int availableQuantity) {
        if (variant == null) return null;
        return new ProductVariantDto(
                variant.getId(),
                variant.getProductId(),
                variant.getSku(),
                variant.getQuantityValue(),
                variant.getQuantityUnit(),
                variant.getFormattedQuantity(),
                variant.getSellingPrice(),
                variant.getStrikeOutPrice(),
                variant.getStatus(),
                availableQuantity,
                variant.getCreatedAt(),
                variant.getUpdatedAt()
        );
    }
}
