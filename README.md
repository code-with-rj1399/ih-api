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
- Spring Security (API keys, disableable for local/dev)
- springdoc OpenAPI / Swagger UI

## Profiles

| Profile | DynamoDB | Security default |
| --- | --- | --- |
| `local` / `dev` | DynamoDB Local (`http://localhost:8000`) | disabled |
| `stg` | AWS DynamoDB table `interview-hq-stg` | enabled |
| `prod` | AWS DynamoDB table `interview-hq` | enabled |

## Status

- [x] Step 1 — Spring Boot scaffold, actuator, meta endpoint
- [ ] Step 2 — DynamoDB client + environment profiles
- [ ] Step 3 — Domain model matching the single-table schema
- [ ] Step 4 — Repositories, GSIs, conditional writes
- [ ] Step 5 — API-key security (disableable)
- [ ] Step 6 — REST API (ingest, questions, seeds, crawl runs, pages)
- [ ] Step 7 — Tests, Docker, OpenAPI polish

## Run (after later steps)

```bash
# local DynamoDB + API, security off
docker compose up dynamodb-local -d
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Health:

- `GET /actuator/health`
- `GET /api/v1/meta`
- Swagger UI at `/swagger-ui.html`
