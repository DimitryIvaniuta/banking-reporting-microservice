# banking-reporting-microservice

Production-grade starter for a **Banking Reporting Microservice** using Java 21, Spring Boot 4, JAX-RS (Jersey), Hibernate/JPA, PostgreSQL, Flyway, Kafka (KRaft), Redis, and Actuator health monitoring.

## Recommended GitHub repo
- **Name:** `banking-reporting-microservice`
- **Description:** Banking reporting microservice (Java 21, Spring Boot 4, JAX-RS/Jersey, PostgreSQL/Flyway, Kafka KRaft, Redis, Actuator health/metrics, tests, Postman).

## What it implements
- JAX-RS API endpoints for report generation and query
- PostgreSQL persistence + Flyway migrations
- Redis cache for report lookups
- Kafka event publication on successful generation
- Health monitoring (Actuator + custom HealthIndicator)
- Prometheus metrics endpoint
- Unit tests and Spring smoke test
- Docker Compose for local infra + Apache HTTP Server reverse proxy (httpd)

## Run locally
1. `docker compose up -d` (starts Postgres, Redis, Kafka, Apache HTTP Server on `localhost:8088`)
2. `mvn spring-boot:run` (Spring app on `localhost:8080`)
3. Call the API through Apache: `http://localhost:8088/api/...`

## Endpoints
- `POST /api/reports` (via Apache: `http://localhost:8088/api/reports`)
- `GET /api/reports/{id}`
- `GET /api/reports?accountId=1001`
- `GET /api/system/ping`
- `GET /actuator/health`
- `GET /actuator/prometheus`


## Authorization (role-based access for reports)
- Authentication: **HTTP Basic** (local/dev default) via Spring Security
- Roles:
  - `REPORT_VIEWER` -> can read/list reports
  - `REPORT_ADMIN` -> can generate reports (and also read/list)
- Default local users (configure in `src/main/resources/application.yml` under `app.security.users`):
  - `report_viewer / changeit-viewer`
  - `report_admin / changeit-admin`
- `GET /api/system/ping` remains public for connectivity checks

**Production recommendation:** replace local basic-auth users with OIDC/OAuth2 (e.g., Keycloak/Entra/Okta) and map external groups/claims to the same application roles.

## Production hardening next steps
- outbox pattern for Kafka publish reliability
- OAuth2/OIDC + RBAC
- async/background report jobs for large ranges
- partitioned transaction tables and archival
- encryption and retention policies for exported reports


## Apache HTTP Server (recommended ingress on Windows 11 + Docker Desktop)
- Included as `apache-httpd` service in `docker-compose.yml`
- Proxies `/api/*` and `/actuator/*` to the Spring app on host port `8080`
- Adds security headers and basic request hardening limits
- Config files: `apache/app-proxy.conf`, `apache/README.md`

### Why Apache here
- Reverse proxy / TLS termination / security headers / access logs
- Keeps Kafka, Redis and Postgres internal (not exposed through Apache)

### Postman
- Collection default `baseUrl` is configured to Apache proxy: `http://localhost:8088`
- Switch to `http://localhost:8080` if you want to call Spring directly


## Edge hardening upgrade (Apache HTTP Server)
### What was added
- **HTTPS/TLS local dev** on `https://localhost:8443` using self-signed certs (generated from `apache/certs/generate-dev-cert.ps1` / `.sh`)
- **Separate public/private vhosts**:
  - Public vhost (80/443): proxies **only** `/api/*`
  - Private vhost (9090): exposes selected `/actuator/*` endpoints and is published to **localhost only**
- **Hardening defaults**: request limits, `mod_reqtimeout`, security headers, TLS protocol/cipher restrictions
- **Rate limiting template**:
  - `mod_ratelimit` response throttling example for large export endpoints
  - ModSecurity / OWASP CRS **ready template** for per-IP/request protections (requires custom httpd image with `mod_security2`)

### Local HTTPS setup (Windows 11)
1. Generate self-signed certs:
   - PowerShell: `./apache/certs/generate-dev-cert.ps1`
   - or Git Bash/WSL: `./apache/certs/generate-dev-cert.sh`
2. Start infra + Apache: `docker compose up -d`
3. Start app: `mvn spring-boot:run`
4. Call API via Apache TLS: `https://localhost:8443/api/...`
5. Private actuator (localhost only): `http://localhost:9090/actuator/health`

### Notes on rate limiting / WAF
- `mod_ratelimit` limits **response bandwidth**, not per-IP request rate. Apache docs define it as a transfer-rate filter. 
- For banking-grade request-rate protections and attack filtering, use **ModSecurity + OWASP CRS** (template included; enable in a custom Apache image). 


## Optional WAF edge (ModSecurity + OWASP CRS, DetectionOnly)

A custom Docker Apache image is included at `apache/waf/Dockerfile`, based on the official OWASP CRS Apache image, with startup logic that **forces ModSecurity `DetectionOnly` mode** and copies local CRS tuning snippets.

### Start optional WAF edge
```bash
# starts public WAF edge on 8089/8444 in addition to the existing Apache edge
Docker compose up -d apache-httpd-waf
```

- HTTPS WAF endpoint: `https://localhost:8444/api/...`
- HTTP WAF endpoint: `http://localhost:8089/api/...`
- WAF proxies to Spring app on host port `8080` via `host.docker.internal`
- Existing private Apache management vhost remains on `http://localhost:9090/actuator/*`

### DetectionOnly rollout notes
- WAF **observes and logs** CRS matches but does not block.
- Tune exclusions in:
  - `apache/waf/conf/REQUEST-900-EXCLUSION-RULES-BEFORE-CRS.conf`
  - `apache/waf/conf/RESPONSE-999-EXCLUSION-RULES-AFTER-CRS.conf`
- After tuning, migrate toward blocking mode by lowering anomaly thresholds and enabling rule engine `On`.

## WAF rollout profiles and audit dashboards
This repo includes staged ModSecurity migration profiles under `apache/waf/profiles/` and sample audit log parsing/dashboard assets under `observability/`.

## Added observability stack (Grafana/Loki/Prometheus + ModSecurity exporter)
Start security observability stack:
```bash
docker compose up -d modsec-exporter prometheus loki promtail grafana
```

Endpoints:
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9091
- Loki: http://localhost:3100
- ModSecurity exporter metrics: http://localhost:9910/metrics

## Canary WAF routing pattern
Two WAF containers are included for progressive rollout:
- Detection WAF: `apache-httpd-waf-detect` (ports 8089/8444)
- Blocking WAF canary: `apache-httpd-waf-block` (ports 8091/8445)
- Header/cookie canary router: `apache-canary-router` (port 8090)

Use `BRMSWAFID sticky cookie (auto-set by router)` to direct test traffic to the blocking WAF while the default flow remains DetectionOnly.


## Hash-based deterministic canary router (optional)

An optional Apache canary router is included at `apache-canary-router-hash` (port `8092`) that performs deterministic split using **mod_lua** and a sticky cookie. It supports a QA override header `X-Canary-Force: block|detect` (applied only when no sticky cookie exists), otherwise hash key precedence is `X-Canary-Key` -> `X-User-Id` -> `X-Forwarded-For` -> client IP. It injects `BRMSWAFID` (`.detect` / `.block`) and persists stickiness with a response cookie.

- Start it: `docker compose up -d apache-canary-router-hash`
- Call it: `http://localhost:8092/api/...`
- Default block canary percent: `10` (edit `SetEnv CANARY_BLOCK_PERCENT` in `apache/canary-router/httpd-hash.conf`)
- QA override examples (first request only before sticky cookie is set): `X-Canary-Force: block` or `X-Canary-Force: detect`

For local Windows/Docker Desktop testing, the existing weighted+sticky router on port `8090` is still the simplest default.
