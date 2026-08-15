package com.sporekart.modules.support.domain.event;

import com.sporekart.modules.support.domain.TicketCategory;
import com.sporekart.modules.support.domain.TicketPriority;

import java.time.OffsetDateTime;

public record SupportTicketCreatedEvent(
        String ticketId,
        String ticketNumber,
        String customerId,
        String orderId,
        TicketCategory category,
        TicketPriority priority,
        OffsetDateTime occurredAt
) {
    public static SupportTicketCreatedEvent create(
            String ticketId,
            String ticketNumber,
            String customerId,
            String orderId,
            TicketCategory category,
            TicketPriority priority
    ) {
        return new SupportTicketCreatedEvent(
                ticketId, ticketNumber, customerId, orderId, category, priority, OffsetDateTime.now()
        );
    }
}
