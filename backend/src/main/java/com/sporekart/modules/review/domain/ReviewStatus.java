package com.sporekart.modules.review.domain;

public enum ReviewStatus {
    PENDING_MODERATION,
    APPROVED,
    REJECTED,
    FLAGGED;

    public boolean isApproved() {
        return this == APPROVED;
    }
}
