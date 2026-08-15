package com.sporekart.modules.review.domain.event;

import java.time.OffsetDateTime;

public record ProductQualityAlertEvent(
        String productId,
        String reviewId,
        int rating,
        String comment,
        OffsetDateTime occurredAt
) {
    public static ProductQualityAlertEvent create(String productId, String reviewId, int rating, String comment) {
        return new ProductQualityAlertEvent(productId, reviewId, rating, comment, OffsetDateTime.now());
    }
}
