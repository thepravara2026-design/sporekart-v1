package com.sporekart.modules.training.domain.exception;

public class BatchReassignmentNotAllowedException extends RuntimeException {
    public BatchReassignmentNotAllowedException(String message) {
        super(message);
    }
}
