#!/usr/bin/env python3
"""ModSecurity audit log exporter for Prometheus (simple file scanner)."""
import glob, json, os, re, time
from collections import Counter, defaultdict
from prometheus_client import start_http_server, Gauge

METRICS = {
    'modsec_audit_events_total': Gauge('modsec_audit_events_total', 'Total parsed ModSecurity audit events'),
    'modsec_alert_events_total': Gauge('modsec_alert_events_total', 'Events containing at least one message'),
    'modsec_blocked_events_total': Gauge('modsec_blocked_events_total', 'Events inferred as blocked/intercepted'),
    'modsec_last_event_timestamp_seconds': Gauge('modsec_last_event_timestamp_seconds', 'Unix timestamp of latest parsed event'),
    'modsec_inbound_anomaly_score': Gauge('modsec_inbound_anomaly_score', 'Latest inbound anomaly score observed'),
    'modsec_outbound_anomaly_score': Gauge('modsec_outbound_anomaly_score', 'Latest outbound anomaly score observed'),
}
RULE_HITS = Gauge('modsec_rule_hits_total', 'Rule hits by rule id', ['rule_id'])
SEV_HITS = Gauge('modsec_severity_hits_total', 'Rule hits by severity', ['severity'])
HOST_HITS = Gauge('modsec_host_events_total', 'Events by host header', ['host'])
URI_HITS = Gauge('modsec_uri_events_total', 'Events by URI path', ['uri'])

RULE_ID_RE = re.compile(r'\[id "(\d+)"\]')
SEV_RE = re.compile(r'\[severity "([^"]+)"\]')
IN_SCORE_RE = re.compile(r'Inbound Anomaly Score Exceeded \(Total Score: (\d+)\)')
OUT_SCORE_RE = re.compile(r'Outbound Anomaly Score Exceeded \(Total Score: (\d+)\)')


def iter_json_objects(fp):
    for line in fp:
        line=line.strip()
        if not line:
            continue
        try:
            yield json.loads(line)
            continue
        except Exception:
            pass
    

def parse_event(obj):
    tx = obj.get('transaction', {}) if isinstance(obj, dict) else {}
    req = tx.get('request', {}) or {}
    resp = tx.get('response', {}) or {}
    messages = tx.get('messages', []) or []
    host = (req.get('headers') or {}).get('Host', 'unknown')
    uri = req.get('uri', 'unknown')
    blocked = False
    if str(tx.get('disposition','')).lower() in {'deny','drop'}:
        blocked = True
    if str(resp.get('http_code','')).startswith(('4','5')):
        # heuristic only for WAF edge responses
        blocked = blocked or resp.get('http_code') in (403,406)
    in_score = out_score = 0
    rule_ids = []
    severities = []
    for m in messages:
        msg = m.get('message','') if isinstance(m, dict) else str(m)
        details = m.get('details', {}) if isinstance(m, dict) else {}
        rid = details.get('ruleId') or details.get('ruleId'.lower())
        sev = details.get('severity') or details.get('severity'.lower())
        if not rid:
            mo = RULE_ID_RE.search(msg)
            rid = mo.group(1) if mo else None
        if not sev:
            mo = SEV_RE.search(msg)
            sev = mo.group(1) if mo else 'unknown'
        if rid:
            rule_ids.append(str(rid))
        severities.append(str(sev or 'unknown'))
        mi = IN_SCORE_RE.search(msg)
        if mi:
            in_score = max(in_score, int(mi.group(1)))
        mo = OUT_SCORE_RE.search(msg)
        if mo:
            out_score = max(out_score, int(mo.group(1)))
    ts = tx.get('timeStamp') or tx.get('timestamp')
    if isinstance(ts, str):
        try:
            # many logs provide epoch as string
            ts = float(ts)
        except Exception:
            ts = time.time()
    elif ts is None:
        ts = time.time()
    return {
        'host': host, 'uri': uri, 'blocked': blocked, 'has_alerts': bool(messages),
        'in_score': in_score, 'out_score': out_score, 'rule_ids': rule_ids, 'severities': severities, 'ts': float(ts)
    }


def scan_once(pattern):
    counts = Counter()
    rule_counts = Counter()
    sev_counts = Counter()
    host_counts = Counter()
    uri_counts = Counter()
    last_ts = 0.0; max_in = 0; max_out = 0
    files = sorted(glob.glob(pattern))
    for path in files:
        try:
            with open(path, 'r', encoding='utf-8', errors='ignore') as f:
                for obj in iter_json_objects(f):
                    ev = parse_event(obj)
                    counts['events'] += 1
                    if ev['has_alerts']:
                        counts['alerts'] += 1
                    if ev['blocked']:
                        counts['blocked'] += 1
                    host_counts[ev['host']] += 1
                    uri_counts[ev['uri']] += 1
                    for rid in ev['rule_ids']:
                        rule_counts[rid] += 1
                    for sev in ev['severities']:
                        sev_counts[sev] += 1
                    last_ts = max(last_ts, ev['ts'])
                    max_in = max(max_in, ev['in_score'])
                    max_out = max(max_out, ev['out_score'])
        except FileNotFoundError:
            continue
        except Exception as e:
            print(f'WARN parse failed {path}: {e}', flush=True)
    return counts, rule_counts, sev_counts, host_counts, uri_counts, last_ts, max_in, max_out


def update_metrics():
    pattern = os.getenv('MODSEC_AUDIT_GLOB', '/var/log/modsecurity/*.log')
    interval = int(os.getenv('SCAN_INTERVAL_SECONDS', '15'))
    while True:
        counts, rule_counts, sev_counts, host_counts, uri_counts, last_ts, max_in, max_out = scan_once(pattern)
        METRICS['modsec_audit_events_total'].set(counts['events'])
        METRICS['modsec_alert_events_total'].set(counts['alerts'])
        METRICS['modsec_blocked_events_total'].set(counts['blocked'])
        METRICS['modsec_last_event_timestamp_seconds'].set(last_ts or 0)
        METRICS['modsec_inbound_anomaly_score'].set(max_in)
        METRICS['modsec_outbound_anomaly_score'].set(max_out)
        RULE_HITS.clear(); SEV_HITS.clear(); HOST_HITS.clear(); URI_HITS.clear()
        for k,v in rule_counts.items(): RULE_HITS.labels(rule_id=k).set(v)
        for k,v in sev_counts.items(): SEV_HITS.labels(severity=k).set(v)
        for k,v in host_counts.items(): HOST_HITS.labels(host=k).set(v)
        for k,v in uri_counts.items(): URI_HITS.labels(uri=k).set(v)
        time.sleep(interval)

if __name__ == '__main__':
    port = 9910
    start_http_server(port)
    print(f'modsec exporter listening on :{port}', flush=True)
    update_metrics()
