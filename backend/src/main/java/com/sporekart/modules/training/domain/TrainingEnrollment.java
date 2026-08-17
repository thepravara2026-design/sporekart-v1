package com.sporekart.modules.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingEnrollment {

    private final String id;
    private final String batchId;
    private final String traineeId;
    private EnrollmentStatus status;
    private String paymentReference;
    private final Instant enrolledAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private String idempotencyKey;

    public TrainingEnrollment(String id, String batchId, String traineeId, EnrollmentStatus status, String paymentReference, Instant createdAt, Instant updatedAt) {
        this(id, batchId, traineeId, status, paymentReference, createdAt, createdAt, updatedAt, traineeId, traineeId, null);
    }

    public TrainingEnrollment(String id, String batchId, String traineeId, EnrollmentStatus status, String paymentReference, Instant enrolledAt, Instant createdAt, Instant updatedAt, String createdBy, String updatedBy, String idempotencyKey) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.status = status != null ? status : EnrollmentStatus.PENDING;
        this.paymentReference = paymentReference;
        this.enrolledAt = enrolledAt != null ? enrolledAt : Instant.now();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.idempotencyKey = idempotencyKey;
    }

    public static TrainingEnrollment create(String batchId, String traineeId) {
        return create(batchId, traineeId, null, traineeId);
    }

    public static TrainingEnrollment create(String batchId, String traineeId, String idempotencyKey, String actor) {
        Instant now = Instant.now();
        return new TrainingEnrollment(null, batchId, traineeId, EnrollmentStatus.PENDING, null, now, now, now, actor != null ? actor : traineeId, actor != null ? actor : traineeId, idempotencyKey);
    }

    public void confirm(String paymentReference) {
        this.status = EnrollmentStatus.CONFIRMED;
        if (paymentReference != null) {
            this.paymentReference = paymentReference;
        }
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = EnrollmentStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void waitlist() {
        this.status = EnrollmentStatus.WAITLISTED;
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public EnrollmentStatus getStatus() { return status; }
    public String getPaymentReference() { return paymentReference; }
    public Instant getEnrolledAt() { return enrolledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public String getIdempotencyKey() { return idempotencyKey; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingEnrollment that = (TrainingEnrollment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
