package com.sporekart.modules.catalog.application;

import com.sporekart.modules.catalog.application.dto.response.PageResponse;
import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.category.CategoryStatus;
import com.sporekart.modules.catalog.domain.exception.CategoryDeletionException;
import com.sporekart.modules.catalog.domain.exception.CategoryNotFoundException;
import com.sporekart.modules.catalog.domain.exception.DuplicateCategoryException;
import com.sporekart.modules.catalog.infrastructure.persistence.CategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CategoryApplicationService {

    private static final Set<String> ALLOWED_CATEGORY_SORT_FIELDS = Set.of("name", "slug", "createdAt", "updatedAt", "status");
    private static final int MAX_PAGE_SIZE = 100;

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

    public PageResponse<CategoryDto> getCategories(int page, int size, String sortParam, CategoryStatus status, String search) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size cannot exceed maximum limit of " + MAX_PAGE_SIZE);
        }

        if (search != null) {
            search = search.trim();
            if (search.isEmpty()) {
                search = null;
            } else {
                if (search.length() > 100) {
                    throw new IllegalArgumentException("Search query cannot exceed 100 characters");
                }
                String stripped = search.replaceAll("[%\\*\\?_]", "").trim();
                if (stripped.isEmpty()) {
                    throw new IllegalArgumentException("Search query contains excessive wildcards");
                }
            }
        }

        Sort sort = parseAndValidateSort(sortParam, ALLOWED_CATEGORY_SORT_FIELDS, Sort.by(Sort.Direction.ASC, "name"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Category> categoryPage = categoryRepository.findByFilters(search, status, pageable);
        Page<CategoryDto> dtoPage = categoryPage.map(CategoryDto::fromDomain);

        return PageResponse.fromPage(dtoPage);
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

    private Sort parseAndValidateSort(String sortParam, Set<String> allowedFields, Sort defaultSort) {
        if (sortParam == null || sortParam.isBlank()) {
            return defaultSort;
        }
        String[] parts = sortParam.split(",");
        String field = parts[0].trim();
        if (!allowedFields.contains(field)) {
            throw new IllegalArgumentException("Invalid sort field: '" + field + "'. Allowed fields are: " + allowedFields);
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String dir = parts[1].trim().toUpperCase();
            if ("DESC".equals(dir)) {
                direction = Sort.Direction.DESC;
            } else if (!"ASC".equals(dir)) {
                throw new IllegalArgumentException("Invalid sort direction: '" + parts[1] + "'. Must be 'asc' or 'desc'");
            }
        }
        return Sort.by(direction, field);
    }
}
