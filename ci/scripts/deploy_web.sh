#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${SSH_HOST:-}" || -z "${SSH_USER:-}" ]]; then
  echo "Missing SSH_HOST or SSH_USER"
  exit 1
fi

APP_DIR="${APP_DIR:-/opt/health-tracker}"

ssh -o StrictHostKeyChecking=no "${SSH_USER}@${SSH_HOST}" "cd ${APP_DIR} && git pull && podman-compose up -d --build web"
