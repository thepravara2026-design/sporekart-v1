# SPOREKART v3.0 — PAC-05 Source of Truth & Training Module Architecture

**Document ID:** `PAC-05-SOURCE-OF-TRUTH`  
**Sprint:** `PAC-05 — Training Module End-to-End Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Authoritative Baseline  

---

## 1. Executive Summary

This document establishes the single source of truth for SPOREKART v3.0 Training Module end-to-end architecture, domain entities, capacity management rules, cancellation boundaries, payment state transitions, and notification processors certified under **PAC-05 — Training Module End-to-End Acceptance**.

---

## 2. Training Module End-to-End Architecture Map

```
Admin / Trainee Frontends (SPA)
        │
        ├─► Admin Operations (`AdminTrainingOperationsConsole`, `TrainingProgramManagement`)
        │     - Create/Configure Training Program & Batch (Capacity, Price, Schedule)
        │     - Announce / Open Batch for Enrollment
        │
        ├─► Trainee Discovery (`TraineeTrainingConsole`)
        │     - Browse open batches, view schedule, price, and available seats
        │
        ▼
Enrollment & Capacity Engine (`TrainingProgramApplicationService`, `EnrollmentLifecycleService`)
        │
        ├─► 1. Check Available Seats (`batch.maxCapacity - confirmedEnrollments > 0`)
        ├─► 2. Create Enrollment Entity (`status = PENDING` / `PAYMENT_PENDING`)
        ├─► 3. Server-Authoritative Price Calculation (from `TrainingBatch.price`)
        │
        ▼
Training Payment Integration (`TrainingPaymentApplicationService`)
        │
        ├─► 4. Initiate Mock Payment Attempt
        ├─► 5. Verify Payment Signature & Amount
        │      - On SUCCESS -> Enrollment transitions to `CONFIRMED`
        │      - Seat count incremented atomically
        │      - OutboxEvent published (`TRAINING_ENROLLED`)
        │
        ▼
Lifecycle Management (`TrainingCancellationService`, `TrainingRescheduleService`)
        │
        ├─► Trainee Cancellation (Rule: Min 2 Days Prior) -> Status `CANCELLED`, Refund initiated
        ├─► Admin Cancellation (Rule: Min 7 Days Prior) -> Batch `CANCELLED`, Bulk Refund
        └─► Rescheduling -> Enrollment transferred to target batch without capacity loss
```

---

## 3. Authoritative Training Module Domain Entities

| Entity / Domain Object | Table Name | Key Attributes | Business Responsibility |
|------------------------|------------|----------------|-------------------------|
| `TrainingProgram` | `training_programs` | `id`, `title`, `description`, `category` | High-level course definition |
| `TrainingBatch` | `training_batches` | `id`, `program_id`, `start_date`, `end_date`, `max_capacity`, `price`, `status` | Scheduled batch with seat capacity & price |
| `TrainingEnrollment` | `training_enrollments`| `id`, `batch_id`, `trainee_id`, `status`, `enrolled_at` | Individual trainee seat reservation |
| `TrainingPayment` | `training_payments` | `id`, `enrollment_id`, `amount`, `status`, `reference` | Payment status (`SUCCESS`, `FAILED`, `REFUNDED`) |
| `TrainingRefund` | `training_refunds` | `id`, `payment_id`, `amount`, `reason`, `status` | Refund processing audit record |

---

## 4. Key Business Rule Safeguards

1. **Capacity Overbooking Prevention:** Enrollment creation and payment confirmation execute within database transactions, checking `confirmedEnrollments < maxCapacity`. Excess attempts are rejected (`CapacityExceededException` / `HTTP 400`).
2. **Server-Authoritative Pricing:** Enrollment payable amounts are derived from `TrainingBatch.price`. Client-submitted pricing payloads are ignored.
3. **Date Boundary Rules:** Trainee cancellations require $\ge 2$ days before batch start date; Admin cancellations/reschedules require $\ge 7$ days before start date.
4. **Trainee Data Isolation:** Trainee endpoints (`/api/v1/trainee/**`) strictly validate `traineeId` against `SecurityContextHolder`. Cross-trainee access returns `AccessDeniedException` (`HTTP 403`).

---

## 5. Governance Alignment Verdict

**VERDICT: CERTIFIED PASS** — The Training Module architecture strictly satisfies all domain, capacity, date boundary, and security rules required for PAC-05.
