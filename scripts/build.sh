#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo " SPOREKART v3.0 — REPRODUCIBLE BUILD PIPELINE"
echo "=========================================================="

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> [1/2] Building and testing Backend (Java 21 + Spring Boot)..."
cd "$ROOT_DIR/backend"
if [ -f "./mvnw" ]; then
    ./mvnw clean verify
else
    mvn clean verify
fi

echo "==> [2/2] Building Frontend (Vite + React)..."
cd "$ROOT_DIR/frontend"
if [ -f "package-lock.json" ]; then
    npm ci
else
    npm install
fi
npm run build

echo "=========================================================="
echo " BUILD SUCCESSFUL: Backend JAR & Frontend Dist Ready!"
echo "=========================================================="
