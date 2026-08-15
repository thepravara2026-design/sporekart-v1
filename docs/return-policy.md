# SPOREKART v3.0 — RETURN ELIGIBILITY POLICY SPECIFICATION

---

## 1. Centralized Policy Evaluation (`ReturnEligibilityService`)

Evaluation Factors:
1. **Order Delivery Status**: Order must be in `DELIVERED` status.
2. **Return Window**: Default 10-day return window from `deliveredAt` timestamp.
3. **Quantity Ceiling**: `returnableQuantity = item.quantity - previouslyReturnedQuantity - pendingReturnQuantity`.
4. **Item Returnability**: Non-returnable categories or non-returnable flags reject item returnability.
