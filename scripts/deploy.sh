#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="/opt/siso"
ACTIVE_FILE="/etc/nginx/snippets/siso-upstream.conf"

# 빌드 산출물 위치: Actions가 build/libs를 번들에 포함시킴
JAR_PATH="$(ls -1t ${REPO_DIR}/build/libs/*.jar 2>/dev/null | head -n1 || true)"
[ -z "${JAR_PATH}" ] && echo "ERROR: ${REPO_DIR}/build/libs 에 *.jar 없음" && exit 1

ACTIVE_PORT="$(grep -oE '127\.0\.0\.1:[0-9]+' ${ACTIVE_FILE} | cut -d: -f2 || echo 8081)"
if [ "${ACTIVE_PORT}" = "8081" ]; then IDLE_PORT=8082; else IDLE_PORT=8081; fi
echo "> 활성:${ACTIVE_PORT} / 대기:${IDLE_PORT}"

# 1) 대기 포트 종료(있다면)
IDLE_PID="$(pgrep -fa "java .* --server.port=${IDLE_PORT}" | awk '{print $1}' || true)"
[ -n "${IDLE_PID}" ] && { echo "> kill ${IDLE_PID}"; kill -15 "${IDLE_PID}" || true; sleep 2; }

# 2) 새 버전 기동 (Java 17)
echo "> start ${JAR_PATH} on :${IDLE_PORT}"
nohup java -jar -Duser.timezone=Asia/Seoul "${JAR_PATH}" \
  --server.port=${IDLE_PORT} \
  >> "${REPO_DIR}/logs/app-${IDLE_PORT}.log" 2>&1 &

# 3) 헬스체크 (Actuator 권장)
HEALTH_URL="http://127.0.0.1:${IDLE_PORT}/actuator/health"
for i in {1..20}; do
  sleep 3
  if curl -sf "${HEALTH_URL}" | grep -q '"status":"UP"'; then
    echo "> health OK"
    break
  fi
  [ "$i" = "20" ] && { echo "ERROR: healthcheck fail ${HEALTH_URL}"; exit 1; }
done

# 4) Nginx 전환
echo "server 127.0.0.1:${IDLE_PORT};" | sudo tee "${ACTIVE_FILE}" >/dev/null
sudo nginx -t && sudo systemctl reload nginx
echo "> switched -> ${IDLE_PORT}"

# 5) 이전 포트 종료
OLD_PID="$(pgrep -fa "java .* --server.port=${ACTIVE_PORT}" | awk '{print $1}' || true)"
[ -n "${OLD_PID}" ] && { echo "> kill old ${OLD_PID}"; kill -15 "${OLD_PID}" || true; }

echo "> DONE"