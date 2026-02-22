# Apache HTTP Server edge hardening (Windows 11 + Docker Desktop)

This project now uses **Apache HTTP Server** as the recommended edge proxy with:
- **Public vhosts**: `:80` (redirect) + `:443` (TLS, API only)
- **Private vhost**: `:9090` (selected Actuator endpoints only, localhost-published)
- TLS dev cert scripts for **self-signed localhost certificate**
- Hardening defaults (`mod_reqtimeout`, request size/field limits, security headers)
- ModSecurity / OWASP CRS **ready template** (disabled by default)

## Topology
- Apache in Docker Desktop (`apache-httpd`)
- Spring Boot app on host (`http://localhost:8080`)
- Apache proxies to `host.docker.internal:8080`

## Ports
- `8088` -> public HTTP (redirects to HTTPS)
- `8443` -> public HTTPS (API only)
- `9090` -> private Actuator vhost (**published to 127.0.0.1 only**)

## Files
- `httpd-local.conf` - main Apache config (loads modules, listeners, includes vhosts)
- `conf.d/vhosts-public.conf` - public HTTP/HTTPS vhosts, blocks `/actuator`
- `conf.d/vhosts-private.conf` - private management vhost for selected Actuator endpoints
- `certs/generate-dev-cert.ps1` / `.sh` - self-signed cert generation scripts
- `modsecurity/modsecurity.conf.template` - WAF template (requires `mod_security2`)

## Local setup (Windows PowerShell)
```powershell
./apache/certs/generate-dev-cert.ps1
# optional trust in Windows cert store manually if you want browser trust

docker compose up -d
mvn spring-boot:run
```

## Test URLs
- Public API via TLS: `https://localhost:8443/api/system/ping`
- Public HTTP redirect: `http://localhost:8088/api/system/ping` -> `https://localhost/...`
- Private actuator (localhost only): `http://localhost:9090/actuator/health`

## ModSecurity / CRS enablement (production)
The stock `httpd:2.4` image often does **not** include `mod_security2`. For production:
1. Build a custom image with `mod_security2`
2. Mount OWASP CRS rules
3. Enable `Include conf/extra/modsecurity.conf` in `httpd-local.conf`
4. Start with `SecRuleEngine DetectionOnly`, tune, then switch to `On`


## Optional custom WAF image (ModSecurity + OWASP CRS)

This project now includes `apache/waf/Dockerfile` which builds a **custom Apache WAF image** from the official `owasp/modsecurity-crs:apache` image.

What it does:
- ships ModSecurity + OWASP CRS preinstalled (in base image)
- forces **DetectionOnly** mode at container startup (best-effort across common config paths)
- copies BRMS-specific CRS tuning snippets into the CRS rules directory
- exposes HTTPS on `8444` (host) and HTTP on `8089` (host)

Use it as an **optional public edge WAF** in front of the app while keeping the existing hardened Apache vhosts/private management path available.

## Blocking-mode migration profiles (DetectionOnly → tuned blocking)

Profile env files live in `apache/waf/profiles/`:
- `waf-detection.env`
- `waf-blocking-canary.env`
- `waf-blocking-tuned.env`
- `waf-blocking-strict.env`

The `apache-httpd-waf` service now uses an `env_file`. Switch the file in `docker-compose.yml`, then restart:
```bash
docker compose up -d --build apache-httpd-waf
```

## Sample audit log parsing dashboards
- `observability/modsec/` - local parser + sample audit event
- `observability/grafana/dashboards/modsecurity-audit-overview.sample.json` - sample Grafana dashboard
- `observability/promtail/promtail-modsecurity-sample.yaml` - sample Loki ingestion pipeline

## Canary routing (dual WAF pattern)
A dedicated `apache-canary-router` service routes traffic to two separate WAF containers:
- `apache-httpd-waf-detect` (DetectionOnly)
- `apache-httpd-waf-block` (blocking canary profile)

Traffic is routed by a **weighted balancer** with a **sticky cookie**:
- initial assignment uses weighted split (default example: `detect=90`, `block=10`)
- Apache sets `BRMSWAFID` cookie after the first routed request
- subsequent requests from the same client stick to the same WAF backend

Tune canary percentage by editing `loadfactor` values in `apache/canary-router/httpd.conf` for:
- `route=detect` (stable path)
- `route=block` (canary blocking path)

Router endpoint: `http://localhost:8090/api/...`


## Hash-based deterministic canary router (optional)

An optional Apache canary router is included at `apache-canary-router-hash` (port `8092`) that performs deterministic split using **mod_lua** and a sticky cookie. It supports a QA override header `X-Canary-Force: block|detect` (applied only when no sticky cookie exists), otherwise hash key precedence is `X-Canary-Key` -> `X-User-Id` -> `X-Forwarded-For` -> client IP. It injects `BRMSWAFID` (`.detect` / `.block`) and persists stickiness with a response cookie.

- Start it: `docker compose up -d apache-canary-router-hash`
- Call it: `http://localhost:8092/api/...`
- Default block canary percent: `10` (edit `SetEnv CANARY_BLOCK_PERCENT` in `apache/canary-router/httpd-hash.conf`)
- QA override examples (first request only before sticky cookie is set): `X-Canary-Force: block` or `X-Canary-Force: detect`

For local Windows/Docker Desktop testing, the existing weighted+sticky router on port `8090` is still the simplest default.
