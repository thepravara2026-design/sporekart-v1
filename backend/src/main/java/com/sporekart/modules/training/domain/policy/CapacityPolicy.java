package com.sporekart.modules.training.domain.policy;

import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.Capacity;
import com.sporekart.modules.training.domain.exception.BatchFullException;
import com.sporekart.modules.training.domain.exception.CapacityExceededException;

public class CapacityPolicy {

    public Capacity allocateSeat(Capacity currentCapacity, BatchStatus batchStatus) {
        if (batchStatus == BatchStatus.FULL || currentCapacity.isFull()) {
            throw new BatchFullException("Cannot allocate seat: Batch is FULL");
        }
        if (batchStatus == BatchStatus.CANCELLED || batchStatus == BatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot allocate seat: Batch status is " + batchStatus);
        }
        return currentCapacity.allocateSeat();
    }

    public Capacity releaseSeat(Capacity currentCapacity) {
        return currentCapacity.releaseSeat();
    }

    public Capacity updateCapacity(Capacity currentCapacity, int newTotalCapacity) {
        if (newTotalCapacity <= 0) {
            throw new IllegalArgumentException("Total capacity must be greater than zero");
        }
        if (newTotalCapacity < currentCapacity.getOccupiedSeats()) {
            throw new CapacityExceededException(
                    "New capacity (" + newTotalCapacity + ") cannot be less than currently occupied seats (" + currentCapacity.getOccupiedSeats() + ")"
            );
        }
        return currentCapacity.withTotalCapacity(newTotalCapacity);
    }

    public BatchStatus evaluateBatchStatus(BatchStatus currentStatus, Capacity capacity) {
        if (currentStatus == BatchStatus.CANCELLED || currentStatus == BatchStatus.COMPLETED) {
            return currentStatus;
        }
        if (capacity.isFull()) {
            return BatchStatus.FULL;
        }
        if (currentStatus == BatchStatus.FULL && !capacity.isFull()) {
            return BatchStatus.ACTIVE;
        }
        return currentStatus;
    }
}
