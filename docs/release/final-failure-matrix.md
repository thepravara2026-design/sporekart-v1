# SPOREKART v3.0 — Final Failure Matrix

**Date**: 2026-08-15

---

## 1. System Failure Mode & Recovery Matrix

| Failure Mode | Detection Mechanism | Business Impact | Automated Recovery | Manual Operational Recovery Procedure |
| :--- | :--- | :--- | :--- | :--- |
| **PostgreSQL Outage** | Spring DB connection failure / Actuator DOWN | High (all transactions fail) | HikariCP reconnect loop | Failover to PostgreSQL Read-Write Replica |
| **Razorpay Webhook Delay** | Order remains `PAYMENT_PENDING` | Medium (delay in order processing) | None | Trigger admin payment reconciliation endpoint |
| **Shiprocket API Outage** | Shipment creation throws 503 | Low (fulfillment queue builds) | Shipment status set to `FAILED_RETRY` | Admin manual carrier dispatch override |
| **Concurrent Stock Reservation Clash** | `OptimisticLockingFailureException` | Low (user gets 409 Conflict) | Client automatic retry | Customer retries checkout submission |
| **Invalid Webhook Signature** | Log WARN + 400 Bad Request | None (attack blocked) | Request rejected | No action required (security control working) |
| **Excessive Request Flood (DoS)** | Log WARN + 429 Too Many Requests | None (protected downstream) | RateLimitingFilter drops flood | IP block at WAF/Nginx level if persistent |