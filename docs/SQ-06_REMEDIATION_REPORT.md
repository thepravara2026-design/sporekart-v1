# SQ-06 — Remediation Report

## Remediation Log

### 1. `OutboxService.java` (`com.sporekart.application.outbox.application`)
- **Issue**: Missing parameter validation on `publish` entry point allowed null values into outbox event initialization.
- **Fix**: Added explicit `Objects.requireNonNull` checks for `aggregateType`, `aggregateId`, and `eventType`.
- **Commit**: `bc4ac38`

### 2. `OutboxEvent.java` (`com.sporekart.application.outbox.domain`)
- **Issue**: Microsecond race condition during `markReplayed()` where `scheduledAt` timestamp coincided exactly with `OffsetDateTime.now()` passed to `findPendingOrFailedEventsForProcessing`, causing intermittent query omission.
- **Fix**: Updated `markReplayed()` and `markStaleReset()` to set `scheduledAt = OffsetDateTime.now().minusSeconds(1)` ensuring immediate polling eligibility for worker sweeps.
- **Commit**: `d026806`

### 3. `NotificationOutboxDomainBoundaryTest.java` (`com.sporekart.application.outbox`)
- **Issue**: Lack of explicit unit tests verifying retry exhaustion, outbox parameter validation, and template substitution edge cases.
- **Fix**: Created dedicated unit test suite with 4 unit boundary tests.
- **Commit**: `be9a318`

---

## Architectural & Security Audit Summary
- **Transactional Integrity**: Outbox events continue to execute strictly within the caller's transaction boundary.
- **Data Protection**: Sensitive attributes in notification logs are masked prior to log emission.
- **Idempotency Guarantee**: Duplicate notifications are prevented via primary idempotency key lookup and secondary event-id/channel/recipient deduplication.
