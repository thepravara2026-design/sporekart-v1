package com.sporekart.modules.review.application.dto;

import com.sporekart.modules.review.domain.ProductRatingSummary;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductRatingSummaryDto(
        String productId,
        BigDecimal averageRating,
        int totalReviewsCount,
        int star1Count,
        int star2Count,
        int star3Count,
        int star4Count,
        int star5Count,
        int verifiedPurchaseCount,
        OffsetDateTime updatedAt
) {
    public static ProductRatingSummaryDto fromDomain(ProductRatingSummary summary) {
        return new ProductRatingSummaryDto(
                summary.getProductId(),
                summary.getAverageRating(),
                summary.getTotalReviewsCount(),
                summary.getStar1Count(),
                summary.getStar2Count(),
                summary.getStar3Count(),
                summary.getStar4Count(),
                summary.getStar5Count(),
                summary.getVerifiedPurchaseCount(),
                summary.getUpdatedAt()
        );
    }
}
