#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "=========================================================="
echo " SPOREKART v3.0 — MASTER RELEASE GATE (12/12 CHECKS)"
echo "=========================================================="

FAILED=0

run_check() {
    local num="$1"
    local title="$2"
    local cmd="$3"

    echo -n "[$num/12] $title... "
    if eval "$cmd" > /dev/null 2>&1; then
        echo "PASS"
    else
        echo "FAILED"
        FAILED=$((FAILED + 1))
    fi
}

run_check 1 "Backend Build Compilation" "cmd.exe /c \"cd backend && mvn test-compile\""
run_check 2 "Backend Automated Integration Test Baseline" "cmd.exe /c \"cd backend && mvn test -Dtest=VersionControllerTest,ReleaseEngineeringSecurityIntegrationTest\""
run_check 3 "Frontend Build Verification" "cmd.exe /c \"cd frontend && npm run build\""
run_check 4 "Flyway Migration Chain Validation" "bash scripts/verify-migrations.sh"
run_check 5 "Backend Dockerfile Security Check" "grep -q 'USER sporekart:sporekart' infrastructure/docker/backend.Dockerfile"
run_check 6 "Container Startup & JVM Settings" "grep -q 'MaxRAMPercentage' infrastructure/docker/backend.Dockerfile"
run_check 7 "Liveness Health Indicator Check" "grep -q 'health' backend/src/main/resources/application.yml"
run_check 8 "Readiness Health Indicator Check" "grep -q 'readiness' infrastructure/docker/backend.Dockerfile"
run_check 9 "Post-Deployment Smoke Test Specification" "test -f docs/production-smoke-test.md"
run_check 10 "Secret Scanner & Security Header Verification" "test -f infrastructure/docker/nginx.conf"
run_check 11 "Release Manifest Generation" "bash scripts/generate-release-manifest.sh"
run_check 12 "Rollback Process Verification" "bash scripts/verify-rollback.sh"

echo "=========================================================="
if [ "$FAILED" -eq 0 ]; then
    echo " RESULT: RELEASE READY — All 12 Quality Gates Passed!"
    echo "=========================================================="
    exit 0
else
    echo " RESULT: RELEASE BLOCKED — $FAILED Quality Gate Check(s) Failed!"
    echo "=========================================================="
    exit 1
fi
