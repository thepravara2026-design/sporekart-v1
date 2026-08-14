# Database Guidelines & Migration Policy — Sporekart v3.0

## Target Database
- Cloud Target: **Supabase PostgreSQL**
- Local/Testing: Local PostgreSQL container or H2 PostgreSQL-compatibility mode.

## Migration Rules (Flyway)
1. Migration files reside in `backend/src/main/resources/db/migration`.
2. File naming convention: `V[Number]__[description].sql` (e.g. `V1__initial_foundation.sql`).
3. **IMMUTABILITY RULE**: Never modify an already-applied migration file. Always create a new versioned migration script (`V2__...sql`).

## Schema Conventions
- Primary Keys: UUIDs or BIGINT identity columns.
- Table & Column Names: `snake_case` (e.g. `system_metadata`, `created_at`).
- Timestamps: Always use `TIMESTAMP WITH TIME ZONE` in UTC.
- Foreign Keys: Explicit foreign key constraints and indexed lookup columns.
