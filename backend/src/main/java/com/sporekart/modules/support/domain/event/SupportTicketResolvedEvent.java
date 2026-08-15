package com.sporekart.modules.support.domain.event;

import java.time.OffsetDateTime;

public record SupportTicketResolvedEvent(
        String ticketId,
        String ticketNumber,
        String customerId,
        OffsetDateTime occurredAt
) {
    public static SupportTicketResolvedEvent create(String ticketId, String ticketNumber, String customerId) {
        return new SupportTicketResolvedEvent(ticketId, ticketNumber, customerId, OffsetDateTime.now());
    }
}
