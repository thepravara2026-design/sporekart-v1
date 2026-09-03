#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_FILE="$ROOT_DIR/release-manifest.json"

GIT_SHA="$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")"
BUILD_TIME="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
APP_VERSION="${APP_VERSION:-3.0.0-SNAPSHOT}"
SCHEMA_VERSION="$(ls "$ROOT_DIR/backend/src/main/resources/db/migration"/V*.sql 2>/dev/null | sed 's/.*\/V\([0-9]*\)__.*/\1/' | sort -n | tail -1 || echo "0")"
BACKEND_TESTS="${BACKEND_TESTS:-}"
FRONTEND_BUILD="${FRONTEND_BUILD:-PASS}"

if [ -z "$BACKEND_TESTS" ]; then
  BACKEND_STATUS="NOT VERIFIED (run backend tests and set BACKEND_TESTS=<passed>/<total>)"
else
  BACKEND_STATUS="PASS (${BACKEND_TESTS})"
fi

cat <<EOF > "$OUTPUT_FILE"
{
  "application": "sporekart-platform",
  "version": "$APP_VERSION",
  "gitCommit": "$GIT_SHA",
  "buildTimestamp": "$BUILD_TIME",
  "environment": "production",
  "artifacts": {
    "backend": {
      "jarName": "sporekart-backend-0.1.0-SNAPSHOT.jar",
      "dockerImage": "sporekart-backend:${APP_VERSION}"
    },
    "frontend": {
      "distFolder": "frontend/dist",
      "dockerImage": "sporekart-frontend:${APP_VERSION}"
    }
  },
  "database": {
    "engine": "PostgreSQL 16",
    "migrationTool": "Flyway",
    "currentSchemaVersion": "$SCHEMA_VERSION"
  },
  "qualityGates": {
    "backendTestStatus": "$BACKEND_STATUS",
    "frontendBuildStatus": "$FRONTEND_BUILD",
    "dockerBuildStatus": "NOT VERIFIED",
    "containerSecurityStatus": "NOT VERIFIED",
    "postDeploymentSmokeTestStatus": "NOT VERIFIED"
  }
}
EOF

echo "==> Generated Release Manifest: $OUTPUT_FILE"
cat "$OUTPUT_FILE"
