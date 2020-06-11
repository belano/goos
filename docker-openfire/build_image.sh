#!/bin/bash
set -e

docker build \
  --no-cache \
  -t goos/openfire .