package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.domain.PaymentStatusHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_status_history")
public class PaymentStatusHistoryEntity {

    @Id
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private PaymentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private PaymentStatus newStatus;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "actor_type", nullable = false)
    private String actorType;

    @Column(name = "actor_id")
    private String actorId;

    @Column(name = "provider_event_id")
    private String providerEventId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public PaymentStatusHistoryEntity() {}

    public PaymentStatusHistoryEntity(
            UUID id,
            UUID paymentId,
            PaymentStatus previousStatus,
            PaymentStatus newStatus,
            String source,
            String actorType,
            String actorId,
            String providerEventId,
            String reason,
            String correlationId,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.paymentId = paymentId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.source = source;
        this.actorType = actorType;
        this.actorId = actorId;
        this.providerEventId = providerEventId;
        this.reason = reason;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
    }

    public static PaymentStatusHistoryEntity fromDomain(PaymentStatusHistory history) {
        return new PaymentStatusHistoryEntity(
                history.getId(),
                history.getPaymentId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getSource(),
                history.getActorType(),
                history.getActorId(),
                history.getProviderEventId(),
                history.getReason(),
                history.getCorrelationId(),
                history.getCreatedAt()
        );
    }

    public PaymentStatusHistory toDomain() {
        return new PaymentStatusHistory(
                id, paymentId, previousStatus, newStatus, source, actorType, actorId, providerEventId, reason, correlationId, createdAt
        );
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public PaymentStatus getPreviousStatus() { return previousStatus; }
    public PaymentStatus getNewStatus() { return newStatus; }
    public String getSource() { return source; }
    public String getActorType() { return actorType; }
    public String getActorId() { return actorId; }
    public String getProviderEventId() { return providerEventId; }
    public String getReason() { return reason; }
    public String getCorrelationId() { return correlationId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
