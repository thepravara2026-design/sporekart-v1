package com.sporekart.modules.review.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_reviews", uniqueConstraints = {
    @UniqueConstraint(name = "uq_customer_order_item_review", columnNames = {"customer_id", "order_item_id"})
})
public class ProductReview {

    @Id
    private String id;

    @Column(name = "review_reference", nullable = false, unique = true, length = 32)
    private String reviewReference;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "order_item_id", nullable = false, length = 36)
    private String orderItemId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "quality_rating")
    private Integer qualityRating;

    @Column(name = "value_rating")
    private Integer valueRating;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_verified_purchase", nullable = false)
    private boolean isVerifiedPurchase;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReviewStatus status;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "helpful_count", nullable = false)
    private int helpfulCount;

    @Column(name = "unhelpful_count", nullable = false)
    private int unhelpfulCount;

    @Column(name = "media_urls", columnDefinition = "TEXT")
    private String mediaUrls;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Long version;

    protected ProductReview() {}

    public static ProductReview create(
            String reviewReference,
            String productId,
            String orderId,
            String orderItemId,
            String customerId,
            int rating,
            Integer qualityRating,
            Integer valueRating,
            String title,
            String comment,
            boolean isVerifiedPurchase,
            ReviewStatus initialStatus,
            String mediaUrls
    ) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
        }
        OffsetDateTime now = OffsetDateTime.now();
        ProductReview review = new ProductReview();
        review.id = UUID.randomUUID().toString();
        review.reviewReference = reviewReference;
        review.productId = productId;
        review.orderId = orderId;
        review.orderItemId = orderItemId;
        review.customerId = customerId;
        review.rating = rating;
        review.qualityRating = qualityRating;
        review.valueRating = valueRating;
        review.title = title;
        review.comment = comment;
        review.isVerifiedPurchase = isVerifiedPurchase;
        review.status = initialStatus != null ? initialStatus : ReviewStatus.PENDING_MODERATION;
        review.helpfulCount = 0;
        review.unhelpfulCount = 0;
        review.mediaUrls = mediaUrls;
        review.createdAt = now;
        review.updatedAt = now;
        return review;
    }

    public void approve() {
        ReviewStateMachine.validateTransition(this.status, ReviewStatus.APPROVED);
        this.status = ReviewStatus.APPROVED;
        this.rejectionReason = null;
        this.updatedAt = OffsetDateTime.now();
    }

    public void reject(String reason) {
        ReviewStateMachine.validateTransition(this.status, ReviewStatus.REJECTED);
        this.status = ReviewStatus.REJECTED;
        this.rejectionReason = reason;
        this.updatedAt = OffsetDateTime.now();
    }

    public void flag() {
        ReviewStateMachine.validateTransition(this.status, ReviewStatus.FLAGGED);
        this.status = ReviewStatus.FLAGGED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void applyHelpfulnessVote(boolean isHelpful) {
        if (isHelpful) {
            this.helpfulCount++;
        } else {
            this.unhelpfulCount++;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getReviewReference() { return reviewReference; }
    public String getProductId() { return productId; }
    public String getOrderId() { return orderId; }
    public String getOrderItemId() { return orderItemId; }
    public String getCustomerId() { return customerId; }
    public int getRating() { return rating; }
    public Integer getQualityRating() { return qualityRating; }
    public Integer getValueRating() { return valueRating; }
    public String getTitle() { return title; }
    public String getComment() { return comment; }
    public boolean isVerifiedPurchase() { return isVerifiedPurchase; }
    public ReviewStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public int getHelpfulCount() { return helpfulCount; }
    public int getUnhelpfulCount() { return unhelpfulCount; }
    public String getMediaUrls() { return mediaUrls; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
