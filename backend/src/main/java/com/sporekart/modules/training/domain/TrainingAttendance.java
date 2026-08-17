package com.sporekart.modules.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingAttendance {

    private final String id;
    private final String enrollmentId;
    private final String scheduleId;
    private final String batchId;
    private final String traineeId;
    private AttendanceStatus status;
    private String notes;
    private String markedBy;
    private Instant markedAt;

    public TrainingAttendance(
            String id,
            String enrollmentId,
            String scheduleId,
            String batchId,
            String traineeId,
            AttendanceStatus status,
            String notes,
            String markedBy,
            Instant markedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.enrollmentId = Objects.requireNonNull(enrollmentId, "enrollmentId must not be null");
        this.scheduleId = Objects.requireNonNull(scheduleId, "scheduleId must not be null");
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.notes = notes;
        this.markedBy = markedBy != null ? markedBy : "SYSTEM";
        this.markedAt = markedAt != null ? markedAt : Instant.now();
    }

    public static TrainingAttendance mark(
            String enrollmentId, String scheduleId, String batchId, String traineeId,
            AttendanceStatus status, String notes, String markedBy) {
        return new TrainingAttendance(
                UUID.randomUUID().toString(), enrollmentId, scheduleId, batchId, traineeId,
                status, notes, markedBy, Instant.now()
        );
    }

    public void updateStatus(AttendanceStatus newStatus, String notes, String markedBy) {
        this.status = Objects.requireNonNull(newStatus, "newStatus must not be null");
        this.notes = notes;
        this.markedBy = markedBy;
        this.markedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getEnrollmentId() { return enrollmentId; }
    public String getScheduleId() { return scheduleId; }
    public String getBatchId() { return batchId; }
    public String getTraineeId() { return traineeId; }
    public AttendanceStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getMarkedBy() { return markedBy; }
    public Instant getMarkedAt() { return markedAt; }
}
