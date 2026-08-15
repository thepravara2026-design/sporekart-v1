# Payment Domain & Payment Orchestration (`com.sporekart.modules.payment`)

## Overview

The Payment module provides a provider-agnostic, production-grade, concurrency-safe, and idempotent payment orchestration engine inside the SPOREKART v3.0 modular monolith application.

It supports payment attempt lifecycle tracking, provider payment order generation, cryptographic HMAC SHA-256 signature verification, webhook processing, idempotency enforcement, and integration with `Order` (Sprint 3C) and `Inventory` (Sprint 3D).

---

## Core Business Invariants & Security Rules

1. **Client Success Never Trusted**: Client payment success callbacks are never trusted alone. All payments MUST be verified server-side using HMAC SHA-256 signature verification against provider secrets (`razorpay.keySecret`) or cryptographically verified webhooks (`X-Razorpay-Signature`).
2. **Authoritative Payment Amounts**: Money amounts come strictly from the authoritative `Order` aggregate (`order.getGrandTotal()`). Frontend amount inputs are ignored.
3. **Integer Minor Units**: Amounts are converted to integer minor units (paise for INR) at provider boundaries. Floating point arithmetic for money is strictly prohibited.
4. **Active Inventory Reservation Required**: Payment attempts require an active, non-expired inventory reservation (`StockReservation`).
5. **Webhook Idempotency**: Webhook events enforce unique constraint `uq_provider_event` (`provider`, `provider_event_id`). Duplicate webhooks return `PROCESSED_DUPLICATE` without re-triggering business side-effects.
6. **Stock Release on Payment Failure**: If a payment fails or expires, the system triggers `inventoryApplicationService.releaseReservation(...)` to restore stock.

---

## Database Architecture (`V7__payment_domain.sql`)

### 1. `payments` Table
- `id UUID PRIMARY KEY`
- `payment_reference VARCHAR(100) NOT NULL UNIQUE` (e.g. `PAY-SPK-YYYYMMDD-XXXXXX`)
- `order_id UUID NOT NULL REFERENCES orders(id)`
- `customer_id VARCHAR(100) NOT NULL`
- `amount DECIMAL(12,2) NOT NULL CHECK (amount > 0)`
- `currency VARCHAR(3) NOT NULL DEFAULT 'INR'`
- `status VARCHAR(20) NOT NULL` (`CREATED`, `PENDING`, `AUTHORIZED`, `SUCCESS`, `FAILED`, `CANCELLED`, `EXPIRED`)
- `provider VARCHAR(50) NOT NULL` (`RAZORPAY`, `MOCK`)
- `active_attempt_id UUID NULL`
- `version BIGINT NOT NULL DEFAULT 0`
- `created_at / updated_at TIMESTAMP WITH TIME ZONE`

### 2. `payment_attempts` Table
- `id UUID PRIMARY KEY`
- `payment_id UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE`
- `attempt_reference VARCHAR(100) NOT NULL UNIQUE`
- `provider VARCHAR(50) NOT NULL`
- `provider_order_id VARCHAR(100) NULL`
- `provider_payment_id VARCHAR(100) NULL`
- `provider_signature VARCHAR(255) NULL`
- `status VARCHAR(20) NOT NULL`
- `amount DECIMAL(12,2) NOT NULL`
- `currency VARCHAR(3) NOT NULL`
- `failure_code / failure_reason VARCHAR`

### 3. `payment_webhook_events` Table
- `id UUID PRIMARY KEY`
- `provider VARCHAR(50) NOT NULL`
- `provider_event_id VARCHAR(100) NOT NULL`
- `event_type VARCHAR(100) NOT NULL`
- `signature_verified BOOLEAN NOT NULL`
- `processing_status VARCHAR(20) NOT NULL` (`RECEIVED`, `PROCESSED`, `FAILED`, `IGNORED`, `DUPLICATE`)
- `CONSTRAINT uq_provider_event UNIQUE (provider, provider_event_id)`

---

## REST Endpoints

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/payments` | Initiate payment attempt for order | Yes (`User`) |
| `POST` | `/api/v1/payments/verify` | Verify client payment callback | Yes (`User`) |
| `GET` | `/api/v1/payments/{paymentReference}` | Fetch payment status | Yes (`User`) |
| `POST` | `/api/v1/payments/webhooks/razorpay` | Process Razorpay webhook event | Public (Cryptographic Sig) |

---

## Configuration Properties (`sporekart.payment`)

```yaml
sporekart:
  payment:
    provider: MOCK # Options: MOCK, RAZORPAY
    razorpay:
      key-id: ${RAZORPAY_KEY_ID:rzp_test_mockKeyId123}
      key-secret: ${RAZORPAY_KEY_SECRET:mockSecretKey456}
      webhook-secret: ${RAZORPAY_WEBHOOK_SECRET:mockWebhookSecret789}
```
