package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.AttendanceStatus;
import com.sporekart.modules.training.domain.TrainingAttendance;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "training_attendances", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_enrollment_schedule", columnNames = {"enrollment_id", "schedule_id"})
})
public class TrainingAttendanceEntity {

    @Id
    private String id;

    @Column(name = "enrollment_id", nullable = false)
    private String enrollmentId;

    @Column(name = "schedule_id", nullable = false)
    private String scheduleId;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "trainee_id", nullable = false)
    private String traineeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "marked_by", nullable = false)
    private String markedBy;

    @Column(name = "marked_at", nullable = false)
    private Instant markedAt;

    public TrainingAttendanceEntity() {}

    public static TrainingAttendanceEntity fromDomain(TrainingAttendance a) {
        TrainingAttendanceEntity e = new TrainingAttendanceEntity();
        e.id = a.getId();
        e.enrollmentId = a.getEnrollmentId();
        e.scheduleId = a.getScheduleId();
        e.batchId = a.getBatchId();
        e.traineeId = a.getTraineeId();
        e.status = a.getStatus();
        e.notes = a.getNotes();
        e.markedBy = a.getMarkedBy();
        e.markedAt = a.getMarkedAt();
        return e;
    }

    public TrainingAttendance toDomain() {
        return new TrainingAttendance(
                id, enrollmentId, scheduleId, batchId, traineeId, status, notes, markedBy, markedAt
        );
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
