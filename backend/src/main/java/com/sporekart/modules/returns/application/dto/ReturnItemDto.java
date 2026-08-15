package com.sporekart.modules.returns.application.dto;

import com.sporekart.modules.returns.domain.ReturnItem;
import com.sporekart.modules.returns.domain.ReturnReasonCode;

import java.math.BigDecimal;
import java.util.UUID;

public record ReturnItemDto(
        UUID id,
        UUID orderItemId,
        UUID productId,
        String sku,
        String productNameSnapshot,
        int requestedQuantity,
        int approvedQuantity,
        int receivedQuantity,
        int acceptedQuantity,
        int rejectedQuantity,
        BigDecimal unitPrice,
        BigDecimal refundAmount,
        ReturnReasonCode reasonCode
) {
    public static ReturnItemDto fromDomain(ReturnItem item) {
        return new ReturnItemDto(
                item.getId(),
                item.getOrderItemId(),
                item.getProductId(),
                item.getSku(),
                item.getProductNameSnapshot(),
                item.getRequestedQuantity(),
                item.getApprovedQuantity(),
                item.getReceivedQuantity(),
                item.getAcceptedQuantity(),
                item.getRejectedQuantity(),
                item.getUnitPrice(),
                item.getRefundAmount(),
                item.getReasonCode()
        );
    }
}
