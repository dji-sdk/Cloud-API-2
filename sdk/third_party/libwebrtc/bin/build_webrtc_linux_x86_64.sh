#!/bin/bash
# WebRTC Linux x86_64。外部仓内 openssl_1_1_1；编完 verify。
# 默认不覆盖仓内 lib/linux/x86_64/libwebrtc.a；加 --install 才拷 .a（不改 include/）。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=webrtc_common.sh
source "$SCRIPT_DIR/webrtc_common.sh"

DO_INSTALL=0
for arg in "$@"; do
    case "$arg" in
        --install) DO_INSTALL=1 ;;
        -h|--help)
            echo "用法: $0 [--install]"
            echo "  --install  verify 通过后拷 libwebrtc.a 到 lib/linux/x86_64/"
            exit 0
            ;;
        *) echo "未知参数: $arg"; exit 1 ;;
    esac
done

echo "开始构建 WebRTC Linux x86_64..."

if [ ! -d "$WEBRTC_SRC" ] || [ ! -f "$WEBRTC_SRC/BUILD.gn" ]; then
    echo "错误: 完整源码不存在，请先运行 $SCRIPT_DIR/setup_webrtc.sh"
    exit 1
fi

OPENSSL_ROOT="$SDK_ROOT/third_party/openssl_1_1_1"
OPENSSL_INCLUDE_DIR="$OPENSSL_ROOT/include"
OPENSSL_LIB_DIR="$OPENSSL_ROOT/lib/linux/x86_64"

echo "WebRTC 根目录: $LIBWEBRTC_ROOT"
echo "checkout:     $WEBRTC_SRC"
echo "Cloud API 2:  $SDK_ROOT"
echo "OpenSSL:      $OPENSSL_ROOT"

if [ ! -d "$OPENSSL_INCLUDE_DIR" ]; then
    echo "错误: OpenSSL 头不存在: $OPENSSL_INCLUDE_DIR"
    exit 1
fi
if [ ! -f "$OPENSSL_LIB_DIR/libssl.a" ]; then
    echo "错误: 没有 $OPENSSL_LIB_DIR/libssl.a"
    exit 1
fi
if [ ! -f "$OPENSSL_LIB_DIR/libcrypto.a" ]; then
    echo "错误: 没有 $OPENSSL_LIB_DIR/libcrypto.a"
    exit 1
fi

export PATH="$DEPO:$PATH"
CLANG="$WEBRTC_SRC/third_party/llvm-build/Release+Asserts/bin/clang"
GN="$WEBRTC_SRC/buildtools/linux64/gn"
NINJA="$DEPO/ninja"
for tool in "$CLANG" "$GN" "$NINJA"; do
    if [ ! -x "$tool" ]; then
        echo "错误: 缺少工具 $tool；请重新运行 gclient sync"
        exit 1
    fi
done
echo "Clang: $("$CLANG" --version | head -n 1)"

echo "初始化 depot_tools..."
cd "$DEPO"
./ensure_bootstrap
cd "$WEBRTC_SRC"

webrtc_apply_patches

echo "清理之前的构建..."
rm -rf out/Release

echo "生成构建配置..."
"$GN" gen out/Release --args="
is_debug = false
is_component_build = false
rtc_use_h264 = true
ffmpeg_branding = \"Chrome\"

use_custom_libcxx = false
use_custom_libcxx_for_host = false
libcxx_is_shared = false
rtc_include_tests = false
rtc_build_examples = false
target_os = \"linux\"
target_cpu = \"x64\"
rtc_include_pulse_audio = false
rtc_use_x11 = false
rtc_use_dummy_audio_file_devices = true
use_rtti = true
symbol_level=0
clang_use_chrome_plugins = false
use_sysroot = false

rtc_build_ssl = false
rtc_ssl_root = \"$OPENSSL_INCLUDE_DIR\"
rtc_builtin_ssl_root_certificates = true

libsrtp_build_boringssl = false
libsrtp_ssl_root = \"$OPENSSL_INCLUDE_DIR\"

rtc_enable_grpc = false
treat_warnings_as_errors = false
rtc_include_tests = false
rtc_build_examples = false
rtc_build_tools = false
rtc_enable_protobuf = false
rtc_enable_sctp = false
rtc_build_dcsctp = false
"

echo "开始编译 WebRTC..."
# 资源门控：ninja 不带 -j 时默认并发 = 核数+2（20 核机器上就是 22），WebRTC 又是超大工程，
# 比 SDK 主编译更容易打满 CPU/内存把 SSH 挤断。优先用上层 export 的 FHSDK_MAX_JOBS，
# 单独跑时用"核数但封顶 8"；再整体降到 nice 15 让 sshd 抢得过。详见 scripts/README.md。
NINJA_JOBS="${FHSDK_MAX_JOBS:-$(n=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4); [ "$n" -gt 8 ] && echo 8 || echo "$n")}"
echo "  并行度: $NINJA_JOBS | nice: ${FHSDK_NICE:-15}"
nice -n "${FHSDK_NICE:-15}" "$NINJA" -C out/Release -j "$NINJA_JOBS" webrtc

if [ ! -f "out/Release/obj/libwebrtc.a" ]; then
    echo "错误: 没有 out/Release/obj/libwebrtc.a"
    exit 1
fi

echo "编译成功: $WEBRTC_SRC/out/Release/obj/libwebrtc.a"
echo "最终链接需: -L$OPENSSL_LIB_DIR -lssl -lcrypto"
echo "验证 BoringSSL 依赖已移除..."
"$SCRIPT_DIR/verify_no_boringssl.sh"

if [ "$DO_INSTALL" -eq 1 ]; then
    webrtc_install_lib "linux/x86_64"
else
    echo "未安装。verify 通过后: $0 --install"
    echo "目标: $LIBWEBRTC_ROOT/lib/linux/x86_64/libwebrtc.a"
fi
