package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollmentHistory;

import java.time.Instant;

public record EnrollmentHistoryResponseDto(
        String id,
        String enrollmentId,
        EnrollmentStatus fromStatus,
        EnrollmentStatus toStatus,
        String reason,
        String actor,
        Instant createdAt
) {
    public static EnrollmentHistoryResponseDto fromDomain(TrainingEnrollmentHistory domain) {
        return new EnrollmentHistoryResponseDto(
                domain.getId(),
                domain.getEnrollmentId(),
                domain.getFromStatus(),
                domain.getToStatus(),
                domain.getReason(),
                domain.getActor(),
                domain.getCreatedAt()
        );
    }
}
