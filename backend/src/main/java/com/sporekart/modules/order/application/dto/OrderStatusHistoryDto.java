package com.sporekart.modules.order.application.dto;

import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderStatusHistory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderStatusHistoryDto(
        UUID id,
        UUID orderId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String reason,
        OrderActorType actorType,
        String actorId,
        OffsetDateTime createdAt
) {
    public static OrderStatusHistoryDto fromDomain(OrderStatusHistory history) {
        return new OrderStatusHistoryDto(
                history.getId(),
                history.getOrderId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getReason(),
                history.getActorType(),
                history.getActorId(),
                history.getCreatedAt()
        );
    }
}
