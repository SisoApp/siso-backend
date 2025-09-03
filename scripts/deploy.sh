#!/usr/bin/env bash
set -euo pipefail

APP_HOME="/opt/siso"
LOG_DIR="${APP_HOME}/logs"
COMPOSE_FILE="${APP_HOME}/docker-compose.yml"

mkdir -p "${LOG_DIR}"

echo "> Stop old containers"
docker compose -f "${COMPOSE_FILE}" down || true

echo "> Build and start new containers"
docker compose -f "${COMPOSE_FILE}" up -d --build

echo "> Wait for containers to stabilize"
sleep 5

echo "> Running containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo "> Deployment done"

##!/usr/bin/env bash
#set -euo pipefail
#
#APP_HOME="/opt/siso"
#LOG_DIR="${APP_HOME}/logs"
## JAR 경로: Actions가 build/libs를 번들에 넣어줌
#JAR_PATH="$(ls -1t ${APP_HOME}/build/libs/*.jar 2>/dev/null | grep -v -- '-plain' | head -n1 || true)"
#[ -z "${JAR_PATH}" ] && echo "ERROR: ${APP_HOME}/build/libs 에 *.jar 없음" && exit 1
#
#PID_FILE="${APP_HOME}/app.pid"
#PORT=8080
#PROFILE="prod"   # 운영 프로필 이름 (다르면 바꿔주세요)
#
#mkdir -p "${LOG_DIR}"
#
#echo "> stop old process if exists"
#if [ -f "${PID_FILE}" ]; then
#  OLD_PID="$(cat "${PID_FILE}")" || true
#  if [ -n "${OLD_PID:-}" ] && ps -p "${OLD_PID}" >/dev/null 2>&1; then
#    kill "${OLD_PID}" || true
#    for i in {1..20}; do
#      if ps -p "${OLD_PID}" >/dev/null 2>&1; then
#        sleep 1
#      else
#        break
#      fi
#      [ "$i" = "20" ] && kill -9 "${OLD_PID}" || true
#    done
#  fi
#fi
#
## 혹시 8080을 점유한 프로세스가 있으면 강제 종료
#if ss -lptn | grep -q ":${PORT} "; then
#  KPID=$(ss -lptn "sport = :${PORT}" | awk 'NR>1 {print $NF}' | sed -n 's/.*pid=\([0-9]\+\).*/\1/p' | head -n1)
#  [ -n "${KPID:-}" ] && kill -9 "${KPID}" || true
#fi
#
#echo "> start ${JAR_PATH} on :${PORT}"
#nohup java -jar -Duser.timezone=Asia/Seoul "${JAR_PATH}" \
#  --server.port=${PORT} \
#  --spring.profiles.active=${PROFILE} \
#  >> "${LOG_DIR}/app.log" 2>&1 &
#
#NEW_PID=$!
#echo "${NEW_PID}" > "${PID_FILE}"
#echo "> new pid: ${NEW_PID}"
#
## (선택) 짧게 대기, ALB 헬스체크가 대신 확인해줄 것
#sleep 5
#echo "> done"
