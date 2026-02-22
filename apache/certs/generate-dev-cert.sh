#!/usr/bin/env sh
set -eu
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CRT="$DIR/dev-localhost.crt"
KEY="$DIR/dev-localhost.key"
if [ -f "$CRT" ] && [ -f "$KEY" ]; then
  echo "Dev cert already exists: $CRT"
  exit 0
fi
openssl req -x509 -nodes -newkey rsa:2048 -sha256 -days 365 \
  -keyout "$KEY" -out "$CRT" \
  -subj "/C=PL/ST=Pomorskie/L=Gdansk/O=LocalDev/OU=Engineering/CN=localhost" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"
echo "Generated: $CRT and $KEY"
