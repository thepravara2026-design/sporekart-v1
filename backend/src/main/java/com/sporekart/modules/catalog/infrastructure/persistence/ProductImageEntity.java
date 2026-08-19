package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.product.ProductImage;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_images")
public class ProductImageEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductImageEntity() {}

    public ProductImageEntity(
            UUID id,
            ProductEntity product,
            String imageUrl,
            int displayOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.product = product;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductImageEntity fromDomain(ProductImage image, ProductEntity parentEntity) {
        if (image == null) return null;
        return new ProductImageEntity(
                image.getId(),
                parentEntity,
                image.getImageUrl(),
                image.getDisplayOrder(),
                image.getCreatedAt(),
                image.getUpdatedAt()
        );
    }

    public ProductImage toDomain() {
        return new ProductImage(
                id,
                product != null ? product.getId() : null,
                imageUrl,
                displayOrder,
                createdAt,
                updatedAt
        );
    }

    public UUID getId() { return id; }
    public ProductEntity getProduct() { return product; }
    public void setProduct(ProductEntity product) { this.product = product; }
    public String getImageUrl() { return imageUrl; }
    public int getDisplayOrder() { return displayOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}