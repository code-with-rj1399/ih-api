# InterviewHQ hq-API — User-Facing API Implementation Tasks — Part 3

Continue from Parts 1–2. These tasks remain API-only and are intended to be implemented sequentially.

## Progress

- Total: 24
- Verified: 0
- Implemented, CI pending: 5 (T017–T021)
- Blocked: T015–T016
- Not started: T022+

## Task list

## T015 — Experience company filter API

- [!] Status

**Goal**

Support company filtering on the experience collection when DynamoDB provides an efficient access path.

**API responsibility**

Allows the UI to browse interview experiences for a company.

**Endpoints**

`GET /api/v1/experiences?company={company}`

**Request contract**

`company` is optional, trimmed, and length-bounded. Existing limit/cursor/sort parameters remain supported.

**Response contract**

Common collection envelope with only matching experiences.

**Error contract**

400 for invalid filter/cursor/limit/sort; 500 for unexpected failures.

**DynamoDB access pattern**

Use a verified company experience Query/projection. Never Scan.

**Dependencies**

T012.

**Acceptance criteria**

- [x] Company filter uses a Query access path.
- [x] Unknown company returns an empty collection.
- [x] Pagination works with the filter.
- [x] No Scan is used.

**Tests**

- [ ] MockMvc company-filter test.
- [ ] Repository Query test.
- [ ] Pagination test.

**Implementation notes**

The crawler now provides EINDEX#COMPANY#{normalizedCompany}; hq-API selects that Query partition and never falls back to Scan.

---

## T016 — Public metadata/facet APIs

- [!] Status

**Goal**

Expose bounded company and question-type metadata needed by UI filters.

**API responsibility**

Lets the UI populate filter selectors without scanning large question/experience collections.

**Endpoints**

`GET /api/v1/meta/companies`  
`GET /api/v1/meta/question-types`

**Request contract**

| Parameter | Type | Default | Notes |
|---|---|---|---|
| `limit` | integer | 100 | bounded |
| `cursor` | string | null | opaque |

**Response contract**

```json
{
  "items": [
    { "name": "Amazon", "slug": "amazon" }
  ],
  "pagination": {
    "limit": 100,
    "nextCursor": null,
    "hasMore": false
  }
}
```

**Error contract**

400 for invalid limit/cursor; 500 for unexpected failures.

**DynamoDB access pattern**

Prefer dedicated materialized metadata/access-path items. A large Scan of questions or experiences on every request is prohibited.

**Dependencies**

T003, T013, T014.

**Acceptance criteria**

- [ ] Results are deterministic.
- [ ] No unbounded Scan is used.
- [ ] Values correspond to supported stored data/taxonomy.
- [ ] Pagination or a documented bounded result is implemented.

**Tests**

- [ ] MockMvc response tests.
- [ ] Repository/access-path tests.

**Implementation notes**

The current schema provides question-type taxonomy but no metadata partition that can enumerate distinct companies or types efficiently. This task is blocked until a bounded metadata projection/access path is added to the crawler/database write path. Do not invent a search index or Scan.

---

## T017 — Public sorting contract

- [~] Status

**Goal**

Standardize supported sorting for question and experience collections.

**API responsibility**

Provides deterministic ordering requested by UI consumers.

**Endpoints**

Applies to relevant collection endpoints.

**Request contract**

Supported sort values must be explicit, for example `newest` and `oldest`, only where the backing access path supports them.

**Response contract**

Same collection envelope.

**Error contract**

400 for unsupported sort values.

**DynamoDB access pattern**

Map sort to Query direction/order. Do not retrieve a large collection and sort it in memory.

**Dependencies**

T008, T012.

**Acceptance criteria**

- [x] Supported sort values are documented.
- [x] Sort maps directly to storage ordering.
- [x] Unsupported sorts return 400.
- [x] No full-result in-memory sorting is used.

**Tests**

- [ ] Controller validation tests.
- [ ] Repository sort-direction tests.

**Implementation notes**

Do not expose arbitrary sort fields unless the schema can support them efficiently.

---

## T018 — Combined filters and DynamoDB access-pattern rules

- [~] Status

**Goal**

Define and implement efficient behavior for filter combinations such as company + type.

**API responsibility**

Prevents public APIs from degrading into scans when multiple filters are applied.

**Endpoints**

Primarily `GET /api/v1/questions` and `GET /api/v1/experiences`.

**Request contract**

Combined filters are accepted only where a documented efficient access path exists.

**Response contract**

Common collection envelope.

**Error contract**

Use a clear 400 error code for an unsupported expensive combination instead of falling back to Scan.

**DynamoDB access pattern**

Choose the most selective supported partition. Apply only bounded residual predicates when appropriate. Never Scan to rescue an unsupported combination.

**Dependencies**

T013, T014, T015, T017.

**Acceptance criteria**

- [x] Supported combinations choose a documented Query partition.
- [x] Residual filtering is bounded.
- [x] Unsupported expensive combinations return a clear client error.
- [x] Cursor behavior remains correct.

**Tests**

- [ ] Combination tests.
- [ ] Repository access-path verification.

**Implementation notes**

DynamoDB limitations are part of the public API contract.

---

## T019 — Request bounds and validation hardening

- [~] Status

**Goal**

Apply consistent bounds to anonymous public read requests.

**API responsibility**

Prevents pathological requests and keeps DynamoDB work bounded.

**Endpoints**

All `/api/v1/**`.

**Request contract**

At minimum:

- `limit`: 1–100
- `cursor`: length-bounded, opaque
- IDs: non-blank and length-bounded
- string filters: trimmed and length-bounded
- sort/type values: validated against supported values

**Response contract**

Common error envelope.

**Error contract**

Invalid requests return 400 before DynamoDB access.

**DynamoDB access pattern**

Validation happens before any DynamoDB operation.

**Dependencies**

T002, T003, T008, T009, T012, T013, T014, T015, T017, T018.

**Acceptance criteria**

- [x] Every public endpoint has validation.
- [x] Invalid requests do not invoke repositories.
- [x] Limits are enforced consistently.
- [x] Public error messages are actionable without leaking internals.

**Tests**

- [ ] Parameterized MockMvc tests.
- [ ] Verify no repository call for invalid input.

**Implementation notes**

Do not add a full authentication system in this task.

---

## T020 — Public API observability

- [~] Status

**Goal**

Add lightweight request and DynamoDB failure observability for `/api/v1`.

**API responsibility**

Provides production diagnostics without logging raw interview content.

**Endpoints**

All `/api/v1/**`.

**Request contract**

N/A.

**Response contract**

No public shape change.

**Error contract**

Existing public error envelope remains unchanged.

**DynamoDB access pattern**

Failed Query/GetItem/BatchGetItem operations log operation context and failure type without item payloads or credentials.

**Dependencies**

T008, T009, T010, T011, T012.

**Acceptance criteria**

- [ ] Method/path/status/duration are observable.
- [ ] DynamoDB failures include useful operation context.
- [ ] Question/experience bodies are not dumped to logs.

**Tests**

- [ ] Request smoke test.
- [ ] Repository-failure path test where practical.

**Implementation notes**

Use existing SLF4J/Actuator conventions. Do not add new observability infrastructure.

---

## T021 — CORS and browser access configuration

- [~] Status

**Goal**

Configure safe browser access for the future InterviewHQ UI.

**API responsibility**

Allows configured browser origins to call the public API.

**Endpoints**

All `/api/v1/**`.

**Request contract**

Standard browser GET/OPTIONS behavior.

**Response contract**

Normal API responses plus CORS headers where applicable.

**Error contract**

No change to API errors.

**DynamoDB access pattern**

N/A.

**Dependencies**

T008, T009.

**Acceptance criteria**

- [ ] Allowed origins are environment-configurable.
- [ ] Production can restrict origins.
- [ ] Credentialed wildcard CORS is not enabled.
- [ ] Existing API behavior remains unchanged.

**Tests**

- [ ] CORS preflight test.
- [ ] Configured-origin response test.

**Implementation notes**

Use Spring's existing web configuration conventions.

---

## Definition of Done

All tasks in this part are complete only when their acceptance criteria and tests pass, their status is checked, and the implementation continues to obey the API-only architecture in Part 1.
