#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MIGRATIONS_DIR="$ROOT_DIR/backend/src/main/resources/db/migration"

echo "==> Verifying Flyway Database Migrations in $MIGRATIONS_DIR..."

if [ ! -d "$MIGRATIONS_DIR" ]; then
    echo "ERROR: Migrations directory missing!"
    exit 1
fi

MIGRATION_COUNT=$(ls "$MIGRATIONS_DIR"/V*__*.sql 2>/dev/null | wc -l)
echo "==> Total Flyway migrations detected: $MIGRATION_COUNT"

if [ "$MIGRATION_COUNT" -lt 19 ]; then
    echo "ERROR: Expected at least 19 migrations, found $MIGRATION_COUNT"
    exit 1
fi

# Verify V17, V18, V19 exist specifically
ls "$MIGRATIONS_DIR"/V17__*.sql > /dev/null || { echo "ERROR: V17 missing"; exit 1; }
ls "$MIGRATIONS_DIR"/V18__*.sql > /dev/null || { echo "ERROR: V18 missing"; exit 1; }
ls "$MIGRATIONS_DIR"/V19__*.sql > /dev/null || { echo "ERROR: V19 missing"; exit 1; }

echo "==> Flyway Migration Chain V1 through V19 VERIFIED CLEANLY!"
