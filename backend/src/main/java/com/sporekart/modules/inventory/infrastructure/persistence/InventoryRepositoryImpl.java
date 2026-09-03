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
        InventoryItemEntity saved = jpaRepository.saveAndFlush(entity);
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
        List<InventoryItemEntity> entities = jpaRepository.findAllBySkuInOrderBySkuAscForUpdate(skus);
        if (entities.isEmpty() && !skus.isEmpty()) {
            entities = jpaRepository.findAllBySkuIn(skus);
        }
        return entities.stream()
                .map(InventoryItemEntity::toDomain)
                .toList();
    }

    @Override
    public List<InventoryItem> findAll() {
        return jpaRepository.findAll().stream()
                .map(InventoryItemEntity::toDomain)
                .toList();
    }

    @Override
    public List<InventoryItem> findAllByGrowerId(String growerId) {
        return jpaRepository.findAllByGrowerId(growerId).stream()
                .map(InventoryItemEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<InventoryItem> findBySkuAndGrowerId(String sku, String growerId) {
        return jpaRepository.findBySkuAndGrowerId(sku, growerId).map(InventoryItemEntity::toDomain);
    }
}
