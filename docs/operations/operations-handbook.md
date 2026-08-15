# SPOREKART v3.0 — Master Operations Handbook

**Date**: 2026-08-15

---

## 1. Daily Operational Checks
- Verify `/actuator/health` returns `UP`.
- Check database disk space & PostgreSQL connection count.
- Monitor payment webhook error log (`payment_webhook_events` processing status).
- Review low-stock inventory alerts (`GET /api/v1/admin/inventory/low-stock`).

## 2. Emergency Operational Playbooks
- **Rollback Playbook**: [docs/release/rollback-decision-matrix.md](file:///f:/sporekart-v3.0/docs/release/rollback-decision-matrix.md)
- **Rate Limiting Playbook**: [docs/runbooks/rate-limiting-runbook.md](file:///f:/sporekart-v3.0/docs/runbooks/rate-limiting-runbook.md)
- **Customer Support Playbook**: [docs/operations/customer-support-readiness.md](file:///f:/sporekart-v3.0/docs/operations/customer-support-readiness.md)
- **Admin Operations Playbook**: [docs/operations/admin-operations-guide.md](file:///f:/sporekart-v3.0/docs/operations/admin-operations-guide.md)