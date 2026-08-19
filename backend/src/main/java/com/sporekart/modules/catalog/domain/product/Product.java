package com.sporekart.modules.catalog.domain.product;

import com.sporekart.modules.catalog.domain.category.Category;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Product {
    private final UUID id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal strikeOutPrice;
    private String currency;
    private ProductStatus status;
    private Category category;
    private String growerId;
    private final List<ProductVariant> variants = new ArrayList<>();
    private final Instant createdAt;
    private Instant updatedAt;

    public Product(UUID id, String sku, String name, String description, BigDecimal price, String currency, ProductStatus status, Category category, Instant createdAt, Instant updatedAt) {
        this(id, sku, name, description, price, null, currency, status, category, null, createdAt, updatedAt);
    }

    public Product(UUID id, String sku, String name, String description, BigDecimal price, String currency, ProductStatus status, Category category, String growerId, Instant createdAt, Instant updatedAt) {
        this(id, sku, name, description, price, null, currency, status, category, growerId, createdAt, updatedAt);
    }

    public Product(UUID id, String sku, String name, String description, BigDecimal price, BigDecimal strikeOutPrice, String currency, ProductStatus status, Category category, String growerId, Instant createdAt, Instant updatedAt) {
        this(id, sku, name, description, price, strikeOutPrice, currency, status, category, growerId, null, createdAt, updatedAt);
    }

    public Product(UUID id, String sku, String name, String description, BigDecimal price, BigDecimal strikeOutPrice, String currency, ProductStatus status, Category category, String growerId, List<ProductVariant> variants, Instant createdAt, Instant updatedAt) {
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

        if (strikeOutPrice != null) {
            BigDecimal scaledStrikeOut = strikeOutPrice.setScale(2, RoundingMode.HALF_UP);
            if (scaledStrikeOut.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Strike-out price must be positive");
            }
            if (scaledStrikeOut.compareTo(this.price) <= 0) {
                throw new IllegalArgumentException("Strike-out price must be strictly greater than actual selling price");
            }
            this.strikeOutPrice = scaledStrikeOut;
        } else {
            this.strikeOutPrice = null;
        }

        this.currency = (currency != null && !currency.isBlank()) ? currency.trim().toUpperCase() : "INR";
        this.status = status != null ? status : ProductStatus.DRAFT;
        this.category = category;
        this.growerId = growerId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;

        if (variants != null) {
            for (ProductVariant v : variants) {
                addVariant(v);
            }
        }
    }

    public static Product create(String sku, String name, String description, BigDecimal price, String currency, Category category) {
        return create(sku, name, description, price, null, currency, category);
    }

    public static Product create(String sku, String name, String description, BigDecimal price, BigDecimal strikeOutPrice, String currency, Category category) {
        UUID newId = UUID.randomUUID();
        Instant now = Instant.now();
        return new Product(newId, sku, name, description, price, strikeOutPrice, currency, ProductStatus.DRAFT, category, null, now, now);
    }

    public static String normalizeSku(String rawSku) {
        if (rawSku == null || rawSku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be blank");
        }
        String normalized = rawSku.trim().toUpperCase().replaceAll("[^A-Z0-9-]", "");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("SKU must contain at least one valid alphanumeric character or hyphen");
        }
        return normalized;
    }

    public void addVariant(ProductVariant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }
        // Check duplicate quantity/unit invariant
        for (ProductVariant existing : variants) {
            if (existing.getQuantityValue().compareTo(variant.getQuantityValue()) == 0
                    && existing.getQuantityUnit() == variant.getQuantityUnit()) {
                throw new IllegalArgumentException("Duplicate variant for quantity " + variant.getFormattedQuantity() + " already exists on product " + name);
            }
        }
        variants.add(variant);
        recalculateBasePrice();
    }

    public Optional<ProductVariant> findVariantById(UUID variantId) {
        if (variantId == null) return Optional.empty();
        return variants.stream().filter(v -> v.getId().equals(variantId)).findFirst();
    }

    public void setVariants(List<ProductVariant> newVariants) {
        this.variants.clear();
        if (newVariants != null) {
            for (ProductVariant v : newVariants) {
                addVariant(v);
            }
        }
        recalculateBasePrice();
    }

    private void recalculateBasePrice() {
        if (!variants.isEmpty()) {
            BigDecimal minSelling = variants.stream()
                    .map(ProductVariant::getSellingPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(this.price);
            this.price = minSelling;
        }
    }

    public void updateDetails(String name, String description, BigDecimal price, String currency, Category category) {
        updateDetails(name, description, price, null, currency, category);
    }

    public void updateDetails(String name, String description, BigDecimal price, BigDecimal strikeOutPrice, String currency, Category category) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price must be a non-negative decimal value");
        }

        BigDecimal scaledPrice = price.setScale(2, RoundingMode.HALF_UP);
        if (strikeOutPrice != null) {
            BigDecimal scaledStrikeOut = strikeOutPrice.setScale(2, RoundingMode.HALF_UP);
            if (scaledStrikeOut.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Strike-out price must be positive");
            }
            if (scaledStrikeOut.compareTo(scaledPrice) <= 0) {
                throw new IllegalArgumentException("Strike-out price must be strictly greater than actual selling price");
            }
            this.strikeOutPrice = scaledStrikeOut;
        } else {
            this.strikeOutPrice = null;
        }

        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.price = scaledPrice;

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

    public BigDecimal getStrikeOutPrice() {
        return strikeOutPrice;
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

    public String getGrowerId() {
        return growerId;
    }

    public void setGrowerId(String growerId) {
        this.growerId = growerId;
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
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
