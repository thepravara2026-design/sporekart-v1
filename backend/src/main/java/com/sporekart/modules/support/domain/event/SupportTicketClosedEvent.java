package com.sporekart.modules.support.domain.event;

import java.time.OffsetDateTime;

public record SupportTicketClosedEvent(
        String ticketId,
        String ticketNumber,
        String customerId,
        OffsetDateTime occurredAt
) {
    public static SupportTicketClosedEvent create(String ticketId, String ticketNumber, String customerId) {
        return new SupportTicketClosedEvent(ticketId, ticketNumber, customerId, OffsetDateTime.now());
    }
}
