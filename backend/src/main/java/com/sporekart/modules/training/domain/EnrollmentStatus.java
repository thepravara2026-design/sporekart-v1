package com.sporekart.modules.training.domain;

public enum EnrollmentStatus {
    PENDING,
    PAYMENT_PENDING,
    PAYMENT_VERIFIED,
    CONFIRMED,
    ACTIVE,
    COMPLETED,
    WAITLISTED,
    REJECTED,
    PAYMENT_FAILED,
    CANCELLED,
    RESCHEDULED;

    public boolean isCapacityConsuming() {
        return this == CONFIRMED || this == ACTIVE || this == COMPLETED;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED || this == PAYMENT_FAILED || this == CANCELLED || this == RESCHEDULED;
    }

    public boolean isValidTransitionTo(EnrollmentStatus target) {
        if (this == target) {
            return true; // No-op transition
        }
        if (isTerminal()) {
            return false; // Terminal states cannot transition
        }
        return switch (this) {
            case PENDING -> target == PAYMENT_PENDING || target == CONFIRMED || target == REJECTED || target == CANCELLED;
            case PAYMENT_PENDING -> target == PAYMENT_VERIFIED || target == PAYMENT_FAILED || target == CANCELLED;
            case PAYMENT_VERIFIED -> target == CONFIRMED || target == PAYMENT_FAILED || target == CANCELLED;
            case CONFIRMED -> target == ACTIVE || target == CANCELLED || target == RESCHEDULED;
            case ACTIVE -> target == COMPLETED || target == CANCELLED || target == RESCHEDULED;
            case WAITLISTED -> target == PENDING || target == CONFIRMED || target == CANCELLED;
            default -> false;
        };
    }
}
