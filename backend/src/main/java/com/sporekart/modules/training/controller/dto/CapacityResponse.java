package com.sporekart.modules.training.controller.dto;

import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.TrainingBatch;

public class CapacityResponse {

    private final String batchId;
    private final String batchCode;
    private final int totalCapacity;
    private final int occupiedSeats;
    private final int availableSeats;
    private final boolean full;
    private final BatchStatus status;

    public CapacityResponse(String batchId, String batchCode, int totalCapacity, int occupiedSeats, int availableSeats, boolean full, BatchStatus status) {
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.totalCapacity = totalCapacity;
        this.occupiedSeats = occupiedSeats;
        this.availableSeats = availableSeats;
        this.full = full;
        this.status = status;
    }

    public static CapacityResponse fromDomain(TrainingBatch batch) {
        return new CapacityResponse(
                batch.getId(),
                batch.getBatchCode(),
                batch.getCapacity().getTotalCapacity(),
                batch.getCapacity().getOccupiedSeats(),
                batch.getCapacity().getAvailableSeats(),
                batch.getCapacity().isFull() || batch.getStatus() == BatchStatus.FULL,
                batch.getStatus()
        );
    }

    public String getBatchId() { return batchId; }
    public String getBatchCode() { return batchCode; }
    public int getTotalCapacity() { return totalCapacity; }
    public int getOccupiedSeats() { return occupiedSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public boolean isFull() { return full; }
    public BatchStatus getStatus() { return status; }
}
