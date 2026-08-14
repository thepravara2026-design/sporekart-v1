package com.sporekart.modules.order.application.dto;

import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderSummaryDto(
        UUID id,
        String orderNumber,
        OrderStatus status,
        String currency,
        BigDecimal grandTotal,
        int itemCount,
        OffsetDateTime createdAt
) {
    public static OrderSummaryDto fromDomain(Order order) {
        int count = order.getItems() != null ? order.getItems().size() : 0;
        return new OrderSummaryDto(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getCurrency(),
                order.getGrandTotal(),
                count,
                order.getCreatedAt()
        );
    }
}
