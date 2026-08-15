package com.sporekart.modules.payment.application.dto;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentDto(
        UUID id,
        String paymentReference,
        UUID orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        PaymentProviderType provider,
        UUID activeAttemptId,
        List<PaymentAttemptDto> attempts,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PaymentDto fromDomain(Payment payment) {
        List<PaymentAttemptDto> attemptDtos = payment.getAttempts() != null
                ? payment.getAttempts().stream().map(PaymentAttemptDto::fromDomain).toList()
                : List.of();

        return new PaymentDto(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getProvider(),
                payment.getActiveAttemptId(),
                attemptDtos,
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
