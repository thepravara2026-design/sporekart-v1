package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.BatchSchedule;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.TrainingBatch;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "training_batches")
public class TrainingBatchEntity {

    @Id
    private String id;

    @Column(name = "program_id", nullable = false)
    private String programId;

    @Column(name = "batch_code", nullable = false, unique = true)
    private String batchCode;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "total_capacity", nullable = false)
    private int totalCapacity;

    @Column(name = "occupied_seats", nullable = false)
    private int occupiedSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BatchScheduleEntity> schedules = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TrainingBatchEntity() {}

    public TrainingBatchEntity(String id, String programId, String batchCode, Instant startDate, Instant endDate, int totalCapacity, int occupiedSeats, BatchStatus status, List<BatchScheduleEntity> schedules, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.programId = programId;
        this.batchCode = batchCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCapacity = totalCapacity;
        this.occupiedSeats = occupiedSeats;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        if (schedules != null) {
            this.schedules = schedules;
            this.schedules.forEach(s -> s.setBatch(this));
        }
    }

    public static TrainingBatchEntity fromDomain(TrainingBatch domain) {
        TrainingBatchEntity entity = new TrainingBatchEntity(
                domain.getId(),
                domain.getProgramId(),
                domain.getBatchCode(),
                domain.getStartDate(),
                domain.getEndDate(),
                domain.getCapacity().getTotalCapacity(),
                domain.getCapacity().getOccupiedSeats(),
                domain.getStatus(),
                null,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
        List<BatchScheduleEntity> scheduleEntities = domain.getSchedules().stream()
                .map(s -> BatchScheduleEntity.fromDomain(s, entity))
                .collect(Collectors.toList());
        entity.setSchedules(scheduleEntities);
        return entity;
    }

    public TrainingBatch toDomain() {
        Capacity capacity = new Capacity(totalCapacity, occupiedSeats);
        List<BatchSchedule> domainSchedules = schedules.stream()
                .map(BatchScheduleEntity::toDomain)
                .collect(Collectors.toList());
        return new TrainingBatch(id, programId, batchCode, startDate, endDate, capacity, status, domainSchedules, createdAt, updatedAt);
    }

    // Getters and setters
    public String getId() { return id; }
    public String getProgramId() { return programId; }
    public String getBatchCode() { return batchCode; }
    public Instant getStartDate() { return startDate; }
    public Instant getEndDate() { return endDate; }
    public int getTotalCapacity() { return totalCapacity; }
    public int getOccupiedSeats() { return occupiedSeats; }
    public BatchStatus getStatus() { return status; }
    public List<BatchScheduleEntity> getSchedules() { return schedules; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setSchedules(List<BatchScheduleEntity> schedules) {
        this.schedules = schedules;
        if (schedules != null) {
            schedules.forEach(s -> s.setBatch(this));
        }
    }
}
