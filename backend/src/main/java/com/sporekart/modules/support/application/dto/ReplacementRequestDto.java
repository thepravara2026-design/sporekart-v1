package com.sporekart.modules.support.application.dto;

import com.sporekart.modules.support.domain.ReplacementRequest;
import com.sporekart.modules.support.domain.ReplacementStatus;

import java.time.OffsetDateTime;

public record ReplacementRequestDto(
        String id,
        String replacementReference,
        String ticketId,
        String ticketNumber,
        String orderId,
        String orderReference,
        String customerId,
        String orderItemId,
        String productId,
        String sku,
        int quantity,
        String reason,
        ReplacementStatus status,
        String reservationId,
        String replacementShipmentId,
        String adminNotes,
        OffsetDateTime approvedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ReplacementRequestDto fromDomain(ReplacementRequest req) {
        return new ReplacementRequestDto(
                req.getId(),
                req.getReplacementReference(),
                req.getTicketId(),
                req.getTicketNumber(),
                req.getOrderId(),
                req.getOrderReference(),
                req.getCustomerId(),
                req.getOrderItemId(),
                req.getProductId(),
                req.getSku(),
                req.getQuantity(),
                req.getReason(),
                req.getStatus(),
                req.getReservationId(),
                req.getReplacementShipmentId(),
                req.getAdminNotes(),
                req.getApprovedAt(),
                req.getCreatedAt(),
                req.getUpdatedAt()
        );
    }
}
