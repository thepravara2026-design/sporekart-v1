package com.sporekart.modules.returns.application.dto;

import com.sporekart.modules.returns.domain.InspectionOutcome;
import com.sporekart.modules.returns.domain.ReturnInspection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ReturnInspectionDto(
        UUID id,
        @NotBlank(message = "inspectorId is required") String inspectorId,
        @NotNull(message = "outcome is required") InspectionOutcome outcome,
        String notes,
        OffsetDateTime inspectedAt,
        List<ItemInspectionInput> itemInspections
) {
    public record ItemInspectionInput(UUID returnItemId, int acceptedQuantity, int rejectedQuantity) {}

    public static ReturnInspectionDto fromDomain(ReturnInspection i) {
        if (i == null) return null;
        return new ReturnInspectionDto(
                i.id(),
                i.inspectorId(),
                i.outcome(),
                i.notes(),
                i.inspectedAt(),
                List.of()
        );
    }
}
