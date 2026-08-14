package com.sporekart.modules.catalog.application;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductDto(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        ProductStatus status,
        CategoryDto category,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductDto fromDomain(Product product) {
        if (product == null) return null;
        CategoryDto categoryDto = product.getCategory() != null ? CategoryDto.fromDomain(product.getCategory()) : null;
        return new ProductDto(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getStatus(),
                categoryDto,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
