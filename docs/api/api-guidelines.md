# API Standards & Design Guidelines — Sporekart v3.0

## URL Structure
All public APIs must use the `/api/v1` prefix.

Examples:
- `GET /api/v1/health`
- `GET /api/v1/version`

## Standard Success Response
```json
{
  "success": true,
  "data": { ... }
}
```

## Standard Error Response
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error explanation",
    "timestamp": "2026-08-14T20:00:00Z",
    "path": "/api/v1/endpoint"
  }
}
```

## HTTP Status Codes
- `200 OK`: Successful operation.
- `400 Bad Request`: Client validation error or malformed payload.
- `401 Unauthorized`: Authentication required.
- `403 Forbidden`: Insufficient permissions.
- `404 Not Found`: Resource does not exist.
- `500 Internal Server Error`: Server error (details logged, stack trace hidden from client).
