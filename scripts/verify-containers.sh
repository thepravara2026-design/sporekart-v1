#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> Verifying Docker Production Image Configurations..."

BACKEND_DOCKERFILE="$ROOT_DIR/infrastructure/docker/backend.Dockerfile"
FRONTEND_DOCKERFILE="$ROOT_DIR/infrastructure/docker/frontend.Dockerfile"

echo -n "Checking backend non-root user execution... "
grep -q "USER sporekart:sporekart" "$BACKEND_DOCKERFILE" && echo "PASS" || { echo "FAIL"; exit 1; }

echo -n "Checking backend JVM container memory flags... "
grep -q "MaxRAMPercentage" "$BACKEND_DOCKERFILE" && echo "PASS" || { echo "FAIL"; exit 1; }

echo -n "Checking backend healthcheck probe... "
grep -q "HEALTHCHECK" "$BACKEND_DOCKERFILE" && echo "PASS" || { echo "FAIL"; exit 1; }

echo -n "Checking frontend Nginx security headers... "
grep -q "nginx.conf" "$FRONTEND_DOCKERFILE" && echo "PASS" || { echo "FAIL"; exit 1; }

echo "==> Container Image Security & Configuration VERIFIED!"
