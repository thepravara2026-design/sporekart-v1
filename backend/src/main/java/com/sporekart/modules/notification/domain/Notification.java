package com.sporekart.modules.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private String id;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "customer_id")
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(name = "template_code", nullable = false)
    private String templateCode;

    @Column(name = "template_version", nullable = false)
    private int templateVersion;

    @Column(nullable = false)
    private String recipient;

    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPriority priority;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "provider_event_id")
    private String providerEventId;

    @Column(name = "provider_status")
    private String providerStatus;

    @Column(name = "last_provider_update_at")
    private Instant lastProviderUpdateAt;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "suppressed_at")
    private Instant suppressedAt;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    protected Notification() {}

    public Notification(String eventId, String eventType, String userId, String customerId,
                        NotificationChannel channel, String templateCode, int templateVersion,
                        String recipient, String subject, String body, NotificationPriority priority,
                        String idempotencyKey, String correlationId, String traceId) {
        this.id = UUID.randomUUID().toString();
        this.eventId = eventId;
        this.eventType = eventType;
        this.userId = userId;
        this.customerId = customerId;
        this.channel = channel;
        this.templateCode = templateCode;
        this.templateVersion = templateVersion > 0 ? templateVersion : 1;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.status = NotificationStatus.CREATED;
        this.priority = priority != null ? priority : NotificationPriority.NORMAL;
        this.idempotencyKey = idempotencyKey;
        this.correlationId = correlationId;
        this.traceId = traceId;
        this.attemptCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void transitionTo(NotificationStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void markProcessing(String providerName) {
        transitionTo(NotificationStatus.PROCESSING);
        this.providerName = providerName;
        this.attemptCount++;
    }

    public void markSent(String providerMessageId) {
        transitionTo(NotificationStatus.SENT);
        this.providerMessageId = providerMessageId;
    }

    public void markDelivered(String providerMessageId) {
        if (this.status != NotificationStatus.DELIVERED) {
            transitionTo(NotificationStatus.DELIVERED);
        }
        if (providerMessageId != null) {
            this.providerMessageId = providerMessageId;
        }
        this.deliveredAt = Instant.now();
    }

    public void markFailed(String reason, boolean permanent) {
        NotificationStatus nextStatus = permanent ? NotificationStatus.FAILED_PERMANENTLY : NotificationStatus.RETRY_SCHEDULED;
        transitionTo(nextStatus);
        this.failedAt = Instant.now();
        this.failureReason = reason;
    }

    public void markCancelled(String reason) {
        transitionTo(NotificationStatus.CANCELLED);
        this.cancelledAt = Instant.now();
        this.failureReason = reason;
    }

    public void markSuppressed(String reason) {
        transitionTo(NotificationStatus.SUPPRESSED);
        this.suppressedAt = Instant.now();
        this.failureReason = reason;
    }

    public void scheduleRetry(String reason, Instant nextScheduledAt) {
        transitionTo(NotificationStatus.RETRY_SCHEDULED);
        this.failedAt = Instant.now();
        this.failureReason = reason;
        this.scheduledAt = nextScheduledAt;
    }

    public void markRead() {
        if (this.readAt == null) {
            this.readAt = Instant.now();
            this.updatedAt = Instant.now();
        }
    }

    public void updateProviderReconciliation(String providerEventId, String providerStatus, NotificationStatus targetStatus) {
        this.providerEventId = providerEventId;
        this.providerStatus = providerStatus;
        this.lastProviderUpdateAt = Instant.now();
        if (targetStatus != null && this.status != targetStatus) {
            if (targetStatus == NotificationStatus.DELIVERED) {
                markDelivered(this.providerMessageId);
            } else if (targetStatus == NotificationStatus.FAILED_PERMANENTLY) {
                markFailed("Failed per provider reconciliation: " + providerStatus, true);
            } else if (this.status.canTransitionTo(targetStatus)) {
                transitionTo(targetStatus);
            }
        }
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getUserId() { return userId; }
    public String getCustomerId() { return customerId; }
    public NotificationChannel getChannel() { return channel; }
    public String getTemplateCode() { return templateCode; }
    public int getTemplateVersion() { return templateVersion; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationStatus getStatus() { return status; }
    public NotificationPriority getPriority() { return priority; }
    public String getProviderName() { return providerName; }
    public String getProviderMessageId() { return providerMessageId; }
    public String getProviderEventId() { return providerEventId; }
    public String getProviderStatus() { return providerStatus; }
    public Instant getLastProviderUpdateAt() { return lastProviderUpdateAt; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getCorrelationId() { return correlationId; }
    public String getTraceId() { return traceId; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getReadAt() { return readAt; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getSuppressedAt() { return suppressedAt; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getFailedAt() { return failedAt; }
    public String getFailureReason() { return failureReason; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
