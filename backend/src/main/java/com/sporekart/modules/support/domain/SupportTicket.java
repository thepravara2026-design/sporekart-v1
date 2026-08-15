package com.sporekart.modules.support.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    private String id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 32)
    private String ticketNumber;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "order_id", length = 36)
    private String orderId;

    @Column(name = "order_reference", length = 64)
    private String orderReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false, length = 64)
    private IssueType issueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 32)
    private TicketSource source;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "assigned_agent_id", length = 64)
    private String assignedAgentId;

    @Column(name = "first_response_at")
    private OffsetDateTime firstResponseAt;

    @Column(name = "first_response_due_at", nullable = false)
    private OffsetDateTime firstResponseDueAt;

    @Column(name = "resolution_due_at", nullable = false)
    private OffsetDateTime resolutionDueAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sla_status", nullable = false, length = 16)
    private SlaStatus slaStatus;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Long version;

    protected SupportTicket() {}

    public static SupportTicket createNew(
            String ticketNumber,
            String customerId,
            String orderId,
            String orderReference,
            TicketCategory category,
            IssueType issueType,
            TicketPriority priority,
            TicketSource source,
            String subject,
            String description
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        SupportTicket ticket = new SupportTicket();
        ticket.id = UUID.randomUUID().toString();
        ticket.ticketNumber = ticketNumber;
        ticket.customerId = customerId;
        ticket.orderId = orderId;
        ticket.orderReference = orderReference;
        ticket.category = category;
        ticket.issueType = issueType;
        ticket.status = TicketStatus.OPEN;
        ticket.priority = priority != null ? priority : TicketPriority.NORMAL;
        ticket.source = source != null ? source : TicketSource.CUSTOMER_WEB;
        ticket.subject = subject;
        ticket.description = description;
        ticket.slaStatus = SlaStatus.MET;
        ticket.createdAt = now;
        ticket.updatedAt = now;

        ticket.firstResponseDueAt = now.plusHours(ticket.priority.getFirstResponseHours());
        ticket.resolutionDueAt = now.plusHours(ticket.priority.getResolutionHours());

        return ticket;
    }

    public void assign(String agentId) {
        SupportStateMachine.validateTransition(this.status, TicketStatus.ASSIGNED);
        this.assignedAgentId = agentId;
        this.status = TicketStatus.ASSIGNED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updatePriority(TicketPriority newPriority) {
        if (newPriority != null && newPriority != this.priority) {
            this.priority = newPriority;
            this.updatedAt = OffsetDateTime.now();
        }
    }

    public void recordFirstResponse() {
        if (this.firstResponseAt == null) {
            this.firstResponseAt = OffsetDateTime.now();
            if (this.firstResponseAt.isAfter(this.firstResponseDueAt)) {
                this.slaStatus = SlaStatus.BREACHED;
            }
            this.updatedAt = OffsetDateTime.now();
        }
    }

    public void escalate(String reason) {
        SupportStateMachine.validateTransition(this.status, TicketStatus.ESCALATED);
        this.status = TicketStatus.ESCALATED;
        this.priority = TicketPriority.URGENT;
        this.slaStatus = SlaStatus.BREACHED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void resolve(String resolutionNotes) {
        SupportStateMachine.validateTransition(this.status, TicketStatus.RESOLVED);
        OffsetDateTime now = OffsetDateTime.now();
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = now;
        if (now.isAfter(this.resolutionDueAt) && this.slaStatus != SlaStatus.BREACHED) {
            this.slaStatus = SlaStatus.BREACHED;
        }
        this.updatedAt = now;
    }

    public void reopen(String reason) {
        SupportStateMachine.validateTransition(this.status, TicketStatus.REOPENED);
        this.status = TicketStatus.REOPENED;
        this.resolvedAt = null;
        this.updatedAt = OffsetDateTime.now();
    }

    public void close() {
        SupportStateMachine.validateTransition(this.status, TicketStatus.CLOSED);
        OffsetDateTime now = OffsetDateTime.now();
        this.status = TicketStatus.CLOSED;
        this.closedAt = now;
        this.updatedAt = now;
    }

    public void cancel(String reason) {
        SupportStateMachine.validateTransition(this.status, TicketStatus.CANCELLED);
        this.status = TicketStatus.CANCELLED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateStatus(TicketStatus nextStatus) {
        SupportStateMachine.validateTransition(this.status, nextStatus);
        this.status = nextStatus;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateSlaStatus(SlaStatus newSlaStatus) {
        this.slaStatus = newSlaStatus;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getTicketNumber() { return ticketNumber; }
    public String getCustomerId() { return customerId; }
    public String getOrderId() { return orderId; }
    public String getOrderReference() { return orderReference; }
    public TicketCategory getCategory() { return category; }
    public IssueType getIssueType() { return issueType; }
    public TicketStatus getStatus() { return status; }
    public TicketPriority getPriority() { return priority; }
    public TicketSource getSource() { return source; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public String getAssignedAgentId() { return assignedAgentId; }
    public OffsetDateTime getFirstResponseAt() { return firstResponseAt; }
    public OffsetDateTime getFirstResponseDueAt() { return firstResponseDueAt; }
    public OffsetDateTime getResolutionDueAt() { return resolutionDueAt; }
    public SlaStatus getSlaStatus() { return slaStatus; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public OffsetDateTime getClosedAt() { return closedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
