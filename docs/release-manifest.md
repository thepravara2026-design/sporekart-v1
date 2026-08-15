# SPOREKART v3.0 — RELEASE MANIFEST SPECIFICATION

## 1. RELEASE MANIFEST OVERVIEW

The release manifest (`release-manifest.json`) provides immutable traceability for every Sporekart v3.0 release artifact. It records the exact Git commit SHA, application version, build timestamp, database migration version, test results, and artifact checksums.

---

## 2. MANIFEST SCHEMA FORMAT (`release-manifest.json`)

```json
{
  "application": "sporekart-platform",
  "version": "3.0.0-RELEASE",
  "gitCommit": "f0339a4",
  "buildTimestamp": "2026-08-15T17:55:00Z",
  "environment": "production",
  "artifacts": {
    "backend": {
      "jarName": "sporekart-backend-0.1.0-SNAPSHOT.jar",
      "dockerImage": "sporekart-backend:3.0.0-RELEASE"
    },
    "frontend": {
      "distFolder": "frontend/dist",
      "dockerImage": "sporekart-frontend:3.0.0-RELEASE"
    }
  },
  "database": {
    "engine": "PostgreSQL 16",
    "migrationTool": "Flyway",
    "currentSchemaVersion": "19"
  },
  "qualityGates": {
    "backendTestStatus": "PASS (295/295 passing)",
    "frontendBuildStatus": "PASS",
    "dockerBuildStatus": "PASS",
    "containerSecurityStatus": "PASS (non-root sporekart user)",
    "postDeploymentSmokeTestStatus": "PASS"
  }
}
```

---

## 3. MANIFEST GENERATION PROCEDURE

The release manifest is generated deterministically during the build pipeline by `scripts/generate-release-manifest.sh`:

```bash
bash scripts/generate-release-manifest.sh
```

It is attached to CI build outputs and saved in `release-manifest.json`.
