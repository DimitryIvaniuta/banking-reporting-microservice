# ModSecurity audit log parsing and dashboards (sample)

Includes sample assets for a staged WAF rollout:
- `parse_modsec_audit.py` (local parser for JSON audit logs)
- `sample-audit-log.json` (example input)
- `../grafana/dashboards/modsecurity-audit-overview.sample.json`
- `../promtail/promtail-modsecurity-sample.yaml`

Example:
```bash
python observability/modsec/parse_modsec_audit.py observability/modsec/sample-audit-log.json
```

Production approach:
1. DetectionOnly baseline + dashboards
2. Canary blocking profile
3. Tuned blocking profile
4. Strict profile after exclusions and low false positives

## Prometheus exporter (real `modsec_*` metrics)
A local exporter is included in `observability/modsec-exporter` and exposes metrics on `:9910/metrics` by scanning JSON audit logs from `./apache/waf/logs`.

Sample metrics:
- `modsec_audit_events_total`
- `modsec_alert_events_total`
- `modsec_blocked_events_total`
- `modsec_rule_hits_total{rule_id="..."}`
- `modsec_severity_hits_total{severity="..."}`
- `modsec_uri_events_total{uri="..."}`
- `modsec_host_events_total{host="..."}`
