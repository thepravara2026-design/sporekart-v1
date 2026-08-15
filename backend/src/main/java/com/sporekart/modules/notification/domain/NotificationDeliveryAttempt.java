package com.sporekart.modules.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notification_delivery_attempts")
public class NotificationDeliveryAttempt {

    @Id
    private String id;

    @Column(name = "notification_id", nullable = false)
    private String notificationId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    protected NotificationDeliveryAttempt() {}

    public NotificationDeliveryAttempt(String notificationId, int attemptNumber, String providerName,
                                       NotificationStatus status, String providerMessageId,
                                       String errorMessage, long durationMs) {
        this.id = UUID.randomUUID().toString();
        this.notificationId = notificationId;
        this.attemptNumber = attemptNumber;
        this.providerName = providerName;
        this.status = status;
        this.providerMessageId = providerMessageId;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
        this.attemptedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getNotificationId() { return notificationId; }
    public int getAttemptNumber() { return attemptNumber; }
    public String getProviderName() { return providerName; }
    public NotificationStatus getStatus() { return status; }
    public String getProviderMessageId() { return providerMessageId; }
    public String getErrorMessage() { return errorMessage; }
    public long getDurationMs() { return durationMs; }
    public Instant getAttemptedAt() { return attemptedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationDeliveryAttempt that = (NotificationDeliveryAttempt) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
