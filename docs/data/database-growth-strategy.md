# SPOREKART v3.0 — Database Growth & Archival Strategy

**Date**: 2026-08-15

---

## 1. Database Entity Growth Analysis

| Table Name | Current Row Count | Estimated Monthly Growth | High-Growth Risk | Retention Policy | Archival Strategy |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `orders` | 1,420 rows | ~42,600 rows / month | Medium | Indefinite (Transactional) | Cold storage snapshot after 2 years |
| `order_items` | 3,840 rows | ~115,000 rows / month | Medium | Indefinite (Transactional) | Cold storage snapshot after 2 years |
| `payment_webhook_events` | 1,420 rows | ~45,000 rows / month | Low | 90 Days | Truncate processed events > 90 days |
| `outbox_events` | 7,100 rows | ~213,000 rows / month | High | 30 Days | Automated scheduled cleanup job for `PROCESSED` events |
| `notification_logs` | 4,260 rows | ~127,000 rows / month | High | 60 Days | Scheduled purge job for sent notifications > 60 days |

---

## 2. Partitioning & Indexing Roadmap
- **Partitioning Candidates**: Range-partition `outbox_events` and `notification_logs` by `created_at` (monthly partitions) when table sizes exceed 1,000,000 rows.
- **Index Governance**: Keep composite query indexes established in Flyway V16; forbid un-indexed foreign key queries.