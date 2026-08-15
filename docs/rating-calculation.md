# SPOREKART v3.0 — RATING CALCULATION & SCORE PROJECTOR SPECIFICATION

---

## 1. Score Calculation Formula

$$\text{Average Rating} = \frac{\sum_{i=1}^{5} (i \times \text{star}_i\text{Count})}{\text{Total Approved Reviews Count}}$$

Rules:
- Calculated with `BigDecimal` using scale `2` and `RoundingMode.HALF_UP`.
- Evaluates only reviews with status `APPROVED`.
- Histogram counts `star1Count`, `star2Count`, `star3Count`, `star4Count`, `star5Count` and `verifiedPurchaseCount`.
