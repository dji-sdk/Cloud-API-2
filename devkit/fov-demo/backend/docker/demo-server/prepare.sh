#!/usr/bin/env bash
# 构建后端 Docker 镜像前的准备：复制 fat jar，并确保 DRTC 配置在 config/ 下供镜像 COPY。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
SERVER="$ROOT/backend/fhsdk-demo-server"
DIR="$(cd "$(dirname "$0")" && pwd)"
CONFIG_DIR="$DIR/config"
DRTC_DEST="$CONFIG_DIR/drtc_config_node.json"
DRTC_SRC="${SDK_DRTC_CONFIG:-$DRTC_DEST}"

mkdir -p "$CONFIG_DIR"
JAR="$SERVER/target/fhsdk-demo-server-0.1.0.jar"
if [[ ! -f "$JAR" ]]; then
  echo "missing $JAR — run: cd $SERVER && mvn -DskipTests package" >&2
  exit 1
fi
cp -f "$JAR" "$DIR/app.jar"

if [[ ! -f "$DRTC_SRC" ]]; then
  echo "missing DRTC config: $DRTC_SRC" >&2
  echo "  copy $DIR/drtc_config_node.json.example to $DRTC_DEST and set YOUR_FH_SERVER_HOST," >&2
  echo "  or export SDK_DRTC_CONFIG=/path/to/drtc_config_node.json from your FHSDK package." >&2
  exit 1
fi
if [[ "$DRTC_SRC" != "$DRTC_DEST" ]]; then
  cp -f "$DRTC_SRC" "$DRTC_DEST"
fi
echo "prepared app.jar + config/drtc_config_node.json"
