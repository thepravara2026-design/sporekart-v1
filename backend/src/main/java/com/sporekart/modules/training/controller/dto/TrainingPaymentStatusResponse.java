package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.TrainingPaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TrainingPaymentStatusResponse(
        String trainingPaymentId,
        String paymentId,
        String batchId,
        String traineeId,
        String enrollmentId,
        BigDecimal amount,
        String currency,
        TrainingPaymentStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
