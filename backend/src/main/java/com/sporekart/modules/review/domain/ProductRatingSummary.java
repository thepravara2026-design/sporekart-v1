package com.sporekart.modules.review.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "product_rating_summaries")
public class ProductRatingSummary {

    @Id
    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "total_reviews_count", nullable = false)
    private int totalReviewsCount;

    @Column(name = "star1_count", nullable = false)
    private int star1Count;

    @Column(name = "star2_count", nullable = false)
    private int star2Count;

    @Column(name = "star3_count", nullable = false)
    private int star3Count;

    @Column(name = "star4_count", nullable = false)
    private int star4Count;

    @Column(name = "star5_count", nullable = false)
    private int star5Count;

    @Column(name = "verified_purchase_count", nullable = false)
    private int verifiedPurchaseCount;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ProductRatingSummary() {}

    public static ProductRatingSummary createEmpty(String productId) {
        ProductRatingSummary summary = new ProductRatingSummary();
        summary.productId = productId;
        summary.averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        summary.totalReviewsCount = 0;
        summary.star1Count = 0;
        summary.star2Count = 0;
        summary.star3Count = 0;
        summary.star4Count = 0;
        summary.star5Count = 0;
        summary.verifiedPurchaseCount = 0;
        summary.updatedAt = OffsetDateTime.now();
        return summary;
    }

    public void recalculate(List<ProductReview> approvedReviews) {
        this.totalReviewsCount = approvedReviews.size();
        this.star1Count = 0;
        this.star2Count = 0;
        this.star3Count = 0;
        this.star4Count = 0;
        this.star5Count = 0;
        this.verifiedPurchaseCount = 0;

        if (approvedReviews.isEmpty()) {
            this.averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            this.updatedAt = OffsetDateTime.now();
            return;
        }

        long sumRating = 0;
        for (ProductReview r : approvedReviews) {
            sumRating += r.getRating();
            if (r.isVerifiedPurchase()) {
                this.verifiedPurchaseCount++;
            }
            switch (r.getRating()) {
                case 1 -> this.star1Count++;
                case 2 -> this.star2Count++;
                case 3 -> this.star3Count++;
                case 4 -> this.star4Count++;
                case 5 -> this.star5Count++;
            }
        }

        this.averageRating = BigDecimal.valueOf(sumRating)
                .divide(BigDecimal.valueOf(approvedReviews.size()), 2, RoundingMode.HALF_UP);
        this.updatedAt = OffsetDateTime.now();
    }

    public String getProductId() { return productId; }
    public BigDecimal getAverageRating() { return averageRating; }
    public int getTotalReviewsCount() { return totalReviewsCount; }
    public int getStar1Count() { return star1Count; }
    public int getStar2Count() { return star2Count; }
    public int getStar3Count() { return star3Count; }
    public int getStar4Count() { return star4Count; }
    public int getStar5Count() { return star5Count; }
    public int getVerifiedPurchaseCount() { return verifiedPurchaseCount; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
