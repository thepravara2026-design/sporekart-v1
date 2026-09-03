package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;

import java.time.Instant;

public class EnrollmentResponse {

    private String id;
    private String batchId;
    private String traineeId;
    private EnrollmentStatus status;
    private String paymentReference;
    private Instant enrolledAt;
    private Instant createdAt;
    private Instant updatedAt;

    public EnrollmentResponse() {}

    public EnrollmentResponse(String id, String batchId, String traineeId, EnrollmentStatus status, String paymentReference, Instant enrolledAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.batchId = batchId;
        this.traineeId = traineeId;
        this.status = status;
        this.paymentReference = paymentReference;
        this.enrolledAt = enrolledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EnrollmentResponse fromDomain(TrainingEnrollment domain) {
        if (domain == null) return null;
        return new EnrollmentResponse(
                domain.getId(),
                domain.getBatchId(),
                domain.getTraineeId(),
                domain.getStatus(),
                domain.getPaymentReference(),
                domain.getEnrolledAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getTraineeId() { return traineeId; }
    public void setTraineeId(String traineeId) { this.traineeId = traineeId; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
