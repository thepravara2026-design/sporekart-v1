package com.sporekart.modules.support.domain.event;

import java.time.OffsetDateTime;

public record SupportTicketAssignedEvent(
        String ticketId,
        String ticketNumber,
        String assignedAgentId,
        OffsetDateTime occurredAt
) {
    public static SupportTicketAssignedEvent create(String ticketId, String ticketNumber, String assignedAgentId) {
        return new SupportTicketAssignedEvent(ticketId, ticketNumber, assignedAgentId, OffsetDateTime.now());
    }
}
