package com.sporekart.modules.training.domain.exception;

public class InsufficientCapacityException extends RuntimeException {
    public InsufficientCapacityException(String message) {
        super(message);
    }
}
