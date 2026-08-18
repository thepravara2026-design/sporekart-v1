package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(name = "grower_id", length = 100)
    private String growerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductEntity() {}

    public ProductEntity(UUID id, String sku, String name, String description, BigDecimal price, String currency, ProductStatus status, CategoryEntity category, String growerId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.category = category;
        this.growerId = growerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ProductEntity(UUID id, String sku, String name, String description, BigDecimal price, String currency, ProductStatus status, CategoryEntity category, Instant createdAt, Instant updatedAt) {
        this(id, sku, name, description, price, currency, status, category, null, createdAt, updatedAt);
    }

    public static ProductEntity fromDomain(Product product) {
        if (product == null) return null;
        CategoryEntity categoryEntity = product.getCategory() != null ? CategoryEntity.fromDomain(product.getCategory()) : null;
        return new ProductEntity(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getStatus(),
                categoryEntity,
                product.getGrowerId(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public Product toDomain() {
        Category categoryDomain = category != null ? category.toDomain() : null;
        return new Product(id, sku, name, description, price, currency, status, categoryDomain, growerId, createdAt, updatedAt);
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public ProductStatus getStatus() { return status; }
    public CategoryEntity getCategory() { return category; }
    public String getGrowerId() { return growerId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
