# SPOREKART v3.0 — PAC-07 Source of Truth & Payment Architecture

**Document ID:** `PAC-07-SOURCE-OF-TRUTH`  
**Sprint:** `PAC-07 — Mock Payment, Cancellation & Refund Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Authoritative Baseline  

---

## 1. Executive Summary

This document establishes the single source of truth for SPOREKART v3.0 Mock Payment, Cancellation, and Refund architecture, domain state machine transitions, monetary precision rules, idempotency boundaries, and outbox/notification events certified under **PAC-07 — Mock Payment, Cancellation & Refund Acceptance**.

---

## 2. Mock Payment, Cancellation & Refund Architecture

```
Customer / Trainee Frontend Checkout
        │
        ▼ Checkout & Payment Gateway Abstraction (`MockPaymentProvider`)
Server-Side Payable Amount Derivation
   ├── Commerce: Derived from Cart items & catalog prices (`order_total`)
   └── Training: Derived from `TrainingBatch.price` (`fee_amount`)
        │
        ▼ Mock Payment Execution (`PaymentApplicationService` / `TrainingPaymentApplicationService`)
   ├── SUCCESS  ──► Order created / Enrollment CONFIRMED + Inventory reserved / Seat decremented
   └── FAILURE  ──► Payment FAILED + Zero permanent stock/capacity consumption
        │
        ▼ Lifecycle & Cancellation Control (`OrderApplicationService` / `TrainingCancellationService`)
   ├── Customer Order Cancel (Eligible state check)  ──► Refund initiated + Stock released
   └── Trainee Training Cancel (Notice Window >= 2 days) ──► Refund initiated + Seat released
        │
        ▼ Mock Refund Engine (`PaymentRefundService` / `TrainingRefundProcessor`)
   ├── SUCCESS  ──► Refund status PROCESSED + Payment status REFUNDED + Outbox Event published
   └── FAILURE  ──► Refund status FAILED + No false refund success + Idempotent retry available
```

---

## 3. Authoritative Financial State Entities

| Entity / Domain Object | Table Name | Key Attributes | Financial / Business Responsibility |
|------------------------|------------|----------------|------------------------------------|
| `Payment` | `payments` | `id`, `order_id`, `amount`, `status`, `reference` | Commerce payment transaction |
| `TrainingPayment` | `training_payments` | `id`, `enrollment_id`, `amount`, `status`, `reference` | Training fee payment transaction |
| `Refund` | `refunds` | `id`, `payment_id`, `amount`, `status`, `reason` | Commerce refund transaction record |
| `TrainingRefund` | `training_refunds` | `id`, `training_payment_id`, `amount`, `status` | Training refund transaction record |

---

## 4. Key Financial Governance Safeguards

1. **Server-Authoritative Price & Amount Derivation:** Payable amounts for commerce checkouts and training enrollments are calculated strictly on the backend from authoritative database catalog prices and batch fees. Client-submitted price parameters in request DTOs are ignored.
2. **Double Payment & Double Refund Defense:** Idempotency checks on payment transaction keys and refund requests prevent duplicate financial transactions. Re-submitting an already processed payment or refund returns the existing transaction result without duplicating state mutations.
3. **State Machine Invariants:** Terminal payment/refund states (`SUCCESS`, `REFUNDED`, `FAILED`) cannot be overridden by unverified callbacks or client claims. Order/Enrollment states transition atomically with payment verification.

---

## 5. Governance Alignment Verdict

**VERDICT: CERTIFIED PASS** — The Mock Payment, Cancellation, and Refund architecture strictly satisfies all financial integrity, state machine, and data protection rules required for PAC-07.
