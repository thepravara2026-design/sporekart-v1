package com.sporekart.modules.shipment.infrastructure.persistence;

import com.sporekart.modules.shipment.domain.ShipmentItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipment_items")
public class ShipmentItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private ShipmentEntity shipment;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ShipmentItemEntity() {}

    public static ShipmentItemEntity fromDomain(ShipmentItem item, ShipmentEntity shipment) {
        ShipmentItemEntity entity = new ShipmentItemEntity();
        entity.id = item.getId();
        entity.shipment = shipment;
        entity.orderItemId = item.getOrderItemId();
        entity.productId = item.getProductId();
        entity.sku = item.getSku();
        entity.productNameSnapshot = item.getProductNameSnapshot();
        entity.quantity = item.getQuantity();
        entity.createdAt = Instant.now();
        return entity;
    }

    public ShipmentItem toDomain() {
        return new ShipmentItem(id, orderItemId, productId, sku, productNameSnapshot, quantity);
    }
}
