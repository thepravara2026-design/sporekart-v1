# SPOREKART v3.0 — Production Knowledge Base & Operational Triage

**Date**: 2026-08-15

---

## 1. Common Post-Launch Operational Scenarios

### Scenario A: Payment Webhook Re-Transmission
- **Symptom**: Warning log entry `Processing duplicate payment webhook event`.
- **Diagnosis**: Query `payment_webhook_events` by `provider_event_id`.
- **Resolution**: No manual action required. System idempotency key drops duplicate attempt cleanly.

### Scenario B: Transient Payment Gateway Timeout
- **Symptom**: Customer payment state shows `PENDING` after 5 minutes.
- **Diagnosis**: Execute Admin payment status refresh `/api/v1/admin/payments/{paymentId}/sync`.
- **Resolution**: Synchronizes status directly with Razorpay REST API without double-charging customer.