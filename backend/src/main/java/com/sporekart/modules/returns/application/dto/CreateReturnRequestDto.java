package com.sporekart.modules.returns.application.dto;

import com.sporekart.modules.returns.domain.ReturnReasonCode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateReturnRequestDto(
        @NotNull(message = "Reason code is required")
        ReturnReasonCode reasonCode,
        String reasonDescription,
        String evidenceUrls,
        @NotEmpty(message = "Items list cannot be empty")
        List<CreateReturnItemInput> items
) {
    public record CreateReturnItemInput(
            @NotNull(message = "Order item ID is required")
            UUID orderItemId,
            int quantity,
            ReturnReasonCode itemReasonCode
    ) {}
}
