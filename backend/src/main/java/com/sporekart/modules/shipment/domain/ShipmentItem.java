package com.sporekart.modules.shipment.domain;

import java.util.Objects;
import java.util.UUID;

public class ShipmentItem {
    private final UUID id;
    private final UUID orderItemId;
    private final UUID productId;
    private final String sku;
    private final String productNameSnapshot;
    private final int quantity;

    public ShipmentItem(
            UUID id,
            UUID orderItemId,
            UUID productId,
            String sku,
            String productNameSnapshot,
            int quantity
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.orderItemId = Objects.requireNonNull(orderItemId, "orderItemId required");
        this.productId = Objects.requireNonNull(productId, "productId required");
        this.sku = Objects.requireNonNull(sku, "sku required");
        this.productNameSnapshot = Objects.requireNonNull(productNameSnapshot, "productNameSnapshot required");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }

    public UUID getId() { return id; }
    public UUID getOrderItemId() { return orderItemId; }
    public UUID getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public int getQuantity() { return quantity; }
}
