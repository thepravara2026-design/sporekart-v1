# SPOREKART v3.0 — SPRINT 4I RECONNAISSANCE & ARCHITECTURE ANALYSIS

---

## 1. Executive Summary

Sprint 4I establishes a production-grade **Customer Reviews, Product Ratings, Quality Signals & Trust Governance domain** (`com.sporekart.modules.review`).
This document details our reconnaissance of the Sporekart codebase across Sprint 0 through Sprint 4H, outlining the canonical product review aggregate (`ProductReview`), verified purchase validation boundary (`OrderApplicationService`), moderation state machine (`ReviewStateMachine`), aggregate score calculation projector (`ProductRatingSummary`), merchant replies, helpfulness upvoting, Flyway database schema plan (`V15`), security authorization rules, and REST endpoints.

---

## 2. Codebase Reconnaissance & Subsystem Audits

### 2.1 Domain Boundaries & Interplays
- **Reviews Domain (`com.sporekart.modules.review`)**: Coordinates customer product reviews (`REV-2026-000001`), ratings (1-5 stars), sub-ratings (quality/value), helpfulness votes, moderation status (`PENDING_MODERATION`, `APPROVED`, `REJECTED`, `FLAGGED`), merchant reply threads, and product rating summary projections.
- **Order Domain (`com.sporekart.modules.order`)**: Validates verified buyer status (`order.customerId == authenticatedCustomer`, `order.status == DELIVERED`).
- **Catalog Domain (`com.sporekart.modules.catalog`)**: Public product listings read cached rating summaries (`averageRating`, `totalReviewsCount`, star distribution histogram).
- **Support & Quality Domain (`com.sporekart.modules.support`)**: Listens to `ProductQualityAlertEvent` when low ratings occur to trigger proactive quality investigation.

### 2.2 Database Schema Plan (`V15__reviews_ratings_domain.sql`)
Sprint 4I introduces four database tables:
1. `product_reviews`: `id`, `review_reference`, `product_id`, `order_id`, `order_item_id`, `customer_id`, `rating`, `quality_rating`, `value_rating`, `title`, `comment`, `is_verified_purchase`, `status`, `rejection_reason`, `helpful_count`, `unhelpful_count`, `media_urls`, `created_at`, `updated_at`, `version`, `CONSTRAINT uq_customer_order_item_review UNIQUE`.
2. `review_helpfulness_votes`: `id`, `review_id`, `customer_id`, `is_helpful`, `voted_at`, `CONSTRAINT uq_review_customer_vote UNIQUE`.
3. `review_merchant_replies`: `id`, `review_id`, `author_id`, `reply_text`, `created_at`.
4. `product_rating_summaries`: `product_id`, `average_rating`, `total_reviews_count`, `star1_count`, `star2_count`, `star3_count`, `star4_count`, `star5_count`, `verified_purchase_count`, `updated_at`.

---

## 3. Reviews & Trust Governance Architecture Diagram

```
                            CUSTOMER
                               |
                               v
                  ORDER DOMAIN (Verify Purchase)
                               |
                               v
                        REVIEWS DOMAIN
                 (ProductReview Aggregate Root)
                               |
      +------------------------+------------------------+
      |                        |                        |
      v                        v                        v
 RATING SUMMARY         MODERATION ENGINE        QUALITY SIGNALS
(Cached Histogram &    (Spam Filter & Admin      (Support Alerts on
  Average Rating)       Approval / Rejection)       Low Ratings)
```

---

## 4. Safety & Security Invariants

1. **Verified Buyer Protection**: Verified purchase badge is awarded only when an order is verified delivered for that item.
2. **One Review per Order Item**: Database constraint `CONSTRAINT uq_customer_order_item_review UNIQUE (customer_id, order_item_id)` prevents duplicate review spam.
3. **Idempotent Recalculation**: `ProductRatingSummary` recalculates transactionally using `BigDecimal` (`2` decimal scale, `HALF_UP` rounding).
