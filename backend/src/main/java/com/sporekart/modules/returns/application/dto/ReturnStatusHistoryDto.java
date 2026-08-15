package com.sporekart.modules.returns.application.dto;

import com.sporekart.modules.returns.domain.ReturnStatus;
import com.sporekart.modules.returns.domain.ReturnStatusHistory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReturnStatusHistoryDto(
        UUID id,
        ReturnStatus previousStatus,
        ReturnStatus newStatus,
        String reason,
        String actorType,
        String actorId,
        String correlationId,
        OffsetDateTime createdAt
) {
    public static ReturnStatusHistoryDto fromDomain(ReturnStatusHistory h) {
        return new ReturnStatusHistoryDto(
                h.id(),
                h.previousStatus(),
                h.newStatus(),
                h.reason(),
                h.actorType(),
                h.actorId(),
                h.correlationId(),
                h.createdAt()
        );
    }
}
