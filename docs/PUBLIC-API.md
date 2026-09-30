# InterviewHQ hq-API Public API

This document is the UI-facing contract for hq-API. It is intentionally separate from crawler/admin API documentation.

## Base

All public application endpoints use: `/api/v1`

The browser talks to hq-API only. DynamoDB keys and persistence structures are never part of the public contract.

## Common collection response

```json
{ "items": [], "pagination": { "limit": 25, "nextCursor": null, "hasMore": false } }
```

`limit` defaults to 25 and is bounded to 1–100. `cursor` is an opaque URL-safe token.

## Common single-resource response

```json
{"item": {}}
```

## Errors

```json
{"error":{"code":"NOT_FOUND","message":"Interview question not found","details":[]},"timestamp":"2026-09-30T00:00:00Z","path":"/api/v1/questions/123"}
```

Common codes currently implemented:
- `VALIDATION_ERROR`
- `INVALID_CURSOR`
- `INVALID_SORT`
- `NOT_FOUND`
- `INTERNAL_ERROR`

## Health

### GET /api/v1/health

Returns service health information.

## Questions

### GET /api/v1/questions

Returns a bounded question collection.

Query parameters:

| Parameter | Values |
|---|---|
| `limit` | 1–100, default 25 |
| `cursor` | opaque pagination token |
| `sort` | `newest` or `oldest` |
| `company` | optional company name |
| `type` | optional canonical question type |

The question list uses DynamoDB materialized question projections. It does not use a table Scan.

When both `company` and `type` are supplied, the company projection is queried and the type is applied as a bounded DynamoDB filter.

### GET /api/v1/questions/{id}

Returns one question by numeric id.

The implementation resolves the numeric ID lookup and then reads the canonical question item. Persistence keys, dedupe hashes, and model metadata are not returned.

## Experiences

### GET /api/v1/experiences/{id}

Returns one interview experience by numeric id.

The canonical experience item is read directly with GetItem.

### GET /api/v1/experiences/{id}/questions

Returns questions belonging to an experience using the `EXPERIENCE#{id}` item collection and a `QUESTION#` sort-key prefix.

Pagination is opaque and bounded.

## CORS

Allowed origins are configured through `API_CORS_ALLOWED_ORIGINS`.

Default local development value: `http://localhost:3000`

Only GET and OPTIONS are enabled for the public API CORS configuration, and credentialed wildcard CORS is not enabled.

## Persistence boundary

The public API implementation uses:
- DynamoDB GetItem for canonical detail resources.
- DynamoDB Query for question projections and experience question collections.
- Opaque cursors for LastEvaluatedKey state.
- No table Scan for public list paths.

DynamoDB `pk`, `sk`, `entityType`, projection keys, and raw LastEvaluatedKey values are implementation details.

## Deliberately unavailable until the crawler provides access paths

The following APIs are not exposed yet because the current crawler schema does not provide the required efficient global access paths:
- `GET /api/v1/experiences`
- `GET /api/v1/experiences?company=...`
- `GET /api/v1/meta/companies`
- `GET /api/v1/meta/question-types`

The hq-API must not implement these by scanning canonical experience/question data. The required materialized projections should be added to the crawler/database write path first, then these endpoints can be implemented against Query access patterns.