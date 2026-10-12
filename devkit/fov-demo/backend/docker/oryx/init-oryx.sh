#!/usr/bin/env bash
# 首次启动 Oryx 后执行：初始化管理密码并打印推流 secret
set -euo pipefail

ORYX_URL="${ORYX_URL:-"http://127.0.0.1:12022"}"
PASSWORD="${ORYX_PASSWORD:-CHANGE_ME_BEFORE_DEPLOY}"

echo "==> 检查 Oryx 是否已初始化..."
INIT=$(curl -sf "$ORYX_URL/terraform/v1/mgmt/init" -X POST \
  -H 'Content-Type: application/json' -d "{\"password\":\"$PASSWORD\"}" || true)

if echo "$INIT" | grep -q '"token"'; then
  BEARER=$(echo "$INIT" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['bearer'])")
  echo "    首次初始化成功"
else
  echo "    已初始化，使用密码登录..."
  LOGIN=$(curl -sf "$ORYX_URL/terraform/v1/mgmt/login" -X POST \
    -H 'Content-Type: application/json' -d "{\"password\":\"$PASSWORD\"}")
  BEARER=$(echo "$LOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['bearer'])")
fi

SECRET=$(curl -sf "$ORYX_URL/terraform/v1/hooks/srs/secret/query" -X POST \
  -H "Authorization: Bearer $BEARER" \
  -H 'Content-Type: application/json' -d '{}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['publish'])")

echo ""
echo "Oryx 推流 secret: $SECRET"
echo ""
echo "请将以下内容写入 fhsdk-demo-server/src/main/resources/application.yml："
echo "  oryx.publish-secret: $SECRET"
echo ""
echo "测试 WHIP 推流（需 FFmpeg ≥ 8.0，支持 -f whip）："
echo "  ffmpeg -re -f lavfi -i testsrc=size=640x480:rate=25 -c:v libx264 -preset ultrafast -tune zerolatency -an -t 10 \\"
echo "    -f whip \"http://127.0.0.1:12022/rtc/v1/whip/?app=live&stream=demo_test&secret=$SECRET\""
echo ""
echo "WHEP 播放 URL："
echo "  http://127.0.0.1:12022/rtc/v1/whep/?app=live&stream=demo_test"
