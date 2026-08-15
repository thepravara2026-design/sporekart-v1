package com.sporekart.modules.review.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "review_merchant_replies")
public class ReviewMerchantReply {

    @Id
    private String id;

    @Column(name = "review_id", nullable = false, length = 36)
    private String reviewId;

    @Column(name = "author_id", nullable = false, length = 64)
    private String authorId;

    @Column(name = "reply_text", nullable = false, columnDefinition = "TEXT")
    private String replyText;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ReviewMerchantReply() {}

    public static ReviewMerchantReply create(String reviewId, String authorId, String replyText) {
        ReviewMerchantReply reply = new ReviewMerchantReply();
        reply.id = UUID.randomUUID().toString();
        reply.reviewId = reviewId;
        reply.authorId = authorId;
        reply.replyText = replyText;
        reply.createdAt = OffsetDateTime.now();
        return reply;
    }

    public String getId() { return id; }
    public String getReviewId() { return reviewId; }
    public String getAuthorId() { return authorId; }
    public String getReplyText() { return replyText; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
