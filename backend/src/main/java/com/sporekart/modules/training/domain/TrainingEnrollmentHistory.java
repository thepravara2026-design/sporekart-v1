package com.sporekart.modules.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingEnrollmentHistory {

    private final String id;
    private final String enrollmentId;
    private final EnrollmentStatus fromStatus;
    private final EnrollmentStatus toStatus;
    private final String reason;
    private final String actor;
    private final Instant createdAt;

    public TrainingEnrollmentHistory(String id, String enrollmentId, EnrollmentStatus fromStatus, EnrollmentStatus toStatus, String reason, String actor, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.enrollmentId = Objects.requireNonNull(enrollmentId, "enrollmentId must not be null");
        this.fromStatus = fromStatus;
        this.toStatus = Objects.requireNonNull(toStatus, "toStatus must not be null");
        this.reason = reason;
        this.actor = actor != null ? actor : "SYSTEM";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static TrainingEnrollmentHistory record(String enrollmentId, EnrollmentStatus fromStatus, EnrollmentStatus toStatus, String reason, String actor) {
        return new TrainingEnrollmentHistory(null, enrollmentId, fromStatus, toStatus, reason, actor, Instant.now());
    }

    public String getId() { return id; }
    public String getEnrollmentId() { return enrollmentId; }
    public EnrollmentStatus getFromStatus() { return fromStatus; }
    public EnrollmentStatus getToStatus() { return toStatus; }
    public String getReason() { return reason; }
    public String getActor() { return actor; }
    public Instant getCreatedAt() { return createdAt; }
}
