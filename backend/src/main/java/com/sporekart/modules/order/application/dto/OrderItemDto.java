package com.sporekart.modules.order.application.dto;

import com.sporekart.modules.order.domain.OrderItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderItemDto(
        UUID id,
        UUID orderId,
        UUID productId,
        UUID variantId,
        String sku,
        String productNameSnapshot,
        String variantNameSnapshot,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal lineSubtotal,
        BigDecimal lineTotal,
        OffsetDateTime createdAt
) {
    public static OrderItemDto fromDomain(OrderItem item) {
        return new OrderItemDto(
                item.getId(),
                item.getOrderId(),
                item.getProductId(),
                item.getVariantId(),
                item.getSku(),
                item.getProductNameSnapshot(),
                item.getVariantNameSnapshot(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getDiscountAmount(),
                item.getTaxAmount(),
                item.getLineSubtotal(),
                item.getLineTotal(),
                item.getCreatedAt()
        );
    }
}
