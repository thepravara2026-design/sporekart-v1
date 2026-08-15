# SPOREKART v3.0 — CUSTOMER REVIEWS & RATINGS GOVERNANCE ARCHITECTURE

---

## 1. Subsystem Architecture & Projection Model

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

## 2. Core Domain Invariants

1. **Verified Buyer Boundary**: Reviews marked as `isVerifiedPurchase = true` require validation through `OrderApplicationService` verifying that the customer has a `DELIVERED` order for the product.
2. **One Review per Order Item**: Database constraint `CONSTRAINT uq_customer_order_item_review UNIQUE (customer_id, order_item_id)` prevents duplicate review submissions.
3. **Public Score Isolation**: Only `APPROVED` reviews contribute to `ProductRatingSummary` calculations (`averageRating`, 1-to-5 star distribution histogram).
4. **Idempotent Score Recalculation**: Ratings recalculate using `BigDecimal` with scale 2 and `HALF_UP` rounding.
