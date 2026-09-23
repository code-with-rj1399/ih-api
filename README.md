# ih-api

Spring Boot API for InterviewHQ. This service is the canonical persistence boundary for the `interview-hq` DynamoDB table.

Crawler and UI talk to DynamoDB through this API — not directly.

```
ih-crawler  --HTTPS + API key-->  ih-api  --IAM / local endpoint-->  DynamoDB
InterviewHQ UI  --HTTPS + API key-->  ih-api
```

This repository is being built iteratively on the `grok-changes` branch.

## Stack

- Java 17
- Spring Boot 3.5
- AWS SDK v2 DynamoDB
- Spring Security (API keys, disableable for local/dev)
- springdoc OpenAPI / Swagger UI — upcoming

## Profiles

| Profile | DynamoDB | Table default | Security default |
| --- | --- | --- | --- |
| `local` | DynamoDB Local (`http://localhost:8000`) | `interview-hq` | **disabled** |
| `dev` | DynamoDB Local (overrideable) | `interview-hq-dev` | **disabled** |
| `stg` | AWS DynamoDB (no endpoint override) | `interview-hq-stg` | enabled |
| `prod` | AWS DynamoDB via IAM role | `interview-hq` | enabled |

Disable (or re-enable) security independently of the profile:

```bash
IH_SECURITY_ENABLED=false SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
IH_SECURITY_ENABLED=true  IH_API_KEY_CRAWLER=secret SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

### API keys

Send the key as `X-API-Key` or `Authorization: Bearer <key>`.

| Env var | Roles |
| --- | --- |
| `IH_API_KEY_UI` | `READ` |
| `IH_API_KEY_CRAWLER` | `CRAWLER`, `READ` |
| `IH_API_KEY_ADMIN` | `ADMIN`, `CRAWLER`, `READ` |

When security is enabled:

- `GET /actuator/health` and `/api/v1/meta` stay public
- reads require `READ`
- ingest / crawler writes require `CRAWLER` or `ADMIN`

## Single-table keys

Owned by this service — crawlers must not construct them.

| Entity | PK | SK |
| --- | --- | --- |
| Question | `QUESTION#{sha256}` | `ENTITY` |
| Experience | `EXPERIENCE#{id}` | `ENTITY` |
| Experience → Question | `EXPERIENCE#{id}` | `QUESTION#{questionId}` |
| SourceSeed | `SOURCE#{sourceId}` | `SEED#{seedId}` |
| CrawlRun | `CRAWL_RUN#{runId}` | `ENTITY` |
| CrawlPage | `PAGE#{urlHash}` | `ENTITY` |

## Indexes

| Index | Access pattern |
| --- | --- |
| GSI1 `COMPANY#{company}` | recent questions for a company |
| GSI2 `TYPE#{questionType}` | recent questions by type |
| GSI3 `SOURCE#{sourceName}` | questions from a source |
| GSI4 `ENTITY_TYPE#{entityType}` | list recent entities |

## Status

- [x] Step 1 — Spring Boot scaffold, actuator, meta endpoint
- [x] Step 2 — DynamoDB client + local vs AWS profiles
- [x] Step 3 — Domain model matching the single-table schema
- [x] Step 4 — Repositories, GSIs, conditional writes
- [x] Step 5 — API-key security (disableable)
- [ ] Step 6 — REST API (ingest, questions, seeds, crawl runs, pages)
- [ ] Step 7 — Tests, Docker image, OpenAPI polish

## Run locally

```bash
docker compose up dynamodb-local -d
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Health:

- `GET /actuator/health`
- `GET /api/v1/meta`
