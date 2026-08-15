# SPOREKART v3.0 — Payment Lifecycle & State Machine Specification

## 1. Overview
Sprint 4B establishes a production-grade, authoritative Payment Lifecycle and State Machine for Sporekart v3.0. The Payment domain is distinct from the Order domain and maintains its own authoritative aggregate state, audit trail, and provider abstraction.

---

## 2. Payment State Machine Graph

```text
                 ┌──────────────┐
                 │   CREATED    │
                 └──────┬───────┘
                        │
                        ↓
                 ┌──────────────┐
                 │   PENDING    │
                 └──────┬───────┘
                        │
         ┌──────────────┼──────────────┐
         ↓              ↓              ↓
   ┌───────────┐  ┌───────────┐  ┌───────────┐
   │  SUCCESS  │  │  FAILED   │  │  EXPIRED  │
   └─────┬─────┘  └───────────┘  └───────────┘
         │
         ↓
   ┌───────────┐
   │ CANCELLED │
   └───────────┘
```

---

## 3. Transition Matrix & Permitted Invocations

| Current State | Target State | Trigger / Source | Permitted Actors | Side Effects / Order Integration |
| :--- | :--- | :--- | :--- | :--- |
| `CREATED` | `PENDING` | `createPayment` initiation | `CUSTOMER`, `SYSTEM` | Provider order ID generated & stored |
| `PENDING` | `SUCCESS` | HMAC verified payment / webhook | `PAYMENT_PROVIDER`, `WEBHOOK` | Invokes `OrderApplicationService.confirmOrderPayment` -> Order `CONFIRMED` |
| `PENDING` | `FAILED` | Verification failure / webhook failed | `PAYMENT_PROVIDER`, `WEBHOOK` | Releases stock reservation if payment failed |
| `PENDING` | `EXPIRED` | Payment window timeout | `SYSTEM` | Releases stock reservation |
| `PENDING` | `CANCELLED` | Customer cancellation | `CUSTOMER`, `ADMIN` | Releases stock reservation |

---

## 4. Terminal State Invariants
- Terminal states: `SUCCESS`, `FAILED`, `CANCELLED`, `EXPIRED`.
- `SUCCESS` is immutable. Once a payment reaches `SUCCESS`, subsequent attempt mark-failed or cancellation calls throw `PaymentInvalidStateException`. Duplicate `SUCCESS` verification calls execute idempotent replay returning existing `PaymentDto`.

---

## 5. Security & Verification Rules
1. **Cryptographic HMAC SHA-256 Signature Verification**: All client verification and webhook callbacks must validate signature against `RAZORPAY_KEY_SECRET` / `RAZORPAY_WEBHOOK_SECRET`.
2. **Server-Side Amount Integrity**: Payment amount and currency must match `order.getGrandTotal()` and `order.getCurrency()`. Mismatched amounts trigger immediate verification failure (`PAYMENT_AMOUNT_MISMATCH`).
3. **Webhook Idempotency**: Raw body and event ID are recorded in `payment_webhook_events`. Duplicate events return `DUPLICATE` processing status without re-executing state transitions.
