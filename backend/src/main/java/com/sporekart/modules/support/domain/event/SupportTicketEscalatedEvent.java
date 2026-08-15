package com.sporekart.modules.support.domain.event;

import java.time.OffsetDateTime;

public record SupportTicketEscalatedEvent(
        String ticketId,
        String ticketNumber,
        String reason,
        OffsetDateTime occurredAt
) {
    public static SupportTicketEscalatedEvent create(String ticketId, String ticketNumber, String reason) {
        return new SupportTicketEscalatedEvent(ticketId, ticketNumber, reason, OffsetDateTime.now());
    }
}
