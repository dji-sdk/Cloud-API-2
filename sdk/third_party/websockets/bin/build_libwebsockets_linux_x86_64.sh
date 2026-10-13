#!/bin/bash

set -e

# Linux x86_64 专用 libwebsockets 构建脚本
# 使用项目 OpenSSL 1.1.1 和 libwebsockets v2.4.2
# 默认 Release（与仓内预编译一致）；排障可 NCSDK_LWS_BUILD_TYPE=Debug

echo "=== Linux x86_64 libwebsockets 构建脚本 ==="
echo "目标平台: Linux x86_64"
echo "OpenSSL: 项目 OpenSSL 1.1.1"
echo "libwebsockets: v2.4.2"
echo "构建类型: ${NCSDK_LWS_BUILD_TYPE:-Release} (可通过 NCSDK_LWS_BUILD_TYPE 覆盖)"
echo ""

# 默认配置
USE_OPENSSL=true  # Linux x86_64 默认启用 OpenSSL
PLATFORM="linux"
ARCH="x86_64"
# 与仓内预编译一致：Release（体积更小）。排障可: NCSDK_LWS_BUILD_TYPE=Debug
BUILD_TYPE="${NCSDK_LWS_BUILD_TYPE:-Release}"

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WEBSOCKETS_ROOT="$(dirname "$SCRIPT_DIR")"
PROJECT_ROOT="$(dirname "$(dirname "$WEBSOCKETS_ROOT")")"
THIRD_PARTY_ROOT="${PROJECT_ROOT}/third_party"
# 路径卫生：编译期映射本机/CI 路径
PATH_HYGIENE_FLAGS="-ffile-prefix-map=${THIRD_PARTY_ROOT}=/build/vendor/third_party -ffile-prefix-map=${HOME}=/build/vendor/home -ffile-prefix-map=/home/jenkins=/build/vendor"

echo "脚本目录: $SCRIPT_DIR"
echo "Websockets根目录: $WEBSOCKETS_ROOT"
echo "项目根目录: $PROJECT_ROOT"

if [ ! -f "$WEBSOCKETS_ROOT/src/CMakeLists.txt" ]; then
    echo "❌ 没有源码: $WEBSOCKETS_ROOT/src"
    echo "git clone --branch v2.4.2 https://github.com/warmcat/libwebsockets.git $WEBSOCKETS_ROOT/src"
    exit 1
fi

# 进入libwebsockets源码目录
cd "$SCRIPT_DIR/../src"

# Linux x86_64 专用配置
# 资源门控：优先用上层 export 的 FHSDK_MAX_JOBS（scripts/build_java.sh 统一定版）；
# 单独跑本脚本时用"核数但封顶 8"，避免在 20 核机器上 -j20 打满 CPU 把 SSH 挤断。详见 scripts/README.md。
CPU_CORES="${FHSDK_MAX_JOBS:-$(n=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4); [ "$n" -gt 8 ] && echo 8 || echo "$n")}"
echo "检测到平台: $PLATFORM, 架构: $ARCH"
echo "CPU 核心数: $CPU_CORES"

# 检查并切换到 libwebsockets v2.4.2 版本
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

# Linux x86_64 专用 OpenSSL 配置
echo "=== 配置 OpenSSL ==="
OPENSSL_ROOT="$THIRD_PARTY_ROOT/openssl_1_1_1"
OPENSSL_LIB_DIR="$OPENSSL_ROOT/lib/$PLATFORM/$ARCH"
OPENSSL_INCLUDE_DIR="$OPENSSL_ROOT/include"

# 检查 OpenSSL 库文件是否存在
if [[ ! -f "$OPENSSL_LIB_DIR/libssl.so.1.1" ]]; then
    echo "❌ 未找到 OpenSSL 库: $OPENSSL_LIB_DIR/libssl.so.1.1"
    exit 1
fi
if [[ ! -f "$OPENSSL_LIB_DIR/libcrypto.so.1.1" ]]; then
    echo "❌ 未找到 OpenSSL 库: $OPENSSL_LIB_DIR/libcrypto.so.1.1"
    exit 1
fi
if [[ ! -d "$OPENSSL_INCLUDE_DIR" ]]; then
    echo "❌ 未找到 OpenSSL 头文件目录: $OPENSSL_INCLUDE_DIR"
    exit 1
fi

echo "使用项目 OpenSSL 1.1.1 库"
echo "OpenSSL 库目录: $OPENSSL_LIB_DIR"
echo "OpenSSL 头文件目录: $OPENSSL_INCLUDE_DIR"

# 使用项目中的 OpenSSL 库的完整路径
OPENSSL_CMAKE_ARGS="-DLWS_OPENSSL_LIBRARIES=$OPENSSL_LIB_DIR/libssl.so.1.1;$OPENSSL_LIB_DIR/libcrypto.so.1.1 -DLWS_OPENSSL_INCLUDE_DIRS=$OPENSSL_INCLUDE_DIR -DOPENSSL_ROOT_DIR=$OPENSSL_ROOT"

# 清理并创建 build 目录
echo "=== 清理构建目录 ==="
rm -rf build
mkdir -p build
cd build

# 配置 CMake（路径卫生：-ffile-prefix-map；禁止把本机 OpenSSL 绝对路径写进 RUNPATH）
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
    -DCMAKE_SKIP_RPATH=TRUE \
    -DCMAKE_SKIP_INSTALL_RPATH=TRUE \
    -DCMAKE_C_FLAGS="${PATH_HYGIENE_FLAGS}" \
    -DLWS_LOGGING_BITFIELD_SET=2047

# 身份卫生：v2.4.2 的 CMakeLists 用 whoami@hostname-git_describe 生成 LWS_BUILD_HASH，
# 会把构建者用户名与机器名嵌进 lws_config.h 和 libwebsockets.so。外部 -D 传不进去
# （if(GIT_FOUND) 分支里无条件覆盖），所以配置后改生成的头，再编译。
echo "=== 中性化 LWS_BUILD_HASH ==="
if [[ -f lws_config.h ]]; then
    sed -i 's|^#define LWS_BUILD_HASH .*|#define LWS_BUILD_HASH "v2.4.2"|' lws_config.h
    grep '#define LWS_BUILD_HASH' lws_config.h
else
    echo "❌ 未生成 lws_config.h"
    exit 1
fi

# 编译
echo "=== 开始编译 ==="
echo "使用 $CPU_CORES 核心进行编译"
nice -n "${FHSDK_NICE:-15}" make -j$CPU_CORES

# 安装库文件
echo "=== 安装库文件 ==="
TARGET_LIB_DIR="$WEBSOCKETS_ROOT/lib/$PLATFORM/$ARCH"
mkdir -p "$TARGET_LIB_DIR"

# 复制动态库；再清一遍 RUNPATH（CMake 链绝对路径 OpenSSL 时偶发仍写入）
if [[ -f "lib/libwebsockets.so.12" ]]; then
    cp -a lib/libwebsockets.so.12 "$TARGET_LIB_DIR/"
    if command -v patchelf >/dev/null 2>&1; then
        patchelf --remove-rpath "$TARGET_LIB_DIR/libwebsockets.so.12" 2>/dev/null || true
    fi
    echo "✅ 已复制动态库: libwebsockets.so.12"
else
    echo "❌ 未找到动态库: lib/libwebsockets.so.12"
    exit 1
fi

# 复制头文件（cwd = libwebsockets/build；v2.4.2 只有单文件公共头，无 lib/libwebsockets/ 子目录）
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
echo "   - 平台: Linux x86_64"
echo "   - 版本: v2.4.2"
echo "   - 构建类型: $BUILD_TYPE"
echo "   - OpenSSL: 项目 OpenSSL 1.1.1 ($OPENSSL_ROOT)"
echo "   - 动态库: libwebsockets.so.12"