#!/bin/bash
set -e

docker build \
  --no-cache \
  -t goos/openfire . &&
docker run \
  --name openfire --rm \
  -p 9090:9090 -p 5222:5222 -p 7777:7777 \
  -it -d goos/openfire &&
echo "Waiting for 10 seconds..."
sleep 10 &&
./setup.sh
