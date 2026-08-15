package com.sporekart.modules.returns.application.dto;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.returns.infrastructure.persistence.RefundRecordEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RefundRecordDto(
        UUID id,
        String refundReference,
        UUID returnId,
        String returnReference,
        UUID orderId,
        String customerId,
        UUID paymentId,
        String paymentReference,
        PaymentProviderType provider,
        BigDecimal amount,
        String currency,
        String status,
        String failureReason,
        String providerRefundId,
        String idempotencyKey,
        OffsetDateTime createdAt
) {
    public static RefundRecordDto fromEntity(RefundRecordEntity entity) {
        return new RefundRecordDto(
                entity.getId(),
                entity.getRefundReference(),
                entity.getReturnId(),
                entity.getReturnReference(),
                entity.getOrderId(),
                entity.getCustomerId(),
                entity.getPaymentId(),
                entity.getPaymentReference(),
                entity.getProvider(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getProviderRefundId(),
                entity.getIdempotencyKey(),
                entity.getCreatedAt()
        );
    }
}
