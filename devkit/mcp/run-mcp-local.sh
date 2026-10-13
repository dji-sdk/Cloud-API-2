#!/bin/bash
# FlightHub MCP Server launcher for Claude Code (stdio mode)
# Filters stdout to only pass JSON-RPC lines (starting with '{'),
# redirecting JVM warnings and other non-JSON output to stderr.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

exec java \
  -Xms64m -Xmx384m \
  -XX:MaxMetaspaceSize=128m \
  -XX:+UseG1GC \
  -jar "$SCRIPT_DIR/target/flighthub-mcp-server-1.0.0.jar" \
  --spring.profiles.active=stdio \
  --spring.main.web-application-type=none \
  2>&1 | while IFS= read -r line; do
    if [[ "$line" == \{* ]]; then
      echo "$line"     # JSON-RPC → stdout
    else
      echo "$line" >&2 # everything else → stderr
    fi
  done
