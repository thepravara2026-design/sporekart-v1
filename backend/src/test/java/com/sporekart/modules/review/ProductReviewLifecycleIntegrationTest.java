package com.sporekart.modules.review;

import com.sporekart.modules.review.application.ReviewApplicationService;
import com.sporekart.modules.review.application.dto.*;
import com.sporekart.modules.review.domain.ReviewStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductReviewLifecycleIntegrationTest {

    @Autowired
    private ReviewApplicationService reviewApplicationService;

    @Test
    @DisplayName("Should execute complete product review lifecycle: Submit -> Auto-Approve -> Rating Summary -> Upvote -> Merchant Reply")
    void shouldExecuteFullReviewLifecycle() {
        // 1. Customer submits clean review
        CreateReviewRequestDto createDto = new CreateReviewRequestDto(
                "prod-uuid-101",
                "order-uuid-201",
                "item-uuid-301",
                5,
                5,
                5,
                "Outstanding Quality",
                "Exceeded all expectations. Highly recommended!",
                null
        );

        ProductReviewDto review = reviewApplicationService.submitCustomerReview(createDto, "cust-101");
        assertThat(review).isNotNull();
        assertThat(review.reviewReference()).startsWith("REV-");
        assertThat(review.status()).isEqualTo(ReviewStatus.APPROVED);

        // 2. Fetch updated rating summary for product
        ProductRatingSummaryDto summary = reviewApplicationService.getProductRatingSummary("prod-uuid-101");
        assertThat(summary.totalReviewsCount()).isEqualTo(1);
        assertThat(summary.averageRating()).isNotNull();

        // 3. Customer votes helpfulness
        ProductReviewDto voted = reviewApplicationService.voteHelpfulness(review.reviewReference(), true, "cust-102");
        assertThat(voted.helpfulCount()).isEqualTo(1);

        // 4. Admin adds merchant reply
        MerchantReplyDto reply = reviewApplicationService.addMerchantReply(review.reviewReference(), "Thank you for your fantastic feedback!", "admin-seller-1");
        assertThat(reply).isNotNull();
        assertThat(reply.replyText()).contains("fantastic feedback");

        // 5. Verify product reviews list contains review with merchant reply
        List<ProductReviewDto> reviews = reviewApplicationService.listProductReviews("prod-uuid-101");
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).merchantReplies()).hasSize(1);
    }
}
