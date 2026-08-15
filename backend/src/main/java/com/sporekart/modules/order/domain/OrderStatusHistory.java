package com.sporekart.modules.order.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class OrderStatusHistory {

    private final UUID id;
    private final UUID orderId;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;
    private final String reason;
    private final OrderActorType actorType;
    private final String actorId;
    private final String correlationId;
    private final OffsetDateTime createdAt;

    public OrderStatusHistory(
            UUID id,
            UUID orderId,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String reason,
            OrderActorType actorType,
            String actorId,
            String correlationId,
            OffsetDateTime createdAt
    ) {
        this.id = Objects.requireNonNull(id, "History ID cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.previousStatus = previousStatus;
        this.newStatus = Objects.requireNonNull(newStatus, "New status cannot be null");
        this.reason = Objects.requireNonNull(reason, "Reason cannot be null");
        this.actorType = Objects.requireNonNull(actorType, "Actor type cannot be null");
        this.actorId = actorId;
        this.correlationId = correlationId;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    public static OrderStatusHistory recordTransition(
            UUID orderId,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String reason,
            OrderActorType actorType,
            String actorId,
            String correlationId
    ) {
        return new OrderStatusHistory(
                UUID.randomUUID(),
                orderId,
                previousStatus,
                newStatus,
                reason,
                actorType,
                actorId,
                correlationId,
                OffsetDateTime.now()
        );
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public OrderStatus getPreviousStatus() { return previousStatus; }
    public OrderStatus getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
    public OrderActorType getActorType() { return actorType; }
    public String getActorId() { return actorId; }
    public String getCorrelationId() { return correlationId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderStatusHistory history = (OrderStatusHistory) o;
        return Objects.equals(id, history.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
