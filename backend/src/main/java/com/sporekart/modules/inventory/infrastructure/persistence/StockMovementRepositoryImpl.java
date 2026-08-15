package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.StockMovement;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class StockMovementRepositoryImpl implements StockMovementRepository {

    private final SpringDataJpaStockMovementRepository jpaRepository;

    public StockMovementRepositoryImpl(SpringDataJpaStockMovementRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public StockMovement save(StockMovement movement) {
        StockMovementEntity entity = StockMovementEntity.fromDomain(movement);
        StockMovementEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<StockMovement> findByInventoryItemId(UUID inventoryItemId) {
        return jpaRepository.findByInventoryItemIdOrderByCreatedAtDesc(inventoryItemId).stream()
                .map(StockMovementEntity::toDomain)
                .toList();
    }
}
