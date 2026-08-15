package com.sporekart.modules.payment.application.dto;

import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentAttemptDto(
        UUID id,
        UUID paymentId,
        String attemptReference,
        PaymentProviderType provider,
        String providerOrderId,
        String providerPaymentId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        String failureCode,
        String failureReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PaymentAttemptDto fromDomain(PaymentAttempt attempt) {
        return new PaymentAttemptDto(
                attempt.getId(),
                attempt.getPaymentId(),
                attempt.getAttemptReference(),
                attempt.getProvider(),
                attempt.getProviderOrderId(),
                attempt.getProviderPaymentId(),
                attempt.getStatus(),
                attempt.getAmount(),
                attempt.getCurrency(),
                attempt.getFailureCode(),
                attempt.getFailureReason(),
                attempt.getCreatedAt(),
                attempt.getUpdatedAt()
        );
    }
}
