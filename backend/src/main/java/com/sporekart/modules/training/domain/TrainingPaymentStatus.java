package com.sporekart.modules.training.domain;

public enum TrainingPaymentStatus {
    PENDING,
    VERIFIED,
    ENROLLMENT_CONFIRMED,
    ENROLLMENT_PENDING,
    FAILED,
    EXPIRED;

    public boolean isTerminal() {
        return this == ENROLLMENT_CONFIRMED || this == FAILED || this == EXPIRED;
    }
}
