# InterviewHQ hq-API — User-Facing API Implementation Tasks

These tasks are the source of truth for the **InterviewHQ UI → hq-API** contract.

**Initial planning session:** task documentation only. Implementation is now being completed incrementally.

## Grok — Start Here

Repository: https://github.com/code-with-rj1399/ih-api  
Branch: `ih-apis-user-facing`

Start by reading these task files and inspecting the repository. Pick the first unfinished (`- [ ]`) task whose dependencies are satisfied, implement only that task, run its tests, mark it complete, commit it, and push to `ih-apis-user-facing`. After pushing, immediately continue with the next unfinished dependency-satisfied task. Never combine multiple tasks into one commit.

## Status legend

- `[x]` Verified complete.
- `[~]` Implemented; CI verification is pending.
- `[!]` Blocked by a missing upstream/data access path.
- `[ ]` Not started.

## Package structure convention

Business features use feature-oriented packages (`question`, `experience`, `metadata`, `system`). Shared contracts live under `common`; DynamoDB implementation details live under `infrastructure/dynamodb`. Keep the dependency direction `controller → service → repository interface → infrastructure implementation`. Tests should mirror the production package structure. Public DTOs must never depend on DynamoDB types.

## How to use these files

Each session must:

1. Read the task files.
2. Implement the first unfinished task in dependency order.
3. Run that task's tests.
4. Mark the task complete (`- [x] Status`) and update **Progress** only when acceptance criteria and tests pass.
5. Commit with a focused message.
6. `git push origin ih-apis-user-facing`.
7. Immediately continue with the next unfinished dependency-satisfied task.

Never mark a task complete unless its acceptance criteria and tests pass. If blocked, document the blocker in the task file.

## Repository snapshot

Inspected current `ih-api` branch against `master`.

| Area | What exists today |
|---|---|
| Service | Spring Boot API service under `ai.interviewhq.api` |
| Persistence | DynamoDB client/configuration foundation |
| Existing endpoint | `/api/hello` |
| Production boundary | InterviewHQ UI → hq-API → DynamoDB |
| Public API | `/api/v1/**` established namespace |
| Scope | API-only implementation consumed by InterviewHQ UI |

Do not assume public indexes/projections exist without verifying the actual repository and crawler schema.

## Locked architecture decisions

1. hq-API is **API-only**. No Next.js, React, HTML, static UI, or frontend state.
2. Public application APIs use `/api/v1/**`.
3. Browser clients never access DynamoDB directly.
4. Public DTOs hide DynamoDB `pk`, `sk`, `entityType`, projection keys, and persistence internals.
5. AWS DynamoDB remains the database.
6. User-facing reads use `Query`, `GetItem`, or bounded `BatchGetItem`; no Scan for normal traffic.
7. Pagination uses opaque cursors; raw DynamoDB keys are never exposed.
8. Default collection page size is 25; maximum is 100 unless explicitly justified.
9. Follow the crawler's documented `InterviewExperience` and `InterviewQuestion` schema; do not invent unsupported fields.
10. Public APIs are read-only for this feature.
11. Crawler→API ingestion, crawler operations, admin APIs, and `/dev/**` are out of scope.
12. Search/filter behavior must reflect actual DynamoDB access patterns.
13. Missing access paths become explicit projection/index tasks before dependent API tasks.
14. Keep contracts stable and implementation-friendly for a future Next.js consumer.

## Source-of-truth data model

### InterviewExperience

Fields:

`id, sourceId, crawlRunId, sourcePlatform, title, summary, author, postedAt, originalPostUrl, company, role, level, location, candidateYoE, questionCount, dedupeHash, createdAt`

Canonical item:

```
PK = EXPERIENCE#{experienceId}
SK = ENTITY
entityType = InterviewExperience
```

### InterviewQuestion

Fields:

`id, experienceId, problemUrl, questionTypes[], difficulty, questionText, questionDescription, candidateApproach, confidence, questionParticularity, modelName, dedupeHash, extractedAt, createdAt`

Canonical item:

```
PK = EXPERIENCE#{experienceId}
SK = QUESTION#{questionDedupeHash}
entityType = InterviewQuestion
```

Question ID lookup:

```
PK = QUESTION_ID#{questionId}
SK = LOOKUP
entityType = QuestionIdLookup
```

### Experience/question relationship

```
EXPERIENCE#123
  +-- ENTITY
  +-- QUESTION#abc
  +-- QUESTION#def
```

## Public API conventions

Collection response:

```json
{
  "items": [],
  "pagination": {
    "limit": 25,
    "nextCursor": null,
    "hasMore": false
  }
}
```

Single resource response:

```json
{
  "item": {}
}
```

Error response:

```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Interview question not found",
    "details": []
  },
  "timestamp": "2026-09-30T00:00:00Z",
  "path": "/api/v1/questions/123"
}
```

## Testing conventions

- JUnit 5 / Spring Boot test stack.
- Prefer MockMvc for controller contracts.
- Mock DynamoDB client/repositories for repository/service tests.
- CI must not require real AWS credentials.
- No new test infrastructure unless a later task proves it necessary.

## Progress

- Total: 24
- Verified: 0
- Implemented, CI pending: 20
- Blocked: 3
- Not started: 1

Planning commit: task documentation only.

## Task list

## T001 — Public API conventions and package foundation

- [~] Status**Goal**

Establish package layout and base conventions for the public API namespace.

**API responsibility**

All future user-facing controllers use one coherent versioned API structure.

**Endpoints**

None; foundation only.

**Dependencies**

None.

**Acceptance criteria**

- [x] Public controllers consistently use `/api/v1`.
- [x] Existing `/api/hello` behavior remains unchanged.
- [x] No crawler, ingestion, admin, or `/dev` endpoint is modified.

**Tests**

- [x] Existing test suite passes.
- [x] Namespace/controller smoke coverage exists.

**Implementation notes**

Use repository conventions under `ai.interviewhq.api`. Do not introduce unrelated infrastructure.

---

## T002 — Common DTO envelope and error handling

- [~] Status**Goal**

Introduce shared public response/error models and exception handling.

**API responsibility**

Provide consistent collection, single-resource, validation, not-found, and internal-error responses.

**Endpoints**

Cross-cutting for `/api/v1/**`.

**Dependencies**

T001.

**Acceptance criteria**

- [x] Shared public error envelope exists.
- [x] Validation and not-found errors map consistently.
- [x] Unexpected exceptions do not expose DynamoDB internals.

**Tests**

- [x] MockMvc coverage for 400, 404, and 500 mappings.
- [x] JSON shape assertions.

**Implementation notes**

Keep the contract small and stable; do not expose stack traces.

---

## T003 — Opaque cursor pagination

- [~] Status**Goal**

Implement one cursor codec and pagination model for collection APIs.

**API responsibility**

Provides bounded pagination without exposing DynamoDB keys.

**Endpoints**

Applies to all public collection endpoints.

**Dependencies**

T002.

**Acceptance criteria**

- [x] Cursor round-trips required DynamoDB key data.
- [x] Malformed cursor returns a documented 400.
- [x] Default limit is 25.
- [x] Maximum limit is 100.
- [x] Raw DynamoDB `LastEvaluatedKey` is never returned.

**Tests**

- [x] Cursor encode/decode round-trip.
- [x] Invalid cursor tests.
- [x] Page-size boundary tests.

**Implementation notes**

Prefer URL-safe opaque Base64 JSON. Cursor contents are implementation details.

---

## T004 — Public Question DTO and mapper

- [~] Status**Goal**

Define the public question representation and mapper from persisted data.

**API responsibility**

Provides the stable Question object consumed by the InterviewHQ UI.

**Endpoints**

Consumed by question endpoints.

**Dependencies**

T001, T002.

**Acceptance criteria**

- [ ] DTO contains only supported public fields.
- [ ] DynamoDB keys/storage fields are hidden.
- [ ] Optional fields are null-safe.
- [ ] `questionTypes` is represented consistently.

**Tests**

- [ ] Mapper tests with representative persisted items.
- [ ] Optional-field tests.

**Implementation notes**

Use the crawler schema as the field source. Do not invent topic fields absent from persisted data.

---

## T005 — Public Experience DTO and mapper

- [~] Status**Goal**

Define the public interview-experience representation and mapper.

**API responsibility**

Provides the stable Experience object consumed by the InterviewHQ UI.

**Endpoints**

Consumed by experience endpoints.

**Dependencies**

T001, T002.

**Acceptance criteria**

- [ ] DTO maps documented experience fields.
- [ ] Internal storage fields are hidden.
- [ ] Optional fields are null-safe.

**Tests**

- [ ] Mapper tests.
- [ ] Missing optional-field tests.

**Implementation notes**

Preserve useful provenance such as source URL/platform, dates, company, role, and location.

---

## T006 — Question repository access paths

- [~] Status**Goal**

Implement repository methods for question detail and experience-scoped question reads.

**API responsibility**

Creates the DynamoDB read layer used by public question APIs.

**Endpoints**

No HTTP endpoint in this task.

**Dependencies**

T003, T004.

**Acceptance criteria**

- [ ] Question detail follows the documented question ID lookup and canonical item path.
- [ ] Experience questions use Query on `EXPERIENCE#{id}` with `QUESTION#` children.
- [ ] No Scan is used.
- [ ] Repository returns pagination metadata.

**Tests**

- [ ] Mock DynamoDB request tests.
- [ ] Key-condition and cursor handling tests.

**Implementation notes**

Verify the actual repository/schema before coding; do not guess key names.

---

## T007 — Experience repository access paths

- [~] Status**Goal**

Implement repository methods for experience detail and required list reads.

**API responsibility**

Creates the DynamoDB read layer used by public experience APIs.

**Endpoints**

No HTTP endpoint in this task.

**Dependencies**

T003, T005.

**Acceptance criteria**

- [ ] Experience detail uses canonical GetItem.
- [ ] List operations use a verified Query/projection path.
- [ ] No Scan is used.
- [ ] Pagination is supported.

**Tests**

- [ ] GetItem/Query request tests.
- [ ] Pagination tests.

**Implementation notes**

If a required experience list access path is absent, create a dedicated projection task before the dependent API task.

---
