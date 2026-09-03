# SPOREKART v3.0 — Top Production Risks Register

**Date**: 2026-08-15

---

| Risk ID | Production Risk Description | Severity | Probability | Impact | Mitigation Strategy | Risk Owner Role | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `RSK-4L-01` | External Payment Gateway Outage | High | Low | High | Circuit breaker fallback & automated webhook reconciliation | SRE Lead | MITIGATED |
| `RSK-4L-02` | Third-Party Courier API Rate Limit | Medium | Low | Medium | Exponential backoff retries & dead-letter queueing | Logistics Lead | MITIGATED |