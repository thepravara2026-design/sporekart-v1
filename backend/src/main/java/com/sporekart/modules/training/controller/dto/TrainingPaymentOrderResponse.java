package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.payment.domain.PaymentProviderType;

import java.math.BigDecimal;

public record TrainingPaymentOrderResponse(
        String trainingPaymentId,
        String paymentId,
        String paymentReference,
        String batchId,
        BigDecimal amount,
        String currency,
        PaymentProviderType provider,
        String providerOrderId,
        String keyId
) {}
