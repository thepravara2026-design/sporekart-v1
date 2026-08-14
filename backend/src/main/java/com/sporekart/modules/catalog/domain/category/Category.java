package com.sporekart.modules.catalog.domain.category;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Category {
    private final UUID id;
    private String name;
    private String slug;
    private String description;
    private CategoryStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Category(UUID id, String name, String slug, String description, CategoryStatus status, Instant createdAt, Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Category slug cannot be blank");
        }

        this.id = id;
        this.name = name.trim();
        this.slug = normalizeSlug(slug);
        this.description = description != null ? description.trim() : null;
        this.status = status != null ? status : CategoryStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Category create(String name, String description) {
        UUID newId = UUID.randomUUID();
        String generatedSlug = generateSlug(name);
        Instant now = Instant.now();
        return new Category(newId, name, generatedSlug, description, CategoryStatus.ACTIVE, now, now);
    }

    public static String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input name for slug generation cannot be blank");
        }
        return input.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-");
    }

    private static String normalizeSlug(String slug) {
        return slug.trim().toLowerCase().replaceAll("[^a-z0-9-]", "");
    }

    public void update(String name, String description, CategoryStatus status) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        this.name = name.trim();
        this.slug = generateSlug(name);
        this.description = description != null ? description.trim() : null;
        if (status != null) {
            this.status = status;
        }
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public CategoryStatus getStatus() {
        return status;
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
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
