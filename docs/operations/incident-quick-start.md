# SPOREKART v3.0 — Incident Quick-Start Guide

**Target Audience**: On-Call Engineers & Incident Responders

---

## 1. Incident Triage Steps

1. **Detect**: Alert fires or user reports high error rate / payment failure.
2. **Assess**: Check `/actuator/health` and query logs for error spikes (`grep "ERROR"`).
3. **Contain**:
   - If payment gateway issue: enable payment circuit breaker / notify users.
   - If DB saturation: scale application workers or restart Hikari connection pool.
   - If severe deployment regression: execute immediate application rollback ([docs/release/rollback-decision-matrix.md](file:///f:/sporekart-v3.0/docs/release/rollback-decision-matrix.md)).
4. **Recover & Validate**: Verify system health via smoke test ([docs/release/production-smoke-test.md](file:///f:/sporekart-v3.0/docs/release/production-smoke-test.md)).