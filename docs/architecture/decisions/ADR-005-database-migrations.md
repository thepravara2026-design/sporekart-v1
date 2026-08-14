# ADR-005: Flyway Schema Migration Infrastructure

## Status
Accepted

## Context
Database schema changes must be versioned, immutable, reproducible, and tracked alongside application code.

## Decision
We select **Flyway** as our database schema migration framework integrated directly into Spring Boot.

## Migration Rules
1. All schema modifications are versioned SQL scripts (e.g. `V1__initial_foundation.sql`, `V2__add_users.sql`) in `backend/src/main/resources/db/migration`.
2. **Immutability Rule**: Never edit an already-applied migration file. Always append a new versioned migration script (`V[N]__*.sql`).
3. Automated migrations run automatically on Spring Boot application startup.

## Consequences
- **Positive**: Controlled schema deployments across LOCAL, DEVELOPMENT, STAGING, and PRODUCTION environments.
- **Negative**: Manual database alterations in production environments are strictly forbidden.
