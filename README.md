# ih-api

Spring Boot API for InterviewHQ. This service is the canonical persistence boundary for the `interview-hq` DynamoDB table.

Crawler and UI talk to DynamoDB through this API — not directly.

```
ih-crawler  --ingest-->  ih-api  --IAM / local endpoint-->  DynamoDB
InterviewHQ UI  --read-->  ih-api
```

This repository is being built iteratively on the `grok-changes` branch.

## Stack

- Java 17
- Spring Boot 3.5
- AWS SDK v2 DynamoDB
- Spring Security (API keys, disableable for local/dev) — upcoming
- springdoc OpenAPI / Swagger UI — upcoming

## Profiles

| Profile | DynamoDB | Table default | Security default |
| --- | --- | --- | --- |
| `local` | DynamoDB Local (`http://localhost:8000`) | `interview-hq` | disabled |
| `dev` | DynamoDB Local (overrideable) | `interview-hq-dev` | disabled |
| `stg` | AWS DynamoDB (no endpoint override) | `interview-hq-stg` | enabled |
| `prod` | AWS DynamoDB via IAM role | `interview-hq` | enabled |

The DynamoDB endpoint is configuration-driven. The same application code runs locally and in AWS.

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

Question id = `SHA-256(normalizedCompany + normalizedQuestionText + normalizedQuestionType)`.

DSA / algorithm / LeetCode coding problems are stored as **Coding**.

## Indexes

| Index | PK | SK | Access pattern |
| --- | --- | --- | --- |
| GSI1 | `COMPANY#{company}` | `{postedAt}#{questionId}` | recent questions for a company |
| GSI2 | `TYPE#{questionType}` | `{postedAt}#{questionId}` | recent questions by type |
| GSI3 | `SOURCE#{sourceName}` | `{postedAt}#{questionId}` | questions from a source |
| GSI4 | `ENTITY_TYPE#{entityType}` | `{timestamp}#{id}` | list recent entities (API listing) |

Ingestion uses DynamoDB conditional writes (`attribute_not_exists(PK)`) so duplicate questions and experiences are idempotent.

`local`/`dev` auto-create the table and GSIs. `stg`/`prod` must provision the table out of band (`DYNAMODB_AUTO_CREATE_TABLE=false`).

## Status

- [x] Step 1 — Spring Boot scaffold, actuator, meta endpoint
- [x] Step 2 — DynamoDB client + local vs AWS profiles
- [x] Step 3 — Domain model matching the single-table schema
- [x] Step 4 — Repositories, GSIs, conditional writes
- [ ] Step 5 — API-key security (disableable)
- [ ] Step 6 — REST API (ingest, questions, seeds, crawl runs, pages)
- [ ] Step 7 — Tests, Docker image, OpenAPI polish

## Run locally

```bash
docker compose up dynamodb-local -d
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Health:

- `GET /actuator/health`
- `GET /api/v1/meta` — reports table, region, and whether the client is in `local` or `aws` mode
