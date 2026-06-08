#!/bin/bash

mkdir -p ssl

openssl req -x509 -newkey rsa:4096 -keyout ssl/server.key -out ssl/server.crt -sha256 -days 365 -nodes \
  -subj "/C=FR/ST=Normandie/L=Rouen/O=Dev/OU=Local/CN=localhost" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

echo "SSL certificates generated successfully in the ssl/ folder !"
