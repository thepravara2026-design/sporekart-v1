# SPOREKART v3.0 — SPRINT 4I IMPLEMENTATION DETAILS

---

### 1. Architectural Overview

Sprint 4I establishes a production-grade **Customer Reviews, Product Ratings, Quality Signals & Trust Governance domain** (`com.sporekart.modules.review`):
- **Product Review Aggregate (`ProductReview`)**: Manages post-purchase reviews (`REV-2026-XXXXXX`), 1-to-5 star ratings, sub-ratings (quality/value), evidence media attachments, and moderation status (`PENDING_MODERATION`, `APPROVED`, `REJECTED`, `FLAGGED`).
- **Verified Buyer Purchase Boundary**: Validates verified buyer status via `OrderApplicationService` verifying that the customer has a `DELIVERED` order containing the specific product. Unique constraint `CONSTRAINT uq_customer_order_item_review UNIQUE (customer_id, order_item_id)` prevents duplicate review submissions.
- **Automated Moderation & Spam Filter (`SpamModerationFilter`)**: Clean reviews automatically pass to `APPROVED`, while flagged ones land in `PENDING_MODERATION` or `FLAGGED` for admin moderation review.
- **Aggregate Rating Score Projector (`ProductRatingSummary`)**: Calculates `averageRating` (`BigDecimal` scale 2, `HALF_UP` rounding), total review counts, and 1-star to 5-star distribution histograms for approved reviews.
- **Merchant Replies & Helpfulness Upvoting**: Supports customer helpfulness voting (`ReviewHelpfulnessVote`) and seller reply threads (`ReviewMerchantReply`).
- **Product Quality Alerts**: Emits `ProductQualityAlertEvent` when low ratings (rating <= 2) occur to notify support and catalog engineering.
- **Flyway Database Migration (`V15`)**: Creates `product_reviews`, `review_helpfulness_votes`, `review_merchant_replies`, and `product_rating_summaries` with database indexes and uniqueness constraints.
- **REST Endpoints**: Customer endpoints (`/api/v1/reviews/**`, `/api/v1/products/{productId}/reviews/**`) and Admin moderation endpoints (`/api/v1/admin/reviews/**`).
- **Frontend Service (`reviewApi.ts`)**: Axios service wrapping review submission, rating summary fetching, helpfulness voting, and admin moderation actions.

---

### 2. File Changes Summary

#### Backend (`com.sporekart.modules.review`)
- Domain Enums: `ReviewStatus`, `ModerationOutcome`.
- Entities: `ProductReview.java`, `ReviewHelpfulnessVote.java`, `ReviewMerchantReply.java`, `ProductRatingSummary.java`.
- Domain Services & Events: `ReviewStateMachine.java`, `SpamModerationFilter.java`, `RatingCalculatorService.java`, `ReviewSubmittedEvent`, `ReviewApprovedEvent`, `ReviewRejectedEvent`, `ProductQualityAlertEvent`.
- Repositories: `ProductReviewRepository`, `ReviewHelpfulnessVoteRepository`, `ReviewMerchantReplyRepository`, `ProductRatingSummaryRepository`.
- Application Layer: `ReviewApplicationService.java`, `CreateReviewRequestDto`, `ReviewHelpfulnessDto`, `MerchantReplyDto`, `ProductReviewDto`, `ProductRatingSummaryDto`, `AdminReviewFilterDto`.
- Controllers: `CustomerReviewController.java`, `AdminReviewController.java`.

#### Frontend (`frontend/src/`)
- `endpoints.ts`: Added customer and admin review endpoints.
- `reviewApi.ts`: Axios API service.

#### Database Migration
- `V15__reviews_ratings_domain.sql`: Schema definition.
