package com.sporekart.modules.review.infrastructure.persistence;

import com.sporekart.modules.review.domain.ReviewMerchantReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewMerchantReplyRepository extends JpaRepository<ReviewMerchantReply, String> {

    List<ReviewMerchantReply> findByReviewIdOrderByCreatedAtAsc(String reviewId);
}
