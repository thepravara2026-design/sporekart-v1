package com.sporekart.modules.notification.domain;

public enum NotificationStatus {
    CREATED,
    QUEUED,
    PROCESSING,
    SENT,
    DELIVERED,
    FAILED,
    RETRY_SCHEDULED,
    FAILED_PERMANENTLY;

    public boolean canTransitionTo(NotificationStatus target) {
        if (this == target) return true;
        return switch (this) {
            case CREATED -> target == QUEUED || target == PROCESSING || target == FAILED || target == FAILED_PERMANENTLY;
            case QUEUED -> target == PROCESSING || target == FAILED || target == FAILED_PERMANENTLY;
            case PROCESSING -> target == SENT || target == DELIVERED || target == FAILED || target == RETRY_SCHEDULED || target == FAILED_PERMANENTLY;
            case FAILED, RETRY_SCHEDULED -> target == PROCESSING || target == RETRY_SCHEDULED || target == FAILED_PERMANENTLY || target == FAILED;
            case SENT -> target == DELIVERED || target == FAILED;
            case DELIVERED, FAILED_PERMANENTLY -> false;
        };
    }
}
