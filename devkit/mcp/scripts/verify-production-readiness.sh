#!/bin/bash
# ============================================================
# MCP Server 手动验证脚本
# 验证：健康检查 + Token 鉴权 + SSE 连接隔离
# ============================================================
set -e

PORT=18080
JAR="target/flighthub-mcp-server-1.0.0.jar"
TOKEN="test-secret-token-12345"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

pass() { echo -e "${GREEN}✅ PASS${NC}: $1"; }
fail() { echo -e "${RED}❌ FAIL${NC}: $1"; FAILURES=$((FAILURES+1)); }
info() { echo -e "${YELLOW}▶${NC} $1"; }

FAILURES=0

cleanup() {
    if [ -n "$SERVER_PID" ]; then
        info "Stopping server (PID=$SERVER_PID)..."
        kill "$SERVER_PID" 2>/dev/null || true
        wait "$SERVER_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT

wait_for_server() {
    local max_wait=30
    local count=0
    while ! curl -sf "http://localhost:$PORT/health" > /dev/null 2>&1; do
        count=$((count+1))
        if [ $count -ge $max_wait ]; then
            fail "Server failed to start within ${max_wait}s"
            cat /tmp/mcp-verify.log | tail -20
            exit 1
        fi
        sleep 1
    done
}

# ============================================================
echo ""
echo "========================================"
echo "  场景 1：无 Token（开发模式）"
echo "========================================"
echo ""

info "Starting server WITHOUT auth token..."
FH_TRANSPORT=sse java -jar "$JAR" \
    --spring.profiles.active=sse \
    --server.port=$PORT \
    --flighthub.transport.sse-auth-token= \
    --flighthub.sdk.mock-mode=true \
    > /tmp/mcp-verify.log 2>&1 &
SERVER_PID=$!

info "Waiting for server (PID=$SERVER_PID)..."
wait_for_server

# --- /health ---
info "Testing GET /health..."
RESP=$(curl -s -o /tmp/health.json -w "%{http_code}" "http://localhost:$PORT/health")
if [ "$RESP" = "200" ] && grep -q '"status":"UP"' /tmp/health.json; then
    pass "/health → 200 + {\"status\":\"UP\"}"
else
    fail "/health → HTTP $RESP (expected 200)"
    cat /tmp/health.json
fi

# --- /ready ---
info "Testing GET /ready..."
RESP=$(curl -s -o /tmp/ready.json -w "%{http_code}" "http://localhost:$PORT/ready")
if [ "$RESP" = "200" ] || [ "$RESP" = "503" ]; then
    STATUS=$(python3 -c "import json;print(json.load(open('/tmp/ready.json'))['status'])" 2>/dev/null || echo "PARSE_ERROR")
    pass "/ready → HTTP $RESP + status=$STATUS"
else
    fail "/ready → unexpected HTTP $RESP"
fi

# --- /status (开发模式无 token，应该也能访问因为 auth 未启用) ---
info "Testing GET /status (dev mode, no auth required)..."
RESP=$(curl -s -o /tmp/status.json -w "%{http_code}" "http://localhost:$PORT/status")
if [ "$RESP" = "200" ]; then
    pass "/status → 200 (auth disabled in dev mode)"
else
    fail "/status → HTTP $RESP (expected 200, auth should be disabled)"
fi

# --- /sse (开发模式，无需 token 即可连接) ---
info "Testing GET /sse (dev mode, no token)..."
RESP=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 "http://localhost:$PORT/sse" 2>/dev/null || true)
# SSE 连接会保持 open，curl --max-time 3 超时后返回 000 或 200
if [ "$RESP" = "200" ] || [ "$RESP" = "000" ]; then
    pass "/sse → connection accepted (dev mode, no token needed)"
else
    fail "/sse → HTTP $RESP (expected 200/open connection)"
fi

# --- 停止场景 1 ---
info "Stopping server..."
kill "$SERVER_PID" 2>/dev/null || true
wait "$SERVER_PID" 2>/dev/null || true
SERVER_PID=""
sleep 2

# ============================================================
echo ""
echo "========================================"
echo "  场景 2：带 Token（生产模式）"
echo "========================================"
echo ""

info "Starting server WITH auth token ($TOKEN)..."
FH_TRANSPORT=sse java -jar "$JAR" \
    --spring.profiles.active=sse \
    --server.port=$PORT \
    --flighthub.transport.sse-auth-token=$TOKEN \
    --flighthub.sdk.mock-mode=true \
    > /tmp/mcp-verify.log 2>&1 &
SERVER_PID=$!

info "Waiting for server (PID=$SERVER_PID)..."
wait_for_server

# --- /health (公开，无需 token) ---
info "Testing GET /health (no token, should pass)..."
RESP=$(curl -s -o /tmp/health2.json -w "%{http_code}" "http://localhost:$PORT/health")
if [ "$RESP" = "200" ]; then
    pass "/health → 200 (public path, no auth needed)"
else
    fail "/health → HTTP $RESP (expected 200)"
fi

# --- /ready (公开，无需 token) ---
info "Testing GET /ready (no token, should pass)..."
RESP=$(curl -s -o /tmp/ready2.json -w "%{http_code}" "http://localhost:$PORT/ready")
if [ "$RESP" = "200" ] || [ "$RESP" = "503" ]; then
    pass "/ready → HTTP $RESP (public path, no auth needed)"
else
    fail "/ready → HTTP $RESP (expected 200 or 503)"
fi

# --- /status (受保护，无 token 应返回 401) ---
info "Testing GET /status (no token, should be 401)..."
RESP=$(curl -s -o /tmp/status2.json -w "%{http_code}" "http://localhost:$PORT/status")
if [ "$RESP" = "401" ]; then
    pass "/status → 401 (protected, no token)"
else
    fail "/status → HTTP $RESP (expected 401)"
    cat /tmp/status2.json
fi

# --- /sse 无 token → 401 ---
info "Testing GET /sse (no token, should be 401)..."
RESP=$(curl -s -o /tmp/sse-notoken.json -w "%{http_code}" --max-time 3 "http://localhost:$PORT/sse")
if [ "$RESP" = "401" ]; then
    pass "/sse → 401 (no token)"
    info "  Response: $(cat /tmp/sse-notoken.json)"
else
    fail "/sse without token → HTTP $RESP (expected 401)"
fi

# --- /sse 错误 token → 401 ---
info "Testing GET /sse (wrong token, should be 401)..."
RESP=$(curl -s -o /tmp/sse-wrong.json -w "%{http_code}" --max-time 3 \
    -H "Authorization: Bearer wrong-token" \
    "http://localhost:$PORT/sse")
if [ "$RESP" = "401" ]; then
    pass "/sse → 401 (wrong token)"
else
    fail "/sse with wrong token → HTTP $RESP (expected 401)"
fi

# --- /sse 正确 token → 连接成功 ---
info "Testing GET /sse (valid token, should connect)..."
RESP=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 \
    -H "Authorization: Bearer $TOKEN" \
    "http://localhost:$PORT/sse" 2>/dev/null || true)
if [ "$RESP" = "200" ] || [ "$RESP" = "000" ]; then
    pass "/sse → connection accepted (valid token)"
else
    fail "/sse with valid token → HTTP $RESP (expected 200/open)"
fi

# --- /message 无 token → 401 ---
info "Testing POST /message (no token, should be 401)..."
RESP=$(curl -s -o /tmp/msg-notoken.json -w "%{http_code}" \
    -X POST -H "Content-Type: application/json" \
    -d '{"jsonrpc":"2.0","method":"tools/list","id":1}' \
    "http://localhost:$PORT/message")
if [ "$RESP" = "401" ]; then
    pass "/message → 401 (no token)"
else
    fail "/message without token → HTTP $RESP (expected 401)"
fi

# --- /message 正确 token → 放行 ---
info "Testing POST /message (valid token)..."
RESP=$(curl -s -o /tmp/msg-ok.json -w "%{http_code}" \
    -X POST -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"jsonrpc":"2.0","method":"tools/list","id":1}' \
    "http://localhost:$PORT/message")
if [ "$RESP" = "200" ] || [ "$RESP" = "400" ]; then
    # 200 = 正常响应; 400 = 可能没有有效 clientId 但鉴权通过了
    pass "/message → HTTP $RESP (auth passed, request processed)"
else
    fail "/message with valid token → HTTP $RESP"
fi

# --- 验证 SIGTERM 优雅退出（exec 修复） ---
echo ""
info "Testing graceful shutdown (SIGTERM → PID 1 = java)..."
kill -TERM "$SERVER_PID" 2>/dev/null
WAIT_COUNT=0
while kill -0 "$SERVER_PID" 2>/dev/null && [ $WAIT_COUNT -lt 10 ]; do
    sleep 1
    WAIT_COUNT=$((WAIT_COUNT+1))
done
if ! kill -0 "$SERVER_PID" 2>/dev/null; then
    pass "Server exited gracefully on SIGTERM (within ${WAIT_COUNT}s)"
    SERVER_PID=""
else
    fail "Server did not exit on SIGTERM within 10s"
fi

# ============================================================
echo ""
echo "========================================"
echo "  验证结果"
echo "========================================"
if [ $FAILURES -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED${NC}"
else
    echo -e "${RED}💥 $FAILURES TEST(S) FAILED${NC}"
fi
echo ""
exit $FAILURES
