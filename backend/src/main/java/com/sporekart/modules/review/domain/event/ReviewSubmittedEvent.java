package com.sporekart.modules.review.domain.event;

import java.time.OffsetDateTime;

public record ReviewSubmittedEvent(
        String reviewId,
        String reviewReference,
        String productId,
        String customerId,
        int rating,
        OffsetDateTime occurredAt
) {
    public static ReviewSubmittedEvent create(String reviewId, String reviewReference, String productId, String customerId, int rating) {
        return new ReviewSubmittedEvent(reviewId, reviewReference, productId, customerId, rating, OffsetDateTime.now());
    }
}
