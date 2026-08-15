package com.sporekart.modules.security.application.dto;

import com.sporekart.modules.security.domain.UserSession;
import java.time.Instant;

public class UserSessionDto {

    private String id;
    private String userId;
    private String deviceInfo;
    private String ipAddress;
    private boolean active;
    private Instant createdAt;
    private Instant expiresAt;

    public UserSessionDto() {}

    public UserSessionDto(UserSession session) {
        this.id = session.getId();
        this.userId = session.getUserId();
        this.deviceInfo = session.getDeviceInfo();
        this.ipAddress = session.getIpAddress();
        this.active = session.isActive();
        this.createdAt = session.getCreatedAt();
        this.expiresAt = session.getExpiresAt();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getDeviceInfo() { return deviceInfo; }
    public String getIpAddress() { return ipAddress; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
}
