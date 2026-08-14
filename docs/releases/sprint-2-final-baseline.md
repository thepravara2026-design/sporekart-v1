# Sporekart v3.0 — Sprint 2 Final Baseline Record

## Baseline Summary

- **Project**: Sporekart v3.0
- **Domain**: Catalog Vertical (Sprint 2A – 2G)
- **Status**: FROZEN & READY
- **Branch**: `develop`
- **Release Tag**: `sporekart-sprint-2-catalog-v1.0.0`
- **Date**: 2026-08-15

---

## Commit Chain History

| Sprint | Description | Commit Hash | Gate Decision |
|---|---|---|---|
| Sprint 2A | Catalog Backend Foundation | `a9018bc` | GO |
| Sprint 2B | Catalog API Contract Hardening | `9d554a1` | GO |
| Sprint 2C | Customer-Facing Catalog Experience | `4affbb8` | GO |
| Sprint 2D | Catalog End-to-End Hardening | `6693180` | GO |
| Sprint 2E | Catalog Release Baseline & Documentation | `05304f1` | READY |
| Sprint 2F | Production Readiness & Integration Gate | `c293da2` | READY |
| Sprint 2G | Final Sign-Off & Baseline Freeze | `HEAD` | READY |

---

## Technology Stack Baseline
- **Backend**: Java 21 + Spring Boot v3.4.2
- **Frontend**: Vite v5.4.21 + React 18 + TypeScript 5
- **Production Database**: PostgreSQL (Flyway Schema v3)
- **QA/Test Database**: H2 In-Memory (MODE=PostgreSQL)
- **OpenAPI**: OpenAPI 3.0 via Springdoc v2.8.5

---

## Verification Evidence Summary
- **Backend Tests**: 50/50 Passed (100%)
- **Frontend Tests**: 20/20 Passed (100%)
- **ESLint Warnings**: 0
- **Frontend Bundle Size**: 275.80 kB JS / 8.17 kB CSS
- **Working Tree**: Clean
