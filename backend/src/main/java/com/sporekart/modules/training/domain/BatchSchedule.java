package com.sporekart.modules.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class BatchSchedule {

    private final String id;
    private final String batchId;
    private String title;
    private Instant scheduledAt;
    private int durationMinutes;
    private String location;
    private final Instant createdAt;
    private Instant updatedAt;

    public BatchSchedule(String id, String batchId, String title, Instant scheduledAt, int durationMinutes, String location, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null").trim();
        this.scheduledAt = Objects.requireNonNull(scheduledAt, "scheduledAt must not be null");
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be greater than zero");
        }
        this.durationMinutes = durationMinutes;
        this.location = location;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static BatchSchedule create(String batchId, String title, Instant scheduledAt, int durationMinutes, String location) {
        return new BatchSchedule(null, batchId, title, scheduledAt, durationMinutes, location, Instant.now(), Instant.now());
    }

    public void updateSchedule(String title, Instant scheduledAt, int durationMinutes, String location) {
        if (title != null && !title.isBlank()) {
            this.title = title.trim();
        }
        if (scheduledAt != null) {
            this.scheduledAt = scheduledAt;
        }
        if (durationMinutes > 0) {
            this.durationMinutes = durationMinutes;
        }
        this.location = location;
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getBatchId() { return batchId; }
    public String getTitle() { return title; }
    public Instant getScheduledAt() { return scheduledAt; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getLocation() { return location; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchSchedule schedule = (BatchSchedule) o;
        return Objects.equals(id, schedule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
