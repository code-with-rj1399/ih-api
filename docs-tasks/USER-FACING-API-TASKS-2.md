# InterviewHQ hq-API — User-Facing API Implementation Tasks — Part 2

Continue from Part 1. These tasks are part of the same implementation source of truth.

## Progress

- Total: 24
- Verified: 0
- Implemented, CI pending: 7 (T008–T014 except blocked T012)
- Blocked: T012
- Not started: T015+

## Task list

## T008 — Recent questions API

- [~] Status**Goal**

Expose the primary recent-questions collection.

**API responsibility**

Allows the InterviewHQ UI to retrieve recent interview questions with stable cursor pagination.

**Endpoints**

`GET /api/v1/questions`

**Request contract**

| Parameter | Type | Default | Notes |
|---|---|---|---|
| `limit` | integer | 25 | 1–100 |
| `cursor` | string | null | opaque |
| `sort` | string | newest | supported values documented by T017 |

**Response contract**

Use the common collection envelope from Part 1. List items should use a bounded/slim Question representation.

**Error contract**

400 for invalid limit, cursor, or sort. 500 for unexpected failures.

**DynamoDB access pattern**

Use a verified recent-question Query/projection. `ScanIndexForward=false` for newest when supported. Never Scan.

**Dependencies**

T003, T004, T006.

**Acceptance criteria**

- [ ] Newest is the default order.
- [ ] Cursor pagination works across pages.
- [ ] Empty result is a successful empty collection.
- [ ] No Scan is used.

**Tests**

- [ ] MockMvc happy path.
- [ ] Pagination test.
- [ ] Empty-result test.
- [ ] Repository Query test.

**Implementation notes**

Do not fabricate a recent index. Verify the crawler schema/repository and create an explicit access-path task if required.

---

## T009 — Question detail API

- [~] Status**Goal**

Expose one interview question by id.

**API responsibility**

Allows the UI to open a question detail view.

**Endpoints**

`GET /api/v1/questions/{id}`

**Request contract**

`id` is required and validated before any DynamoDB read.

**Response contract**

```json
{
  "item": {
    "id": "123",
    "experienceId": "exp-1",
    "questionText": "Design a notification system.",
    "questionDescription": "...",
    "questionTypes": ["System Design"],
    "difficulty": "Medium",
    "candidateApproach": null,
    "confidence": 0.92,
    "questionParticularity": "SPECIFIC",
    "problemUrl": null,
    "extractedAt": "2026-09-30T08:00:00Z",
    "createdAt": "2026-09-30T08:00:00Z"
  }
}
```

**Error contract**

404 when the question does not exist; 400 for invalid id; 500 for unexpected failures.

**DynamoDB access pattern**

Question ID lookup followed by canonical question read as documented by crawler schema.

**Dependencies**

T002, T004, T006.

**Acceptance criteria**

- [ ] Public Question DTO is returned.
- [ ] Missing resource is 404.
- [ ] DynamoDB keys and internal storage fields are hidden.

**Tests**

- [ ] MockMvc 200/404.
- [ ] Invalid-id test.
- [ ] Service/repository test.

**Implementation notes**

Do not expose modelName, dedupeHash, or other internal provenance unless explicitly approved as a public field.

---

## T010 — Experience questions API

- [~] Status**Goal**

Expose questions belonging to one interview experience.

**API responsibility**

Allows a UI experience page to load its questions as one API resource.

**Endpoints**

`GET /api/v1/experiences/{id}/questions`

**Request contract**

| Parameter | Type | Default | Notes |
|---|---|---|---|
| `id` | string | — | required |
| `limit` | integer | 25 | 1–100 |
| `cursor` | string | null | opaque |

**Response contract**

Common collection envelope with Question DTOs.

**Error contract**

404 for missing experience when existence is checked; 400 for invalid input/cursor; 500 for unexpected failures.

**DynamoDB access pattern**

Query `PK=EXPERIENCE#{id}` with `SK begins_with QUESTION#`. Do not issue one GetItem per question.

**Dependencies**

T003, T004, T006, T007.

**Acceptance criteria**

- [ ] Query uses the experience partition.
- [ ] Pagination is cursor-based.
- [ ] No unbounded N+1 reads.
- [ ] Resource existence behavior is explicit.

**Tests**

- [ ] MockMvc pagination tests.
- [ ] Verify Query key condition.
- [ ] Empty-result test.

**Implementation notes**

Prefer the documented adjacency pattern over client-side question ID iteration.

---

## T011 — Experience detail API

- [~] Status**Goal**

Expose a single interview experience.

**API responsibility**

Allows the UI to open an interview experience/story.

**Endpoints**

`GET /api/v1/experiences/{id}`

**Request contract**

Required path `id`.

**Response contract**

```json
{
  "item": {
    "id": "exp-1",
    "title": "SDE II Interview Experience",
    "company": "Amazon",
    "role": "SDE II",
    "level": "L5",
    "location": "Seattle, WA",
    "sourcePlatform": "example",
    "originalPostUrl": "https://example.com/post/1",
    "postedAt": "2026-09-28T00:00:00Z",
    "summary": "...",
    "author": null,
    "questionCount": 4
  }
}
```

**Error contract**

404 for missing experience; 400 for invalid id; 500 for unexpected failures.

**DynamoDB access pattern**

GetItem on `PK=EXPERIENCE#{experienceId}`, `SK=ENTITY`.

**Dependencies**

T002, T005, T007.

**Acceptance criteria**

- [ ] Public Experience DTO is returned.
- [ ] Missing resource is 404.
- [ ] Internal keys/hashes are hidden.

**Tests**

- [ ] MockMvc 200/404.
- [ ] Mapper/service tests.

**Implementation notes**

Do not eagerly load all questions unless a separate endpoint contract requires it.

---

## T012 — Experience list access path and API

- [!] Status**Goal**

Expose a paginated collection of interview experiences.

**API responsibility**

Allows the UI to browse interview stories/experiences.

**Endpoints**

`GET /api/v1/experiences`

**Request contract**

| Parameter | Type | Default | Notes |
|---|---|---|---|
| `limit` | integer | 25 | 1–100 |
| `cursor` | string | null | opaque |
| `sort` | string | newest | documented sort |
| `company` | string | null | optional |

**Response contract**

Common collection envelope with slim Experience DTOs.

**Error contract**

400 for invalid input/cursor/sort; 500 for unexpected failures.

**DynamoDB access pattern**

Use only a verified time/company Query projection. If one is missing, add the required projection/access-path task before implementing the controller. No Scan.

**Dependencies**

T003, T005, T007.

**Acceptance criteria**

- [ ] List is cursor paginated.
- [ ] Ordering is deterministic.
- [ ] No Scan is used.
- [ ] List payload is bounded.

**Tests**

- [ ] MockMvc list test.
- [ ] Repository Query test.
- [ ] Cursor pagination test.

**Implementation notes**

The crawler schema currently has canonical experience items and a run-to-experience index, but no global time/company experience-list projection. Do not use Scan. This task is blocked until the crawler/database write path provides a Queryable global experience-list projection.

---

## T013 — Company-filtered question API

- [~] Status**Goal**

Support company filtering on the question collection.

**API responsibility**

Allows the UI to browse questions for a company.

**Endpoints**

`GET /api/v1/questions?company={company}`

**Request contract**

`company` is optional, trimmed, and bounded in length. Existing pagination/sort parameters remain supported.

**Response contract**

Common collection envelope; every returned item matches the company filter.

**Error contract**

400 for invalid company input, cursor, limit, or sort; 500 for unexpected failures.

**DynamoDB access pattern**

Use the documented company question projection/query path. Never perform a table Scan to satisfy the filter.

**Dependencies**

T008.

**Acceptance criteria**

- [ ] Company filter selects an efficient Query partition.
- [ ] Unknown company returns an empty page.
- [ ] Pagination works with the filter.
- [ ] No Scan is introduced.

**Tests**

- [ ] MockMvc company-filter test.
- [ ] Repository partition-key test.
- [ ] Pagination with company filter.

**Implementation notes**

Follow the crawler's stored company normalization exactly; do not add fuzzy matching.

---

## T014 — Question-type filter API

- [~] Status**Goal**

Support question-type filtering on the question collection.

**API responsibility**

Allows the UI to browse questions by category such as Coding or System Design.

**Endpoints**

`GET /api/v1/questions?type={type}`

**Request contract**

`type` is optional, trimmed, bounded, and must match a supported stored taxonomy when an explicit taxonomy is enforced.

**Response contract**

Common collection envelope; returned items match the type.

**Error contract**

400 for invalid type/cursor/limit/sort or unsupported expensive combinations; 500 for unexpected failures.

**DynamoDB access pattern**

Use the documented question-type Query/projection path. Do not Scan.

**Dependencies**

T008.

**Acceptance criteria**

- [ ] Type-only filter uses Query.
- [ ] Empty match returns an empty collection.
- [ ] Pagination remains opaque and correct.
- [ ] Expensive combinations are explicit instead of silently scanning.

**Tests**

- [ ] MockMvc type-filter test.
- [ ] Repository key-condition test.
- [ ] Combination behavior test.

**Implementation notes**

Use canonical question-type values from crawler documentation.

---

## Definition of Done

For this part, all applicable tasks are complete only when their acceptance criteria and tests pass, their status is checked, and the implementation follows the locked architecture from Part 1.
