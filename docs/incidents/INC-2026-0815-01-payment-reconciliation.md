# Postmortem: INC-2026-0815-01 — Payment Webhook Retry Delay

**Incident Date**: 2026-08-15  
**Severity**: SEV-3 (Informational / Non-Customer Impacting)  
**Lead Investigator**: Lead Production Reliability Engineer  

---

## 1. Summary & Timeline
- **11:20 UTC**: Third-party payment provider re-sent webhook event `evt_payment_captured_10091` after initial response timed out on connection closure.
- **11:20 UTC**: Application `PaymentApplicationService` received duplicate webhook event.
- **11:20 UTC**: Database index `idx_payment_webhook_events_provider_event` matched existing record.
- **11:20 UTC**: System returned HTTP 200 OK with `processing_status: IGNORED_DUPLICATE`.

---

## 2. Five-Whys Analysis
1. *Why did log show duplicate webhook warning?* Because Razorpay re-transmitted webhook payload.
2. *Why did Razorpay re-transmit payload?* The HTTP response ACK took > 2000ms during network glitch.
3. *Why was order state unaffected?* Because `payment_webhook_events` table enforces unique constraint on `(provider, provider_event_id)`.
4. *Why did system succeed safely?* Idempotent webhook handler design implemented in Sprint 3E and hardened in Sprint 4J.
5. *Why was no customer impacted?* The initial payment processing transaction had already completed successfully.

---

## 3. Preventive Action Items
- Adjust webhook ACK handler to return HTTP 200 immediately upon outbox write before secondary notifications trigger.