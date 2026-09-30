# InterviewHQ User-Facing API Tasks — Part 2 (Questions listing & company filters)

---

## T007 — GET /api/v1/questions — recent questions list

Status: NOT_STARTED

### Objective
List recent interview questions with cursor pagination.

### API responsibility
Primary feed for “Recent Questions” and home discovery.

### Endpoints
`GET /api/v1/questions`

### Request contract
| Name | Type | Required | Default | Validation |
|------|------|----------|---------|------------|
| `limit` | int | no | 20 | 1–50 |
| `cursor` | string | no | — | opaque |
| `sort` | string | no | `newest` | `newest` \| `oldest` |

### Response contract
```json
{
  "data": [
    {
      "id": "8a91c7...",
      "text": "Design a notification system.",
      "type": "System Design",
      "company": "Amazon",
      "topics": ["Notifications"],
      "postedAt": "2026-09-23T10:20:00Z",
      "sourceName": "Example",
      "sourceUrl": "https://..."
    }
  ],
  "pagination": {
    "limit": 20,
    "nextCursor": "...",
    "hasMore": true
  }
}
```
List items may be a slim projection (subset of full Question DTO) if desired; full fields allowed.

### Error contract
- 400 VALIDATION_ERROR / INVALID_CURSOR / unsupported `sort`
- 500 INTERNAL_ERROR

### DynamoDB access pattern
Query time-ordered index (schema: recent-by-type GSI2 or equivalent recent projection).  
`ScanIndexForward = false` for `newest`, `true` for `oldest`.  
Cursor = opaque encoding of `LastEvaluatedKey`.  
**No Scan.**

### Dependencies
T003, T004, T005

### Acceptance criteria
- Returns page of questions newest-first by default.
- Pagination works across at least two pages when data exists.
- Empty table → `data: []`, `hasMore: false`.

### Testing requirements
- MockMvc with mocked service/repository.
- Unit test for sort direction mapping.

### Implementation notes
- If a pure global “recent” GSI is missing, document and implement against the best existing GSI (e.g. fixed type partition or company-agnostic pattern from schema); do not Scan.

---

## T008 — GET /api/v1/questions?company= — filter by company

Status: NOT_STARTED

### Objective
Filter the questions list by company using an efficient GSI Query.

### API responsibility
Company pages (e.g. `/google-interview-questions` UI equivalent).

### Endpoints
Same collection endpoint with query param:
`GET /api/v1/questions?company={company}`

### Request contract
| Name | Type | Required | Default | Validation |
|------|------|----------|---------|------------|
| `company` | string | no | — | non-blank when present; case-normalise (e.g. trim) |
| `limit` | int | no | 20 | 1–50 |
| `cursor` | string | no | — | opaque |
| `sort` | string | no | `newest` | `newest` \| `oldest` |

When `company` is absent, behaviour is T007 (global recent).

### Response contract
Same envelope as T007; all items must match the requested company.

### Error contract
- 400 if `company` is blank string
- Same pagination/sort errors as T007

### DynamoDB access pattern
Schema: **GSI1 — recent company questions**  
`GSI1PK = COMPANY#{company}` (or equivalent documented key), SK ordered by `postedAt` / question id.  
Query GSI1 with `ScanIndexForward` per `sort`.  
Cursor must include GSI keys used.

### Dependencies
T007

### Acceptance criteria
- Company filter uses Query on GSI1, not filter-expression on a scan.
- Combined with limit/cursor correctly.
- Unknown company → empty page, not 404.

### Testing requirements
- MockMvc: with company, without company, invalid limit.
- Verify repository builds correct GSI key condition.

### Implementation notes
- Normalise company for key construction (consistent casing strategy, e.g. as stored by crawler).
- Do not invent fuzzy company search in this task.

---

## T009 — GET /api/v1/questions?type= — filter by question type

Status: NOT_STARTED

### Objective
Filter questions by `questionType` (e.g. System Design, Coding) via GSI.

### API responsibility
Discovery by question category.

### Endpoints
`GET /api/v1/questions?type={type}`  
(combinable with `company` only if both can be satisfied efficiently; otherwise document mutual exclusion or preferred single dimension).

### Request contract
| Name | Type | Required | Validation |
|------|------|----------|------------|
| `type` | string | no | non-blank when present |
| `company` | string | no | see T008 |
| `limit`, `cursor`, `sort` | | | same as T007 |

**Filter interaction rule:**  
- `company` alone → GSI1  
- `type` alone → GSI2 (schema: recent by type)  
- both together: if no composite index exists, prefer `company` as partition and FilterExpression on type **only when result set is expected small**; otherwise return 400 UNSUPPORTED_FILTER_COMBINATION. Prefer not to FilterExpression on large partitions.

### Response contract
Same as T007; items match filter(s).

### Error contract
- 400 VALIDATION_ERROR / UNSUPPORTED_FILTER_COMBINATION when both filters cannot be served efficiently
- 400 invalid sort/cursor/limit

### DynamoDB access pattern
Schema: **GSI2 — recent questions by type**  
`GSI2PK` ≈ type dimension, SK time-ordered.  
Query only; no Scan.

### Dependencies
T007, T008

### Acceptance criteria
- Type-only list uses GSI2 Query.
- Documented behaviour for company+type combination.
- Empty match → empty page.

### Testing requirements
- Unit/MockMvc for type filter and unsupported combination if enforced.

### Implementation notes
- Allowed type values are free-form strings matching stored `questionType` (no hard enum unless product later freezes one).

---

## T010 — Company and type metadata endpoints (lightweight discovery)

Status: NOT_STARTED

### Objective
Provide minimal discovery lists so the UI can populate company/type filters without scanning all questions.

### API responsibility
Filter facet values for the public UI.

### Endpoints
- `GET /api/v1/meta/companies`
- `GET /api/v1/meta/question-types`

### Request contract
| Name | Type | Default | Validation |
|------|------|---------|------------|
| `limit` | int | 100 | 1–200 |
| `cursor` | string | — | opaque |

### Response contract
```json
{
  "data": [
    { "name": "Google", "slug": "google" },
    { "name": "Amazon", "slug": "amazon" }
  ],
  "pagination": {
    "limit": 100,
    "nextCursor": null,
    "hasMore": false
  }
}
```
Types:
```json
{
  "data": [
    { "name": "System Design" },
    { "name": "Coding" }
  ],
  "pagination": { "limit": 100, "nextCursor": null, "hasMore": false }
}
```

### Error contract
400 on invalid limit/cursor; 500 on internal error.

### DynamoDB access pattern
Prefer a small curated/meta projection if the crawler schema defines one.  
If no meta entity exists, implement as a **bounded** Query against known GSI partitions **or** document a required new access pattern/task dependency (do not unbounded Scan).  
If true facet index is missing, task may return a static starter list from config for MVP and mark follow-up index work in Implementation notes.

### Dependencies
T003, T004

### Acceptance criteria
- Endpoints return deterministic, paginated lists.
- No full table Scan in production path.
- Response hides all DynamoDB keys.

### Testing requirements
- MockMvc 200 shape tests.
- Limit validation.

### Implementation notes
- Product site shows fixed company list; aligning API with that list is acceptable for v1 if DynamoDB facet index is absent.
- Prefer Query-based design when schema supports it.

---

## T011 — Experience list and detail APIs

Status: NOT_STARTED

### Objective
Expose interview experiences (posts) when present in the data model.

### API responsibility
Experience listing and detail for UI that shows “interview stories”.

### Endpoints
- `GET /api/v1/experiences`
- `GET /api/v1/experiences/{id}`

### Request contract
**List**
| Name | Type | Default | Validation |
|------|------|---------|------------|
| `company` | string | — | optional |
| `limit` | int | 20 | 1–50 |
| `cursor` | string | — | opaque |
| `sort` | string | `newest` | `newest` \| `oldest` |

**Detail:** path `id` required non-blank.

### Response contract
List: pagination envelope with Experience DTOs (see T004).  
Detail:
```json
{
  "data": {
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
}
```

### Error contract
- 404 for unknown experience id
- 400 validation/cursor
- 500 internal

### DynamoDB access pattern
- Detail: GetItem on Experience PK/SK as defined in crawler schema (Experience entity).
- List: Query time- or company-oriented GSI for experiences if present; otherwise implement only detail + document list as dependent on a new projection (separate note). **No Scan.**

### Dependencies
T002, T003, T004

### Acceptance criteria
- Detail returns public DTO only.
- List uses cursor pagination when implemented.
- Missing Experience entity support is explicitly handled (clear service error or empty list), not silent Scan.

### Testing requirements
- MockMvc 200/404 for detail.
- List pagination tests if list is implemented.

### Implementation notes
- Schema lists Experience as a logical entity; map fields conservatively.
- If Experience items are not written yet by crawler, APIs still compile and return empty/404 until data exists.

---

## T012 — GET /api/v1/experiences/{id}/questions — questions for an experience

Status: NOT_STARTED

### Objective
Return questions belonging to one experience without N+1 client calls when relationship data exists.

### API responsibility
Nested resource: Experience → Questions.

### Endpoints
`GET /api/v1/experiences/{id}/questions`

### Request contract
| Name | Type | Default | Validation |
|------|------|---------|------------|
| `id` | path | | required |
| `limit` | int | 20 | 1–50 |
| `cursor` | string | | opaque |

### Response contract
```json
{
  "data": [ /* Question DTOs */ ],
  "pagination": {
    "limit": 20,
    "nextCursor": null,
    "hasMore": false
  }
}
```

### Error contract
- 404 if experience does not exist
- 400 invalid cursor/limit
- 500 internal

### DynamoDB access pattern
1. Confirm experience exists (GetItem).
2. Prefer Query on a relationship key (e.g. items under `PK = EXPERIENCE#{id}` with `SK` begins_with `QUESTION#`) **if schema stores adjacency**.
3. Else: use `questionIds` on the experience item and **BatchGetItem** (bounded by page size) — avoid per-id GetItem loops in a loop without batching.
4. No Scan.

### Dependencies
T006, T011

### Acceptance criteria
- No unbounded N+1 GetItem.
- Pagination consistent with T003.
- 404 when experience missing.

### Testing requirements
- MockMvc with fixture experience + questions.
- Verify BatchGet or Query used as designed.

### Implementation notes
- Cap batch size to page limit.
- Order: prefer order stored on experience if available; otherwise stable by id.
