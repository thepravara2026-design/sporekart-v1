package com.sporekart.modules.catalog.application;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record ProductDto(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        BigDecimal strikeOutPrice,
        String currency,
        ProductStatus status,
        CategoryDto category,
        List<ProductVariantDto> variants,
        Instant createdAt,
        Instant updatedAt
) {
    public ProductDto(UUID id, String sku, String name, String description, BigDecimal price, String currency, ProductStatus status, CategoryDto category, Instant createdAt, Instant updatedAt) {
        this(id, sku, name, description, price, null, currency, status, category, Collections.emptyList(), createdAt, updatedAt);
    }

    public ProductDto(UUID id, String sku, String name, String description, BigDecimal price, BigDecimal strikeOutPrice, String currency, ProductStatus status, CategoryDto category, Instant createdAt, Instant updatedAt) {
        this(id, sku, name, description, price, strikeOutPrice, currency, status, category, Collections.emptyList(), createdAt, updatedAt);
    }

    public static ProductDto fromDomain(Product product) {
        if (product == null) return null;
        CategoryDto categoryDto = product.getCategory() != null ? CategoryDto.fromDomain(product.getCategory()) : null;
        List<ProductVariantDto> variantDtos = product.getVariants() != null
                ? product.getVariants().stream().map(ProductVariantDto::fromDomain).toList()
                : Collections.emptyList();
        return new ProductDto(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStrikeOutPrice(),
                product.getCurrency(),
                product.getStatus(),
                categoryDto,
                variantDtos,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
