param(
  [string]$OutDir = $PSScriptRoot
)
$crt = Join-Path $OutDir 'dev-localhost.crt'
$key = Join-Path $OutDir 'dev-localhost.key'
if ((Test-Path $crt) -and (Test-Path $key)) {
  Write-Host "Dev cert already exists: $crt"
  exit 0
}
if (-not (Get-Command openssl -ErrorAction SilentlyContinue)) {
  Write-Error "OpenSSL not found. Install via Chocolatey (openssl.light) or Git for Windows, then rerun."
  exit 1
}
& openssl req -x509 -nodes -newkey rsa:2048 -sha256 -days 365 `
  -keyout $key -out $crt `
  -subj '/C=PL/ST=Pomorskie/L=Gdansk/O=LocalDev/OU=Engineering/CN=localhost' `
  -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1'
Write-Host "Generated: $crt and $key"
