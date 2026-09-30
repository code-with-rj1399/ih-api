# InterviewHQ User-Facing API Tasks — Part 3 (Validation, performance, observability, docs, debugger)

---

## T013 — Request validation hardening and ID rules

Status: NOT_STARTED

### Objective
Centralise validation for path IDs, query params, and reject unsafe inputs consistently.

### API responsibility
Safe, predictable 400 responses for all public endpoints.

### Endpoints
Applies to all `/api/v1/**` endpoints defined in prior tasks.

### Request contract
Rules:
- Path `id`: non-blank, max length 128, allowed charset `[A-Za-z0-9_-]` (or match crawler id format).
- `limit`: 1–50 (lists) or 1–200 (meta).
- `sort`: only documented enums.
- `company` / `type`: max length 100, trimmed; reject empty after trim.
- `cursor`: max length bound (e.g. 2048); invalid → INVALID_CURSOR.

### Response contract
Error envelope from T002.

### Error contract
All validation failures → 400 with `VALIDATION_ERROR` or `INVALID_CURSOR`.

### DynamoDB access pattern
N/A (pre-query validation)

### Dependencies
T002, T003, T006–T012

### Acceptance criteria
- Bean Validation and/or explicit guards on every public controller method.
- No DynamoDB call on clearly invalid input.
- Consistent messages via error envelope.

### Testing requirements
- Parameterised MockMvc tests for invalid id, limit=0, limit=51, bad sort, oversized cursor.

### Implementation notes
- Prefer `@Validated` + constraint annotations on request records.
- Do not expose internal constraint names in `message` if avoidable; use clear public text.

---

## T014 — Performance: response size, BatchGet, and query bounds

Status: NOT_STARTED

### Objective
Ensure list and nested endpoints stay within bounded latency and payload size for production traffic.

### API responsibility
Production-ready read path characteristics for user-facing APIs.

### Endpoints
All collection and nested list endpoints.

### Request contract
N/A (behavioural)

### Response contract
- List item DTOs should omit bulky optional fields if not needed for list views (e.g. long `description` can be detail-only) — document slim vs full.
- Max page size remains 50.

### Error contract
Unchanged; timeouts surface as 500 without internal detail.

### DynamoDB access pattern
- Always Query/GetItem/BatchGetItem with Limit.
- Experience questions: BatchGetItem in chunks ≤ 100, still capped by API `limit`.
- Avoid loading related entities unless the endpoint requires them.
- No N+1 GetItem in loops without batching.

### Dependencies
T005–T012

### Acceptance criteria
- Code review checklist: no Scan for user paths; no unbounded loops of GetItem.
- Slim list DTO used for `GET /api/v1/questions` if full description is large.
- Integration or unit test proving BatchGet path for experience questions.

### Testing requirements
- Unit test that repository list methods set Limit.
- Test BatchGet batching boundary.

### Implementation notes
- Consider future cache headers only if measured need; not required in this task.
- Keep single-table Query patterns from schema GSIs.

---

## T015 — Observability for public APIs

Status: NOT_STARTED

### Objective
Add structured logging and basic metrics hooks for public API latency and DynamoDB failures.

### API responsibility
Operability of user-facing endpoints.

### Endpoints
All `/api/v1/**`

### Request contract
N/A

### Response contract
N/A (logging only)

### Error contract
Errors still use public envelope; logs may include exception class + message server-side.

### DynamoDB access pattern
Log failed Query/GetItem (table, operation, error code) without logging full item payloads or credentials.

### Dependencies
T006–T012

### Acceptance criteria
- Request log line or structured fields: method, path, status, duration ms.
- DynamoDB failures logged at ERROR with correlation-friendly message.
- Actuator health remains available; no requirement for custom metrics backend in this task (Micrometer counters optional).

### Testing requirements
- Smoke test that successful request does not error.
- Optional: assert logger invoked on forced repository failure (mock).

### Implementation notes
- Use SLF4J; avoid logging PII (none expected on these APIs).
- Filter or interceptor preferred over copy-paste in every controller.

---

## T016 — Public API documentation (Markdown contract)

Status: NOT_STARTED

### Objective
Maintain human-readable API contract documentation for all user-facing endpoints.

### API responsibility
Single source of truth for UI and debugger consumers.

### Endpoints
Documents every endpoint from T006–T012 and T010.

### Request contract
Documented per endpoint in `docs/PUBLIC-API.md` (create under repo `docs/`).

### Response contract
Examples matching implementation DTOs.

### Error contract
Document shared error envelope and status codes.

### DynamoDB access pattern
Brief note per endpoint (Query vs GetItem) for implementers; not required for external clients.

### Dependencies
T006–T012, T003

### Acceptance criteria
- File `docs/PUBLIC-API.md` covers: base URL, auth (none for public reads), pagination, filters, sorting, each endpoint, errors, example curl.
- Matches actual path names and field names.

### Testing requirements
- Manual review against controllers (no automated OpenAPI required in this task).

### Implementation notes
- Optional later: springdoc OpenAPI; out of scope unless trivial.
- Keep docs in sync when contracts change.

---

## T017 — Static debugger HTML for manual API testing

Status: NOT_STARTED

### Objective
Add a static HTML page that calls the user-facing APIs so developers can exercise the API without a full UI.

### API responsibility
Developer testing surface only (static assets + same public APIs). **Not** an admin UI and **not** crawler tooling.

### Endpoints
Consumed (client-side):
- `GET /api/v1/questions`
- `GET /api/v1/questions/{id}`
- `GET /api/v1/questions?company=`
- `GET /api/v1/experiences` (if available)
- `GET /api/v1/experiences/{id}`
- `GET /api/v1/experiences/{id}/questions`
- `GET /api/v1/meta/companies`
- `GET /api/v1/meta/question-types`

Served as static file, e.g.:
- `GET /debugger` or `GET /debugger/index.html` via Spring static resources.

### Request contract
HTML form fields for: base URL (default same origin), company, type, limit, cursor, question id, experience id.

### Response contract
Display raw JSON response in a `<pre>` panel; show HTTP status.

### Error contract
Show API error JSON as returned.

### DynamoDB access pattern
N/A (browser → API only)

### Dependencies
T006–T012, T010

### Acceptance criteria
- Static HTML under `src/main/resources/static/debugger/index.html` (or equivalent).
- Page works when API is running locally (CORS not required if same origin).
- No credentials, no admin actions, no write operations.
- README section: how to open `/debugger`.

### Testing requirements
- Manual verification sufficient; optional WebMvcTest that static resource is mapped.

### Implementation notes
- Pure HTML/JS; no React/Next.js build.
- Keep UI minimal: inputs + “Fetch” buttons + JSON output.
- Do not place under `/admin` or `/dev` API namespaces; static `/debugger` is acceptable for local testing.

---

## T018 — Integration / contract tests for public read APIs

Status: NOT_STARTED

### Objective
Add automated tests that lock the public contracts (status codes, JSON shapes, pagination fields).

### API responsibility
Regression safety for UI consumers.

### Endpoints
Cover at least:
- `GET /api/v1/questions`
- `GET /api/v1/questions/{id}`
- `GET /api/v1/questions?company=Amazon`
- `GET /api/v1/experiences/{id}` (404 path)
- Error paths: bad limit, bad cursor, missing id

### Request contract
As specified in prior tasks.

### Response contract
Assert presence of `data`, `pagination.limit`, `pagination.hasMore`; question fields `id`, `text`/`company` as applicable.

### Error contract
Assert `error.code` on 400/404.

### DynamoDB access pattern
Prefer mocked repository/service for pure contract tests; optional `@SpringBootTest` with DynamoDB Local if already in docker-compose.

### Dependencies
T006–T013

### Acceptance criteria
- Tests fail if public JSON field names change unexpectedly.
- CI-friendly (no real AWS required).

### Testing requirements
- JUnit 5 + MockMvc (and/or WebTestClient).
- At least one happy path and one error path per major endpoint group.

### Implementation notes
- Reuse fixtures from mapper tests.
- Do not test crawler or ingestion.
