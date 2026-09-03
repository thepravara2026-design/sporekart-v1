package com.sporekart.modules.inventory.infrastructure.persistence;

import com.sporekart.modules.inventory.domain.InventoryItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findById(UUID id);

    Optional<InventoryItem> findBySku(String sku);

    Optional<InventoryItem> findBySkuForUpdate(String sku);

    List<InventoryItem> findAllBySkuInOrderBySkuAscForUpdate(List<String> skus);

    List<InventoryItem> findAll();

    List<InventoryItem> findAllByGrowerId(String growerId);

    Optional<InventoryItem> findBySkuAndGrowerId(String sku, String growerId);
}
