package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductImage;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.domain.product.ProductVariant;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "strike_out_price", precision = 12, scale = 2)
    private BigDecimal strikeOutPrice;

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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantEntity> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImageEntity> images = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductEntity() {}

    public ProductEntity(UUID id, String sku, String name, String description, BigDecimal price, BigDecimal strikeOutPrice, String currency, ProductStatus status, CategoryEntity category, String growerId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.strikeOutPrice = strikeOutPrice;
        this.currency = currency;
        this.status = status;
        this.category = category;
        this.growerId = growerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ProductEntity(UUID id, String sku, String name, String description, BigDecimal price, String currency, ProductStatus status, CategoryEntity category, String growerId, Instant createdAt, Instant updatedAt) {
        this(id, sku, name, description, price, null, currency, status, category, growerId, createdAt, updatedAt);
    }

    public ProductEntity(UUID id, String sku, String name, String description, BigDecimal price, String currency, ProductStatus status, CategoryEntity category, Instant createdAt, Instant updatedAt) {
        this(id, sku, name, description, price, null, currency, status, category, null, createdAt, updatedAt);
    }

    public static ProductEntity fromDomain(Product product) {
        if (product == null) return null;
        CategoryEntity categoryEntity = product.getCategory() != null ? CategoryEntity.fromDomain(product.getCategory()) : null;
        ProductEntity entity = new ProductEntity(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStrikeOutPrice(),
                product.getCurrency(),
                product.getStatus(),
                categoryEntity,
                product.getGrowerId(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            List<ProductVariantEntity> variantEntities = product.getVariants().stream()
                    .map(v -> ProductVariantEntity.fromDomain(v, entity))
                    .toList();
            entity.setVariants(variantEntities);
        }
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<ProductImageEntity> imageEntities = product.getImages().stream()
                    .map(img -> ProductImageEntity.fromDomain(img, entity))
                    .toList();
            entity.setImages(imageEntities);
        }
        return entity;
    }

    public Product toDomain() {
        Category categoryDomain = category != null ? category.toDomain() : null;
        List<ProductVariant> domainVariants = variants.stream()
                .map(ProductVariantEntity::toDomain)
                .toList();
        List<ProductImage> domainImages = images.stream()
                .map(ProductImageEntity::toDomain)
                .toList();
        return new Product(id, sku, name, description, price, strikeOutPrice, currency, status, categoryDomain, growerId, domainVariants, domainImages, createdAt, updatedAt);
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getStrikeOutPrice() { return strikeOutPrice; }
    public String getCurrency() { return currency; }
    public ProductStatus getStatus() { return status; }
    public CategoryEntity getCategory() { return category; }
    public String getGrowerId() { return growerId; }
    public List<ProductVariantEntity> getVariants() { return variants; }
    public void setVariants(List<ProductVariantEntity> variants) {
        this.variants.clear();
        if (variants != null) {
            for (ProductVariantEntity v : variants) {
                v.setProduct(this);
                this.variants.add(v);
            }
        }
    }
    public List<ProductImageEntity> getImages() { return images; }
    public void setImages(List<ProductImageEntity> images) {
        this.images.clear();
        if (images != null) {
            for (ProductImageEntity img : images) {
                img.setProduct(this);
                this.images.add(img);
            }
        }
    }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
