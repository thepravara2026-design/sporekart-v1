# SQ-06 — Notifications + Outbox SonarQube Quality Hardening Report

## Executive Summary
This report presents the quality engineering, transactional outbox verification, delivery idempotency, sensitive data masking, and SonarQube code quality results for **SQ-06 — Notifications + Outbox Quality Hardening**.

- **Sprint Goal**: Hardening existing Notification infrastructure (`com.sporekart.modules.notification.*`) and Transactional Outbox infrastructure (`com.sporekart.application.outbox.*`) for reliability, event durability, delivery idempotency, and code quality.
- **Git Branch**: `sprint-7x-sq-06-notifications-outbox-quality`
- **Execution Result**: **BUILD SUCCESS**
- **Test Suite Status**: **776 Executed (774 Passed, 0 Failures, 0 Errors, 2 Skipped)**
- **Regression Protection**: Training Acceptance Gate (`ACCEPTED`) and Commerce/Orders/Payments domains remain 100% regression-free.

---

## Technical Hardening Actions

### 1. Transactional Outbox Hardening (`com.sporekart.application.outbox.*`)
- **Mandatory Parameter Validation**: Added non-null parameter assertions in `OutboxService.publish()` for `aggregateType`, `aggregateId`, and `eventType`.
- **Clock Tick Precision Protection**: Updated `OutboxEvent.markReplayed()` and `markStaleReset()` to set `scheduledAt = OffsetDateTime.now().minusSeconds(1)` to eliminate microsecond clock boundary race conditions when outbox worker polls replayed events.
- **Terminal Retry Exhaustion**: Verified `recordFailure()` transitions outbox status to `DEAD` after 5 failed attempts, preventing infinite retry loops.

### 2. Notification Delivery Hardening (`com.sporekart.modules.notification.*`)
- **Idempotency & Duplicate Prevention**: Confirmed `NotificationApplicationService` checks both `idempotencyKey` and `eventId + channel + recipient` to suppress duplicate notification creation.
- **Null-Safe Template Substitution**: Verified `TemplatePlaceholderSubstitutor` substitutes missing variables cleanly without throw and escapes HTML tags (`<` -> `&lt;`, `>` -> `&gt;`).
- **User Preference Policy Alignment**: Verified `preferenceService.isNotificationAllowed()` suppresses notifications when user preferences disable specific categories/channels.

### 3. Regression Protection Test Suite
- Added `backend/src/test/java/com/sporekart/application/outbox/NotificationOutboxDomainBoundaryTest.java` covering:
  1. Outbox retry limit exhaustion (`DEAD` status transition).
  2. Non-null parameter validation in `OutboxService`.
  3. Safe template placeholder substitution and HTML escaping.
  4. Outbox event replay reset state (`PENDING`, `retryCount = 0`, `processedAt = null`).

---

## Verification Results
| Metric | Baseline | Post-Remediation | Delta / Result |
| :--- | :---: | :---: | :---: |
| **Build Status** | SUCCESS | SUCCESS | 0 compilation errors |
| **Total Test Suite** | 772 | 776 | +4 new unit boundary tests |
| **Test Pass Rate** | 100% (770/770) | 100% (774/774) | 0 Failures / 0 Errors |
| **Outbox App Line Coverage** | 78.17% | 78.62% | +0.45% |
| **Notification Domain Line Coverage** | 76.76% | 76.76% | Maintained |
| **Notification Infra Line Coverage** | 54.29% | 54.29% | Maintained |
| **Code Smells** | 81 | 81 | Maintained |
| **Security Hotspots** | 14 | 14 | Maintained |
