# SPOREKART v3.0 — Commerce Integration Testing Guidelines

## Integration Test Suite Architecture & Verification Practices

---

### Test Suite Structure

Integration tests in SPOREKART v3.0 are located in `backend/src/test/java/com/sporekart/integration/`.

#### Dedicated Suites:
1. `CommerceEndToEndLifecycleTest`: E2E commerce chain validation.
2. `CartCheckoutIntegrationTest`: Cart subtotal, catalog pricing integrity, empty cart handling.
3. `InventoryReservationIntegrationTest`: Stock reservation atomicity, over-booking prevention.
4. `PaymentOrchestrationIntegrationTest`: Payment verification, callback idempotency.
5. `OrderStateMachineIntegrationTest`: Lifecycle state transition constraints.
6. `TransactionIntegrityAndOutboxTest`: Single-transaction boundaries and rollback behavior.
7. `CommerceConcurrencyIntegrationTest`: High-concurrency race condition verification (2 users, 1 stock unit).
8. `CommerceSecurityAndAuthorizationTest`: IDOR isolation and customer access control.
9. `CommerceFailureMatrixAndRecoveryTest`: Payment decline compensating stock release.

---

### Execution Instructions

Run complete integration test suite:
```bash
mvn test -Dtest=*IntegrationTest,*Test
```
