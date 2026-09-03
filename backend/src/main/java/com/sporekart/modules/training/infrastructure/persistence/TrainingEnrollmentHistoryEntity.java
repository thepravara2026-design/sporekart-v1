package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollmentHistory;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "training_enrollment_history")
public class TrainingEnrollmentHistoryEntity {

    @Id
    private String id;

    @Column(name = "enrollment_id", nullable = false)
    private String enrollmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private EnrollmentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private EnrollmentStatus toStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "actor", nullable = false)
    private String actor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TrainingEnrollmentHistoryEntity() {}

    public TrainingEnrollmentHistoryEntity(String id, String enrollmentId, EnrollmentStatus fromStatus, EnrollmentStatus toStatus, String reason, String actor, Instant createdAt) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.actor = actor;
        this.createdAt = createdAt;
    }

    public static TrainingEnrollmentHistoryEntity fromDomain(TrainingEnrollmentHistory domain) {
        return new TrainingEnrollmentHistoryEntity(
                domain.getId(),
                domain.getEnrollmentId(),
                domain.getFromStatus(),
                domain.getToStatus(),
                domain.getReason(),
                domain.getActor(),
                domain.getCreatedAt()
        );
    }

    public TrainingEnrollmentHistory toDomain() {
        return new TrainingEnrollmentHistory(id, enrollmentId, fromStatus, toStatus, reason, actor, createdAt);
    }

    public String getId() { return id; }
    public String getEnrollmentId() { return enrollmentId; }
    public EnrollmentStatus getFromStatus() { return fromStatus; }
    public EnrollmentStatus getToStatus() { return toStatus; }
    public String getReason() { return reason; }
    public String getActor() { return actor; }
    public Instant getCreatedAt() { return createdAt; }
}
