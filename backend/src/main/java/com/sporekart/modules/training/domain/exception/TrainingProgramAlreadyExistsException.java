package com.sporekart.modules.training.domain.exception;

public class TrainingProgramAlreadyExistsException extends RuntimeException {
    public TrainingProgramAlreadyExistsException(String message) {
        super(message);
    }
}
