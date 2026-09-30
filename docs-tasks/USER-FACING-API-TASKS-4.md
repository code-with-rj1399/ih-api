# InterviewHQ User-Facing API Tasks — Part 4 (CORS, README)

---

## T019 — CORS and public exposure defaults for browser clients

Status: NOT_STARTED

### Objective
Configure CORS so a future InterviewHQ UI (and local debugger if cross-origin) can call the API safely.

### API responsibility
Browser access to public read APIs.

### Endpoints
All `/api/v1/**`

### Request contract
Standard CORS preflight for GET.

### Response contract
N/A

### Error contract
N/A

### DynamoDB access pattern
N/A

### Dependencies
T001, T006

### Acceptance criteria
- Configurable allowed origins via environment (e.g. `CORS_ALLOWED_ORIGINS`, default `*` for local only or explicit localhost).
- Credentials not required for public reads.
- Production recommendation documented: restrict origins.

### Testing requirements
- MockMvc or integration test for `OPTIONS` / `Access-Control-Allow-Origin` on a sample GET.

### Implementation notes
- Spring `CorsRegistry` or `CorsConfigurationSource` bean.
- Do not enable permissive CORS with credentials.

---

## T020 — Final README and runbook for user-facing APIs

Status: NOT_STARTED

### Objective
Update project README with how to run the API, hit public endpoints, open debugger, and point to `docs/PUBLIC-API.md`.

### API responsibility
Onboarding for implementers and UI developers.

### Endpoints
Documents existing + `/api/v1/**` overview.

### Request contract
N/A

### Response contract
N/A

### Error contract
N/A

### DynamoDB access pattern
Point to crawler schema doc as source of truth for keys/GSIs.

### Dependencies
T016, T017

### Acceptance criteria
- README sections: stack, local run, public API overview, debugger, env vars, link to task docs under `docs-tasks/`.
- No claim of crawler/admin features in this service beyond existing architecture note.

### Testing requirements
- None (docs only).

### Implementation notes
- Keep architecture diagram text: UI → ih-api → DynamoDB.
- Commit only documentation/code required by completed tasks when executing later; this task is docs-only when run.
