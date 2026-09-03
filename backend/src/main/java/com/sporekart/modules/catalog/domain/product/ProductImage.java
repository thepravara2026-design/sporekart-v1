package com.sporekart.modules.catalog.domain.product;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ProductImage — an ordered product image.
 *
 * Images belong to exactly one product and are rendered as a carousel. The
 * first image (lowest displayOrder) is the primary/thumbnail image.
 */
public class ProductImage {
    private final UUID id;
    private final UUID productId;
    private String imageUrl;
    private int displayOrder;
    private final Instant createdAt;
    private Instant updatedAt;

    public ProductImage(
            UUID id,
            UUID productId,
            String imageUrl,
            int displayOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Image ID cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be blank");
        }
        if (displayOrder < 1) {
            throw new IllegalArgumentException("Image display order must be positive");
        }

        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl.trim();
        this.displayOrder = displayOrder;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static ProductImage create(UUID productId, String imageUrl, int displayOrder) {
        UUID newId = UUID.randomUUID();
        Instant now = Instant.now();
        return new ProductImage(newId, productId, imageUrl, displayOrder, now, now);
    }

    public static List<ProductImage> listFromUrls(UUID productId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        if (imageUrls.size() > Product.MAX_PRODUCT_IMAGES) {
            throw new IllegalArgumentException("A product can have at most " + Product.MAX_PRODUCT_IMAGES + " images");
        }
        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            images.add(create(productId, imageUrls.get(i), i + 1));
        }
        return images;
    }

    public void update(String imageUrl, int displayOrder) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be blank");
        }
        if (displayOrder < 1) {
            throw new IllegalArgumentException("Image display order must be positive");
        }
        this.imageUrl = imageUrl.trim();
        this.displayOrder = displayOrder;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
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
        ProductImage that = (ProductImage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
