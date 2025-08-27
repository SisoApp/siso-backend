#!/usr/bin/env bash
set -euo pipefail

REPOSITORY="/opt/siso"

echo "> 새 JAR 검색"
JAR_NAME="$(ls -1t ${REPOSITORY}/*SNAPSHOT.jar 2>/dev/null | head -n1 || true)"
if [[ -z "${JAR_NAME}" ]]; then
  JAR_NAME="$(ls -1t ${REPOSITORY}/*.jar 2>/dev/null | head -n1 || true)"
fi

if [[ -z "${JAR_NAME}" ]]; then
  echo "ERROR: ${REPOSITORY}에 *.jar 파일을 찾지 못했습니다."
  exit 1
fi

JAR_BASE="$(basename "${JAR_NAME}")"
echo "> JAR NAME: ${JAR_NAME}"

echo "> 현재 구동 중인 애플리케이션 PID 확인(${JAR_BASE} 기준)"
CURRENT_PID="$(pgrep -f "${JAR_BASE}" || true)"

if [[ -z "${CURRENT_PID}" ]]; then
  echo "> 현재 구동 중인 애플리케이션이 없습니다."
else
  echo "> kill -15 ${CURRENT_PID}"
  kill -15 "${CURRENT_PID}" || true
  sleep 5
fi

echo "> 실행 권한 부여"
chmod +x "${JAR_NAME}"

echo "> 로그 디렉터리 준비"
mkdir -p "${REPOSITORY}/logs"

echo "> 애플리케이션 시작"
nohup java -jar -Duser.timezone=Asia/Seoul "${JAR_NAME}" >> "${REPOSITORY}/logs/app.log" 2>&1 &

echo "> 배포 완료"
