# SPOREKART v3.0 — Payment Provider Architecture & Provider Boundary Specification

## 1. Overview
The payment domain in SPOREKART v3.0 isolates external payment gateways (e.g. Razorpay) behind a decoupled provider boundary (`PaymentProvider.java`). Domain entities and application services do not directly reference Razorpay SDK classes.

---

## 2. Provider Abstraction Architecture

```text
               ┌───────────────────────────────┐
               │   PaymentApplicationService   │
               └──────────────┬────────────────┘
                              │
                              ↓
               ┌───────────────────────────────┐
               │    PaymentProviderRegistry    │
               └──────────────┬────────────────┘
                              │
       ┌──────────────────────┴──────────────────────┐
       ↓                                             ↓
┌─────────────────────────┐               ┌─────────────────────────┐
│ RazorpayPaymentProvider │               │   MockPaymentProvider   │
└─────────────────────────┘               └─────────────────────────┘
```

---

## 3. Provider Contract Operations

1. `createPaymentOrder(PaymentProviderOrderRequest)`: Initiates provider order & returns provider order reference ID.
2. `verifyPaymentSignature(PaymentVerificationRequest)`: Validates client callback HMAC SHA-256 signature (`providerOrderId|providerPaymentId`).
3. `verifyWebhookSignature(rawBody, signatureHeader)`: Validates raw HTTP body HMAC SHA-256 signature against webhook secret.
4. `fetchPaymentStatus(providerPaymentId)`: Retrieves payment status from provider for reconciliation.
5. `processRefund(PaymentRefundRequest)`: Establishes refund lifecycle boundary.

---

## 4. Environment Configuration & Security

Credentials are injected cleanly via environment variables:
- `RAZORPAY_KEY_ID`: Public key transmitted to client for Razorpay Checkout UI.
- `RAZORPAY_KEY_SECRET`: Server-only secret used for HMAC signature verification. Never exposed to browser.
- `RAZORPAY_WEBHOOK_SECRET`: Server-only secret used for webhook payload signature validation.
