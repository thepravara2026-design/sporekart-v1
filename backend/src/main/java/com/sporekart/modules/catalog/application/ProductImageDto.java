package com.sporekart.modules.catalog.application;

import com.sporekart.modules.catalog.domain.product.ProductImage;

import java.util.UUID;

public record ProductImageDto(
        UUID id,
        String imageUrl,
        int displayOrder
) {
    public static ProductImageDto fromDomain(ProductImage image) {
        if (image == null) return null;
        return new ProductImageDto(image.getId(), image.getImageUrl(), image.getDisplayOrder());
    }
}