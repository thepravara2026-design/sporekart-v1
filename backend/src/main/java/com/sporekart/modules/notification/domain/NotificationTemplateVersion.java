package com.sporekart.modules.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notification_template_versions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"template_code", "channel", "version", "locale"}))
public class NotificationTemplateVersion {

    public enum TemplateStatus {
        DRAFT,
        ACTIVE,
        ARCHIVED
    }

    @Id
    private String id;

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Column(name = "template_code", nullable = false)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private String locale;

    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationTemplateVersion() {}

    public NotificationTemplateVersion(String templateId, String templateCode, NotificationChannel channel,
                                       int version, String locale, String subject, String body) {
        this.id = UUID.randomUUID().toString();
        this.templateId = templateId;
        this.templateCode = templateCode.toUpperCase().trim();
        this.channel = channel;
        this.version = version > 0 ? version : 1;
        this.locale = locale != null ? locale : "en-US";
        this.subject = subject;
        this.body = body;
        this.status = TemplateStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = TemplateStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        this.status = TemplateStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getTemplateId() { return templateId; }
    public String getTemplateCode() { return templateCode; }
    public NotificationChannel getChannel() { return channel; }
    public int getVersion() { return version; }
    public String getLocale() { return locale; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public TemplateStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTemplateVersion that = (NotificationTemplateVersion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
