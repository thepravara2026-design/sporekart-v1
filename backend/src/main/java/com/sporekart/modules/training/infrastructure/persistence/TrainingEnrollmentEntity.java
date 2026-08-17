package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "training_enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_batch_trainee", columnNames = {"batch_id", "trainee_id"})
})
public class TrainingEnrollmentEntity {

    @Id
    private String id;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "trainee_id", nullable = false)
    private String traineeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    protected TrainingEnrollmentEntity() {}

    public TrainingEnrollmentEntity(String id, String batchId, String traineeId, EnrollmentStatus status, String paymentReference, Instant enrolledAt, Instant createdAt, Instant updatedAt, String createdBy, String updatedBy, String idempotencyKey) {
        this.id = id;
        this.batchId = batchId;
        this.traineeId = traineeId;
        this.status = status;
        this.paymentReference = paymentReference;
        this.enrolledAt = enrolledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.idempotencyKey = idempotencyKey;
    }

    public static TrainingEnrollmentEntity fromDomain(TrainingEnrollment domain) {
        return new TrainingEnrollmentEntity(
                domain.getId(),
                domain.getBatchId(),
                domain.getTraineeId(),
                domain.getStatus(),
                domain.getPaymentReference(),
                domain.getEnrolledAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getCreatedBy(),
                domain.getUpdatedBy(),
                domain.getIdempotencyKey()
        );
    }

    public TrainingEnrollment toDomain() {
        return new TrainingEnrollment(id, batchId, traineeId, status, paymentReference, enrolledAt, createdAt, updatedAt, createdBy, updatedBy, idempotencyKey);
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
}
