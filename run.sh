#!/usr/bin/env bash
set -e

BRANCH="$(git branch --show-current)"

if [ -z "$BRANCH" ]; then
  echo "Error: unable to determine current git branch."
  exit 1
fi

echo "Running branch: $BRANCH"

if [ -f .env ]; then
  echo "Loading .env..."
  set -a
  source .env
  set +a
fi

echo "Pulling latest $BRANCH..."
git pull origin "$BRANCH"

echo "Stopping existing containers..."
docker compose down

echo "Starting containers..."
docker compose up --build
