package com.sporekart.modules.inventory.domain.exception;

import java.util.UUID;

public class InventoryItemNotFoundException extends RuntimeException {

    public InventoryItemNotFoundException(String sku) {
        super("Inventory item not found for SKU: " + sku);
    }

    public InventoryItemNotFoundException(UUID id) {
        super("Inventory item not found with ID: " + id);
    }
}
