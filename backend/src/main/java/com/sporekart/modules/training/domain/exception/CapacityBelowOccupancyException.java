package com.sporekart.modules.training.domain.exception;

public class CapacityBelowOccupancyException extends RuntimeException {
    public CapacityBelowOccupancyException(String message) {
        super(message);
    }
}
