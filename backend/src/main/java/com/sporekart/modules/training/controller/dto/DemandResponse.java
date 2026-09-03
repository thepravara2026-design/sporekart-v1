package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import java.time.Instant;

public class DemandResponse {

    private String id;
    private String batchId;
    private String traineeId;
    private DemandStatus status;
    private Instant requestedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public DemandResponse() {
    }

    public DemandResponse(String id, String batchId, String traineeId, DemandStatus status, Instant requestedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.batchId = batchId;
        this.traineeId = traineeId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DemandResponse fromDomain(TrainingDemandRequest domain) {
        return new DemandResponse(
                domain.getId(),
                domain.getBatchId(),
                domain.getTraineeId(),
                domain.getStatus(),
                domain.getRequestedAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
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
}
