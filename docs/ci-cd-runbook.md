# SPOREKART v3.0 — CI/CD RELEASE RUNBOOK

## 1. CI/CD PIPELINE ARCHITECTURE

The Sporekart v3.0 release pipeline (`.github/workflows/release-ci.yml`) enforces zero-suppression quality gates across 6 sequential stages:

```text
CHECKOUT ──> BACKEND BUILD & TEST ──> FRONTEND INSTALL & BUILD ──> MIGRATION CHECK ──> DOCKER VERIFICATION ──> MANIFEST GENERATION
```

---

## 2. PIPELINE STAGE SPECIFICATION

1. **Checkout**: Checks out source code from `main` or release tags.
2. **Backend Build & Test**:
   - Sets up JDK 21 (Temurin).
   - Runs `mvn clean test` (Enforces 295/295 tests passing).
   - Runs `mvn package -DskipTests` to produce `sporekart-backend-0.1.0-SNAPSHOT.jar`.
3. **Frontend Build**:
   - Sets up Node.js 20.
   - Runs `npm ci` (Uses strict `package-lock.json`).
   - Runs `npm run build` to output production SPA bundle in `frontend/dist`.
4. **Database Migration Verification**:
   - Validates Flyway migration scripts `V1` through `V19` for SQL syntax and naming conventions.
5. **Docker Build & Security Scan**:
   - Builds `sporekart-backend:latest` using `infrastructure/docker/backend.Dockerfile`.
   - Builds `sporekart-frontend:latest` using `infrastructure/docker/frontend.Dockerfile`.
   - Verifies runtime user is non-root (`sporekart`).
6. **Release Artifact & Manifest**:
   - Executes `scripts/generate-release-manifest.sh` to output `release-manifest.json`.

---

## 3. QUALITY GATE POLICY

- Pipeline **MUST FAIL** if any test fails, compilation fails, container build fails, or secret check fails.
- No `|| true` suppressions allowed in release workflows.
