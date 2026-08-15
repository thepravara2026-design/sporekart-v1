# SPOREKART v3.0 — QA Release Checklist

**Target Audience**: QA Engineers & Release Testers

---

## 1. QA Verification Checklist

- [x] **Catalog & Search**: Verify paginated listing, product details, rating summary.
- [x] **Cart & Checkout**: Verify adding items, price calculation, discount voucher application.
- [x] **Orders & Payments**: Verify order creation, Razorpay payment verification, state transition.
- [x] **Fulfillment & Logistics**: Verify shipment creation, AWB assignment, tracking updates.
- [x] **Returns & Refunds**: Verify eligibility checking (14-day window), inspection logging, idempotent refund issuance.
- [x] **Support & Replacements**: Verify ticket creation, SLA calculation, message updates, replacement request approval.
- [x] **Reviews & Moderation**: Verify review submission, moderation approval/rejection, helpfulness voting.
- [x] **Security & IDOR**: Verify unauthenticated requests return 401 and cross-customer requests return 400/403.