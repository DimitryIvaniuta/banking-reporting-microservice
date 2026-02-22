FROM owasp/modsecurity-crs:apache

USER root

# Custom entrypoint to force DetectionOnly across possible config paths and copy local tuning snippets.
COPY apache/waf/entrypoint.sh /usr/local/bin/brms-waf-entrypoint.sh
COPY apache/waf/conf/REQUEST-900-EXCLUSION-RULES-BEFORE-CRS.conf /opt/brms-waf/REQUEST-900-EXCLUSION-RULES-BEFORE-CRS.conf
COPY apache/waf/conf/RESPONSE-999-EXCLUSION-RULES-AFTER-CRS.conf /opt/brms-waf/RESPONSE-999-EXCLUSION-RULES-AFTER-CRS.conf
RUN chmod +x /usr/local/bin/brms-waf-entrypoint.sh

USER 101
ENTRYPOINT ["/usr/local/bin/brms-waf-entrypoint.sh"]
