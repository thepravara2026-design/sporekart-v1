package com.sporekart.modules.review.domain.event;

import java.time.OffsetDateTime;

public record ReviewRejectedEvent(
        String reviewId,
        String reviewReference,
        String productId,
        String reason,
        OffsetDateTime occurredAt
) {
    public static ReviewRejectedEvent create(String reviewId, String reviewReference, String productId, String reason) {
        return new ReviewRejectedEvent(reviewId, reviewReference, productId, reason, OffsetDateTime.now());
    }
}
