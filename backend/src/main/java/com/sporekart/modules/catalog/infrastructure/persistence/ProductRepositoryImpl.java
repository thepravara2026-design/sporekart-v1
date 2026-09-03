package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final SpringDataProductRepository springDataProductRepository;

    public ProductRepositoryImpl(SpringDataProductRepository springDataProductRepository) {
        this.springDataProductRepository = springDataProductRepository;
    }

    @Override
    public Product save(Product product) {
        // Clear existing images before saving so the unique (product_id,
        // display_order) constraint is not violated when a replacement set is
        // written in the same transaction (Hibernate flushes inserts before
        // orphan removals).
        springDataProductRepository.deleteImagesByProductId(product.getId());
        ProductEntity entity = ProductEntity.fromDomain(product);
        ProductEntity saved = springDataProductRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataProductRepository.findById(id).map(ProductEntity::toDomain);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return springDataProductRepository.findBySku(sku).map(ProductEntity::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return springDataProductRepository.existsBySku(sku);
    }

    @Override
    public long countByCategoryId(UUID categoryId) {
        return springDataProductRepository.countByCategoryId(categoryId);
    }

    @Override
    public List<Product> findAll() {
        return springDataProductRepository.findAll().stream()
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAllByGrowerId(String growerId) {
        return springDataProductRepository.findAllByGrowerId(growerId).stream()
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findByIdAndGrowerId(UUID id, String growerId) {
        return springDataProductRepository.findByIdAndGrowerId(id, growerId).map(ProductEntity::toDomain);
    }

    @Override
    public Page<Product> findByFilters(String search, UUID categoryId, ProductStatus status, Pageable pageable) {
        return findByFilters(search, categoryId, status, null, null, pageable);
    }

    @Override
    public Page<Product> findByFilters(String search, UUID categoryId, ProductStatus status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return springDataProductRepository.findByFilters(search, categoryId, status, minPrice, maxPrice, pageable)
                .map(ProductEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        springDataProductRepository.deleteById(id);
    }
}
