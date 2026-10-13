#!/usr/bin/env bash
# ============================================================================
# FH Skeleton 部署 — 密钥/密码生成脚本
# 用途：生成部署必需的安全凭据，输出待填入配置的键值。
# 仅打印，不写入任何文件（由 AI 引导填入对应 .env / Secret / env 文件）。
#
# 用法：
#   bash gen-secrets.sh
# ============================================================================
set -euo pipefail

echo "===== FH Skeleton 部署密钥生成 ====="
echo ""
echo "# 以下值请分别填入对应部署配置（勿外泄，勿提交到 git）"
echo ""
echo "FH_JWT_SECRET=$(openssl rand -hex 32)"
echo "FH_DB_PASSWORD=$(openssl rand -hex 24)"
echo "MYSQL_ROOT_PASSWORD=$(openssl rand -hex 24)"
echo ""
echo "# K8s Secret 中 FH_CHAT_MCP_AUTH_TOKEN（可选，与 MCP Server 的 FH_SSE_AUTH_TOKEN 一致）："
echo "FH_CHAT_MCP_AUTH_TOKEN=$(openssl rand -hex 32)"
