package com.sporekart.modules.returns.application.dto;

import com.sporekart.modules.returns.domain.service.ReturnEligibilityService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ReturnEligibilityDto(
        UUID orderId,
        String orderReference,
        boolean eligible,
        String ineligibilityReason,
        OffsetDateTime deliveryTimestamp,
        OffsetDateTime returnDeadline,
        List<ItemEligibilityDto> items
) {
    public record ItemEligibilityDto(
            UUID orderItemId,
            UUID productId,
            String sku,
            String productName,
            int orderedQuantity,
            int previouslyReturnedQuantity,
            int returnableQuantity,
            boolean isReturnable,
            String reasonCode
    ) {}

    public static ReturnEligibilityDto fromResult(ReturnEligibilityService.OrderEligibilityResult result) {
        List<ItemEligibilityDto> items = result.itemEligibilities().stream()
                .map(i -> new ItemEligibilityDto(
                        i.orderItemId(),
                        i.productId(),
                        i.sku(),
                        i.productName(),
                        i.orderedQuantity(),
                        i.previouslyReturnedQuantity(),
                        i.returnableQuantity(),
                        i.isReturnable(),
                        i.reasonCode()
                ))
                .collect(Collectors.toList());

        return new ReturnEligibilityDto(
                result.orderId(),
                result.orderReference(),
                result.eligible(),
                result.ineligibilityReason(),
                result.deliveryTimestamp(),
                result.returnDeadline(),
                items
        );
    }
}
