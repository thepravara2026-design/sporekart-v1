package com.sporekart.modules.review.infrastructure.persistence;

import com.sporekart.modules.review.domain.ReviewHelpfulnessVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewHelpfulnessVoteRepository extends JpaRepository<ReviewHelpfulnessVote, String> {

    Optional<ReviewHelpfulnessVote> findByReviewIdAndCustomerId(String reviewId, String customerId);
}
