package com.sporekart.modules.support.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "support_ticket_status_history")
public class SupportTicketStatusHistory {

    @Id
    private String id;

    @Column(name = "ticket_id", nullable = false, length = 36)
    private String ticketId;

    @Column(name = "previous_status", length = 32)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 32)
    private String newStatus;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "actor_type", nullable = false, length = 16)
    private String actorType;

    @Column(name = "actor_id", length = 64)
    private String actorId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected SupportTicketStatusHistory() {}

    public static SupportTicketStatusHistory record(
            String ticketId,
            String previousStatus,
            String newStatus,
            String reason,
            String actorType,
            String actorId
    ) {
        SupportTicketStatusHistory history = new SupportTicketStatusHistory();
        history.id = UUID.randomUUID().toString();
        history.ticketId = ticketId;
        history.previousStatus = previousStatus;
        history.newStatus = newStatus;
        history.reason = reason;
        history.actorType = actorType != null ? actorType : "SYSTEM";
        history.actorId = actorId;
        history.createdAt = OffsetDateTime.now();
        return history;
    }

    public String getId() { return id; }
    public String getTicketId() { return ticketId; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
    public String getActorType() { return actorType; }
    public String getActorId() { return actorId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
