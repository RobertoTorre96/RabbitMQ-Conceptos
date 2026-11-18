#!/bin/sh
# Genera una CA de estudio y un certificado para "localhost" en esta carpeta.
# Lo ejecuta el servicio "certs" del docker-compose. Si los certificados ya existen, no hace nada.
set -e
cd /certs
if [ -f ca.pem ] && [ -f server.pem ] && [ -f server.key ]; then
  echo "Certificados ya generados: no se toca nada."
  exit 0
fi
apk add --no-cache openssl >/dev/null
openssl genrsa -out ca.key 2048
openssl req -x509 -new -key ca.key -days 365 -subj "/CN=Estudio CA" -out ca.pem
openssl genrsa -out server.key 2048
openssl req -new -key server.key -subj "/CN=localhost" -out server.csr
printf "subjectAltName=DNS:localhost,IP:127.0.0.1" > san.ext
openssl x509 -req -in server.csr -CA ca.pem -CAkey ca.key -CAcreateserial -days 365 -extfile san.ext -out server.pem
chmod 644 ca.pem server.pem server.key
rm -f server.csr san.ext ca.srl
echo "Listo: ca.pem, server.pem y server.key en docker/certs"
