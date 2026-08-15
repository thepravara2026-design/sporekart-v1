package com.sporekart.modules.review.application.dto;

import com.sporekart.modules.review.domain.ReviewMerchantReply;

import java.time.OffsetDateTime;

public record MerchantReplyDto(
        String id,
        String reviewId,
        String authorId,
        String replyText,
        OffsetDateTime createdAt
) {
    public static MerchantReplyDto fromDomain(ReviewMerchantReply reply) {
        return new MerchantReplyDto(
                reply.getId(),
                reply.getReviewId(),
                reply.getAuthorId(),
                reply.getReplyText(),
                reply.getCreatedAt()
        );
    }
}
