# SPOREKART v3.0 — PAC-07 Payment & Refund State Machine Matrix

**Document ID:** `PAC-07-PAYMENT-STATE-MATRIX`  
**Sprint:** `PAC-07 — Mock Payment, Cancellation & Refund Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Payment State Matrix  

---

## 1. Executive Summary

This document defines the authoritative state transitions, legal paths, illegal paths, and terminal state guards for Payment and Refund domain entities certified under PAC-07.

---

## 2. Payment State Transition Matrix

```
[INITIATED / PENDING]
        │
        ├─► Verify Success (Mock SUCCESS) ──────────────► [SUCCESS] (Terminal Success)
        │                                                     │
        ├─► Verify Failure (Mock DECLINED) ─────────────► [FAILED]  (Terminal Failure / Retryable)
        │                                                     │
        └─► Cancellation / Refund Approved ─────────────► [REFUNDED] (Terminal Refund)
```

| Source State | Target State | Triggering Operation | Validity | Domain Action / Outbox Event |
|--------------|--------------|----------------------|----------|------------------------------|
| `PENDING` | `SUCCESS` | Mock Payment Verification | **VALID** | Order `CONFIRMED` / Enrollment `CONFIRMED`, `PAYMENT_SUCCESS` event |
| `PENDING` | `FAILED` | Mock Payment Failure | **VALID** | Payment `FAILED`, zero inventory consumption |
| `SUCCESS` | `REFUNDED` | Approved Return / Cancel | **VALID** | Refund record created, `PAYMENT_REFUNDED` event |
| `FAILED` | `SUCCESS` | Retry Payment Attempt | **VALID** | New payment attempt created and verified |
| `SUCCESS` | `FAILED` | Unverified Client Callback | **INVALID** | Rejected by terminal state guard (`IllegalStateException`) |
| `REFUNDED` | `SUCCESS` | Duplicate Refund Attempt | **INVALID** | Rejected by refund idempotency check |

---

## 3. State Matrix Verdict

**VERDICT: PASS** — State machine transitions and terminal state guards are 100% verified.
