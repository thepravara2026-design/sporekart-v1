# SPOREKART v3.0 — RELEASE MANIFEST SPECIFICATION

## 1. RELEASE MANIFEST OVERVIEW

The release manifest (`release-manifest.json`) provides immutable traceability for every Sporekart v3.0 release artifact. It records the exact Git commit SHA, application version, build timestamp, database migration version, test results, and artifact checksums.

---

## 2. MANIFEST SCHEMA FORMAT (`release-manifest.json`)

```json
{
  "application": "sporekart-platform",
  "version": "3.0.0-SNAPSHOT",
  "gitCommit": "b69e7c2",
  "buildTimestamp": "2026-08-19T19:24:09Z",
  "environment": "production",
  "artifacts": {
    "backend": {
      "jarName": "sporekart-backend-0.1.0-SNAPSHOT.jar",
      "dockerImage": "sporekart-backend:3.0.0-SNAPSHOT"
    },
    "frontend": {
      "distFolder": "frontend/dist",
      "dockerImage": "sporekart-frontend:3.0.0-SNAPSHOT"
    }
  },
  "database": {
    "engine": "PostgreSQL 16",
    "migrationTool": "Flyway",
    "currentSchemaVersion": "45"
  },
  "qualityGates": {
    "backendTestStatus": "PASS (835/835)",
    "frontendBuildStatus": "PASS",
    "dockerBuildStatus": "NOT VERIFIED",
    "containerSecurityStatus": "NOT VERIFIED",
    "postDeploymentSmokeTestStatus": "NOT VERIFIED"
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
