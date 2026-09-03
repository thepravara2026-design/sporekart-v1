package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;

import java.math.BigDecimal;
import java.time.Instant;

public record EnrollmentLifecycleResponse(
        String id,
        String enrollmentCode,
        String batchId,
        String traineeId,
        EnrollmentStatus status,
        String paymentReference,
        BigDecimal priceAmount,
        String currency,
        Instant enrolledAt,
        Instant confirmedAt,
        Instant activatedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static EnrollmentLifecycleResponse fromDomain(TrainingEnrollment domain) {
        return new EnrollmentLifecycleResponse(
                domain.getId(),
                domain.getEnrollmentCode(),
                domain.getBatchId(),
                domain.getTraineeId(),
                domain.getStatus(),
                domain.getPaymentReference(),
                domain.getPriceAmount(),
                domain.getCurrency(),
                domain.getEnrolledAt(),
                domain.getConfirmedAt(),
                domain.getActivatedAt(),
                domain.getCompletedAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
