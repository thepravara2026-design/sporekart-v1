package com.sporekart.modules.shipment.application.dto;

import com.sporekart.modules.shipment.domain.ShipmentItem;

import java.util.UUID;

public record ShipmentItemDto(
        UUID id,
        UUID orderItemId,
        UUID productId,
        String sku,
        String productNameSnapshot,
        int quantity
) {
    public static ShipmentItemDto fromDomain(ShipmentItem item) {
        return new ShipmentItemDto(
                item.getId(),
                item.getOrderItemId(),
                item.getProductId(),
                item.getSku(),
                item.getProductNameSnapshot(),
                item.getQuantity()
        );
    }
}
