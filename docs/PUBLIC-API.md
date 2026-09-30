# InterviewHQ hq-API Public API

This document is the UI-facing contract for hq-API. It is separate from crawler/admin API documentation.

## Base conventions

All public application endpoints use /api/v1.

- Collection limit defaults to 25 and is bounded to 1–100.
- Cursors are opaque URL-safe values; clients must not parse or construct them.
- Supported sorting is newest and oldest where documented.
- Browser clients talk to hq-API only; DynamoDB keys and persistence structures are never part of the public contract.

### Collection response

~~~json
{
  "items": [],
  "pagination": {
    "limit": 25,
    "nextCursor": null,
    "hasMore": false
  }
}
~~~

### Single-resource response

~~~json
{
  "item": {}
}
~~~

### Error response

~~~json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Interview question not found",
    "details": []
  },
  "timestamp": "2026-09-30T00:00:00Z",
  "path": "/api/v1/questions/123"
}
~~~

Common error codes:
- VALIDATION_ERROR
- INVALID_CURSOR
- INVALID_SORT
- NOT_FOUND
- INTERNAL_ERROR

## Questions

### GET /api/v1/questions

Returns recent interview questions.

Query parameters:

| Parameter | Default | Bounds / values |
|---|---:|---|
| limit | 25 | 1–100 |
| cursor | — | opaque, max 2048 chars |
| sort | newest | newest, oldest |
| company | — | max 200 chars |
| type | — | max 100 chars |

Access behavior:
- Default and type-only requests use materialized question projections.
- Company filtering uses the company question projection.
- Company + type uses the company partition with a bounded residual type filter.
- No table Scan is used.

### GET /api/v1/questions/{id}

Returns one question by numeric ID.

The implementation resolves the numeric ID lookup and then reads the canonical question item. Persistence keys, dedupe hashes, and model metadata are not returned.

## Experiences

### GET /api/v1/experiences

Returns interview experiences ordered by posted time.

Query parameters:

| Parameter | Default | Bounds / values |
|---|---:|---|
| limit | 25 | 1–100 |
| cursor | — | opaque, max 2048 chars |
| sort | newest | newest, oldest |
| company | — | max 200 chars |

Global browsing uses the EINDEX#POSTED Query partition. Company filtering uses EINDEX#COMPANY#{normalizedCompany}. Unknown companies return an empty collection. No table Scan is used.

### GET /api/v1/experiences/{id}

Returns one interview experience by numeric ID using canonical GetItem access.

### GET /api/v1/experiences/{id}/questions

Returns questions belonging to an experience.

Query parameters:
- limit: 1–100, default 25.
- cursor: opaque, max 2048 chars.

The implementation validates that the experience exists, then queries the EXPERIENCE#{id} question adjacency path with a QUESTION# sort-key prefix. It does not perform one read per question.

## Metadata

### GET /api/v1/meta/companies

Returns materialized company metadata.

### GET /api/v1/meta/question-types

Returns materialized question-type metadata.

Both endpoints support:
- limit: 1–100, default 25.
- cursor: opaque, max 2048 chars.

Example item:

~~~json
{
  "name": "Amazon",
  "slug": "amazon"
}
~~~

Metadata uses dedicated DynamoDB Query partitions and does not Scan canonical question or experience data.

## CORS

Allowed origins are configured through API_CORS_ALLOWED_ORIGINS.

Default local development value: http://localhost:3000

Only GET and OPTIONS are enabled for public API CORS configuration, and credentialed wildcard CORS is not enabled.

## Persistence boundary

Public list/detail paths use DynamoDB Query, GetItem, or bounded BatchGetItem access patterns. Raw LastEvaluatedKey values are encoded into opaque cursors.

DynamoDB pk, sk, entityType, projection keys, and raw LastEvaluatedKey values are implementation details.

## Scope

This document covers only public UI-facing hq-API endpoints. It does not document crawler ingestion, crawler operations, admin endpoints, or /dev/** behavior.
