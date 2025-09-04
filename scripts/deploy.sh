#!/usr/bin/env bash
set -euo pipefail

cd /opt/siso
docker compose down || true
docker compose up -d --build

APP_HOME="/opt/siso"
COMPOSE_FILE="${APP_HOME}/docker-compose.yml"
ENV_FILE="${APP_HOME}/.env"
LOG_DIR="${APP_HOME}/logs"

mkdir -p "${LOG_DIR}"

echo "> Validate compose with env"
# .env은 CodeDeploy가 /opt/siso로 내려주고, 권한/퍼미션은 appspec대로 root로 실행됨
# (appspec: ApplicationStart에서 runas: root) :contentReference[oaicite:1]{index=1}
[ -r "${ENV_FILE}" ] || { echo "ENV missing: ${ENV_FILE}"; exit 14; }

echo "> Stop old containers"
docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" down || true

echo "> Build and start new containers"
docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" up -d --build

echo "> Wait for containers to stabilize"
sleep 5

echo "> Running containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo "> Deployment done"

