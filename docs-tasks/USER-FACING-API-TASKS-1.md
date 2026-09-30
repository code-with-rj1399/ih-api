# InterviewHQ User-Facing API Tasks — Part 1 (Foundation)

These tasks define the public API surface that the InterviewHQ UI (and a static debugger page) will consume.  
Scope is **read-only user-facing APIs only**. No crawler, ingestion, admin, or `/dev/**` endpoints.

Namespace: `/api/v1/...`  
Table (configurable): `interviewhq-dev` / production equivalent  
Existing foundation: Spring Boot 3.5, DynamoDB client bean, `/api/hello`, validation starter.

---

## T001 — Public API conventions and package foundation

Status: NOT_STARTED

### Objective
Establish package layout, API versioning convention, and shared constants so all subsequent user-facing endpoints follow one coherent structure.

### API responsibility
Defines the public API namespace and packaging that every later endpoint will live under.

### Endpoints
None (foundation only).

### Request contract
N/A

### Response contract
N/A

### Error contract
N/A

### DynamoDB access pattern
N/A

### Dependencies
None

### Acceptance criteria
- Packages exist under `ai.interviewhq.api` for: `controller.v1`, `dto`, `service`, `repository`, `exception`, `config` (extend existing config only as needed).
- Public controllers are annotated under `/api/v1`.
- No change to existing `/api/hello` behaviour.
- README briefly documents the `/api/v1` convention.

### Testing requirements
- Existing `HelloControllerTest` still passes.
- Smoke compile/test of empty package structure.

### Implementation notes
- Prefer `ai.interviewhq.api.controller.v1` for all public REST controllers.
- Do not introduce security or OpenAPI yet (later tasks).
- Keep DynamoDB table name driven by `aws.dynamodb.table` property.

---

## T002 — Common error response and exception handling

Status: NOT_STARTED

### Objective
Introduce one consistent public error JSON shape and a global exception handler for validation and not-found cases.

### API responsibility
All public APIs return the same error envelope on failure.

### Endpoints
N/A (cross-cutting)

### Request contract
N/A

### Response contract
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "pageSize must be between 1 and 50",
    "details": [
      { "field": "pageSize", "reason": "must be <= 50" }
    ]
  },
  "timestamp": "2026-09-30T08:00:00Z",
  "path": "/api/v1/questions"
}
```

### Error contract
| Status | code | When |
|--------|------|------|
| 400 | VALIDATION_ERROR | Query params/body fail validation |
| 400 | INVALID_CURSOR | Cursor cannot be decoded |
| 404 | NOT_FOUND | Resource id not found |
| 500 | INTERNAL_ERROR | Unexpected failure (no internal details leaked) |

### DynamoDB access pattern
N/A

### Dependencies
T001

### Acceptance criteria
- `@ControllerAdvice` maps `MethodArgumentNotValidException`, custom `NotFoundException`, `InvalidCursorException` to the envelope above.
- 500 responses never expose stack traces or DynamoDB exception messages to clients.
- Unit test for at least one 400 and one 404 mapping.

### Testing requirements
- MockMvc tests asserting status + JSON error shape.
- No change to `/api/hello` success path.

### Implementation notes
- Place error DTO under `dto.ErrorResponse`.
- Use RFC 7807-inspired fields only as documented; keep one shape for the whole public API.

---

## T003 — Cursor pagination model

Status: NOT_STARTED

### Objective
Define a single opaque-cursor pagination contract used by all list endpoints.

### API responsibility
Consistent list pagination across questions, experiences, and filtered lists.

### Endpoints
Applies to all `GET` collection endpoints.

### Request contract
Query parameters (all optional unless noted):
| Name | Type | Default | Validation |
|------|------|---------|------------|
| `limit` | integer | 20 | 1–50 inclusive |
| `cursor` | string | null | opaque; reject malformed |

### Response contract
```json
{
  "data": [ /* items */ ],
  "pagination": {
    "limit": 20,
    "nextCursor": "eyJwayI6IlFVRVNUSU9OIy4uLiIsInNrIjoiRU5USVRZIn0",
    "hasMore": true
  }
}
```
When no more pages: `nextCursor` is `null` and `hasMore` is `false`.

### Error contract
- 400 INVALID_CURSOR if cursor is present but invalid Base64/JSON or points to unknown keys.
- 400 VALIDATION_ERROR if `limit` out of range.

### DynamoDB access pattern
- Encode `LastEvaluatedKey` (PK/SK and any GSI keys used) into an opaque Base64URL JSON cursor.
- Never return raw DynamoDB keys to clients.
- On next request, decode cursor → `ExclusiveStartKey`.

### Dependencies
T002

### Acceptance criteria
- Shared `PaginationRequest` / `PaginationResponse` DTOs.
- Utility to encode/decode cursor; invalid decode throws `InvalidCursorException`.
- Default limit 20, max 50 documented and enforced.

### Testing requirements
- Unit tests for encode/decode round-trip and invalid cursor.
- Bound checks for limit.

### Implementation notes
- Prefer Base64URL of compact JSON containing only the keys needed for the query.
- Empty result: `data: []`, `hasMore: false`, `nextCursor: null`.

---

## T004 — Domain models and public DTOs for Question and Experience

Status: NOT_STARTED

### Objective
Define internal DynamoDB item shapes (read-only) and public API DTOs that hide PK/SK/entityType/GSI attributes.

### API responsibility
Clean JSON representations the UI can consume without knowing DynamoDB internals.

### Endpoints
N/A (models only)

### Request contract
N/A

### Response contract
**Question (public)**
```json
{
  "id": "8a91c7...",
  "text": "Design a notification system.",
  "description": "Design a notification system that can deliver notifications reliably...",
  "type": "System Design",
  "topics": ["Notifications", "Distributed Systems"],
  "company": "Amazon",
  "role": null,
  "sourceUrl": "https://example.com/interview/123",
  "problemUrl": null,
  "sourceName": "Example",
  "postedAt": "2026-09-23T10:20:00Z",
  "crawledAt": "2026-09-23T12:00:00Z",
  "confidence": 0.92,
  "experienceId": null
}
```

**Experience (public)**
```json
{
  "id": "exp-...",
  "title": "Amazon SDE II Onsite",
  "company": "Amazon",
  "role": "SDE II",
  "level": "L5",
  "location": null,
  "sourceUrl": "https://...",
  "sourceName": "Example",
  "postedAt": "2026-09-20T00:00:00Z",
  "summary": null,
  "questionIds": ["8a91c7..."]
}
```

### Error contract
N/A

### DynamoDB access pattern
Map from documented schema:
- Question: `PK = QUESTION#{questionId}`, `SK = ENTITY`, attributes as in crawler schema (`questionText`, `questionType`, `company`, `postedAt`, etc.).
- Experience: follow crawler Experience entity when present; if Experience is not yet fully materialised in the table, DTOs still define the public shape for later tasks.

### Dependencies
T001

### Acceptance criteria
- Public DTOs never include `PK`, `SK`, `entityType`, `GSI*`, or internal hashes beyond the public `id`.
- Mapper(s) from AttributeValue map / internal record → public DTO.
- Jackson serialises dates as ISO-8601 UTC (already configured).

### Testing requirements
- Unit tests for mapper with sample DynamoDB attribute maps.
- Null-safe mapping for optional fields.

### Implementation notes
- Align field names with product UI needs (company, type, postedAt, topics).
- `id` for questions = `questionId` (dedupe hash) from schema.

---

## T005 — Question repository: GetItem by id and Query recent via GSI

Status: NOT_STARTED

### Objective
Implement DynamoDB repository methods required for single-question and recent-questions reads using Query (not Scan).

### API responsibility
Efficient data access for question APIs.

### Endpoints
Supports later controllers; no HTTP yet.

### Request contract
N/A

### Response contract
N/A

### Error contract
N/A

### DynamoDB access pattern
| Method | Pattern |
|--------|---------|
| `getById(questionId)` | `GetItem` PK=`QUESTION#{id}` SK=`ENTITY` |
| `listRecent(limit, exclusiveStartKey)` | Query on GSI that supports time order (prefer GSI with SK containing `postedAt` or equivalent recent index from schema). If only company/type GSIs exist, use a dedicated “recent” access path documented in schema (GSI2-style or static partition for recent). **Must not Scan.** |
| Pagination | Pass `ExclusiveStartKey` / return `LastEvaluatedKey` |

From crawler schema access patterns:
- Get a question: `PK = QUESTION#{id}`
- Get recent questions by type: GSI2
- Get recent company questions: GSI1

### Dependencies
T003, T004

### Acceptance criteria
- Repository uses injected `DynamoDbClient` and configured table name.
- `getById` returns empty Optional when missing.
- List methods return page of items + optional last key.
- No full-table Scan for list paths.

### Testing requirements
- Unit tests with mocked DynamoDbClient (or local DynamoDB integration if available in CI).
- Verify KeyConditionExpression / GetItem request shapes.

### Implementation notes
- Table name from `aws.dynamodb.table`.
- Keep repository free of HTTP/DTO concerns; return internal models or maps that T004 mappers consume.

---

## T006 — GET /api/v1/questions/{id} — question detail

Status: NOT_STARTED

### Objective
Expose a single public question by id.

### API responsibility
Question detail for UI and debugger.

### Endpoints
`GET /api/v1/questions/{id}`

### Request contract
| Param | In | Type | Required |
|-------|-----|------|----------|
| `id` | path | string | yes (non-blank) |

### Response contract
200:
```json
{
  "data": {
    "id": "8a91c7...",
    "text": "Design a notification system.",
    "description": "...",
    "type": "System Design",
    "topics": ["Notifications"],
    "company": "Amazon",
    "role": null,
    "sourceUrl": "https://...",
    "problemUrl": null,
    "sourceName": "Example",
    "postedAt": "2026-09-23T10:20:00Z",
    "crawledAt": "2026-09-23T12:00:00Z",
    "confidence": 0.92,
    "experienceId": null
  }
}
```

### Error contract
- 400 if `id` blank
- 404 NOT_FOUND if question missing
- 500 INTERNAL_ERROR on unexpected failures

### DynamoDB access pattern
`GetItem` on `PK = QUESTION#{id}`, `SK = ENTITY`.

### Dependencies
T002, T004, T005

### Acceptance criteria
- Controller → service → repository → public DTO.
- 404 when item absent.
- No internal DynamoDB attributes in JSON.

### Testing requirements
- MockMvc: 200 with fixture, 404, blank id → 400.
- Service unit test.

### Implementation notes
- Path prefix `/api/v1`.
- Reuse error envelope from T002.
