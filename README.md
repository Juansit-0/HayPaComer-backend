# HayPaComer backend

The backend of **HayPaComer**, the smart fridge that answers "what can I cook right now with what is actually at home?". Work happens here and goes straight to `main`; the history up to version 1.1.0 comes from the monorepo [Juansit-0/HayPaComer](https://github.com/Juansit-0/HayPaComer), with each commit kept under its original author.

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

Requirements: Java 25, Maven 3.9+, Docker with Compose.

```bash
cp .env.example .env
docker compose up -d
mvn verify
```

The full instructions live in the monorepo README and `docs/`.
