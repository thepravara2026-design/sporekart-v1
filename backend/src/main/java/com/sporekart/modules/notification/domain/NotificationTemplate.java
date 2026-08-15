package com.sporekart.modules.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {

    @Id
    private String id;

    @Column(name = "template_code", nullable = false, unique = true)
    private String templateCode;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationTemplate() {}

    public NotificationTemplate(String templateCode, String name, String description, NotificationCategory category) {
        this.id = UUID.randomUUID().toString();
        this.templateCode = templateCode.toUpperCase().trim();
        this.name = name;
        this.description = description;
        this.category = category != null ? category : NotificationCategory.ORDER_UPDATES;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getTemplateCode() { return templateCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public NotificationCategory getCategory() { return category; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String name, String description, NotificationCategory category) {
        this.name = name;
        this.description = description;
        if (category != null) this.category = category;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTemplate that = (NotificationTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
