package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.category.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final SpringDataCategoryRepository springDataCategoryRepository;

    public CategoryRepositoryImpl(SpringDataCategoryRepository springDataCategoryRepository) {
        this.springDataCategoryRepository = springDataCategoryRepository;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = CategoryEntity.fromDomain(category);
        CategoryEntity saved = springDataCategoryRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return springDataCategoryRepository.findById(id).map(CategoryEntity::toDomain);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return springDataCategoryRepository.findBySlug(slug).map(CategoryEntity::toDomain);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return springDataCategoryRepository.findByNameIgnoreCase(name).map(CategoryEntity::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return springDataCategoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return springDataCategoryRepository.existsBySlug(slug);
    }

    @Override
    public List<Category> findAll() {
        return springDataCategoryRepository.findAll().stream()
                .map(CategoryEntity::toDomain)
                .toList();
    }

    @Override
    public Page<Category> findByFilters(String search, CategoryStatus status, Pageable pageable) {
        return springDataCategoryRepository.findByFilters(search, status, pageable)
                .map(CategoryEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        springDataCategoryRepository.deleteById(id);
    }
}
