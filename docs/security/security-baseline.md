# Security Baseline Document — Sporekart v3.0

## Status
SECURITY BASELINE VERIFIED

## Key Security Measures

1. **Endpoint Protection**: Only `/api/v1/health` and `/api/v1/version` are public. All future business endpoints are protected by default.
2. **Error Sanitization**: Server-side exception traces, database credentials, SQL syntax, or internal file paths are NEVER returned in HTTP responses.
3. **CORS Control**: Access is restricted via configuration (`CORS_ALLOWED_ORIGINS`).
4. **Secret Protection**: `.env` files are added to `.gitignore`. CI audit scans for committed secret strings.
5. **Sensitive Information Logging**: Logging rules prohibit dumping JWT tokens, passwords, database passwords, or card numbers.
