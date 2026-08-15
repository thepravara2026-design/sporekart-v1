package com.sporekart.modules.order.domain.event;

import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderLifecycleEvent(
        UUID eventId,
        UUID orderId,
        String orderNumber,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String reason,
        OrderActorType actorType,
        String actorId,
        String correlationId,
        OffsetDateTime timestamp
) {
    public static OrderLifecycleEvent create(
            UUID orderId,
            String orderNumber,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String reason,
            OrderActorType actorType,
            String actorId,
            String correlationId
    ) {
        return new OrderLifecycleEvent(
                UUID.randomUUID(),
                orderId,
                orderNumber,
                previousStatus,
                newStatus,
                reason,
                actorType,
                actorId,
                correlationId,
                OffsetDateTime.now()
        );
    }
}
