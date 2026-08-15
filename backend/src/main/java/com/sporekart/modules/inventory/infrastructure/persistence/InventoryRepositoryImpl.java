package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.InventoryItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InventoryRepositoryImpl implements InventoryRepository {

    private final SpringDataJpaInventoryItemRepository jpaRepository;

    public InventoryRepositoryImpl(SpringDataJpaInventoryItemRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        InventoryItemEntity entity = InventoryItemEntity.fromDomain(item);
        InventoryItemEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<InventoryItem> findById(UUID id) {
        return jpaRepository.findById(id).map(InventoryItemEntity::toDomain);
    }

    @Override
    public Optional<InventoryItem> findBySku(String sku) {
        return jpaRepository.findBySku(sku).map(InventoryItemEntity::toDomain);
    }

    @Override
    public Optional<InventoryItem> findBySkuForUpdate(String sku) {
        return jpaRepository.findBySkuForUpdate(sku).map(InventoryItemEntity::toDomain);
    }

    @Override
    public List<InventoryItem> findAllBySkuInOrderBySkuAscForUpdate(List<String> skus) {
        return jpaRepository.findAllBySkuInOrderBySkuAscForUpdate(skus).stream()
                .map(InventoryItemEntity::toDomain)
                .toList();
    }
}
