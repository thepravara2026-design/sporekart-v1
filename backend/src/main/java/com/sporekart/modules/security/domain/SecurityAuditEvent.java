package com.sporekart.modules.security.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "security_audit_events")
public class SecurityAuditEvent {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AuditEventType eventType;

    @Column(name = "actor_id")
    private String actorId;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditStatus status;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SecurityAuditEvent() {}

    public SecurityAuditEvent(AuditEventType eventType, String actorId, String targetId, String ipAddress, String userAgent, AuditStatus status, String details) {
        this.id = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.actorId = actorId;
        this.targetId = targetId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.status = status;
        this.details = details;
        this.createdAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public AuditEventType getEventType() { return eventType; }
    public String getActorId() { return actorId; }
    public String getTargetId() { return targetId; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public AuditStatus getStatus() { return status; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityAuditEvent that = (SecurityAuditEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
