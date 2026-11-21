# HayPaComer backend

The backend of **HayPaComer**, the smart fridge that answers "what can I cook right now with what is actually at home?". Work happens here and goes straight to `main`; the history up to version 1.1.0 comes from the monorepo [Juansit-0/HayPaComer](https://github.com/Juansit-0/HayPaComer), with each commit kept under its original author.

Live: https://haypacomer.jenilarper.dev (API docs at [/docs](https://haypacomer.jenilarper.dev/docs)).

## What is here

The Maven multi-module backend and its tests:

| Module | Responsibility |
| --- | --- |
| `domain` | Framework-free model: fridge tree, quantities, inventory, cold chain, cooking, planning |
| `application` | Use cases and ports; one public method per use case |
| `adapter-persistence` | PostgreSQL and Redis adapters, Flyway migrations |
| `adapter-sensors` | ESP32 event decoding, hardware factories, simulators |
| `adapter-ai` | LLM clients, offline rule engine, resilience (circuit breaker) |
| `adapter-notifications` | Inbox, Telegram and log channels |
| `agent` | Framework-free agent runtime, tools, guardrails, supervisor |
| `web` | Spring Boot application, REST API, security, static UI |

The web UI lives in `HayPaComer-web`; the migrations are also kept in `HayPaComer-db`.

## Build

Requirements: Java 25, Maven 3.9+, Docker with Compose, and sibling clones of `HayPaComer-db` and `HayPaComer-web`.

```bash
# 1. PostgreSQL and Redis (docker-compose.yml and .env.example live in HayPaComer-db)
cd ../HayPaComer-db
cp .env.example .env
docker compose up -d
cd ../HayPaComer-backend

# 2. Web UI: copy HayPaComer-web into the static folder and its assets/ into brand/assets
mkdir -p web/src/main/resources/static brand/assets
cp -R ../HayPaComer-web/assets/. brand/assets/
rsync -a --exclude assets --exclude tools --exclude package.json --exclude README.md ../HayPaComer-web/ web/src/main/resources/static/

# 3. Build and test
mvn verify
```

Set `JWT_SECRET` (32+ bytes) in the environment before running the app. The production image does step 2 by itself (its Dockerfile downloads the `HayPaComer-web` `main` tarball), and production deployment is described in the runbook in `HayPaComer-docs` (`docs/runbook.md`).
