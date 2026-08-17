package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.BatchSchedule;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "batch_schedules")
public class BatchScheduleEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private TrainingBatchEntity batch;

    @Column(nullable = false)
    private String title;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BatchScheduleEntity() {}

    public BatchScheduleEntity(String id, TrainingBatchEntity batch, String title, Instant scheduledAt, int durationMinutes, String location, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.batch = batch;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.location = location;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BatchScheduleEntity fromDomain(BatchSchedule domain, TrainingBatchEntity batchEntity) {
        return new BatchScheduleEntity(
                domain.getId(),
                batchEntity,
                domain.getTitle(),
                domain.getScheduledAt(),
                domain.getDurationMinutes(),
                domain.getLocation(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public BatchSchedule toDomain() {
        String batchIdVal = batch != null ? batch.getId() : null;
        return new BatchSchedule(id, batchIdVal, title, scheduledAt, durationMinutes, location, createdAt, updatedAt);
    }

    // Getters and setters
    public String getId() { return id; }
    public TrainingBatchEntity getBatch() { return batch; }
    public void setBatch(TrainingBatchEntity batch) { this.batch = batch; }
    public String getTitle() { return title; }
    public Instant getScheduledAt() { return scheduledAt; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getLocation() { return location; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
