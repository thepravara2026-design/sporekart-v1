package com.sporekart.modules.catalog.application;

import com.sporekart.modules.catalog.application.dto.response.PageResponse;
import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.exception.CategoryNotFoundException;
import com.sporekart.modules.catalog.domain.exception.DuplicateSkuException;
import com.sporekart.modules.catalog.domain.exception.ProductNotFoundException;
import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
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
public class ProductApplicationService {

    private static final Set<String> ALLOWED_PRODUCT_SORT_FIELDS = Set.of("name", "price", "createdAt", "updatedAt", "sku", "status");
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductApplicationService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductDto createProduct(CreateProductCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateProductCommand cannot be null");
        }

        String normalizedSku = Product.normalizeSku(command.sku());
        if (productRepository.existsBySku(normalizedSku)) {
            throw new DuplicateSkuException(normalizedSku);
        }

        Category category = null;
        if (command.categoryId() != null) {
            category = categoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));
        }

        Product product = Product.create(
                normalizedSku,
                command.name(),
                command.description(),
                command.price(),
                command.currency(),
                category
        );

        Product saved = productRepository.save(product);
        return ProductDto.fromDomain(saved);
    }

    @Transactional
    public ProductDto updateProduct(UUID id, UpdateProductCommand command) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        Category category = product.getCategory();
        if (command.categoryId() != null) {
            if (category == null || !category.getId().equals(command.categoryId())) {
                category = categoryRepository.findById(command.categoryId())
                        .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));
            }
        }

        product.updateDetails(
                command.name(),
                command.description(),
                command.price(),
                command.currency(),
                category
        );

        Product updated = productRepository.save(product);
        return ProductDto.fromDomain(updated);
    }

    @Transactional
    public ProductDto changeProductStatus(UUID id, ProductStatus newStatus) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.changeStatus(newStatus);
        Product updated = productRepository.save(product);
        return ProductDto.fromDomain(updated);
    }

    public ProductDto getProductById(UUID id) {
        return productRepository.findById(id)
                .map(ProductDto::fromDomain)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public ProductDto getProductBySku(String sku) {
        String normalizedSku = Product.normalizeSku(sku);
        return productRepository.findBySku(normalizedSku)
                .map(ProductDto::fromDomain)
                .orElseThrow(() -> new ProductNotFoundException(normalizedSku));
    }

    public PageResponse<ProductDto> getProducts(int page, int size, String sortParam, UUID categoryId, ProductStatus status, String search) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size cannot exceed maximum limit of " + MAX_PAGE_SIZE);
        }

        Sort sort = parseAndValidateSort(sortParam, ALLOWED_PRODUCT_SORT_FIELDS, Sort.by(Sort.Direction.DESC, "createdAt"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByFilters(search, categoryId, status, pageable);
        Page<ProductDto> dtoPage = productPage.map(ProductDto::fromDomain);

        return PageResponse.fromPage(dtoPage);
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDto::fromDomain)
                .toList();
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Hard deletion or transition to ARCHIVED
        productRepository.deleteById(product.getId());
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
