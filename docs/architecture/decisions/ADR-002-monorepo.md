# ADR-002: Monorepo Structure

## Status
Accepted

## Context
Sporekart v3.0 consists of a frontend application (Vite React TS), a backend application (Spring Boot), database schemas/migrations, Docker infrastructure, and developer documentation. We evaluated separate repositories vs a single monorepo.

## Decision
We adopt a single **Monorepo** structure containing `frontend/`, `backend/`, `database/`, `infrastructure/`, `scripts/`, `docs/`, and `.github/`.

## Alternatives Considered
- **Multi-repo**: Storing frontend, backend, and infrastructure in separate GitHub repositories. Rejected because atomic commit tracking, unified CI/CD pipelines, and single-step clone developer experience are lost.

## Consequences
- **Positive**: Simplified cross-stack commits, single source of truth, easier continuous integration.
- **Negative**: Requires well-configured path filters in CI triggers to avoid running unnecessary backend builds on frontend-only changes.
