#!/usr/bin/env bash
set -euo pipefail

APP_HOME="/opt/siso"
ENV_FILE="${APP_HOME}/.env"
COMPOSE_FILE="${APP_HOME}/docker-compose.yml"
LOG_DIR="${APP_HOME}/logs"

mkdir -p "${LOG_DIR}"

# 0) .env 존재 확인
if [[ ! -f "${ENV_FILE}" ]]; then
  echo "ERROR: env file ${ENV_FILE} not found"
  exit 1
fi

# 1) compose 설정 검증(변수 확장 확인)
echo "> Validate compose with env"
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" config >/dev/null

echo "> Stop old containers"
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" down || true

echo "> Build and start new containers"
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" up -d --build --remove-orphans

# 2) (선택) 간단 안정화 대기
echo "> Wait for containers to stabilize"
sleep 5

echo "> Running containers:"
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" ps

# 3) (선택) 헬스체크 대기 루프: app, mariadb 둘 다 healthy 될 때까지(최대 60초)
services=("mariadb" "app")
deadline=$((SECONDS+60))
for svc in "${services[@]}"; do
  echo "> Waiting for ${svc} to be healthy..."
  while true; do
    # 헬스체크가 compose에 정의되어 있을 때만 동작
    status=$(docker inspect --format='{{json .State.Health.Status}}' "${svc}" 2>/dev/null || echo '"unknown"')
    [[ "${status}" == '"healthy"' ]] && break
    [[ ${SECONDS} -gt ${deadline} ]] && { echo "WARN: ${svc} not healthy (status=${status})"; break; }
    sleep 2
  done
done

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
