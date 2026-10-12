#!/bin/bash
# FlightHub MCP Server launcher — SSE 模式（本地调试用）
#
# 用于配合 Claude Desktop / Cursor 等 SSE 客户端本地调试。
# 生产部署请使用 docker-compose.yml。
#
# 使用方式：
#   1. 复制并填写 .env 文件：cp .env.example .env && vim .env
#   2. 运行：./run-mcp-sse.sh
#   3. 访问：http://localhost:8080/pf-devkit/ai-plugin/mcp/sse

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="${SCRIPT_DIR}/target/flighthub-mcp-server-1.0.0.jar"

if [[ ! -f "${JAR}" ]]; then
  echo "[ERROR] JAR not found: ${JAR}" >&2
  echo "Run: cd mcp && mvn clean package -DskipTests" >&2
  exit 1
fi

# 加载 .env 文件（如果存在）
ENV_FILE="${SCRIPT_DIR}/.env"
if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
  echo "[INFO] Loaded .env from ${ENV_FILE}" >&2
fi

# JVM 内存限制：限制堆和 Metaspace，避免与 Claude Code 等其他进程竞争内存
# SSE 模式比 stdio 多 ~100MB（WebFlux/Netty），所以上限设为 512m
JVM_OPTS="${JAVA_OPTS:- -Xms64m -Xmx512m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC}"

echo "[INFO] Starting FlightHub MCP Server (SSE mode)" >&2
echo "[INFO] JVM opts: ${JVM_OPTS}" >&2
echo "[INFO] SSE endpoint: http://localhost:${FH_SSE_PORT:-8080}/pf-devkit/ai-plugin/mcp/sse" >&2

exec java \
  ${JVM_OPTS} \
  -jar "${JAR}" \
  --spring.profiles.active=sse
