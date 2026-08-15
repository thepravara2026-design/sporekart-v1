# SPOREKART v3.0 — Scalability Risk Register

**Date**: 2026-08-15

---

| Component | Current Load | Saturation Risk | Failure Impact | Detection Signal | Mitigation Strategy | Priority |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Catalog Search Index** | 120 SKUs | Low (High at > 10k SKUs) | Slow catalog browsing | p95 Search > 150ms | Add full-text search index or external search node | Low |
| **Outbox Event Processor** | 7,100 events/day | Low | Notification delay | Outbox queue depth > 100 | Batch outbox processing & horizontal worker scaling | Low |