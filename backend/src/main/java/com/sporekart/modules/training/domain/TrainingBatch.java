package com.sporekart.modules.training.domain;

import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.CapacityExceededException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TrainingBatch {

    private final String id;
    private final String programId;
    private String batchCode;
    private Instant startDate;
    private Instant endDate;
    private Capacity capacity;
    private BatchStatus status;
    private final List<BatchSchedule> schedules;
    private final Instant createdAt;
    private Instant updatedAt;

    public TrainingBatch(String id, String programId, String batchCode, Instant startDate, Instant endDate, Capacity capacity, BatchStatus status, List<BatchSchedule> schedules, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.programId = Objects.requireNonNull(programId, "programId must not be null");
        this.batchCode = Objects.requireNonNull(batchCode, "batchCode must not be null").trim();
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = Objects.requireNonNull(endDate, "endDate must not be null");
        this.capacity = Objects.requireNonNull(capacity, "capacity must not be null");
        this.status = status != null ? status : BatchStatus.PLANNED;
        this.schedules = schedules != null ? new ArrayList<>(schedules) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static TrainingBatch create(String programId, String batchCode, Instant startDate, Instant endDate, int totalCapacity) {
        return new TrainingBatch(
                null,
                programId,
                batchCode,
                startDate,
                endDate,
                Capacity.of(totalCapacity),
                BatchStatus.PLANNED,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );
    }

    public void allocateSeat() {
        if (status == BatchStatus.FULL || capacity.isFull()) {
            this.status = BatchStatus.FULL;
            throw new BatchFullException("Cannot allocate seat: Batch " + batchCode + " is FULL");
        }
        if (status == BatchStatus.CANCELLED || status == BatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot allocate seat: Batch is " + status);
        }
        this.capacity = this.capacity.allocateSeat();
        if (this.capacity.isFull()) {
            this.status = BatchStatus.FULL;
        }
        this.updatedAt = Instant.now();
    }

    public void releaseSeat() {
        this.capacity = this.capacity.releaseSeat();
        if (this.status == BatchStatus.FULL && !this.capacity.isFull()) {
            this.status = BatchStatus.ACTIVE;
        }
        this.updatedAt = Instant.now();
    }

    public void updateTotalCapacity(int newTotalCapacity) {
        this.capacity = this.capacity.withTotalCapacity(newTotalCapacity);
        if (this.capacity.isFull()) {
            this.status = BatchStatus.FULL;
        } else if (this.status == BatchStatus.FULL) {
            this.status = BatchStatus.ACTIVE;
        }
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (this.status == BatchStatus.CANCELLED || this.status == BatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot activate batch in status: " + this.status);
        }
        this.status = this.capacity.isFull() ? BatchStatus.FULL : BatchStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = BatchStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.status = BatchStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void addSchedule(BatchSchedule schedule) {
        Objects.requireNonNull(schedule, "schedule must not be null");
        this.schedules.add(schedule);
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getProgramId() { return programId; }
    public String getBatchCode() { return batchCode; }
    public Instant getStartDate() { return startDate; }
    public Instant getEndDate() { return endDate; }
    public Capacity getCapacity() { return capacity; }
    public BatchStatus getStatus() { return status; }
    public List<BatchSchedule> getSchedules() { return Collections.unmodifiableList(schedules); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingBatch that = (TrainingBatch) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
