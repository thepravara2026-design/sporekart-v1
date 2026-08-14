package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.category.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    Optional<Category> findBySlug(String slug);
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    List<Category> findAll();
    Page<Category> findByFilters(String search, CategoryStatus status, Pageable pageable);
    void deleteById(UUID id);
}
