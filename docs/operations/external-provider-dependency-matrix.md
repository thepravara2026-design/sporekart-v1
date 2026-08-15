# SPOREKART v3.0 — External Provider Dependency Matrix

**Date**: 2026-08-15

---

| Provider | Capability | Criticality | Failure Impact | Timeout Policy | Retry Policy | Fallback Behavior | Monitoring Signal | Owner Role |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Razorpay** | Payment Gateway | CRITICAL | Checkout block | 5,000 ms | 3 retries (exp backoff) | Return user-friendly error; retain order in `PAYMENT_PENDING` | `payment.gateway.errors` | SRE Lead |
| **Shiprocket** | Logistics & Shipping | HIGH | Shipment booking delay | 8,000 ms | 3 retries (exp backoff) | Queue shipment in `READY_FOR_SHIPMENT` state for background retry | `shipping.provider.errors` | Logistics Lead |
| **SMTP / Twilio** | Email / SMS Notifications | MEDIUM | Notification delay | 3,000 ms | 5 retries (exponential) | Store failed notification in `outbox_events` for dead-letter retry | `notification.delivery.errors` | SRE Lead |