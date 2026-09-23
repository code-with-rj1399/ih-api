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

```bash
# local / dev — DynamoDB Local
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run

# staging — AWS account credentials / task role
SPRING_PROFILES_ACTIVE=stg ./mvnw spring-boot:run

# production
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

Environment variables (see `.env.example`):

| Variable | Purpose |
| --- | --- |
| `DYNAMODB_ENDPOINT` | Set for local (`http://localhost:8000`). Leave empty for AWS. |
| `DYNAMODB_TABLE` | Table name override |
| `DYNAMODB_AUTO_CREATE_TABLE` | `true` only in local/dev |
| `AWS_REGION` | Default `ap-south-1` |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | Dummy values for DynamoDB Local; IAM role in stg/prod |
| `IH_SECURITY_ENABLED` | `false` to disable API-key auth |

## Status

- [x] Step 1 — Spring Boot scaffold, actuator, meta endpoint
- [x] Step 2 — DynamoDB client + local vs AWS profiles
- [ ] Step 3 — Domain model matching the single-table schema
- [ ] Step 4 — Repositories, GSIs, conditional writes
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
