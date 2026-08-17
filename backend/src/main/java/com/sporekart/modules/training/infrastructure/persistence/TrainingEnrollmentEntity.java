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

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TrainingEnrollmentEntity() {}

    public TrainingEnrollmentEntity(String id, String batchId, String traineeId, EnrollmentStatus status, String paymentReference, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.batchId = batchId;
        this.traineeId = traineeId;
        this.status = status;
        this.paymentReference = paymentReference;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TrainingEnrollmentEntity fromDomain(TrainingEnrollment domain) {
        return new TrainingEnrollmentEntity(
                domain.getId(),
                domain.getBatchId(),
                domain.getTraineeId(),
                domain.getStatus(),
                domain.getPaymentReference(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public TrainingEnrollment toDomain() {
        return new TrainingEnrollment(id, batchId, traineeId, status, paymentReference, createdAt, updatedAt);
    }

    // Getters
    public String getId() { return id; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public EnrollmentStatus getStatus() { return status; }
    public String getPaymentReference() { return paymentReference; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
