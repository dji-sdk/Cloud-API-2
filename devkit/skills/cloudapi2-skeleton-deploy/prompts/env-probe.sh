#!/usr/bin/env bash
# ============================================================================
# FH Skeleton 部署 — 环境探测脚本
# 用途：让 AI 判断目标机器是否满足 skeleton 部署前置条件，并据此推荐部署形态。
# 仅探测、不修改系统。AI 读取输出后决策，用户无手工理解负担。
#
# 用法：
#   bash env-probe.sh
# ============================================================================
set -uo pipefail

echo "===== FH Skeleton 部署环境探测 ====="

# ─── OS / 架构 ─────────────────────────────────────────────────────────────
echo "[OS]"
uname -s -m 2>/dev/null || echo "uname 不可用"
# 架构判断：SDK JNI 原生库仅支持 Linux x86_64（amd64）
case "$(uname -m 2>/dev/null)" in
  x86_64|amd64) echo "  arch: amd64 (sdk 模式可用)" ;;
  aarch64|arm64) echo "  arch: arm64 (⚠️ sdk 模式不可用，native lib 仅 amd64)" ;;
  *) echo "  arch: 未知" ;;
esac

# ─── JDK ───────────────────────────────────────────────────────────────────
echo "[JDK]"
if command -v java >/dev/null 2>&1; then
  java -version 2>&1 | head -1
else
  echo "  ✗ java 未安装（需 JDK 17+）"
fi

# ─── Maven ─────────────────────────────────────────────────────────────────
echo "[Maven]"
if command -v mvn >/dev/null 2>&1; then
  mvn -v 2>&1 | head -1
else
  echo "  ✗ mvn 未安装（需 Maven 3.8+）"
fi

# ─── Node.js ───────────────────────────────────────────────────────────────
echo "[Node.js]"
if command -v node >/dev/null 2>&1; then
  node -v 2>&1
else
  echo "  ✗ node 未安装（需 Node.js 18+）"
fi

# ─── Docker ────────────────────────────────────────────────────────────────
echo "[Docker]"
if command -v docker >/dev/null 2>&1; then
  docker --version 2>&1
  if docker compose version >/dev/null 2>&1; then
    docker compose version 2>&1
  else
    echo "  ✗ docker compose 不可用（需 Compose v2）"
  fi
else
  echo "  ✗ docker 未安装（Docker 形态需要）"
fi

# ─── kubectl ───────────────────────────────────────────────────────────────
echo "[kubectl]"
if command -v kubectl >/dev/null 2>&1; then
  kubectl version --client 2>&1 | head -1
else
  echo "  ✗ kubectl 未安装（K8s 形态需要）"
fi

# ─── MySQL ─────────────────────────────────────────────────────────────────
echo "[MySQL]"
if command -v mysql >/dev/null 2>&1; then
  mysql --version 2>&1
else
  echo "  - mysql 客户端未安装（普通机器形态需要 MySQL 8.0+；Docker/K8s 形态容器内自带）"
fi

echo ""
echo "===== 探测完成 ====="
