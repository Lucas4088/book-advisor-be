#!/bin/bash
set -e

mkdir -p /etc/mongo
cp /tmp/keyfile /etc/mongo/keyfile

chmod 600 /etc/mongo/keyfile
chown mongodb:mongodb /etc/mongo/keyfile

exec docker-entrypoint.sh mongod \
  --replSet rs0 \
  --keyFile /etc/mongo/keyfile \
  --bind_ip_all