package com.sporekart.modules.returns.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReturnStatusHistory(
        UUID id,
        UUID returnId,
        ReturnStatus previousStatus,
        ReturnStatus newStatus,
        String reason,
        String actorType,
        String actorId,
        String correlationId,
        OffsetDateTime createdAt
) {
    public static ReturnStatusHistory recordTransition(
            UUID returnId,
            ReturnStatus previousStatus,
            ReturnStatus newStatus,
            String reason,
            String actorType,
            String actorId,
            String correlationId
    ) {
        return new ReturnStatusHistory(
                UUID.randomUUID(),
                returnId,
                previousStatus,
                newStatus,
                reason,
                actorType,
                actorId,
                correlationId,
                OffsetDateTime.now()
        );
    }
}
