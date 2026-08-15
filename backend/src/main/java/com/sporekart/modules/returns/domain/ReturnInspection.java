package com.sporekart.modules.returns.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReturnInspection(
        UUID id,
        UUID returnId,
        String inspectorId,
        InspectionOutcome outcome,
        String notes,
        OffsetDateTime inspectedAt
) {
    public static ReturnInspection create(UUID returnId, String inspectorId, InspectionOutcome outcome, String notes) {
        return new ReturnInspection(
                UUID.randomUUID(),
                returnId,
                inspectorId,
                outcome,
                notes,
                OffsetDateTime.now()
        );
    }
}
