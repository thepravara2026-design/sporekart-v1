package com.sporekart.modules.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category"}))
public class NotificationPreference {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled;

    @Column(name = "whatsapp_enabled", nullable = false)
    private boolean whatsappEnabled;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationPreference() {}

    public NotificationPreference(String userId, NotificationCategory category,
                                  boolean emailEnabled, boolean smsEnabled,
                                  boolean whatsappEnabled, boolean inAppEnabled) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.category = category;
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
        this.whatsappEnabled = whatsappEnabled;
        this.inAppEnabled = inAppEnabled;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isChannelEnabled(NotificationChannel channel) {
        if (category.isMandatory()) {
            return true; // Mandatory security/transactional notifications cannot be disabled
        }
        return switch (channel) {
            case EMAIL -> emailEnabled;
            case SMS -> smsEnabled;
            case WHATSAPP -> whatsappEnabled;
            case PUSH -> true;
            case IN_APP -> inAppEnabled;
        };
    }

    public void updatePreferences(boolean emailEnabled, boolean smsEnabled, boolean whatsappEnabled, boolean inAppEnabled) {
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
        this.whatsappEnabled = whatsappEnabled;
        this.inAppEnabled = inAppEnabled;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public NotificationCategory getCategory() { return category; }
    public boolean isEmailEnabled() { return emailEnabled; }
    public boolean isSmsEnabled() { return smsEnabled; }
    public boolean isWhatsappEnabled() { return whatsappEnabled; }
    public boolean isInAppEnabled() { return inAppEnabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationPreference that = (NotificationPreference) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
