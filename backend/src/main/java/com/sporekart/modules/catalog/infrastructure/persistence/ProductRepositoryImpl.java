package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.product.Product;
import org.springframework.stereotype.Repository;

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
    public void deleteById(UUID id) {
        springDataProductRepository.deleteById(id);
    }
}
