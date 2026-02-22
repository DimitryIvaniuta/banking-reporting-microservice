#!/bin/sh
set -eu

copy_if_exists() {
  src="$1"
  dst="$2"
  if [ -f "$src" ]; then
    cp "$src" "$dst" || true
  fi
}

# Force ModSecurity engine to DetectionOnly in common config locations used by Apache/CRS images.
for f in \
  /etc/modsecurity.d/modsecurity.conf \
  /etc/modsecurity/modsecurity.conf \
  /usr/local/apache2/conf/modsecurity.conf \
  /etc/apache2/modsecurity.d/modsecurity.conf \
  /etc/modsecurity.d/include.conf
 do
  if [ -f "$f" ]; then
    sed -i 's/^SecRuleEngine .*/SecRuleEngine DetectionOnly/g' "$f" || true
  fi
 done

# Install local banking API tuning snippets if CRS rule directories exist.
for dir in \
  /etc/modsecurity.d/owasp-crs/rules \
  /etc/modsecurity/owasp-crs/rules \
  /opt/owasp-crs/rules
 do
  if [ -d "$dir" ]; then
    copy_if_exists /opt/brms-waf/REQUEST-900-EXCLUSION-RULES-BEFORE-CRS.conf "$dir/REQUEST-900-EXCLUSION-RULES-BEFORE-CRS.conf"
    copy_if_exists /opt/brms-waf/RESPONSE-999-EXCLUSION-RULES-AFTER-CRS.conf "$dir/RESPONSE-999-EXCLUSION-RULES-AFTER-CRS.conf"
  fi
 done

# Helpful startup banner
printf '%s\n' '[brms-waf] ModSecurity+OWASP CRS image starting in DetectionOnly mode (best-effort enforced).' >&2

# Chain to original entrypoint/CMD from base image
# shellcheck disable=SC2068
exec /docker-entrypoint.sh $@

PROFILE="${WAF_PROFILE:-detection}"

apply_engine_mode() {
  mode="$1"
  for f in \
    /etc/modsecurity.d/modsecurity.conf \
    /etc/modsecurity/modsecurity.conf \
    /usr/local/apache2/conf/modsecurity.conf \
    /etc/apache2/modsecurity.d/modsecurity.conf \
    /etc/modsecurity.d/include.conf
  do
    if [ -f "$f" ]; then
      sed -i "s/^SecRuleEngine .*/SecRuleEngine ${mode}/g" "$f" || true
      grep -q '^SecAuditEngine ' "$f" && sed -i 's/^SecAuditEngine .*/SecAuditEngine RelevantOnly/g' "$f" || true
      grep -q '^SecAuditLogFormat ' "$f" && sed -i 's/^SecAuditLogFormat .*/SecAuditLogFormat JSON/g' "$f" || true
    fi
  done
}

case "$PROFILE" in
  detection)
    export ANOMALY_INBOUND="${ANOMALY_INBOUND:-10000}"
    export ANOMALY_OUTBOUND="${ANOMALY_OUTBOUND:-10000}"
    apply_engine_mode "DetectionOnly"
    ;;
  blocking-canary|blocking-tuned|blocking-strict)
    apply_engine_mode "On"
    ;;
  *)
    printf '%s\n' "[brms-waf] Unknown WAF_PROFILE=$PROFILE. Defaulting to detection." >&2
    export ANOMALY_INBOUND="${ANOMALY_INBOUND:-10000}"
    export ANOMALY_OUTBOUND="${ANOMALY_OUTBOUND:-10000}"
    apply_engine_mode "DetectionOnly"
    ;;
esac

printf '%s\n' "[brms-waf] WAF profile=${PROFILE}; PARANOIA=${PARANOIA:-unset}; BLOCKING_PARANOIA=${BLOCKING_PARANOIA:-unset}; INBOUND=${ANOMALY_INBOUND:-unset}; OUTBOUND=${ANOMALY_OUTBOUND:-unset}" >&2
