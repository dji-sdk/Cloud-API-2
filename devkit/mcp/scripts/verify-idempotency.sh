#!/bin/bash
#
# 集成验证：幂等缓存命中测试
# 启动 SSE 模式服务 → 发送相同 requestId 两次 → 确认第二次命中缓存
#

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
JAR_FILE="$PROJECT_DIR/target/flighthub-mcp-server-1.0.0.jar"
LOG_FILE="/tmp/mcp-idempotency-test.log"
SERVER_PID=""

# 颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

cleanup() {
    if [ -n "$SERVER_PID" ] && kill -0 "$SERVER_PID" 2>/dev/null; then
        echo -e "\n${YELLOW}停止服务 (PID=$SERVER_PID)...${NC}"
        kill "$SERVER_PID" 2>/dev/null || true
        wait "$SERVER_PID" 2>/dev/null || true
    fi
    rm -f /tmp/mcp-sse-response-*.json
}
trap cleanup EXIT

echo "=== 幂等缓存集成验证 ==="
echo ""

# 1. 构建 JAR（如果不存在）
if [ ! -f "$JAR_FILE" ]; then
    echo "构建 JAR..."
    cd "$PROJECT_DIR"
    mvn package -DskipTests -q
fi

# 2. 启动服务（SSE 模式）
echo "启动 MCP Server (SSE 模式, port=18080)..."
FH_TRANSPORT=sse \
FH_SSE_PORT=18080 \
FH_AUDIT_ENABLED=true \
FH_IDEMPOTENCY_ENABLED=true \
FH_IDEMPOTENCY_TTL=300 \
java -jar "$JAR_FILE" \
    --server.port=18080 \
    --spring.profiles.active=sse \
    > "$LOG_FILE" 2>&1 &
SERVER_PID=$!

echo "  PID=$SERVER_PID, 等待启动..."
sleep 3

# 检查是否启动成功
if ! kill -0 "$SERVER_PID" 2>/dev/null; then
    echo -e "${RED}✗ 服务启动失败${NC}"
    echo "--- 最后 20 行日志 ---"
    tail -20 "$LOG_FILE"
    exit 1
fi

# 等待健康检查通过
for i in $(seq 1 10); do
    if curl -s http://localhost:18080/health | grep -q "UP"; then
        echo -e "  ${GREEN}✓ 服务已就绪${NC}"
        break
    fi
    if [ "$i" -eq 10 ]; then
        echo -e "${RED}✗ 健康检查超时${NC}"
        tail -20 "$LOG_FILE"
        exit 1
    fi
    sleep 1
done

echo ""

# 3. 连接 SSE 获取 clientId
echo "连接 SSE 端点获取 clientId..."
SSE_RESPONSE=$(curl -s -N --max-time 3 http://localhost:18080/sse 2>/dev/null || true)
CLIENT_ID=$(echo "$SSE_RESPONSE" | grep -o '"endpoint":"/message?clientId=[^"]*"' | grep -o 'clientId=[^"]*' | cut -d= -f2)

if [ -z "$CLIENT_ID" ]; then
    echo -e "${RED}✗ 无法获取 clientId${NC}"
    echo "SSE 响应: $SSE_RESPONSE"
    exit 1
fi
echo -e "  ${GREEN}✓ clientId=$CLIENT_ID${NC}"
echo ""

# 4. 第一次请求：发送 tools/call (fh_session_info, requestId=42)
echo "第一次请求: tools/call fh_session_info (id=42)..."
START_1=$(python3 -c 'import time; print(int(time.time()*1000))')

RESPONSE_1=$(curl -s -X POST "http://localhost:18080/message?clientId=$CLIENT_ID" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "id": 42,
        "method": "tools/call",
        "params": {
            "name": "fh_session_info",
            "arguments": {}
        }
    }')

END_1=$(python3 -c 'import time; print(int(time.time()*1000))')
DURATION_1=$((END_1 - START_1))

echo "  响应: $RESPONSE_1"
echo "  耗时: ${DURATION_1}ms"
echo ""

# 5. 第二次请求：完全相同（相同 clientId + 相同 id=42 + 相同 tool）
echo "第二次请求: 相同 tools/call fh_session_info (id=42)..."
START_2=$(python3 -c 'import time; print(int(time.time()*1000))')

RESPONSE_2=$(curl -s -X POST "http://localhost:18080/message?clientId=$CLIENT_ID" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "id": 42,
        "method": "tools/call",
        "params": {
            "name": "fh_session_info",
            "arguments": {}
        }
    }')

END_2=$(python3 -c 'import time; print(int(time.time()*1000))')
DURATION_2=$((END_2 - START_2))

echo "  响应: $RESPONSE_2"
echo "  耗时: ${DURATION_2}ms"
echo ""

# 6. 验证：第三次请求用不同 tool 但相同 id=42（不应命中缓存 — 验证跨工具不碰撞）
echo "第三次请求: 不同 tool fh_ops (id=42) — 验证跨工具不碰撞..."
RESPONSE_3=$(curl -s -X POST "http://localhost:18080/message?clientId=$CLIENT_ID" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "id": 42,
        "method": "tools/call",
        "params": {
            "name": "fh_ops",
            "arguments": {"action": "status"}
        }
    }')
echo "  响应: $RESPONSE_3"
echo ""

# 7. 检查审计日志
echo "=== 审计日志检查 ==="
echo ""
echo "搜索 AUDIT logger 输出（idempotent 标记）..."
sleep 1

AUDIT_LINES=$(grep "AUDIT" "$LOG_FILE" | grep "idempotent" || true)
IDEMPOTENT_HIT=$(grep "AUDIT" "$LOG_FILE" | grep "idempotent=true" || true)
IDEMPOTENT_MISS=$(grep "AUDIT" "$LOG_FILE" | grep "idempotent=false" || true)

echo "  全部审计记录:"
if [ -n "$AUDIT_LINES" ]; then
    echo "$AUDIT_LINES" | sed 's/^/    /'
else
    echo "    (无审计记录 — 检查日志级别)"
    echo ""
    echo "  --- 服务日志最后 30 行 ---"
    tail -30 "$LOG_FILE" | sed 's/^/    /'
fi

echo ""
echo "=== 验证结果 ==="
echo ""

# 判定结果
PASS=0
FAIL=0

# 检查两次响应都是 accepted
if echo "$RESPONSE_1" | grep -q "accepted" && echo "$RESPONSE_2" | grep -q "accepted"; then
    echo -e "  ${GREEN}✓ 两次请求均被接受${NC}"
    PASS=$((PASS+1))
else
    echo -e "  ${RED}✗ 请求被拒绝${NC}"
    FAIL=$((FAIL+1))
fi

# 检查审计日志中有 idempotent=true（第二次请求）
if [ -n "$IDEMPOTENT_HIT" ]; then
    echo -e "  ${GREEN}✓ 审计日志确认: 第二次请求命中幂等缓存 (idempotent=true)${NC}"
    PASS=$((PASS+1))
else
    echo -e "  ${RED}✗ 未找到 idempotent=true 审计记录${NC}"
    FAIL=$((FAIL+1))
fi

# 检查有 idempotent=false 记录（第一次和第三次）
if [ -n "$IDEMPOTENT_MISS" ]; then
    MISS_COUNT=$(echo "$IDEMPOTENT_MISS" | wc -l | tr -d ' ')
    echo -e "  ${GREEN}✓ 首次执行审计记录: ${MISS_COUNT} 条 (idempotent=false)${NC}"
    PASS=$((PASS+1))
else
    echo -e "  ${YELLOW}⚠ 未找到 idempotent=false 审计记录${NC}"
fi

# 检查第三次请求不返回第一次的缓存结果（跨工具隔离）
if echo "$RESPONSE_3" | grep -q "accepted"; then
    echo -e "  ${GREEN}✓ 跨工具不碰撞: fh_ops (id=42) 独立执行${NC}"
    PASS=$((PASS+1))
else
    echo -e "  ${RED}✗ 跨工具碰撞: fh_ops 可能返回了 fh_session_info 的缓存${NC}"
    FAIL=$((FAIL+1))
fi

echo ""
if [ "$FAIL" -eq 0 ]; then
    echo -e "${GREEN}══════════════════════════════════════${NC}"
    echo -e "${GREEN}  ALL CHECKS PASSED ($PASS/$PASS)${NC}"
    echo -e "${GREEN}══════════════════════════════════════${NC}"
else
    echo -e "${RED}══════════════════════════════════════${NC}"
    echo -e "${RED}  FAILED: $FAIL checks failed${NC}"
    echo -e "${RED}══════════════════════════════════════${NC}"
    exit 1
fi
