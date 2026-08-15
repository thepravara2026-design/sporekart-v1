package com.sporekart.modules.security.application.dto;

import com.sporekart.modules.security.domain.SecurityAuditEvent;
import java.time.Instant;

public class SecurityAuditEventDto {

    private String id;
    private String eventType;
    private String actorId;
    private String targetId;
    private String ipAddress;
    private String userAgent;
    private String status;
    private String details;
    private Instant createdAt;

    public SecurityAuditEventDto() {}

    public SecurityAuditEventDto(SecurityAuditEvent event) {
        this.id = event.getId();
        this.eventType = event.getEventType().name();
        this.actorId = event.getActorId();
        this.targetId = event.getTargetId();
        this.ipAddress = event.getIpAddress();
        this.userAgent = event.getUserAgent();
        this.status = event.getStatus().name();
        this.details = event.getDetails();
        this.createdAt = event.getCreatedAt();
    }

    public String getId() { return id; }
    public String getEventType() { return eventType; }
    public String getActorId() { return actorId; }
    public String getTargetId() { return targetId; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getStatus() { return status; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
