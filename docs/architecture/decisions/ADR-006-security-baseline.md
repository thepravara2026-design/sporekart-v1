# ADR-006: Security Baseline Framework

## Status
Accepted

## Context
Application security must be established from Sprint 0 to prevent secret leakage, unauthorized endpoint access, and unhandled exception data leakage.

## Decision
We enforce a **Security Baseline** combining Spring Security, configuration-driven CORS, centralized exception sanitization, and secret prevention rules.

## Security Baseline Rules
1. **Public Endpoints**: Only `/api/v1/health` and `/api/v1/version` are publicly accessible without authentication.
2. **Standardized Error Payload**: Internal exceptions, stack traces, SQL errors, or DB credentials are strictly hidden from HTTP client responses. Technical details are logged internally with correlation IDs.
3. **CORS Restrictions**: Production environments must specify explicit origins (`CORS_ALLOWED_ORIGINS`). Wildcard (`*`) origins in production are prohibited.
4. **Zero-Secret Commit Policy**: No secret file (`.env`), password, API key, or token may ever be committed to git. Automated CI scanning enforces this rule.

## Consequences
- **Positive**: Hardened baseline ready for Sprint 1 JWT authentication.
- **Negative**: Requires environment configuration setup before running applications locally.
