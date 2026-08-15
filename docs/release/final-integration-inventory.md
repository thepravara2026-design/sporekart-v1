# SPOREKART v3.0 — Final Integration Inventory

**Date**: 2026-08-15

---

## 1. External Integration Matrix

| Integration | Provider | Protocol / Auth | Webhook Signature / Idempotency | Timeout Policy | Failure Handling |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Payment Gateway** | Razorpay | HTTPS REST / Basic Auth (`key_id:key_secret`) | HMAC-SHA256 signature check (`X-Razorpay-Signature`) + `provider_event_id` uniqueness | 10s connection timeout | Retry attempt creation; idempotent webhook handler |
| **Shipping Logistics** | Shiprocket | HTTPS REST / Bearer Token Auth | Secret Header Token check + `provider_event_id` uniqueness | 15s connection timeout | Fallback to manual carrier dispatch in Admin panel |
| **Notifications** | SMTP / SMS Gateway | SMTP / REST | N/A (Outbound only) | 5s timeout | Async listener retry with non-blocking error logging |

---

## 2. Webhook Security Certification

All inbound webhook controllers (`PaymentController`, `ShippingWebhookController`) enforce signature verification before delegating payload processing to domain application services. Invalid signatures immediately return HTTP 400 Bad Request without side-effects.