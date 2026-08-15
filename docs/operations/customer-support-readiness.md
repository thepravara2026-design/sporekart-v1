# SPOREKART v3.0 — Customer Support Operational Readiness Guide

**Date**: 2026-08-15
**Audience**: Customer Support Tier 1 & Tier 2 Leads

---

## 1. Support Scenarios & System State Diagnostics

### Scenario A: Customer reports payment deducted but order pending/failed
- **Diagnostic Tool**: Admin Payments Panel (`/api/v1/admin/payments`) or support ticket view.
- **Root Cause**: Webhook delay or razorpay signature verification timeout.
- **Safe Operational Action**: Inspect `payment_webhook_events` status for `provider_payment_id`. If payment status is `CAPTURED` on Razorpay dashboard, trigger manual payment reconciliation action via Admin UI (`POST /api/v1/admin/payments/{paymentRef}/reconcile`). System automatically transitions order to `PAID` and triggers inventory confirmation.

### Scenario B: Customer requests item replacement for damaged goods
- **Diagnostic Tool**: Support Ticket Management (`/api/v1/customer/support/tickets/{ticketNo}`).
- **Operational Workflow**:
  1. Customer files ticket with `issue_type: DAMAGED_ITEM`.
  2. Support agent reviews evidence and clicks **"Approve Replacement"**.
  3. System creates a `replacement_requests` record linked to ticket, reserves replacement stock via `InventoryApplicationService`, and triggers zero-cost replacement shipment via `ShipmentApplicationService`.

### Scenario C: Customer requests return after 15 days
- **System Rule**: Returns policy window is 14 days from delivery (`ReturnApplicationService.checkEligibility`).
- **Expected System Response**: System returns `eligible: false` with reason `RETURN_WINDOW_EXPIRED`.
- **Support Action**: Support agent cannot override eligibility unless customer escalation approved by CS Manager.

---

## 2. Support Ticket SLA Reference

| Priority | First Response SLA | Resolution SLA | Escalation Trigger |
| :--- | :--- | :--- | :--- |
| **URGENT** | 2 Hours | 24 Hours | CS Lead notified after 1 Hour |
| **HIGH** | 4 Hours | 48 Hours | CS Lead notified after 2 Hours |
| **MEDIUM** | 8 Hours | 72 Hours | Standard Queue |
| **LOW** | 24 Hours | 120 Hours | Standard Queue |