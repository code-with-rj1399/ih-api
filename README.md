# ih-api

Spring Boot API for [InterviewHQ](https://github.com/code-with-rj1399). This service is the **canonical persistence boundary** for the `interview-hq` DynamoDB table. The crawler (`ih-crawler`) and the UI talk to DynamoDB through this API — not directly.

```
ih-crawler  --HTTPS + API key-->  ih-api  --IAM role / local endpoint-->  DynamoDB
InterviewHQ UI --HTTPS + API key-->  ih-api
```

Schema: [`INTERVIEWHQ-DYNAMODB-SCHEMA.md`](https://github.com/code-with-rj1399/ih-crawler/blob/master/docs/INTERVIEWHQ-DYNAMODB-SCHEMA.md).

## Stack

- Java 17, Spring Boot 3.5
- AWS SDK v2 DynamoDB (single-table design, PAY_PER_REQUEST)
- Spring Security API keys (can be turned off for local/dev)
- springdoc OpenAPI / Swagger UI at `/swagger-ui.html`

## Profiles

| Profile | DynamoDB | Table default | Security |
| --- | --- | --- | --- |
| `local` | DynamoDB Local `http://localhost:8000` | `interview-hq` | **off** |
| `dev` | DynamoDB Local (overrideable) | `interview-hq-dev` | **off** |
| `stg` | AWS DynamoDB (IAM / env credentials) | `interview-hq-stg` | **on** |
| `prod` | AWS DynamoDB via IAM role | `interview-hq` | **on** |

The DynamoDB endpoint is configuration-driven. The same JAR runs locally and in AWS.

```bash
# local
docker compose up dynamodb-local -d
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run

# staging / production (no DYNAMODB_ENDPOINT)
SPRING_PROFILES_ACTIVE=stg ./mvnw spring-boot:run
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

| Variable | Purpose |
| --- | --- |
| `DYNAMODB_ENDPOINT` | Set for local. **Leave empty** for AWS. |
| `DYNAMODB_TABLE` | Table name override |
| `DYNAMODB_AUTO_CREATE_TABLE` | `true` only in local/dev |
| `AWS_REGION` | Default `ap-south-1` |
| `IH_SECURITY_ENABLED` | `false` disables API-key auth |
| `IH_API_KEY_UI` | `READ` |
| `IH_API_KEY_CRAWLER` | `CRAWLER` + `READ` |
| `IH_API_KEY_ADMIN` | `ADMIN` + `CRAWLER` + `READ` |

Keys are sent as `X-API-Key` or `Authorization: Bearer <key>`.

```bash
# force security off (development)
IH_SECURITY_ENABLED=false SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run

# force security on even locally
IH_SECURITY_ENABLED=true IH_API_KEY_CRAWLER=dev-crawler SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

## API

Swagger UI: `/swagger-ui.html`  
OpenAPI JSON: `/v3/api-docs`

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/v1/questions/ingest` | CRAWLER | Validate, hash, conditional-write questions |
| `GET` | `/api/v1/questions` | READ | List (`company`, `type`, `source`, `cursor`) |
| `GET` | `/api/v1/questions/{id}` | READ | Get one question |
| `GET` | `/api/v1/questions/types` | READ | Canonical question types |
| `GET` | `/api/v1/experiences/{id}` | READ | Interview experience |
| `GET` | `/api/v1/experiences/{id}/questions` | READ | Questions for an experience |
| `GET` | `/api/v1/sources/seeds` | READ | All crawl seeds |
| `PUT`/`PATCH`/`DELETE` | `/api/v1/sources/{id}/seeds/{seedId}` | CRAWLER | Seed configuration |
| `GET`/`PUT` | `/api/v1/crawl-runs` | CRAWLER | Crawl run metadata |
| `GET`/`PUT` | `/api/v1/pages` | CRAWLER | Crawled page lookup / upsert |
| `GET` | `/api/v1/meta` | public | Service + DynamoDB + security status |
| `GET` | `/actuator/health` | public | Liveness |

`GET /actuator/health` and `/api/v1/meta` stay public even when security is on.

### Ingest

```http
POST /api/v1/questions/ingest
Content-Type: application/json
X-API-Key: ${IH_API_KEY_CRAWLER}

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

Server-side rules (even if the crawler used an LLM):

- `questionText` and `questionDescription` required, non-blank
- `questionType` must be a canonical value (`DSA` / `leetcode` collapse to **Coding**)
- `confidence` and `questionSpecificity` in `[0, 1]` when present
- `problemUrl` is stored only when it is a distinct problem URL — the experience URL is never copied
- Question id = `SHA-256(normalizedCompany + text + type)`
- Writes use `attribute_not_exists(PK)` so duplicate ingest is idempotent (`CREATED` / `DUPLICATE`)

## Single-table layout

| Entity | PK | SK |
| --- | --- | --- |
| Question | `QUESTION#{sha256}` | `ENTITY` |
| Experience | `EXPERIENCE#{id}` | `ENTITY` |
| Experience → Question | `EXPERIENCE#{id}` | `QUESTION#{questionId}` |
| SourceSeed | `SOURCE#{sourceId}` | `SEED#{seedId}` |
| CrawlRun | `CRAWL_RUN#{runId}` | `ENTITY` |
| CrawlPage | `PAGE#{urlHash}` | `ENTITY` |

| Index | Access pattern |
| --- | --- |
| GSI1 `COMPANY#{company}` | recent questions for a company |
| GSI2 `TYPE#{questionType}` | recent questions by type |
| GSI3 `SOURCE#{sourceName}` | questions from a source |
| GSI4 `ENTITY_TYPE#{entityType}` | list recent entities |

`local`/`dev` auto-create the table and GSIs. `stg`/`prod` provision the table out of band.

## Docker

```bash
docker compose up --build
```

Starts DynamoDB Local on `:8000` and the API on `:8080` with `SPRING_PROFILES_ACTIVE=local` and security disabled.

## Tests

```bash
./mvnw test
```
