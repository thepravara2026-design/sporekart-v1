# Sporekart v3.0 — Sprint 2 Final Risk Register

## Overview
This document registers all identified risks for the **Catalog Domain** following the closure of Sprint 2.

---

## Risk Register

| Risk ID | Description | Impact | Likelihood | Severity | Mitigation Strategy | Owner | Status |
|---|---|---|---|---|---|---|---|
| RSK-01 | SQL `ILIKE` pattern search performance degradation at >500k products | High | Medium | P2 (Medium) | Database indexes added in Flyway V3; full-text search engine (Elasticsearch/Meilisearch) scheduled for Sprint 7. | Engineering | MITIGATED |
| RSK-02 | Cross-module direct JPA entity dependency by future modules (Cart/Orders) | High | Low | P1 (High Risk) | Published `docs/catalog/integration-contract.md` enforcing DTO-only service boundaries. | Architecture | MITIGATED |
| RSK-03 | React Router v7 future flag deprecation in Vite frontend tests | Low | Low | P3 (Low) | Non-blocking warning; opt-in flags will be enabled during React Router v7 upgrade. | Frontend Lead | ACCEPTED |
| RSK-04 | Environment secret exposure in production deployment | Critical | Low | P0 (Critical) | Enforced `.env.example` placeholder audit; 0 committed credentials verified in security scan. | DevOps | MITIGATED |

---

## Blocker Summary
- **P0 (Release Blockers)**: 0
- **P1 (High Risk)**: 0
- **P2 (Medium)**: 1 (Mitigated)
- **P3 (Low)**: 1 (Accepted)
