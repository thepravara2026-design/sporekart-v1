# Architectural Policy: Database Schema Evolution & Compatibility — SPOREKART v3.0

This document defines the architectural policy and engineering guidelines for database schema changes in SPOREKART v3.0.

---

## 1. Principles of Zero-Downtime Schema Evolution

1. **Expand-Migrate-Deploy-Backfill-Contract Pattern:**
   - Schema modifications must never break running application versions.
   - Expand phase adds new schema structures without mutating existing columns.
   - Deploy phase introduces application versions compatible with both old and new structures.
   - Backfill phase populates new columns asynchronously in small, non-locking batches.
   - Contract phase removes old schema structures in a separate, subsequent release cycle.

2. **Flyway Immutability:**
   - Historical Flyway migrations are immutable once committed to production.
   - Schema repairs or corrections must be introduced via new, sequentially numbered migrations.

3. **Hibernate DDL Validation (`spring.jpa.hibernate.ddl-auto=validate`):**
   - Hibernate is restricted to schema validation only. Schema modification in production is exclusively governed by Flyway.

4. **PostgreSQL & Supabase Compatibility:**
   - Migrations must adhere to standard PostgreSQL syntax compatible with Supabase managed databases without requiring superuser / `ALTER SYSTEM` privileges.

---

## 2. Non-Destructive Column Evolution Rules

| Action | Allowed in Single Release? | Standard Procedure |
| :--- | :--- | :--- |
| **Add Column** | YES | Must be `NULL` or have explicit default value. |
| **Drop Column** | NO | Phase 1: Stop reading/writing column. Phase 2: Drop column in N+1 release. |
| **Rename Column** | NO | Phase 1: Add new column. Phase 2: Dual-write/backfill. Phase 3: Switch readers. Phase 4: Drop old column in N+1 release. |
| **Add Constraint** | CONDITIONALLY | Validate existing data first to prevent migration validation failure. |
| **Add Index** | YES | Ensure lock timeouts are enforced; evaluate `CONCURRENTLY` for large tables. |
