package com.sporekart.modules.review.domain.event;

import java.time.OffsetDateTime;

public record ReviewApprovedEvent(
        String reviewId,
        String reviewReference,
        String productId,
        int rating,
        OffsetDateTime occurredAt
) {
    public static ReviewApprovedEvent create(String reviewId, String reviewReference, String productId, int rating) {
        return new ReviewApprovedEvent(reviewId, reviewReference, productId, rating, OffsetDateTime.now());
    }
}
