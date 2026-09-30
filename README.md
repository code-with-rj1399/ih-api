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

The API does not require AWS credentials to be committed to the repository.

## Local Docker

```bash
./run.sh
```

This starts DynamoDB Local and the API.

API: http://localhost:8091/api/hello
API Debugger: http://localhost:8091/api-debugger/

DynamoDB Local: http://localhost:8000

## Architecture direction

This service is the future `hq-API` boundary for InterviewHQ. The crawler will communicate with this service over HTTP, while this service owns application-level access to the shared InterviewHQ DynamoDB data model.
