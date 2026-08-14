package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    long countByCategoryId(UUID categoryId);
    List<Product> findAll();
    Page<Product> findByFilters(String search, UUID categoryId, ProductStatus status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    default Page<Product> findByFilters(String search, UUID categoryId, ProductStatus status, Pageable pageable) {
        return findByFilters(search, categoryId, status, null, null, pageable);
    }
    void deleteById(UUID id);
}
