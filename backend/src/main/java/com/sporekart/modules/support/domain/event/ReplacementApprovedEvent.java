package com.sporekart.modules.support.domain.event;

import java.time.OffsetDateTime;

public record ReplacementApprovedEvent(
        String replacementId,
        String replacementReference,
        String ticketId,
        String reservationId,
        String replacementShipmentId,
        OffsetDateTime occurredAt
) {
    public static ReplacementApprovedEvent create(
            String replacementId,
            String replacementReference,
            String ticketId,
            String reservationId,
            String replacementShipmentId
    ) {
        return new ReplacementApprovedEvent(replacementId, replacementReference, ticketId, reservationId, replacementShipmentId, OffsetDateTime.now());
    }
}
