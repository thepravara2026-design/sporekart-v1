package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentWebhookEvent;
import com.sporekart.modules.payment.domain.WebhookProcessingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payment_webhook_events", uniqueConstraints = {
        @UniqueConstraint(name = "uq_provider_event", columnNames = {"provider", "provider_event_id"})
})
public class PaymentWebhookEventEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private PaymentProviderType provider;

    @Column(name = "provider_event_id", nullable = false, length = 100)
    private String providerEventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "signature_verified", nullable = false)
    private boolean signatureVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private WebhookProcessingStatus processingStatus;

    @Column(name = "error_reason", length = 255)
    private String errorReason;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    public PaymentWebhookEventEntity() {}

    public PaymentWebhookEventEntity(
            UUID id,
            PaymentProviderType provider,
            String providerEventId,
            String eventType,
            boolean signatureVerified,
            WebhookProcessingStatus processingStatus,
            String errorReason,
            OffsetDateTime receivedAt,
            OffsetDateTime processedAt
    ) {
        this.id = id;
        this.provider = provider;
        this.providerEventId = providerEventId;
        this.eventType = eventType;
        this.signatureVerified = signatureVerified;
        this.processingStatus = processingStatus;
        this.errorReason = errorReason;
        this.receivedAt = receivedAt;
        this.processedAt = processedAt;
    }

    public static PaymentWebhookEventEntity fromDomain(PaymentWebhookEvent event) {
        return new PaymentWebhookEventEntity(
                event.getId(),
                event.getProvider(),
                event.getProviderEventId(),
                event.getEventType(),
                event.isSignatureVerified(),
                event.getProcessingStatus(),
                event.getErrorReason(),
                event.getReceivedAt(),
                event.getProcessedAt()
        );
    }

    public PaymentWebhookEvent toDomain() {
        return new PaymentWebhookEvent(
                this.id,
                this.provider,
                this.providerEventId,
                this.eventType,
                this.signatureVerified,
                this.processingStatus,
                this.errorReason,
                this.receivedAt,
                this.processedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public PaymentProviderType getProvider() { return provider; }
    public void setProvider(PaymentProviderType provider) { this.provider = provider; }

    public String getProviderEventId() { return providerEventId; }
    public void setProviderEventId(String providerEventId) { this.providerEventId = providerEventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public boolean isSignatureVerified() { return signatureVerified; }
    public void setSignatureVerified(boolean signatureVerified) { this.signatureVerified = signatureVerified; }

    public WebhookProcessingStatus getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(WebhookProcessingStatus processingStatus) { this.processingStatus = processingStatus; }

    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }

    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(OffsetDateTime receivedAt) { this.receivedAt = receivedAt; }

    public OffsetDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(OffsetDateTime processedAt) { this.processedAt = processedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentWebhookEventEntity that = (PaymentWebhookEventEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
