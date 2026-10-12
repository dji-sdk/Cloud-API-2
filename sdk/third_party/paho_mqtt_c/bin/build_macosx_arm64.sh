#!/bin/bash
# 使用 Homebrew openssl@3 编译 paho-mqtt3as 动态库

set -e

echo "=== 编译 paho-mqtt3as (macOS ARM64，SSL) ==="

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PAHO_ROOT="$(dirname "$SCRIPT_DIR")"
SRC_DIR="${PAHO_ROOT}/src"
BUILD_DIR="${PAHO_ROOT}/build"
LIB_DIR="${PAHO_ROOT}/lib/macosx/arm64"
INCLUDE_DIR="${PAHO_ROOT}/include"

if [ ! -f "${SRC_DIR}/CMakeLists.txt" ]; then
    echo "❌ 没有源码: ${SRC_DIR}"
    echo "git clone --branch v1.3.14 https://github.com/eclipse/paho.mqtt.c.git ${SRC_DIR}"
    exit 1
fi

PROJECT_ROOT="$(dirname "$(dirname "$PAHO_ROOT")")"
THIRD_PARTY_ROOT="${PROJECT_ROOT}/third_party"
PATH_HYGIENE_FLAGS="-ffile-prefix-map=${THIRD_PARTY_ROOT}=/build/vendor/third_party -ffile-prefix-map=${HOME}=/build/vendor/home -ffile-prefix-map=/home/jenkins=/build/vendor"

if command -v brew >/dev/null 2>&1; then
    OPENSSL_ROOT="$(brew --prefix openssl@3)"
else
    OPENSSL_ROOT="/opt/homebrew/opt/openssl@3"
fi
OPENSSL_LIB_DIR="${OPENSSL_ROOT}/lib"
OPENSSL_INCLUDE_DIR="${OPENSSL_ROOT}/include"

echo "使用 Homebrew openssl@3"
echo "OpenSSL 根目录: ${OPENSSL_ROOT}"

if [[ ! -d "${OPENSSL_INCLUDE_DIR}" ]]; then
    echo "❌ 未找到 openssl@3 头: ${OPENSSL_INCLUDE_DIR}"
    echo "请安装: brew install openssl@3"
    exit 1
fi
if [[ ! -f "${OPENSSL_LIB_DIR}/libssl.dylib" ]] && [[ ! -f "${OPENSSL_LIB_DIR}/libssl.a" ]]; then
    echo "❌ 未找到 OpenSSL SSL 库: ${OPENSSL_LIB_DIR}"
    exit 1
fi
if [[ ! -f "${OPENSSL_LIB_DIR}/libcrypto.dylib" ]] && [[ ! -f "${OPENSSL_LIB_DIR}/libcrypto.a" ]]; then
    echo "❌ 未找到 OpenSSL Crypto 库: ${OPENSSL_LIB_DIR}"
    exit 1
fi

OPENSSL_SSL_LIB="${OPENSSL_LIB_DIR}/libssl.dylib"
OPENSSL_CRYPTO_LIB="${OPENSSL_LIB_DIR}/libcrypto.dylib"
[[ -f "${OPENSSL_SSL_LIB}" ]] || OPENSSL_SSL_LIB="${OPENSSL_LIB_DIR}/libssl.a"
[[ -f "${OPENSSL_CRYPTO_LIB}" ]] || OPENSSL_CRYPTO_LIB="${OPENSSL_LIB_DIR}/libcrypto.a"

rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}"

# 禁止把本机路径写进 LC_RPATH；OpenSSL 的绝对 LC_LOAD_DYLIB 在拷贝后改成 @rpath
cmake -S "${SRC_DIR}" -B "${BUILD_DIR}" \
      -G"Ninja" \
      -DCMAKE_BUILD_TYPE=Release \
      -DPAHO_WITH_SSL=TRUE \
      -DPAHO_BUILD_SHARED=TRUE \
      -DPAHO_ENABLE_TESTING=FALSE \
      -DPAHO_ENABLE_CPACK=FALSE \
      -DCMAKE_SKIP_RPATH=TRUE \
      -DCMAKE_SKIP_INSTALL_RPATH=TRUE \
      -DCMAKE_C_FLAGS="${PATH_HYGIENE_FLAGS}" \
      -DOPENSSL_ROOT_DIR="${OPENSSL_ROOT}" \
      -DOPENSSL_INCLUDE_DIR="${OPENSSL_INCLUDE_DIR}" \
      -DOPENSSL_SSL_LIBRARY="${OPENSSL_SSL_LIB}" \
      -DOPENSSL_CRYPTO_LIBRARY="${OPENSSL_CRYPTO_LIB}"

cmake --build "${BUILD_DIR}" --target paho-mqtt3as

if [ ! -f "${BUILD_DIR}/src/libpaho-mqtt3as.dylib" ]; then
    echo "❌ 未产出 libpaho-mqtt3as.dylib"
    exit 1
fi

mkdir -p "${LIB_DIR}" "${INCLUDE_DIR}"
cp -a "${BUILD_DIR}"/src/libpaho-mqtt3as*.dylib "${LIB_DIR}/"
cp "${SRC_DIR}"/src/*.h "${INCLUDE_DIR}/"

# 去掉 Homebrew 绝对路径；运行时由 rpath / jar 同目录的 libssl.3 / libcrypto.3 解析
REAL_DYLIB=""
for dylib in "${LIB_DIR}"/libpaho-mqtt3as*.dylib; do
    [[ -f "$dylib" && ! -L "$dylib" ]] || continue
    REAL_DYLIB="$dylib"
    break
done
if [[ -z "${REAL_DYLIB}" ]]; then
    echo "❌ 未找到可改写的 paho dylib"
    exit 1
fi
install_name_tool -id "@rpath/libpaho-mqtt3as.1.dylib" "${REAL_DYLIB}"
for dep in $(otool -L "${REAL_DYLIB}" | awk '/libssl|libcrypto/ {print $1}'); do
    case "$dep" in
        @rpath/*|@loader_path/*|/usr/lib/*) continue ;;
    esac
    base="$(basename "$dep")"
    case "$base" in
        libssl*.dylib)   install_name_tool -change "$dep" "@rpath/libssl.3.dylib" "${REAL_DYLIB}" ;;
        libcrypto*.dylib) install_name_tool -change "$dep" "@rpath/libcrypto.3.dylib" "${REAL_DYLIB}" ;;
    esac
done
codesign --force --sign - "${REAL_DYLIB}" 2>/dev/null || true

echo "✅ 库 -> ${LIB_DIR}"
echo "✅ 头 -> ${INCLUDE_DIR}"
echo "   OpenSSL 依赖已改为 @rpath/libssl.3.dylib + @rpath/libcrypto.3.dylib"
