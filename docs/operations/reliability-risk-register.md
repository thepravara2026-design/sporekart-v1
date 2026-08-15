# SPOREKART v3.0 — Reliability Risk Register

**Date**: 2026-08-15

---

| Risk Scenario | Probability | Impact | Detectability | Risk Mitigation Strategy | Risk Owner Role | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Payment Webhook Latency Spike** | Medium | Low | High | Webhook idempotency key table & async outbox ACK | SRE Lead | MITIGATED |
| **Courier API Network Timeout** | Medium | Medium | High | Shiprocket provider abstraction retry exponential backoff | SRE Lead | MITIGATED |