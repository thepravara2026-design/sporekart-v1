package com.sporekart.modules.returns.application.dto;

import com.sporekart.modules.returns.domain.Return;
import com.sporekart.modules.returns.domain.ReturnReasonCode;
import com.sporekart.modules.returns.domain.ReturnStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ReturnDto(
        UUID id,
        String returnReference,
        UUID orderId,
        String orderReference,
        String customerId,
        ReturnStatus status,
        ReturnReasonCode reasonCode,
        String reasonDescription,
        String evidenceUrls,
        String policyVersion,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,
        OffsetDateTime receivedAt,
        OffsetDateTime inspectedAt,
        OffsetDateTime completedAt,
        long version,
        BigDecimal totalRefundableAmount,
        List<ReturnItemDto> items,
        List<ReturnStatusHistoryDto> statusHistory,
        ReturnInspectionDto inspection,
        RefundRecordDto refundRecord
) {
    public static ReturnDto fromDomain(Return r) {
        return fromDomain(r, null);
    }

    public static ReturnDto fromDomain(Return r, RefundRecordDto refundRecord) {
        List<ReturnItemDto> itemDtos = r.getItems().stream()
                .map(ReturnItemDto::fromDomain)
                .collect(Collectors.toList());

        List<ReturnStatusHistoryDto> historyDtos = r.getStatusHistory().stream()
                .map(ReturnStatusHistoryDto::fromDomain)
                .collect(Collectors.toList());

        ReturnInspectionDto inspectionDto = ReturnInspectionDto.fromDomain(r.getInspection());

        return new ReturnDto(
                r.getId(),
                r.getReturnReference(),
                r.getOrderId(),
                r.getOrderReference(),
                r.getCustomerId(),
                r.getStatus(),
                r.getReasonCode(),
                r.getReasonDescription(),
                r.getEvidenceUrls(),
                r.getPolicyVersion(),
                r.getRequestedAt(),
                r.getApprovedAt(),
                r.getReceivedAt(),
                r.getInspectedAt(),
                r.getCompletedAt(),
                r.getVersion(),
                r.calculateTotalRefundableAmount(),
                itemDtos,
                historyDtos,
                inspectionDto,
                refundRecord
        );
    }
}
