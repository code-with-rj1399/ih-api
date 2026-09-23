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

## Profiles

| Profile | DynamoDB | Table default | Security default |
| --- | --- | --- | --- |
| `local` | DynamoDB Local (`http://localhost:8000`) | `interview-hq` | **disabled** |
| `dev` | DynamoDB Local (overrideable) | `interview-hq-dev` | **disabled** |
| `stg` | AWS DynamoDB (no endpoint override) | `interview-hq-stg` | enabled |
| `prod` | AWS DynamoDB via IAM role | `interview-hq` | enabled |

Disable security for development:

```bash
IH_SECURITY_ENABLED=false SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Send keys as `X-API-Key` or `Authorization: Bearer <key>`.

| Env var | Roles |
| --- | --- |
| `IH_API_KEY_UI` | `READ` |
| `IH_API_KEY_CRAWLER` | `CRAWLER`, `READ` |
| `IH_API_KEY_ADMIN` | `ADMIN`, `CRAWLER`, `READ` |

## API

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/questions/ingest` | Crawler ingestion (validate, hash, conditional write) |
| `GET` | `/api/v1/questions` | List recent questions (`company`, `type`, `source`, `cursor`) |
| `GET` | `/api/v1/questions/{id}` | Get a question |
| `GET` | `/api/v1/questions/types` | Canonical question types |
| `GET` | `/api/v1/experiences/{id}` | Get an interview experience |
| `GET` | `/api/v1/experiences/{id}/questions` | Questions extracted from an experience |
| `GET` | `/api/v1/sources/seeds` | All crawl seeds |
| `GET`/`PUT`/`PATCH`/`DELETE` | `/api/v1/sources/{sourceId}/seeds/{seedId}` | Seed configuration |
| `GET`/`PUT` | `/api/v1/crawl-runs` | Crawl run metadata |
| `GET`/`PUT` | `/api/v1/pages` | Crawled page lookup / upsert (`?url=` or `/{urlHash}`) |
| `GET` | `/api/v1/meta` | Service + DynamoDB + security status |
| `GET` | `/actuator/health` | Liveness |

Ingest example (from the schema):

```http
POST /api/v1/questions/ingest
Content-Type: application/json

{
  "source": {
    "name": "LeetCode",
    "url": "https://leetcode.com/discuss/interview-experience/123"
  },
  "experience": {
    "title": "Google Interview Experience",
    "company": "Google",
    "postedAt": "2026-09-23T10:20:00Z",
    "author": "anonymous"
  },
  "questions": [
    {
      "questionText": "Design a notification system.",
      "questionDescription": "Design a notification system that supports reliable delivery of notifications.",
      "questionType": "System Design",
      "topics": ["Notifications"],
      "problemUrl": null,
      "confidence": 0.92,
      "questionSpecificity": 0.86
    }
  ]
}
```

Invalid questions are rejected (not persisted). Duplicate identity hashes return `status: DUPLICATE` (idempotent). `problemUrl` is dropped if it is the experience URL.

## Status

- [x] Step 1 — Spring Boot scaffold, actuator, meta endpoint
- [x] Step 2 — DynamoDB client + local vs AWS profiles
- [x] Step 3 — Domain model matching the single-table schema
- [x] Step 4 — Repositories, GSIs, conditional writes
- [x] Step 5 — API-key security (disableable)
- [x] Step 6 — REST API (ingest, questions, seeds, crawl runs, pages)
- [ ] Step 7 — Tests, Docker image, OpenAPI polish

## Run locally

```bash
docker compose up dynamodb-local -d
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```
