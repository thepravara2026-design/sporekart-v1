# SPOREKART v3.0 — PAC-05 Cancellation & Reschedule Acceptance Report

**Document ID:** `PAC-05-CANCELLATION-RESCHEDULE`  
**Sprint:** `PAC-05 — Training Module End-to-End Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Cancellation & Reschedule Report  

---

## 1. Executive Summary

This document certifies trainee cancellation rules, admin cancellation rules, rescheduling workflows, and date boundary enforcement for the Training Module.

---

## 2. Authoritative Date Boundary Rules

| Actor | Operation | Minimum Notice Boundary | Enforced Exception on Violation |
|-------|-----------|-------------------------|---------------------------------|
| **Trainee** | Cancel Enrollment | **$\ge 2$ Days** prior to `startDate` | `CancellationWindowExpiredException` / `HTTP 400` |
| **Trainee** | Reschedule Enrollment | **$\ge 2$ Days** prior to `startDate` | `RescheduleWindowExpiredException` / `HTTP 400` |
| **Admin** | Cancel Batch | **$\ge 7$ Days** prior to `startDate` | `AdminCancellationWindowException` / `HTTP 400` |
| **Admin** | Reschedule Batch | **$\ge 7$ Days** prior to `startDate` | `AdminRescheduleWindowException` / `HTTP 400` |

---

## 3. Empirical Test Evidence

1. **Trainee Cancellation Boundary:** `TrainingCancellationWindowHardeningTest.TraineeCancellation` confirms that cancelling $\ge 2$ days prior succeeds (releasing seat and marking enrollment `CANCELLED`). Attempting cancellation $< 2$ days prior throws `CancellationWindowExpiredException`.
2. **Admin Batch Cancellation:** `TrainingCancellationWindowHardeningTest.AdminCancellation` verifies that Admin cancelling a batch $\ge 7$ days prior cancels all active enrollments, releases capacity, and initiates bulk refunds.
3. **Rescheduling Seat Transfer:** `TrainingRescheduleServiceTest` verifies that rescheduling transfers the trainee to the target batch if available capacity exists without losing payment history.

---

## 4. Cancellation & Reschedule Verdict

**VERDICT: PASS** — Date boundary rules, cancellation policies, and rescheduling seat transfers are 100% certified.
