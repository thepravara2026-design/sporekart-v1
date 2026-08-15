# SPOREKART v3.0 — REFUND CALCULATION & DISCOUNT ALLOCATION

---

## 1. Refund Calculation Formula

`Refundable Amount = ∑ (acceptedQuantity × unitPrice) - ProportionalDiscount + TaxAdjustment`

Rules:
- Money representation uses `BigDecimal` with `2` decimal scale and `HALF_UP` rounding mode.
- Cumulative refunds for an order cannot exceed captured order total.
- Partial returns only refund accepted item line totals.
