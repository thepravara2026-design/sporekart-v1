# SPOREKART v3.0 — Platform Maturity Roadmap

**Date**: 2026-08-15

---

## 1. Multi-Horizon Platform Strategy

### Horizon 1: Immediate Stabilization & Production Guardrails (Current / Month 1)
- **Focus**: Maintain 100% data reconciliation, zero-downtime release pipeline, and SLO error-budget monitoring.
- **Key Deliverables**: Automated webhook retries, DORA metric tracking, synthetic health probes.

### Horizon 2: Horizontal Scale Preparedness (3-Month Target)
- **Focus**: Deploy Redis caching layer for catalog queries and rate-limiting shared state.
- **Key Deliverables**: Redis distributed lock configuration (`Redisson` / Spring Data Redis), read-replica database connection routing.

### Horizon 3: Platform Automation & Continuous Resilience (6-Month Target)
- **Focus**: Staged canary releases, automated DB archival policy, and multi-region disaster recovery replication.
- **Key Deliverables**: Partitioning of `outbox_events` and `notification_logs`, zero-downtime expand-contract schema migrations.