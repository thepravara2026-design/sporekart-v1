# SPOREKART v3.0 — Final Security Debt Register

**Date**: 2026-08-15

---

## 1. Security Debt Inventory

- **Release Blockers (0)**: None.
- **Accepted Non-Blocking Items (1)**:
  - **Item**: In-memory rate limiting bucket store.
  - **Risk**: Client requests distributed across multiple server nodes maintain separate rate counters until Redis rate limiting is integrated.
  - **Mitigation**: Multi-node deployments can be protected behind an Nginx or AWS WAF rate-limiting tier.
  - **Acceptance**: Approved by Lead Security Engineer for v3.0.0 release.