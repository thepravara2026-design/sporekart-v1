package com.sporekart.modules.review.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "review_helpfulness_votes", uniqueConstraints = {
    @UniqueConstraint(name = "uq_review_customer_vote", columnNames = {"review_id", "customer_id"})
})
public class ReviewHelpfulnessVote {

    @Id
    private String id;

    @Column(name = "review_id", nullable = false, length = 36)
    private String reviewId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "is_helpful", nullable = false)
    private boolean isHelpful;

    @Column(name = "voted_at", nullable = false)
    private OffsetDateTime votedAt;

    protected ReviewHelpfulnessVote() {}

    public static ReviewHelpfulnessVote create(String reviewId, String customerId, boolean isHelpful) {
        ReviewHelpfulnessVote vote = new ReviewHelpfulnessVote();
        vote.id = UUID.randomUUID().toString();
        vote.reviewId = reviewId;
        vote.customerId = customerId;
        vote.isHelpful = isHelpful;
        vote.votedAt = OffsetDateTime.now();
        return vote;
    }

    public String getId() { return id; }
    public String getReviewId() { return reviewId; }
    public String getCustomerId() { return customerId; }
    public boolean isHelpful() { return isHelpful; }
    public OffsetDateTime getVotedAt() { return votedAt; }
}
