package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.product.Product;

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
    void deleteById(UUID id);
}
