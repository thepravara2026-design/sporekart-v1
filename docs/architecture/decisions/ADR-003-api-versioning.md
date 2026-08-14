# ADR-003: API Versioning Scheme

## Status
Accepted

## Context
All client-server interaction in Sporekart v3.0 takes place via HTTP REST APIs. We need a consistent URI versioning contract for backward compatibility and evolutionary API updates.

## Decision
We enforce mandatory URI prefix versioning using `/api/v1` for all public application APIs (e.g. `GET /api/v1/health`, `GET /api/v1/version`).

## Alternatives Considered
- **Header-Based Versioning (`Accept: application/vnd.sporekart.v1+json`)**: Rejected due to developer complexity and poorer observability in gateway/access logs.
- **Unversioned APIs (`/health`, `/products`)**: Rejected because non-versioned paths risk breaking client contracts during future API iterations.

## Consequences
- **Positive**: Clear, explicit versioning in logs and routing rules.
- **Negative**: Major version upgrades (`/v2`) require explicit controller route duplication or migration paths.
