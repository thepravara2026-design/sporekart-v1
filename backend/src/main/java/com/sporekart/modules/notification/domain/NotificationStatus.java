package com.sporekart.modules.notification.domain;

public enum NotificationStatus {
    CREATED,
    QUEUED,
    PROCESSING,
    SENT,
    DELIVERED,
    FAILED,
    RETRY_SCHEDULED,
    FAILED_PERMANENTLY,
    CANCELLED,
    SUPPRESSED;

    public boolean canTransitionTo(NotificationStatus target) {
        if (this == target) return true;
        return switch (this) {
            case CREATED -> target == QUEUED || target == PROCESSING || target == SENT || target == DELIVERED || target == FAILED || target == RETRY_SCHEDULED || target == FAILED_PERMANENTLY || target == CANCELLED || target == SUPPRESSED;
            case QUEUED -> target == PROCESSING || target == SENT || target == DELIVERED || target == FAILED || target == RETRY_SCHEDULED || target == FAILED_PERMANENTLY || target == CANCELLED || target == SUPPRESSED;
            case PROCESSING -> target == SENT || target == DELIVERED || target == FAILED || target == RETRY_SCHEDULED || target == FAILED_PERMANENTLY || target == CANCELLED;
            case FAILED, RETRY_SCHEDULED -> target == PROCESSING || target == SENT || target == DELIVERED || target == RETRY_SCHEDULED || target == FAILED_PERMANENTLY || target == FAILED || target == CANCELLED;
            case SENT -> target == DELIVERED || target == FAILED;
            case DELIVERED, FAILED_PERMANENTLY, CANCELLED, SUPPRESSED -> false;
        };
    }
}
