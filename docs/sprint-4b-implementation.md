# SPOREKART v3.0 — Sprint 4B Implementation Summary

## Payment Lifecycle, Razorpay Orchestration & Reconciliation Subsystem

---

### Key Architectural Enforcements

1. **State Machine Separation**:
   - `PaymentStatus` (`CREATED`, `PENDING`, `SUCCESS`, `FAILED`, `CANCELLED`, `EXPIRED`) is distinct from `OrderStatus` (`CREATED`, `CONFIRMED`, `PROCESSING`, etc.).
   - Payment state transitions are aggregate-encapsulated in `Payment.java` and audit-logged in `PaymentStatusHistory`.
2. **Sprint 4A Order Integration**:
   - Successful payments invoke `OrderApplicationService.confirmOrderPayment(orderId, paymentReference)`.
   - Order transitions through canonical state machine to `CONFIRMED`, logging `OrderStatusHistory` with `actorType = PAYMENT` and publishing `OrderLifecycleEvent`.
3. **Cryptographic HMAC Signature Verification**:
   - Razorpay client callback verification (`providerOrderId|providerPaymentId`) and webhook verification (`rawBody`) validate HMAC SHA-256 signatures against configured secrets.
4. **Server-Side Amount & Currency Integrity**:
   - Validates that payment attempt amount and currency match order `grandTotal` and `currency`.
5. **Automated Payment Reconciliation Service**:
   - `PaymentReconciliationService.java` allows administrators and background workers to reconcile divergent payment states directly against provider API data.
6. **Webhook Idempotency**:
   - Event IDs are persisted in `payment_webhook_events`. Repeated webhooks are safely skipped with `DUPLICATE` response status.
