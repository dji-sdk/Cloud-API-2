#!/bin/bash
# FlightHub MCP Server - stdio proxy via Docker
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

# Run in stdio mode inside Docker (linux/amd64 for JNI compatibility)
exec docker run --rm -i \
  --platform linux/amd64 \
  -e FH_TRANSPORT=stdio \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE=none \
  $ENV_ARGS \
  "$IMAGE" \
  --spring.profiles.active=stdio \
  --spring.main.web-application-type=none
