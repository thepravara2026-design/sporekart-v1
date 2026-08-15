package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.StockMovement;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository {

    StockMovement save(StockMovement movement);

    List<StockMovement> findByInventoryItemId(UUID inventoryItemId);
}
