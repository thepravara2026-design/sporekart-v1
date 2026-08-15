# SPOREKART v3.0 — Sprint 4B Repository Reconnaissance & Architecture Document

## Payment Lifecycle, Razorpay Orchestration & Reconciliation Analysis

---

### Executive Summary

Reconnaissance of the SPOREKART v3.0 codebase confirms that a modular monolith Payment domain exists under `backend/src/main/java/com/sporekart/modules/payment/`. The Payment aggregate (`Payment.java`), payment attempts (`PaymentAttempt.java`), provider abstraction (`PaymentProvider.java`), Razorpay provider (`RazorpayPaymentProvider.java`), and webhook ingestion handler (`PaymentController.java`) are established.

Sprint 4B hardens this architecture into one authoritative, deterministic, audit-logged, idempotency-protected, concurrency-safe, and reconcilable Payment Lifecycle integrated cleanly with Sprint 4A's canonical Order Lifecycle.

---

### 1. Existing Payment Architecture & Domain Model

#### Domain Aggregate (`Payment.java` & `PaymentAttempt.java`)
- **`Payment`**: Aggregate root with `id` (UUID), `paymentReference`, `orderId`, `customerId`, `amount`, `currency`, `status` (`PaymentStatus`), `provider` (`PaymentProviderType`), `activeAttemptId`, `attempts` (`List<PaymentAttempt>`), `version` (Long), `createdAt`, `updatedAt`.
- **`PaymentAttempt`**: Sub-entity tracking individual provider order initiation attempts, containing `id`, `attemptReference`, `providerOrderId`, `providerPaymentId`, `providerSignature`, `status`, `failureCode`, `failureReason`.
- **`PaymentStatus` Enum**: `CREATED`, `PENDING`, `AUTHORIZED`, `SUCCESS`, `FAILED`, `CANCELLED`, `EXPIRED`. Terminal statuses: `SUCCESS`, `FAILED`, `CANCELLED`, `EXPIRED`.

#### Provider Abstraction (`PaymentProvider.java`)
- Abstraction separating domain from provider SDK details. Supports `createPaymentOrder`, `verifyPaymentSignature`, `verifyWebhookSignature`, `fetchPaymentStatus`, `processRefund`.
- **`RazorpayPaymentProvider`**: Concrete implementation utilizing `HmacSHA256` for cryptographic signature verification.
- **`MockPaymentProvider`**: Deterministic mock provider for local development/testing without external API credentials.

---

### 2. Integration Boundary with Sprint 4A Order Lifecycle

Sprint 4A established `OrderApplicationService.confirmOrderPayment(orderId, paymentReference)` as the authoritative entry point for payment confirmations.

```text
External Payment / Webhook Event
              │
              ↓
     Razorpay HMAC Signature Verification
              │
              ↓
     PaymentApplicationService (Verify Payment / Webhook)
              │
              ↓
     Payment Aggregate State Transition (CREATED/PENDING -> SUCCESS)
              │
              ↓
     PaymentStatusHistory Record Persisted Atomically (saveAndFlush)
              │
              ↓
     OrderApplicationService.confirmOrderPayment(orderId, paymentReference)
              │
              ↓
     Order State Machine Validates (CREATED/PAYMENT_PENDING -> CONFIRMED)
              │
              ↓
     OrderStatusHistory Recorded + OrderLifecycleEvent Published
```

---

### 3. Gaps & Implementation Requirements for Sprint 4B

1. **Immutable `PaymentStatusHistory` Entity & Audit Trail**:
   - Create `PaymentStatusHistory.java` domain aggregate and Flyway table `payment_status_history` logging `paymentId`, `fromStatus`, `toStatus`, `source` (`CLIENT`, `WEBHOOK`, `RECONCILIATION`), `actorType`, `actorId`, `providerEventId`, `reason`, `correlationId`, `createdAt`.
2. **Strict Server-Side Amount & Currency Verification**:
   - Validate that provider/webhook payment amount matches `order.getGrandTotal()` and currency matches `order.getCurrency()`.
3. **Automated Payment Reconciliation Service (`PaymentReconciliationService.java`)**:
   - Create internal reconciliation capability that queries provider status via `fetchPaymentStatus`, verifies order/amount alignment, executes legal state transition, and logs audit record.
   - Expose administrative reconciliation endpoint `POST /api/v1/admin/payments/{paymentId}/reconcile`.
4. **Flyway Migration (`V12__payment_hardening_and_status_history.sql`)**:
   - Schema updates for `payment_status_history` table and database index optimization (`order_id`, `provider_order_id`, `payment_reference`).
5. **Comprehensive Test Suite & Documentation**:
   - Unit tests (`PaymentDomainTest.java`), signature verification tests (`RazorpaySignatureTest.java`), webhook tests (`PaymentWebhookIntegrationTest.java`), concurrency & idempotency tests (`PaymentConcurrencyAndIdempotencyTest.java`), and documentation (`docs/payment-lifecycle.md`, `docs/payment-provider-architecture.md`, `docs/sprint-4b-implementation.md`).
