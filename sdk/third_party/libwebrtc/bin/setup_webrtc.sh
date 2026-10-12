#!/bin/bash
# 把完整 WebRTC 拉到 src/（gitignore）。不碰 include/，不写 shell rc。
# 磁盘大约十几 GB，需要能访问 webrtc.googlesource.com。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=webrtc_common.sh
source "$SCRIPT_DIR/webrtc_common.sh"

if [ "${1:-}" = "-h" ] || [ "${1:-}" = "--help" ]; then
    cat <<EOF
用法: $0 [branch]

默认分支: $WEBRTC_BRANCH
checkout 写到: $WEBRTC_SRC
不会覆盖 include/，也不会改 ~/.zshrc / ~/.bashrc。

中途失败（网络 / gclient）时可在本目录重新执行本脚本，或手动:
  export PATH="\$PWD/depot_tools:\$PATH" && gclient sync --reset
EOF
    exit 0
fi

if [ -n "${1:-}" ]; then
    WEBRTC_BRANCH="$1"
fi

if [ ! -d "$SDK_HEADERS" ]; then
    echo "错误: 缺少仓内头切片 $SDK_HEADERS"
    exit 1
fi

echo "SDK:        $SDK_ROOT"
echo "libwebrtc:  $LIBWEBRTC_ROOT"
echo "checkout:   $WEBRTC_SRC"
echo "分支:       $WEBRTC_BRANCH"
echo ""
echo "注意: 完整树很大。拉下来的是 src/，不会覆盖 include/。"
echo "若 sync 中途失败，重新跑本脚本或: gclient sync --reset"
echo ""

if ! command -v git >/dev/null || ! command -v python3 >/dev/null; then
    echo "错误: 需要 git 和 python3"
    exit 1
fi

if [ ! -d "$DEPO" ]; then
    echo "克隆 depot_tools..."
    git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git "$DEPO"
else
    echo "depot_tools 已存在"
fi

export PATH="$DEPO:$PATH"

# 本目录已有 include/ lib/ README，fetch 会因非空目录失败。用 .gclient 指向 src/。
if [ ! -f "$LIBWEBRTC_ROOT/.gclient" ]; then
    cat > "$LIBWEBRTC_ROOT/.gclient" <<'EOF'
solutions = [
  {
    "name": "src",
    "url": "https://webrtc.googlesource.com/src.git",
    "deps_file": "DEPS",
    "managed": False,
    "custom_deps": {},
  },
]
EOF
    echo "已写 $LIBWEBRTC_ROOT/.gclient"
fi

cd "$LIBWEBRTC_ROOT"
if [ ! -f "$WEBRTC_SRC/BUILD.gn" ]; then
    echo "gclient sync (--nohooks --reset)..."
    gclient sync --nohooks --reset
else
    echo "已有 $WEBRTC_SRC，跳过初次 sync"
fi

cd "$WEBRTC_SRC"
echo "fetch + checkout ${WEBRTC_BRANCH} ..."
# branch-heads/* 不是本地分支名；fetch 后用 FETCH_HEAD 建/重置本地同名分支
git fetch origin "${WEBRTC_BRANCH}"
git checkout -B "${WEBRTC_BRANCH}" FETCH_HEAD
# build 脚本会打本地补丁；重跑 setup 前清掉，否则 gclient 拒绝 sync
git checkout -- . >/dev/null 2>&1 || true
echo "当前: $(git log -1 --oneline)"

cd "$LIBWEBRTC_ROOT"
echo "gclient sync (--reset，对齐 DEPS + hooks，清掉补丁脏树)..."
gclient sync --reset

echo ""
echo "setup 完成。下一步:"
echo "  ./bin/build_webrtc_linux_x86_64.sh    # Linux"
echo "  ./bin/build_webrtc_macos_arm64.sh     # macOS"
echo "默认不覆盖仓内 lib/<os>/<arch>/libwebrtc.a；加 --install 才安装。"
