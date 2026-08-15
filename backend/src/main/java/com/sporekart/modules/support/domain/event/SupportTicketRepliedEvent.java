package com.sporekart.modules.support.domain.event;

import com.sporekart.modules.support.domain.AuthorType;
import com.sporekart.modules.support.domain.MessageVisibility;

import java.time.OffsetDateTime;

public record SupportTicketRepliedEvent(
        String ticketId,
        String ticketNumber,
        String authorId,
        AuthorType authorType,
        MessageVisibility visibility,
        OffsetDateTime occurredAt
) {
    public static SupportTicketRepliedEvent create(
            String ticketId,
            String ticketNumber,
            String authorId,
            AuthorType authorType,
            MessageVisibility visibility
    ) {
        return new SupportTicketRepliedEvent(ticketId, ticketNumber, authorId, authorType, visibility, OffsetDateTime.now());
    }
}
