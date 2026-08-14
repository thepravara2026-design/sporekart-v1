# Environment Strategy Document — Sporekart v3.0

## Environments

1. **LOCAL**: Developer environment using local H2/PostgreSQL, local Spring Boot, and Vite dev server.
2. **DEVELOPMENT**: Shared development environment connected to Development Supabase database.
3. **STAGING**: Pre-production staging environment matching production Supabase & container architecture.
4. **PRODUCTION**: Production environment.

## Promotion Flow

```
feature/*
   │
   ▼
develop ──────► Development Environment
   │
   ▼
release/* ────► Staging Environment
   │
   ▼
main ─────────► Production Environment
```

Note: Git staging is distinct from deployment staging environment.
