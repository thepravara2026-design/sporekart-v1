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

    /**
     * Explicit payment state machine transition guard — mirrors EnrollmentStatus.
     * Every illegal transition is rejected. Terminal states cannot transition.
     * Transitions are strictly forward-only.
     */
    public boolean isValidTransitionTo(TrainingPaymentStatus target) {
        if (this == target) {
            return true; // Idempotent no-op
        }
        if (isTerminal()) {
            return false; // Terminal states cannot transition
        }
        return switch (this) {
            case PENDING            -> target == VERIFIED || target == FAILED || target == EXPIRED;
            case VERIFIED           -> target == ENROLLMENT_CONFIRMED || target == ENROLLMENT_PENDING || target == FAILED;
            case ENROLLMENT_PENDING -> target == ENROLLMENT_CONFIRMED || target == FAILED;
            default -> false;
        };
    }
}
