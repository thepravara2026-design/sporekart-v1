package com.sporekart.modules.training.domain.event;

import java.time.Instant;

public class TrainingDemandCreatedEvent {

    private final String demandId;
    private final String batchId;
    private final String traineeId;
    private final Instant requestedAt;

    public TrainingDemandCreatedEvent(String demandId, String batchId, String traineeId, Instant requestedAt) {
        this.demandId = demandId;
        this.batchId = batchId;
        this.traineeId = traineeId;
        this.requestedAt = requestedAt;
    }

    public String getDemandId() {
        return demandId;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getTraineeId() {
        return traineeId;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }
}
