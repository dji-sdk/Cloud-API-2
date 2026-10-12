#!/bin/bash

set -e

# macOS ARM64 专用 libwebsockets 构建脚本
# 使用 Homebrew openssl@3 和 libwebsockets v2.4.2
# 默认 Release（与仓内预编译一致）；排障可 NCSDK_LWS_BUILD_TYPE=Debug

echo "=== macOS ARM64 libwebsockets 构建脚本 ==="
echo "目标平台: macOS ARM64"
echo "OpenSSL: Homebrew openssl@3"
echo "libwebsockets: v2.4.2"
echo "构建类型: ${NCSDK_LWS_BUILD_TYPE:-Release} (可通过 NCSDK_LWS_BUILD_TYPE 覆盖)"
echo ""

# ========================================================================
# 重要：需要修改 libwebsockets 源码以支持 macOS
# ========================================================================
#
# 由于现代 macOS (10.12+) 系统头文件 <sys/socket.h> 已经定义了 MSG_NOSIGNAL 宏，
# 而 libwebsockets v2.4.2 在 private-libwebsockets.h 中也会定义它，导致编译错误：
# "error: 'MSG_NOSIGNAL' macro redefined [-Werror,-Wmacro-redefined]"
#
# 脚本会在检出 v2.4.2 后自动加 #ifndef 守卫。
# ========================================================================

PLATFORM="macosx"
ARCH="arm64"
BUILD_TYPE="${NCSDK_LWS_BUILD_TYPE:-Release}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WEBSOCKETS_ROOT="$(dirname "$SCRIPT_DIR")"
PROJECT_ROOT="$(dirname "$(dirname "$WEBSOCKETS_ROOT")")"
THIRD_PARTY_ROOT="${PROJECT_ROOT}/third_party"
# 路径卫生：编译期映射本机/CI 路径（含历史 Jenkins，避免泄漏进产物）
PATH_HYGIENE_FLAGS="-ffile-prefix-map=${THIRD_PARTY_ROOT}=/build/vendor/third_party -ffile-prefix-map=${HOME}=/build/vendor/home -ffile-prefix-map=/home/jenkins=/build/vendor"

echo "脚本目录: $SCRIPT_DIR"
echo "Websockets根目录: $WEBSOCKETS_ROOT"
echo "项目根目录: $PROJECT_ROOT"

if [ ! -f "$WEBSOCKETS_ROOT/src/CMakeLists.txt" ]; then
    echo "❌ 没有源码: $WEBSOCKETS_ROOT/src"
    echo "git clone --branch v2.4.2 https://github.com/warmcat/libwebsockets.git $WEBSOCKETS_ROOT/src"
    exit 1
fi

cd "$SCRIPT_DIR/../src"

# 资源门控：优先用上层 export 的 FHSDK_MAX_JOBS（scripts/build_java.sh 统一定版）；
# 单独跑本脚本时用"核数但封顶 8"，避免在 20 核机器上 -j20 打满 CPU 把 SSH 挤断。详见 scripts/README.md。
CPU_CORES="${FHSDK_MAX_JOBS:-$(n=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4); [ "$n" -gt 8 ] && echo 8 || echo "$n")}"
echo "检测到平台: $PLATFORM, 架构: $ARCH"
echo "CPU 核心数: $CPU_CORES"

echo "=== 检查 libwebsockets 版本 ==="
CURRENT_VERSION=$(git describe --tags --exact-match 2>/dev/null || echo "unknown")
echo "当前版本: $CURRENT_VERSION"

if [ "$CURRENT_VERSION" != "v2.4.2" ]; then
    echo "切换到 libwebsockets v2.4.2 版本..."
    git checkout v2.4.2
    echo "已切换到 v2.4.2 版本"
else
    echo "已经是 v2.4.2 版本"
fi

# 现代 macOS 系统头可能已定义 MSG_NOSIGNAL，避免与私有头重复定义
HDR="lib/private-libwebsockets.h"
if [[ -f "$HDR" ]] && grep -q '#define MSG_NOSIGNAL SO_NOSIGPIPE' "$HDR" && ! grep -q '#ifndef MSG_NOSIGNAL' "$HDR"; then
    echo "=== 修补 MSG_NOSIGNAL 宏守卫 ==="
    python3 - "$HDR" <<'PY'
import pathlib, sys
p = pathlib.Path(sys.argv[1])
t = p.read_text()
old = "#ifdef __APPLE__\n#define MSG_NOSIGNAL SO_NOSIGPIPE\n#endif"
new = "#ifdef __APPLE__\n#ifndef MSG_NOSIGNAL\n#define MSG_NOSIGNAL SO_NOSIGPIPE\n#endif\n#endif"
if old in t:
    p.write_text(t.replace(old, new, 1))
    print("patched", p)
else:
    print("MSG_NOSIGNAL pattern not found; skip")
PY
fi

echo "=== 配置 OpenSSL ==="
if command -v brew >/dev/null 2>&1; then
    OPENSSL_ROOT="$(brew --prefix openssl@3)"
else
    OPENSSL_ROOT="/opt/homebrew/opt/openssl@3"
fi
OPENSSL_LIB_DIR="$OPENSSL_ROOT/lib"
OPENSSL_INCLUDE_DIR="$OPENSSL_ROOT/include"

if [[ ! -d "$OPENSSL_INCLUDE_DIR" ]]; then
    echo "❌ 未找到 Homebrew openssl@3 头文件目录: $OPENSSL_INCLUDE_DIR"
    echo "请安装 OpenSSL: brew install openssl@3"
    exit 1
fi

if [[ ! -f "$OPENSSL_LIB_DIR/libssl.dylib" ]] && [[ ! -f "$OPENSSL_LIB_DIR/libssl.a" ]]; then
    echo "❌ 未找到 OpenSSL SSL 库: $OPENSSL_LIB_DIR/libssl.dylib 或 libssl.a"
    echo "请安装 OpenSSL: brew install openssl@3"
    exit 1
fi

if [[ ! -f "$OPENSSL_LIB_DIR/libcrypto.dylib" ]] && [[ ! -f "$OPENSSL_LIB_DIR/libcrypto.a" ]]; then
    echo "❌ 未找到 OpenSSL Crypto 库: $OPENSSL_LIB_DIR/libcrypto.dylib 或 libcrypto.a"
    echo "请安装 OpenSSL: brew install openssl@3"
    exit 1
fi

echo "使用 Homebrew openssl@3"
echo "OpenSSL 库目录: $OPENSSL_LIB_DIR"
echo "OpenSSL 头文件目录: $OPENSSL_INCLUDE_DIR"

OPENSSL_CMAKE_ARGS="-DOPENSSL_ROOT_DIR=$OPENSSL_ROOT -DLWS_OPENSSL_INCLUDE_DIRS=$OPENSSL_INCLUDE_DIR"

echo "=== 清理构建目录 ==="
rm -rf build
mkdir -p build
cd build

echo "=== 配置 CMake ==="
cmake .. \
    $OPENSSL_CMAKE_ARGS \
    -DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
    -DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
    -DLWS_WITH_STATIC=ON \
    -DLWS_WITH_SHARED=ON \
    -DLWS_WITH_MINIMAL_EXAMPLES=OFF \
    -DLWS_WITH_SSL=ON \
    -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
    -DCMAKE_OSX_ARCHITECTURES=arm64 \
    -DCMAKE_SKIP_RPATH=TRUE \
    -DCMAKE_SKIP_INSTALL_RPATH=TRUE \
    -DCMAKE_C_FLAGS="${PATH_HYGIENE_FLAGS} -Wno-error=unused-but-set-variable -Wno-error=macro-redefined -Wno-error=default-const-init-field-unsafe" \
    -DLWS_LOGGING_BITFIELD_SET=2047

# 身份卫生：v2.4.2 的 CMakeLists 用 whoami@hostname-git_describe 生成 LWS_BUILD_HASH。
echo "=== 中性化 LWS_BUILD_HASH ==="
if [[ -f lws_config.h ]]; then
    sed -i '' 's|^#define LWS_BUILD_HASH .*|#define LWS_BUILD_HASH "v2.4.2"|' lws_config.h
    grep '#define LWS_BUILD_HASH' lws_config.h
else
    echo "❌ 未生成 lws_config.h"
    exit 1
fi

echo "=== 开始编译 ==="
echo "使用 $CPU_CORES 核心进行编译"
nice -n "${FHSDK_NICE:-15}" make -j$CPU_CORES

echo "=== 安装库文件 ==="
TARGET_LIB_DIR="$WEBSOCKETS_ROOT/lib/$PLATFORM/$ARCH"
mkdir -p "$TARGET_LIB_DIR"

if [[ -f "lib/libwebsockets.12.dylib" ]]; then
    cp -a lib/libwebsockets.12.dylib "$TARGET_LIB_DIR/"
    DYLIB="$TARGET_LIB_DIR/libwebsockets.12.dylib"
    # id 固定为 @rpath；OpenSSL 去掉 Homebrew 绝对路径（与 paho / jar 打包一致）
    install_name_tool -id "@rpath/libwebsockets.12.dylib" "$DYLIB"
    for dep in $(otool -L "$DYLIB" | awk '/libssl|libcrypto/ {print $1}'); do
        case "$dep" in
            @rpath/*|@loader_path/*|/usr/lib/*) continue ;;
        esac
        base="$(basename "$dep")"
        case "$base" in
            libssl*.dylib)    install_name_tool -change "$dep" "@rpath/libssl.3.dylib" "$DYLIB" ;;
            libcrypto*.dylib) install_name_tool -change "$dep" "@rpath/libcrypto.3.dylib" "$DYLIB" ;;
        esac
    done
    codesign --force --sign - "$DYLIB" 2>/dev/null || true
    echo "✅ 已复制动态库: libwebsockets.12.dylib（id=@rpath，OpenSSL=@rpath）"
else
    echo "❌ 未找到动态库: lib/libwebsockets.12.dylib"
    exit 1
fi

# cwd = libwebsockets/build；v2.4.2 只有单文件公共头，无 lib/libwebsockets/ 子目录
echo "=== 复制头文件 ==="
INCLUDE_DIR="$WEBSOCKETS_ROOT/include"
SRC_LIB_DIR="$(cd .. && pwd)/lib"
rm -rf "$INCLUDE_DIR"
mkdir -p "$INCLUDE_DIR"

if [[ -f "$SRC_LIB_DIR/libwebsockets.h" ]]; then
    cp "$SRC_LIB_DIR/libwebsockets.h" "$INCLUDE_DIR/"
    echo "✅ 已复制主头文件: libwebsockets.h"
else
    echo "❌ 未找到主头文件: $SRC_LIB_DIR/libwebsockets.h"
    exit 1
fi

if [[ -f "lws_config.h" ]]; then
    cp "lws_config.h" "$INCLUDE_DIR/"
    echo "✅ 已复制配置头文件: lws_config.h"
else
    echo "❌ 未找到 lws_config.h"
    exit 1
fi

echo ""
echo "🎉 libwebsockets v2.4.2 编译完成！"
echo "📁 库文件已安装到: $TARGET_LIB_DIR"
echo "📁 头文件已安装到: $INCLUDE_DIR"
echo ""
echo "📋 构建信息:"
echo "   - 平台: macOS ARM64"
echo "   - 版本: v2.4.2"
echo "   - 构建类型: $BUILD_TYPE"
echo "   - OpenSSL: Homebrew openssl@3 ($OPENSSL_ROOT)"
echo "   - 动态库: libwebsockets.12.dylib"
