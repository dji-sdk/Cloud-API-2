#!/bin/bash
# FlightHub MCP Server 密钥生成脚本
# 生成部署所需的强随机密钥
#
# 用法：bash gen-secrets.sh
#
# 跨 Skill 注意：若同时部署 skeleton + MCP，skeleton 的 FH_CHAT_MCP_AUTH_TOKEN
# 必须等于此处的 FH_SSE_AUTH_TOKEN（skeleton 作为客户端连接 MCP Server 用）。

set -uo pipefail

echo "===== FlightHub MCP Server 密钥生成 ====="
echo ""

echo "--- SSE Auth Token（必填，留空拒绝启动） ---"
SSE_TOKEN=$(openssl rand -hex 32)
echo "FH_SSE_AUTH_TOKEN=$SSE_TOKEN"
echo ""

echo "--- Operator Token（可选，L3 紧急停机鉴权） ---"
OP_TOKEN=$(openssl rand -hex 32)
echo "FH_OPERATOR_TOKEN=$OP_TOKEN"
echo ""

echo "===== 生成完成 ====="
echo ""
echo "请将以上值填入 mcp/.env 对应字段。"
echo "⚠️  切勿使用占位值原样部署，StartupAuthValidator 会拒绝启动。"
