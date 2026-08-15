# SPOREKART v3.0 — Cost Risk Register

**Date**: 2026-08-15

---

| Cost Driver | Current Monthly Cost (Est.) | Growth Risk | Optimization Strategy | Expected Savings | Reliability Tradeoff | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Log Storage & Retention** | $45 / month | High if debug level logged | Retention policy (30-day hot, 90-day cold GCS) | 40% reduction | None | ACTIVE |
| **Database Instance Allocation** | $120 / month | Low | Scale instance size based on CPU utilization metrics | 20% on idle | None | MONITORED |