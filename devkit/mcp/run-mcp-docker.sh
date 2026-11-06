#!/bin/bash
# DJI Cloud API 2 MCP Server - stdio proxy via Docker
# This script allows Claude Code (stdio mode) to talk to the Docker container (SSE mode)
# by wrapping the Docker container's stdio transport.
#
# Usage in Claude Code MCP config:
#   "command": "bash",
#   "args": ["/path/to/run-mcp-docker.sh"]

set -e

CONTAINER_NAME="flighthub-mcp-stdio"
IMAGE="dji/flighthub-mcp:latest"

# Pass environment variables from Claude Code config
ENV_ARGS=""
[ -n "$FH_SERVER_ADDRESS" ] && ENV_ARGS="$ENV_ARGS -e FH_SERVER_ADDRESS=$FH_SERVER_ADDRESS"
[ -n "$FH_SERVER_PORT" ] && ENV_ARGS="$ENV_ARGS -e FH_SERVER_PORT=$FH_SERVER_PORT"
[ -n "$FH_ACCESS_TOKEN" ] && ENV_ARGS="$ENV_ARGS -e FH_ACCESS_TOKEN=$FH_ACCESS_TOKEN"
[ -n "$FH_WORKSPACE_UUID" ] && ENV_ARGS="$ENV_ARGS -e FH_WORKSPACE_UUID=$FH_WORKSPACE_UUID"
[ -n "$FH_MAX_SECURITY_LEVEL" ] && ENV_ARGS="$ENV_ARGS -e FH_MAX_SECURITY_LEVEL=$FH_MAX_SECURITY_LEVEL"
[ -n "$FH_OPERATOR_TOKEN" ] && ENV_ARGS="$ENV_ARGS -e FH_OPERATOR_TOKEN=$FH_OPERATOR_TOKEN"

# KMZ 本地路径上传：客户端（Claude Code）与容器在同一台机器上，
# 但容器内 /tmp 与宿主机 /tmp 不共享，需把客户端生成 KMZ 的目录只读挂进容器。
# 默认挂载宿主机 /tmp（SKILL.md 的默认输出目录），可用 FH_WAYLINE_HOST_DIR 覆盖。
# 容器内挂载点固定为 /data/waylines（不覆盖容器 /tmp，避免影响 JVM 临时文件），
# 因此容器化运行时提交路径应写 /data/waylines/<file>.kmz。
KMZ_HOST_DIR="${FH_WAYLINE_HOST_DIR:-/tmp}"

# Run in stdio mode inside Docker (image is a multi-arch manifest covering
# linux/amd64 + linux/arm64; Docker automatically pulls the layer matching
# the host platform, no --platform pin needed)
exec docker run --rm -i \
  -e FH_TRANSPORT=stdio \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE=none \
  -e FH_WAYLINE_UPLOAD_DIR=/data/waylines \
  -v "${KMZ_HOST_DIR}:/data/waylines:ro" \
  $ENV_ARGS \
  "$IMAGE" \
  --spring.profiles.active=stdio \
  --spring.main.web-application-type=none
