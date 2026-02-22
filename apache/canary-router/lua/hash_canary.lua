-- Deterministic canary assignment for Apache mod_lua.
-- Strategy:
--   1) Respect existing sticky cookie BRMSWAFID if present.
--   2) Optional QA override via X-Canary-Force: block|detect (highest priority when no sticky cookie).
--   3) Otherwise build a stable key from headers (X-Canary-Key, X-User-Id, X-Forwarded-For) or client_ip.
--   4) Hash key -> bucket [0..99].
--   5) Assign detect/block route based on CANARY_BLOCK_PERCENT (default 10).
--   6) Inject BRMSWAFID cookie into request so mod_proxy_balancer stickysession routes deterministically.
--   7) Expose env vars for response Set-Cookie + observability headers.

local function djb2(s)
  local h = 5381
  for i = 1, #s do
    h = ((h * 33) + string.byte(s, i)) % 4294967296
  end
  return h
end

local function trim(s)
  if not s then return nil end
  return (s:gsub('^%s+', ''):gsub('%s+$', ''))
end

local function first_csv_token(v)
  if not v then return nil end
  local token = v:match('([^,]+)')
  return trim(token)
end

function canary_fixups(r)
  if not r.uri or not r.uri:match('^/api/') then
    return apache2.DECLINED
  end

  local cookie = r.headers_in['Cookie']
  if cookie and cookie:match('BRMSWAFID=%.(detect|block)') then
    r.subprocess_env['CANARY_MODE'] = 'sticky-existing'
    return apache2.DECLINED
  end

  local force = trim(r.headers_in['X-Canary-Force'])
  if force then force = force:lower() end

  local key = r.headers_in['X-Canary-Key']
  if not key or key == '' then key = r.headers_in['X-User-Id'] end
  if not key or key == '' then key = first_csv_token(r.headers_in['X-Forwarded-For']) end
  if not key or key == '' then key = r.useragent_ip or r.connection.client_ip end
  if not key or key == '' then key = 'unknown' end

  local pct = tonumber(r.subprocess_env['CANARY_BLOCK_PERCENT'] or '10') or 10
  if pct < 0 then pct = 0 end
  if pct > 100 then pct = 100 end

  local bucket = djb2(key) % 100
  local route
  if force == 'block' or force == 'detect' then
    route = force
    r.subprocess_env['CANARY_MODE'] = 'qa-override'
    r.subprocess_env['CANARY_OVERRIDE'] = force
  else
    route = (bucket < pct) and 'block' or 'detect'
    r.subprocess_env['CANARY_MODE'] = 'hash-deterministic'
  end

  local injected = 'BRMSWAFID=.' .. route
  if cookie and cookie ~= '' then
    r.headers_in['Cookie'] = cookie .. '; ' .. injected
  else
    r.headers_in['Cookie'] = injected
  end

  r.subprocess_env['CANARY_ROUTE_ASSIGNED'] = route
  r.subprocess_env['CANARY_BUCKET'] = tostring(bucket)
  r.subprocess_env['CANARY_KEY_SOURCE'] =
    (r.headers_in['X-Canary-Key'] and 'X-Canary-Key') or
    (r.headers_in['X-User-Id'] and 'X-User-Id') or
    (r.headers_in['X-Forwarded-For'] and 'X-Forwarded-For') or
    'client_ip'

  return apache2.DECLINED
end
