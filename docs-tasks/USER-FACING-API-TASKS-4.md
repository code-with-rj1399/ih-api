# InterviewHQ hq-API — User-Facing API Implementation Tasks — Part 4

Continue from Parts 1–3. This file contains the final cross-cutting, contract, and completion tasks.

## Progress

- Total: 24
- Completed: 0
- Remaining: 24

## Task list

## T022 — Public API documentation

- [ ] Status

**Goal**

Document the implemented public API contract in this repository.

**API responsibility**

Provides one source of truth for UI consumers and API implementers.

**Endpoints**

All implemented `/api/v1/**` endpoints from T008–T021.

**Request contract**

Document base URL, path/query parameters, limits, cursors, filters, and sorting.

**Response contract**

Document actual collection, single-resource, and error JSON with examples.

**Error contract**

Document all public 400/404/500 codes actually implemented.

**DynamoDB access pattern**

Document implementation-facing Query/GetItem/BatchGetItem behavior briefly; do not expose internal keys as client contract.

**Dependencies**

T008, T009, T010, T011, T012, T013, T014, T015, T016, T017, T018, T019, T020, T021.

**Acceptance criteria**

- [ ] `docs/PUBLIC-API.md` exists.
- [ ] Every implemented public endpoint is documented.
- [ ] Examples match the actual implementation.
- [ ] Pagination/filter/sort/error behavior is explicit.
- [ ] Crawler, ingestion, admin, and `/dev` endpoints are not presented as public APIs.

**Tests**

- [ ] Manual contract review against controllers/DTOs.

**Implementation notes**

Keep this separate from crawler `docs/API.md`; this file documents the hq-API public contract only.

---

## T023 — Public API contract and integration tests

- [ ] Status

**Goal**

Build a coherent regression suite for UI-facing REST behavior.

**API responsibility**

Protects endpoint shapes and behavior from accidental breaking changes.

**Endpoints**

At minimum:

`GET /api/v1/questions`  
`GET /api/v1/questions/{id}`  
`GET /api/v1/questions?company=...`  
`GET /api/v1/questions?type=...`  
`GET /api/v1/experiences`  
`GET /api/v1/experiences/{id}`  
`GET /api/v1/experiences/{id}/questions`

**Request contract**

Use contracts defined by previous tasks.

**Response contract**

Assert collection envelope, pagination fields, and major public resource fields.

**Error contract**

Assert documented 400 and 404 codes.

**DynamoDB access pattern**

Prefer mocked repository/service boundaries for API contract tests. No live AWS dependency in CI.

**Dependencies**

T008, T009, T010, T011, T012, T013, T014, T015, T016, T017, T018, T019.

**Acceptance criteria**

- [ ] Major collection and detail endpoints have MockMvc coverage.
- [ ] Pagination behavior is tested.
- [ ] Filter and sort behavior is tested.
- [ ] 400/404 error contracts are tested.
- [ ] Tests are deterministic and CI-friendly.

**Tests**

- [ ] Full public API contract test suite passes.

**Implementation notes**

Do not test crawler, ingestion, admin, or `/dev` behavior here.

---

## T024 — Final verification and completion audit

- [ ] Status

**Goal**

Run the relevant verification suite and audit the implementation against this task plan.

**API responsibility**

Closes the user-facing API work with an auditable implementation state.

**Endpoints**

All implemented `/api/v1/**`.

**Request contract**

N/A.

**Response contract**

N/A.

**Error contract**

N/A.

**DynamoDB access pattern**

Verify that no public list path uses Scan.

**Dependencies**

T023.

**Acceptance criteria**

- [ ] All applicable tasks are checked.
- [ ] Progress totals are correct.
- [ ] Public API documentation matches implementation.
- [ ] No crawler/ingestion/admin/`/dev` API work is included.
- [ ] No public list endpoint performs a table Scan.
- [ ] Public cursors remain opaque.
- [ ] Public DTOs do not expose DynamoDB storage internals.

**Tests**

- [ ] Full Maven test suite passes.
- [ ] Final API contract review is recorded in the commit/PR.

**Implementation notes**

Working tree should be clean after the final task commit.

## Definition of Done

The user-facing hq-API is complete when all applicable tasks are checked and:

- Public APIs live under `/api/v1/**`.
- hq-API remains an API-only service.
- Question and experience resources use stable public DTOs.
- Collection APIs use bounded opaque-cursor pagination.
- Supported filters and sorting are explicit and deterministic.
- User-facing reads use Query/GetItem/BatchGetItem and do not fall back to Scan.
- Experience → questions uses the documented relationship access pattern.
- Public error behavior is consistent.
- Request validation and browser CORS are documented and tested.
- Public API documentation matches implementation.
- Regression tests cover the major UI-facing endpoints.
- No crawler ingestion, crawler operations, admin, or `/dev` API behavior is implemented as part of this task set.
