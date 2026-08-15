package com.sporekart.modules.payment.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class PaymentStatusHistory {

    private final UUID id;
    private final UUID paymentId;
    private final PaymentStatus previousStatus;
    private final PaymentStatus newStatus;
    private final String source;
    private final String actorType;
    private final String actorId;
    private final String providerEventId;
    private final String reason;
    private final String correlationId;
    private final OffsetDateTime createdAt;

    public PaymentStatusHistory(
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
        this.id = Objects.requireNonNull(id, "History ID cannot be null");
        this.paymentId = Objects.requireNonNull(paymentId, "Payment ID cannot be null");
        this.previousStatus = previousStatus;
        this.newStatus = Objects.requireNonNull(newStatus, "New status cannot be null");
        this.source = source != null ? source : "SYSTEM";
        this.actorType = actorType != null ? actorType : "SYSTEM";
        this.actorId = actorId;
        this.providerEventId = providerEventId;
        this.reason = reason != null ? reason : "Payment status updated to " + newStatus;
        this.correlationId = correlationId;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    public static PaymentStatusHistory recordTransition(
            UUID paymentId,
            PaymentStatus previousStatus,
            PaymentStatus newStatus,
            String source,
            String actorType,
            String actorId,
            String providerEventId,
            String reason,
            String correlationId
    ) {
        return new PaymentStatusHistory(
                UUID.randomUUID(),
                paymentId,
                previousStatus,
                newStatus,
                source,
                actorType,
                actorId,
                providerEventId,
                reason,
                correlationId,
                OffsetDateTime.now()
        );
    }

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
