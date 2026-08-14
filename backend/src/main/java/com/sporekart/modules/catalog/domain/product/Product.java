package com.sporekart.modules.catalog.domain.product;

import com.sporekart.modules.catalog.domain.category.Category;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Product {
    private final UUID id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private ProductStatus status;
    private Category category;
    private final Instant createdAt;
    private Instant updatedAt;

    public Product(UUID id, String sku, String name, String description, BigDecimal price, String currency, ProductStatus status, Category category, Instant createdAt, Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("Product SKU cannot be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price must be a non-negative decimal value");
        }

        this.id = id;
        this.sku = normalizeSku(sku);
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.currency = (currency != null && !currency.isBlank()) ? currency.trim().toUpperCase() : "USD";
        this.status = status != null ? status : ProductStatus.DRAFT;
        this.category = category;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Product create(String sku, String name, String description, BigDecimal price, String currency, Category category) {
        UUID newId = UUID.randomUUID();
        Instant now = Instant.now();
        return new Product(newId, sku, name, description, price, currency, ProductStatus.DRAFT, category, now, now);
    }

    public static String normalizeSku(String rawSku) {
        if (rawSku == null || rawSku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be blank");
        }
        return rawSku.trim().toUpperCase().replaceAll("[^A-Z0-9-]", "");
    }

    public void updateDetails(String name, String description, BigDecimal price, String currency, Category category) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price must be a non-negative decimal value");
        }
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        if (currency != null && !currency.isBlank()) {
            this.currency = currency.trim().toUpperCase();
        }
        this.category = category;
        this.updatedAt = Instant.now();
    }

    public void changeStatus(ProductStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Product status cannot be null");
        }
        if (this.status == ProductStatus.ARCHIVED && newStatus != ProductStatus.ARCHIVED) {
            throw new IllegalStateException("Archived products cannot be reactivated directly");
        }
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
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
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
