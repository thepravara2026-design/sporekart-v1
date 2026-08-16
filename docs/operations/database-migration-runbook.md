# Database Migration Runbook — SPOREKART v3.0

This runbook documents the operational procedure for executing zero-downtime database schema migrations and failure recovery for SPOREKART v3.0.

---

## 1. Pre-Migration Prerequisites

1. **Pre-Migration Database Backup:**
   - Execute snapshot/dump backup of production PostgreSQL / Supabase instance.
   - Verify backup validity before executing schema migrations.
2. **Flyway Migration Immutability Check:**
   - Verify that historical Flyway migrations (`V1` through `V23`) are untouched.
   - Ensure new migrations strictly follow sequential versioning (`V24`, `V25`, etc.).
3. **Lock & Statement Timeout Verification:**
   - Ensure session `lock_timeout` is configured (default: 5000ms) to prevent indefinite table locks during schema changes.

---

## 2. Execution Sequence (Expand-Migrate-Deploy-Backfill-Contract)

```
1. PRECHECK: Confirm backup & statement lock timeouts
2. EXPAND SCHEMA: Apply non-breaking additive Flyway migrations (e.g. nullable columns, new tables)
3. APPLICATION DEPLOYMENT: Deploy release artifact compatible with both old and new schema
4. READINESS PROBE: Verify `/actuator/health/readiness` returns HTTP 200 UP
5. DATA BACKFILL: Execute batched, non-locking background data migration if required
6. CONTRACT SCHEMA: (Optional future release) Decommission deprecated schema elements after zero-traffic verification
```

---

## 3. Lock & Index Safety Guidelines

- **Index Creation Safety:** In PostgreSQL, large table indexes must be evaluated for non-blocking creation (`CREATE INDEX CONCURRENTLY`). Because `CONCURRENTLY` cannot execute inside Flyway transactional blocks, execute heavy index creations via managed maintenance procedures.
- **Column Addition:** Always add new columns as `NULL` or with default values without table locks.
- **Nullability Transitions:** Perform `NULL` to `NOT NULL` transitions via: (1) add nullable column, (2) backfill default values, (3) enforce `NOT NULL` constraint.

---

## 4. Migration Failure & Failure Recovery Procedure

If a Flyway schema migration fails in production:
1. **Identify Failure Cause:** Inspect backend logs using `X-Correlation-ID` or Flyway error details.
2. **Do NOT Edit Applied Migration:** Never modify an applied migration file.
3. **Repair & Recovery:**
   - If schema change partially applied: Execute `flyway repair` via CLI or administrative pipeline.
   - Fix underlying SQL issue in a NEW Flyway migration script (e.g., `V24__fix_column_constraint.sql`).
4. **Verify Application Readiness:** Re-run `/actuator/health/readiness` to confirm system stability.
