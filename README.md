# ih-api

InterviewHQ backend API service.

## Stack

- Java 17
- Spring Boot 3.5
- Spring Web
- AWS SDK for Java v2
- Amazon DynamoDB
- Maven
- Docker / Docker Compose

## Current API

GET /api/hello

Health: GET /actuator/health

## DynamoDB configuration

Production uses AWS DynamoDB.

Configuration is provided through environment variables:

- `AWS_REGION`
- `DYNAMODB_TABLE`
- `DYNAMODB_ENDPOINT` (optional; used for local DynamoDB)
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

The API does not create or start DynamoDB. In local development, DynamoDB Local is owned and started by `ih-crawler`; `ih-api` connects to that shared instance.

## Local Docker

Start `ih-crawler` first so it owns the DynamoDB Local container:

```bash
cd ../ih-crawler
docker compose up -d
```

Then start the API:

```bash
cd ../ih-api
docker compose up -d --build
```

API: http://localhost:8091/api/hello
API Debugger: http://localhost:8091/api-debugger/

The API Docker Compose configuration connects to the DynamoDB Local instance exposed by `ih-crawler` at `host.docker.internal:8000`.

## Architecture

Local development:

    ih-crawler
        |
        +--> DynamoDB Local :8000
        |
    ih-api ------------> DynamoDB Local :8000

Production:

    ih-crawler --HTTPS--> hq-API --DynamoDB--> AWS
    InterviewHQ UI --HTTPS--> hq-API

The crawler owns the local DynamoDB lifecycle. The API only creates a DynamoDB client and uses the configured table; it does not create or initialize DynamoDB tables.
