# SPOREKART v3.0 — REVIEWS & RATINGS FAILURE RECOVERY MATRIX

---

## Failure Recovery Specifications

| Failure Event | Detection Mechanism | System Behavior | Recovery / Admin Action |
| :--- | :--- | :--- | :--- |
| **Duplicate Review Submission** | Database constraint (`uq_customer_order_item_review`) | Throws `DuplicateReviewException` (HTTP 409). | Reject duplicate submission; protect review integrity. |
| **Unverified Buyer Attempt** | Order verification check | Review marked `isVerifiedPurchase = false` or rejected. | Only verified purchases get verified badge. |
| **Spam / Abuse Detection** | `SpamModerationFilter` evaluation | Review placed in `FLAGGED` / `PENDING_MODERATION`. | Admin moderation queue review. |
| **Concurrent Recalculation Conflict** | Optimistic locking on `ProductRatingSummary` | Transaction aborted & retried automatically. | Ensures score projector consistency. |
| **Negative Quality Spike** | Rating <= 2 evaluation | Publishes `ProductQualityAlertEvent`. | Support & catalog teams alerted. |
