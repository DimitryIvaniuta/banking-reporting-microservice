#!/usr/bin/env python3
"""Parse ModSecurity JSON audit logs and emit a compact summary."""
from __future__ import annotations
import json, sys, collections, datetime as dt
from pathlib import Path

def iter_events(path: Path):
    for line in path.read_text(encoding='utf-8', errors='ignore').splitlines():
        line=line.strip()
        if not line:
            continue
        try:
            obj=json.loads(line)
        except Exception:
            continue
        if isinstance(obj, dict):
            yield obj
        elif isinstance(obj, list):
            for x in obj:
                if isinstance(x, dict):
                    yield x

def messages(ev):
    tx = ev.get('transaction', {}) or {}
    req = tx.get('request', {}) or {}
    for m in (tx.get('messages', []) or []):
        det = (m.get('details', {}) or {}) if isinstance(m, dict) else {}
        yield {'rule_id': str(det.get('ruleId', 'unknown')), 'severity': str(det.get('severity', 'unknown')), 'uri': req.get('uri', 'unknown')}

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: parse_modsec_audit.py <audit-log.json>', file=sys.stderr); sys.exit(1)
    sev=collections.Counter(); rule=collections.Counter(); uri=collections.Counter(); total=0
    for ev in iter_events(Path(sys.argv[1])):
        for m in messages(ev):
            total += 1; sev[m['severity']] += 1; rule[m['rule_id']] += 1; uri[m['uri']] += 1
    print(json.dumps({'generated_at_utc': dt.datetime.utcnow().isoformat()+'Z','events_total': total,'by_severity': sev,'top_rules': rule.most_common(10),'top_uris': uri.most_common(10)}, indent=2, default=dict))
