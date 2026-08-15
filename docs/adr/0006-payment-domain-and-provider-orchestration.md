# ADR 0006: Payment Domain & Provider Orchestration Architecture

## Context & Problem Statement
In an e-commerce platform, payment integration requires server-side payment order generation, cryptographic callback verification, webhook idempotency, and non-leaking provider abstractions. Client payment success callbacks can be forged or tampered with and must never be trusted without server-side HMAC SHA-256 signature verification.

## Decision Drivers
1. **Security**: Mandatory server-side cryptographic HMAC SHA-256 signature verification for client callbacks and webhooks.
2. **Provider Agnosticism**: Provider interface (`PaymentProvider`) shielding domain logic from Razorpay-specific SDK types.
3. **Financial Integrity**: Authoritative order grand totals used for payment amounts, stored as exact `DECIMAL(12,2)` / minor integer units (paise for INR).
4. **Idempotency**: Webhook processing backed by `payment_webhook_events` database table with unique constraint `uq_provider_event`.
5. **Domain Handoff**: Payment success confirms `Order` (`PAID`) and `StockReservation` (`CONFIRMED`); payment failure releases stock reservation.

## Considered Options
1. **Option 1**: Direct client trust of payment completion. (REJECTED: Severe security vulnerability).
2. **Option 2**: Provider-agnostic domain abstraction with Razorpay and Mock adapters and server-side HMAC SHA-256 verification. (ACCEPTED).

## Decision Outcome
Accepted Option 2.
- Created `Payment`, `PaymentAttempt`, and `PaymentWebhookEvent` domain entities.
- Implemented `PaymentProvider` interface with `RazorpayPaymentProvider` and `MockPaymentProvider` adapters.
- Configured database migration `V7__payment_domain.sql`.
- Added 100% test coverage including domain, provider HMAC verification, webhook idempotency, concurrency, and controller tests.
