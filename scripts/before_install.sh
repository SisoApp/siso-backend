##!/usr/bin/env bash
#set -euo pipefail
#
## 디렉토리/권한
#mkdir -p /opt/siso/logs
#sudo chown -R ec2-user:ec2-user /opt/siso
#
## Nginx upstream 초기값(없으면 생성)
#if [ ! -f /etc/nginx/snippets/siso-upstream.conf ]; then
#  echo "server 127.0.0.1:8081;" | sudo tee /etc/nginx/snippets/siso-upstream.conf >/dev/null
#  sudo nginx -t && sudo systemctl reload nginx || true
#fi
#
#echo "[BeforeInstall] ok"
