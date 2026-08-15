package com.sporekart.modules.review.application.dto;

import com.sporekart.modules.review.domain.ProductReview;
import com.sporekart.modules.review.domain.ReviewStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record ProductReviewDto(
        String id,
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
        ReviewStatus status,
        String rejectionReason,
        int helpfulCount,
        int unhelpfulCount,
        String mediaUrls,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<MerchantReplyDto> merchantReplies
) {
    public static ProductReviewDto fromDomain(ProductReview review, List<MerchantReplyDto> replies) {
        return new ProductReviewDto(
                review.getId(),
                review.getReviewReference(),
                review.getProductId(),
                review.getOrderId(),
                review.getOrderItemId(),
                review.getCustomerId(),
                review.getRating(),
                review.getQualityRating(),
                review.getValueRating(),
                review.getTitle(),
                review.getComment(),
                review.isVerifiedPurchase(),
                review.getStatus(),
                review.getRejectionReason(),
                review.getHelpfulCount(),
                review.getUnhelpfulCount(),
                review.getMediaUrls(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                replies != null ? replies : List.of()
        );
    }
}
