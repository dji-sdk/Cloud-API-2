#!/bin/bash
# FlightHub MCP Server 环境探测脚本
# AI 让用户在目标机器上执行，回报输出后分析推荐部署路径
#
# 用法：bash env-probe.sh（或复制内容逐段执行）

set -uo pipefail

echo "===== FlightHub MCP Server 环境探测 ====="
echo ""

echo "--- 操作系统 ---"
uname -s -r -m
cat /etc/os-release 2>/dev/null | grep -E "^(NAME|VERSION)=" || sw_vers 2>/dev/null || echo "Unknown OS"
echo ""

echo "--- CPU 架构 ---"
ARCH=$(uname -m)
echo "Architecture: $ARCH"
if [ "$ARCH" != "x86_64" ] && [ "$ARCH" != "amd64" ]; then
  echo "⚠️  非 x86_64 架构：真实 SDK 模式需 Docker --platform linux/amd64 或远程 x86_64 服务器"
fi
echo ""

echo "--- Docker ---"
if command -v docker &>/dev/null; then
  docker --version
  docker compose version 2>/dev/null || docker-compose --version 2>/dev/null || echo "docker compose 未安装"
  docker info --format '{{.OSType}}/{{.Architecture}}' 2>/dev/null
else
  echo "❌ Docker 未安装"
fi
echo ""

echo "--- Java ---"
if command -v java &>/dev/null; then
  java -version 2>&1 | head -1
else
  echo "Java 未安装（仅本地 JAR 运行需要，Docker 部署不需要）"
fi
echo ""

echo "--- Maven ---"
if command -v mvn &>/dev/null; then
  mvn --version 2>&1 | head -1
else
  echo "Maven 未安装（仅本地构建需要，Docker multi-stage 内置）"
fi
echo ""

echo "--- 网络可达性 ---"
echo -n "Internet: "
curl -sf --max-time 5 https://registry-1.docker.io/v2/ > /dev/null && echo "✅ Docker Hub 可达" || echo "⚠️  Docker Hub 不可达（可能需要镜像源）"
echo ""

echo "--- 磁盘空间 ---"
df -h . | tail -1
echo "(MCP 镜像约 500MB，构建过程需约 2GB 临时空间)"
echo ""

echo "--- 端口占用 ---"
if command -v ss &>/dev/null; then
  ss -tlnp 2>/dev/null | grep -E ":8080\b" && echo "⚠️  端口 8080 已被占用" || echo "✅ 端口 8080 可用"
elif command -v lsof &>/dev/null; then
  lsof -i :8080 2>/dev/null | grep LISTEN && echo "⚠️  端口 8080 已被占用" || echo "✅ 端口 8080 可用"
else
  echo "无法检测端口占用（ss/lsof 不可用）"
fi
echo ""

echo "===== 探测完成 ====="
