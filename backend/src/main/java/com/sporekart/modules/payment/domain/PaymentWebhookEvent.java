package com.sporekart.modules.payment.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class PaymentWebhookEvent {

    private final UUID id;
    private final PaymentProviderType provider;
    private final String providerEventId;
    private final String eventType;
    private boolean signatureVerified;
    private WebhookProcessingStatus processingStatus;
    private String errorReason;
    private final OffsetDateTime receivedAt;
    private OffsetDateTime processedAt;

    public PaymentWebhookEvent(
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
        this.id = Objects.requireNonNull(id, "Webhook event ID cannot be null");
        this.provider = Objects.requireNonNull(provider, "Provider cannot be null");
        this.providerEventId = Objects.requireNonNull(providerEventId, "Provider event ID cannot be null");
        this.eventType = Objects.requireNonNull(eventType, "Event type cannot be null");
        this.signatureVerified = signatureVerified;
        this.processingStatus = Objects.requireNonNull(processingStatus, "Processing status cannot be null");
        this.errorReason = errorReason;
        this.receivedAt = receivedAt != null ? receivedAt : OffsetDateTime.now();
        this.processedAt = processedAt;
    }

    public static PaymentWebhookEvent recordEvent(
            PaymentProviderType provider,
            String providerEventId,
            String eventType,
            boolean signatureVerified
    ) {
        return new PaymentWebhookEvent(
                UUID.randomUUID(),
                provider,
                providerEventId,
                eventType,
                signatureVerified,
                WebhookProcessingStatus.RECEIVED,
                null,
                OffsetDateTime.now(),
                null
        );
    }

    public void markProcessed() {
        this.processingStatus = WebhookProcessingStatus.PROCESSED;
        this.processedAt = OffsetDateTime.now();
    }

    public void markFailed(String reason) {
        this.processingStatus = WebhookProcessingStatus.FAILED;
        this.errorReason = reason;
        this.processedAt = OffsetDateTime.now();
    }

    public void markDuplicate() {
        this.processingStatus = WebhookProcessingStatus.DUPLICATE;
        this.processedAt = OffsetDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public PaymentProviderType getProvider() { return provider; }
    public String getProviderEventId() { return providerEventId; }
    public String getEventType() { return eventType; }
    public boolean isSignatureVerified() { return signatureVerified; }
    public WebhookProcessingStatus getProcessingStatus() { return processingStatus; }
    public String getErrorReason() { return errorReason; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public OffsetDateTime getProcessedAt() { return processedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentWebhookEvent event = (PaymentWebhookEvent) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
