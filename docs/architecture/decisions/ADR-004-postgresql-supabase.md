# ADR-004: Supabase PostgreSQL Engine

## Status
Accepted

## Context
Sporekart v3.0 requires an enterprise relational database with full ACID compliance, JSONB support, index optimization, and spatial/UTC time zone capabilities.

## Decision
We select **Supabase PostgreSQL** as our target cloud relational database system and local PostgreSQL container for isolated integration testing.

## Alternatives Considered
- **MySQL / MariaDB**: Rejected due to inferior JSON support and stricter transaction isolation constraints.
- **NoSQL (MongoDB)**: Rejected because e-commerce transactional workloads (orders, payments, inventory balance) require strict relational integrity and ACID constraints.

## Consequences
- **Positive**: High reliability, cloud management via Supabase, seamless migration execution.
- **Negative**: All database-specific features must remain compatible with standard PostgreSQL syntax.
