# SPOREKART v3.0 — SPRINT 5D DOCUMENTATION REPORT

## RELIABILITY, RESILIENCE & FAILURE RECOVERY

**Project:** Sporekart v3.0  
**Sprint:** 5D  
**Status:** COMPLETE — 290/290 Tests Passing (100% Clean)  
**Priority:** P0 — Production Readiness Blocker  
**Architecture:** Modular Monolith  
**Backend Framework:** Java 21 + Spring Boot 4  
**Database:** Supabase PostgreSQL (H2 for Integration Test profiles)  

---

## EXECUTIVE SUMMARY

Sprint 5D establishes production-grade reliability, resilience, failure recovery, idempotency-guarded retries, status reconciliation, inventory race condition protection, out-of-order event defense, and transaction rollback recovery across the Sporekart v3.0 platform.

Every outbound integration (Razorpay payments, shipping booking providers) and internal transaction boundary has been hardened against transient network failures, timeouts, concurrent race conditions, out-of-order webhooks, and partial state corruption while preserving 100% commerce correctness and passing the complete test suite (290/290 clean).

---

## 1. ARCHITECTURAL HIGHLIGHTS & KEY COMPONENTS

### 1.1 Core Resilience Subsystem
- **`TransientFailureException`**: Specialized runtime exception classifying transient network timeouts (`SocketTimeoutException`, `ConnectTimeoutException`, HTTP 502/503/504, DB deadlock).
- **`ResilienceProperties`**: Binds configuration parameters from `app.resilience` (`payment`, `shipping`, `database` timeouts, `max-attempts`, `backoff-multiplier`, `jitter-factor`).
- **`ResilientExecutor`**: Retries transient operations using exponential backoff and randomized jitter to prevent thundering herd spikes. Protects retries by enforcing strict idempotency classification (never retries non-idempotent mutations).

### 1.2 Reconciliation Engine
- **`PaymentStatus.PENDING_RECONCILIATION`**: New status enum value for payment records whose status cannot be authoritatively determined due to gateway timeout during creation or verification.
- **`PaymentReconciliationService`**: Background reconciliation engine fetching pending payments, querying provider APIs, updating status safely to `SUCCESS` or `FAILED`, confirming stock reservations, and notifying `OrderApplicationService` without double charging or duplicate ledger entries.
- **`ShipmentReconciliationService`**: Reconciliation service scanning `BOOKING_PENDING` shipments and resolving waybill / booking states with external delivery providers.

### 1.3 Concurrency & Out-of-Order Event Protection
- **Deterministic Inventory Ordering & Pessimistic Locking**: `InventoryApplicationService` extracts requested SKUs in sorted ASC order and applies `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`findAllBySkuInOrderBySkuAscForUpdate`) to eliminate database deadlock risks.
- **Atomic Stock Checks**: Validates available stock (`on_hand - reserved - damaged >= qty`) before applying stock reservations, preventing negative stock under parallel checkout spikes.
- **Out-of-Order Webhook & Lifecycle Guards**: `ShipmentStateMachine` and `OrderStateMachine` reject regressive state updates (e.g. late `SHIPPED` event received after `DELIVERED`), preserving terminal order states (`CANCELLED`, `COMPLETED`, `EXPIRED`).

### 1.4 Database Schema Evolution
- **`V19__resilience_reconciliation_schema.sql`**: Flyway migration expanding `payments.status` column to `VARCHAR(50)` to accommodate `PENDING_RECONCILIATION` status strings.

---

## 2. VERIFIED RESILIENCE INTEGRATION TEST MATRIX

Sprint 5D added 7 targeted integration test scenarios across 4 test suites:

1. **`PaymentFailureAndReconciliationTest`**:
   - `shouldRetryTransientFailures`: Validates `ResilientExecutor` retrying transient network timeouts 3 times with backoff before succeeding.
   - `shouldFailFastOnPermanentErrors`: Confirms non-transient 400 Bad Request errors fail fast without retry attempts.
   - `shouldReconcilePendingPayments`: Verifies `PaymentReconciliationService` reconciling `PENDING_RECONCILIATION` records to `SUCCESS` and confirming order payment.

2. **`ShippingResilienceIntegrationTest`**:
   - `shouldPreventStateRegressionOnOutOfOrderEvents`: Verifies out-of-order tracking events arriving after `DELIVERED` status do not regress shipment status while logging the event for audit.
   - `shouldReconcileBookingPendingShipments`: Tests `ShipmentReconciliationService` transitioning `BOOKING_PENDING` shipments to `BOOKED`.

3. **`InventoryConcurrencyResilienceTest`**:
   - `shouldHandleConcurrentReservationsWithoutNegativeStock`: Launches 10 parallel threads contending for 1 available unit of stock. Verifies exactly 1 thread succeeds, 9 fail with `InsufficientStockException`, and stock remains non-negative.

4. **`OrderStateResilienceTest`**:
   - `shouldRejectTransitionsOnTerminalOrders`: Verifies terminal order states (`CANCELLED`) reject invalid regressive state transitions (`markShipped`, `markDelivered`).

---

## 3. FINAL TEST BASELINE & VERIFICATION RESULTS

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Results :

Tests run: 290, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  02:39 min
```

---

## 4. SPRINT COMPARISON SUMMARY

| Sprint | Objective | Test Baseline |
|:---|:---|:---|
| **Sprint 5A** | Security & Identity Hardening | 258 / 258 Passing |
| **Sprint 5B** | API Security & Abuse Protection | 275 / 275 Passing |
| **Sprint 5C** | Observability & Production Diagnostics | 283 / 283 Passing |
| **Sprint 5D** | Reliability, Resilience & Failure Recovery | **290 / 290 Passing** |

---

## 5. CONCLUSION & PRODUCTION READINESS SUMMARY

With Sprint 5D complete, Sporekart v3.0 possesses a hardened, resilient, observable, and secure backend platform capable of recovering gracefully from transient outages, network timeouts, parallel inventory contention, out-of-order events, and database transaction rollbacks.
