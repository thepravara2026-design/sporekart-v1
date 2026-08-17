package com.sporekart.modules.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TrainingDemandRequest {

    private final String id;
    private final String batchId;
    private final String traineeId;
    private DemandStatus status;
    private final Instant requestedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final String createdBy;
    private String updatedBy;

    public TrainingDemandRequest(
            String id,
            String batchId,
            String traineeId,
            DemandStatus status,
            Instant requestedAt,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.batchId = Objects.requireNonNull(batchId, "batchId must not be null");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId must not be null");
        this.status = status != null ? status : DemandStatus.ACTIVE;
        Instant now = Instant.now();
        this.requestedAt = requestedAt != null ? requestedAt : now;
        this.createdAt = createdAt != null ? createdAt : now;
        this.updatedAt = updatedAt != null ? updatedAt : now;
        this.createdBy = createdBy != null ? createdBy : "SYSTEM";
        this.updatedBy = updatedBy != null ? updatedBy : "SYSTEM";
    }

    public static TrainingDemandRequest create(String batchId, String traineeId, String actor) {
        Instant now = Instant.now();
        return new TrainingDemandRequest(
                UUID.randomUUID().toString(),
                batchId,
                traineeId,
                DemandStatus.ACTIVE,
                now,
                now,
                now,
                actor,
                actor
        );
    }

    public void resolve(String actor) {
        this.status = DemandStatus.RESOLVED;
        this.updatedAt = Instant.now();
        this.updatedBy = actor != null ? actor : "SYSTEM";
    }

    public void withdraw(String actor) {
        this.status = DemandStatus.WITHDRAWN;
        this.updatedAt = Instant.now();
        this.updatedBy = actor != null ? actor : "SYSTEM";
    }

    public void expire(String actor) {
        this.status = DemandStatus.EXPIRED;
        this.updatedAt = Instant.now();
        this.updatedBy = actor != null ? actor : "SYSTEM";
    }

    public boolean isActive() {
        return this.status == DemandStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getTraineeId() {
        return traineeId;
    }

    public DemandStatus getStatus() {
        return status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
