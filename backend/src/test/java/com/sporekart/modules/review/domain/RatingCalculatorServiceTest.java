package com.sporekart.modules.review.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RatingCalculatorServiceTest {

    private final RatingCalculatorService ratingCalculatorService = new RatingCalculatorService();

    @Test
    @DisplayName("Should correctly calculate average rating and star distribution histogram")
    void shouldCalculateRatingSummaryCorrectly() {
        ProductRatingSummary summary = ProductRatingSummary.createEmpty("prod-100");

        ProductReview r1 = ProductReview.create("REV-1", "prod-100", "ord-1", "item-1", "cust-1", 5, 5, 5, "Great", "Excellent product", true, ReviewStatus.APPROVED, null);
        ProductReview r2 = ProductReview.create("REV-2", "prod-100", "ord-2", "item-2", "cust-2", 4, 4, 4, "Good", "Very nice", true, ReviewStatus.APPROVED, null);
        ProductReview r3 = ProductReview.create("REV-3", "prod-100", "ord-3", "item-3", "cust-3", 1, 1, 1, "Poor", "Damaged product", false, ReviewStatus.APPROVED, null);

        ratingCalculatorService.updateRatingSummary(summary, List.of(r1, r2, r3));

        assertThat(summary.getTotalReviewsCount()).isEqualTo(3);
        assertThat(summary.getVerifiedPurchaseCount()).isEqualTo(2);
        assertThat(summary.getStar5Count()).isEqualTo(1);
        assertThat(summary.getStar4Count()).isEqualTo(1);
        assertThat(summary.getStar1Count()).isEqualTo(1);
        // (5 + 4 + 1) / 3 = 3.33
        assertThat(summary.getAverageRating()).isEqualTo(new BigDecimal("3.33"));
    }
}
