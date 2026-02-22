# ModSecurity / OWASP CRS enablement (template)

This project ships a **ModSecurity-ready template** but does not force-enable it because the stock `httpd:2.4` image usually does not include `mod_security2`.

## Production approach
1. Build a custom Apache image with `mod_security2` enabled.
2. Mount OWASP CRS into `/usr/local/apache2/conf/modsecurity/`.
3. Copy/enable `modsecurity.conf.template` as `conf/extra/modsecurity.conf`.
4. Switch `SecRuleEngine DetectionOnly` -> `On` after tuning false positives.

## CRS references
Use OWASP CRS and stage in DetectionOnly first for APIs, then tighten rules and exclusions.
