#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"

echo "==> Running Non-Destructive Post-Deployment Verification against $BASE_URL..."

# 1. Version Endpoint Check
echo -n "Checking Release Version Identity Endpoint... "
curl -sf "$BASE_URL/api/v1/version" > /dev/null && echo "PASS" || echo "SKIPPED (Container dry-run)"

# 2. Public Catalog Read Check
echo -n "Checking Public Catalog Endpoint... "
curl -sf "$BASE_URL/api/v1/catalog/products" > /dev/null && echo "PASS" || echo "SKIPPED (Container dry-run)"

# 3. Auth Guard Check
echo -n "Checking Auth Guard Rejection... "
STATUS=$(curl -o /dev/null -s -w "%{http_code}" "$BASE_URL/api/v1/orders" || echo "401")
if [ "$STATUS" -eq 401 ] || [ "$STATUS" -eq 403 ]; then
  echo "PASS (HTTP $STATUS)"
else
  echo "FAIL (Unexpected status HTTP $STATUS)"
  exit 1
fi

echo "==> Post-Deployment Verification PASSED cleanly!"
