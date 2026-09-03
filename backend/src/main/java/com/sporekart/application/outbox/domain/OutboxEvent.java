package com.sporekart.application.outbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "aggregate_type", length = 64, nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 128, nullable = false)
    private String aggregateId;

    @Column(name = "event_type", length = 128, nullable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected OutboxEvent() {
        // JPA constructor
    }

    public OutboxEvent(String id, String aggregateType, String aggregateId, String eventType,
                       String payload, OutboxStatus status, int retryCount, int maxRetries,
                       String lastError, OffsetDateTime createdAt, OffsetDateTime scheduledAt,
                       OffsetDateTime processedAt, Long version) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.aggregateType = Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        this.aggregateId = Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.payload = Objects.requireNonNull(payload, "payload must not be null");
        this.status = status != null ? status : OutboxStatus.PENDING;
        this.retryCount = Math.max(0, retryCount);
        this.maxRetries = Math.max(1, maxRetries);
        this.lastError = lastError;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.scheduledAt = scheduledAt != null ? scheduledAt : this.createdAt;
        this.processedAt = processedAt;
        this.version = version;
    }

    public static OutboxEvent create(String aggregateType, String aggregateId, String eventType, String payload) {
        OffsetDateTime now = OffsetDateTime.now();
        return new OutboxEvent(
                UUID.randomUUID().toString(),
                aggregateType,
                aggregateId,
                eventType,
                payload,
                OutboxStatus.PENDING,
                0,
                5,
                null,
                now,
                now,
                null,
                null
        );
    }

    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
    }

    public void markProcessed() {
        this.status = OutboxStatus.PROCESSED;
        this.processedAt = OffsetDateTime.now();
        this.lastError = null;
    }

    public void recordFailure(String errorMessage, int backoffSeconds) {
        this.retryCount++;
        this.lastError = errorMessage;
        if (this.retryCount >= this.maxRetries) {
            this.status = OutboxStatus.DEAD;
        } else {
            this.status = OutboxStatus.FAILED;
            this.scheduledAt = OffsetDateTime.now().plusSeconds(backoffSeconds);
        }
    }

    public void markReplayed(String requestedBy, String reason) {
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.scheduledAt = OffsetDateTime.now().minusSeconds(1);
        this.processedAt = null;
        this.lastError = "Replayed by " + (requestedBy != null ? requestedBy : "OPERATOR") + ": " + (reason != null ? reason : "Operational recovery request");
    }

    public void markStaleReset() {
        this.status = OutboxStatus.PENDING;
        this.scheduledAt = OffsetDateTime.now().minusSeconds(1);
        this.lastError = "Reset stale processing claim at " + OffsetDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public OutboxStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public String getLastError() { return lastError; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getScheduledAt() { return scheduledAt; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
    public Long getVersion() { return version; }
}
