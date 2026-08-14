package com.sporekart.modules.catalog.application;

import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.exception.CategoryDeletionException;
import com.sporekart.modules.catalog.domain.exception.CategoryNotFoundException;
import com.sporekart.modules.catalog.domain.exception.DuplicateCategoryException;
import com.sporekart.modules.catalog.infrastructure.persistence.CategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryApplicationService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryCommand command) {
        if (command == null || command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        if (categoryRepository.existsByName(command.name().trim())) {
            throw new DuplicateCategoryException(command.name().trim());
        }

        Category category = Category.create(command.name(), command.description());
        Category saved = categoryRepository.save(category);
        return CategoryDto.fromDomain(saved);
    }

    @Transactional
    public CategoryDto updateCategory(UUID id, UpdateCategoryCommand command) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (command.name() != null && !command.name().trim().equalsIgnoreCase(category.getName())) {
            if (categoryRepository.existsByName(command.name().trim())) {
                throw new DuplicateCategoryException(command.name().trim());
            }
        }

        category.update(command.name(), command.description(), command.status());
        Category updated = categoryRepository.save(category);
        return CategoryDto.fromDomain(updated);
    }

    public CategoryDto getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .map(CategoryDto::fromDomain)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public CategoryDto getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(CategoryDto::fromDomain)
                .orElseThrow(() -> new CategoryNotFoundException(slug));
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::fromDomain)
                .toList();
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new CategoryDeletionException(id, productCount);
        }

        categoryRepository.deleteById(id);
    }
}
