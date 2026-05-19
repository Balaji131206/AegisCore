# Deployment Documentation

**Project:** Distributed Multithreaded Secure Server Platform
**Version:** 0.2.0 | **Date:** 2026-05-18

---

## Current Stage: Local Development Only

The server runs on a single machine. No containerization, no cloud. This document records current deployment steps and projects forward to production deployment at Level 15.

---

## 1. Local Deployment (Level 1–3)

### Prerequisites
- Java 21 JDK: `java --version` → `openjdk 21.x.x`
- All files in same directory or on classpath

### Compile
```bash
cd src
javac Server.java ClientHandler.java Client.java
```

### Run Server
```bash
java Server
# Expected: Server is listening on port 5000
```

### Run Client (repeat in multiple terminals)
```bash
java Client
# Expected: Connected to server!
```

### Stop Server
Press `Ctrl+C` in server terminal. Note: no graceful shutdown at Level 2 — sockets are forcibly closed.

---

## 2. Planned Deployment Stages

| Level | Deployment Model | Technology |
|-------|-----------------|-----------|
| 4 | Maven build, JAR packaging | `mvn package` |
| 6 | Config file, env vars | `.env` + `application.properties` |
| 7 | PostgreSQL setup, schema migration | Flyway migrations |
| 8 | SSL cert generation | OpenSSL + Java keystore |
| 15 | Docker containerization | `Dockerfile` + `docker-compose.yml` |
| 15 | Kubernetes deployment | `deployment.yaml` + `service.yaml` |
| 15 | CI/CD pipeline | GitHub Actions |

---

## 3. Docker Deployment Plan (Level 15)

```dockerfile
# Dockerfile (planned)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/server.jar server.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "server.jar"]
```

```yaml
# docker-compose.yml (planned)
version: '3.9'
services:
  server:
    build: .
    ports:
      - "5000:5000"
    environment:
      - SERVER_PORT=5000
      - DB_URL=jdbc:postgresql://db:5432/serverdb
    depends_on:
      - db

  db:
    image: postgres:16
    environment:
      POSTGRES_DB: serverdb
      POSTGRES_USER: serveruser
      POSTGRES_PASSWORD: securepassword
```

---

## 4. Environment Variables (Level 4+)

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `5000` | TCP port to bind |
| `MAX_CLIENTS` | `200` | Thread pool size |
| `DB_URL` | — | PostgreSQL JDBC URL |
| `DB_USER` | — | Database username |
| `DB_PASS` | — | Database password |
| `JWT_SECRET` | — | HMAC key for JWT signing |
| `LOG_LEVEL` | `INFO` | Logging verbosity |

---

## 5. Monitoring Plan (Level 15)

| Tool | Purpose |
|------|---------|
| Prometheus | Metrics: connections/sec, active threads, error rate |
| Grafana | Dashboards for all server metrics |
| ELK Stack | Centralized log aggregation |
| GitHub Actions | CI: build → test → deploy on push |
