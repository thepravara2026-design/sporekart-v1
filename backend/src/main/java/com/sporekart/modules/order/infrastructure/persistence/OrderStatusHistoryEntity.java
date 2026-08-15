package com.sporekart.modules.order.infrastructure.persistence;

import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderStatusHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistoryEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private OrderStatus newStatus;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 30)
    private OrderActorType actorType;

    @Column(name = "actor_id", length = 100)
    private String actorId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public OrderStatusHistoryEntity() {}

    public OrderStatusHistoryEntity(
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
        this.id = id;
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.actorType = actorType;
        this.actorId = actorId;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
    }

    public static OrderStatusHistoryEntity fromDomain(OrderStatusHistory history) {
        return new OrderStatusHistoryEntity(
                history.getId(),
                history.getOrderId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getReason(),
                history.getActorType(),
                history.getActorId(),
                history.getCorrelationId(),
                history.getCreatedAt()
        );
    }

    public OrderStatusHistory toDomain() {
        return new OrderStatusHistory(
                this.id,
                this.orderId,
                this.previousStatus,
                this.newStatus,
                this.reason,
                this.actorType,
                this.actorId,
                this.correlationId,
                this.createdAt
        );
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public OrderStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(OrderStatus previousStatus) { this.previousStatus = previousStatus; }

    public OrderStatus getNewStatus() { return newStatus; }
    public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public OrderActorType getActorType() { return actorType; }
    public void setActorType(OrderActorType actorType) { this.actorType = actorType; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderStatusHistoryEntity that = (OrderStatusHistoryEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
