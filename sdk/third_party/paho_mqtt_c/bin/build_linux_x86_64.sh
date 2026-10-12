#!/bin/bash
# 使用仓内 OpenSSL 1.1.1 编译 paho-mqtt3as 动态库

set -e

echo "=== 编译 paho-mqtt3as (Linux x86_64，SSL) ==="

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PAHO_ROOT="$(dirname "$SCRIPT_DIR")"
SRC_DIR="${PAHO_ROOT}/src"
BUILD_DIR="${PAHO_ROOT}/build"
LIB_DIR="${PAHO_ROOT}/lib/linux/x86_64"
INCLUDE_DIR="${PAHO_ROOT}/include"

if [ ! -f "${SRC_DIR}/CMakeLists.txt" ]; then
    echo "❌ 没有源码: ${SRC_DIR}"
    echo "git clone --branch v1.3.14 https://github.com/eclipse/paho.mqtt.c.git ${SRC_DIR}"
    exit 1
fi

PROJECT_ROOT="$(dirname "$(dirname "$PAHO_ROOT")")"
THIRD_PARTY_ROOT="${PROJECT_ROOT}/third_party"
PATH_HYGIENE_FLAGS="-ffile-prefix-map=${THIRD_PARTY_ROOT}=/build/vendor/third_party -ffile-prefix-map=${HOME}=/build/vendor/home -ffile-prefix-map=/home/jenkins=/build/vendor"

OPENSSL_ROOT="${THIRD_PARTY_ROOT}/openssl_1_1_1"
OPENSSL_LIB_DIR="${OPENSSL_ROOT}/lib/linux/x86_64"
OPENSSL_INCLUDE_DIR="${OPENSSL_ROOT}/include"

echo "OpenSSL 根目录: ${OPENSSL_ROOT}"

if [[ ! -f "${OPENSSL_LIB_DIR}/libssl.so.1.1" ]] && [[ ! -L "${OPENSSL_LIB_DIR}/libssl.so" ]]; then
    echo "❌ 未找到 OpenSSL 库: ${OPENSSL_LIB_DIR}/libssl.so.1.1"
    exit 1
fi
if [[ ! -f "${OPENSSL_LIB_DIR}/libcrypto.so.1.1" ]]; then
    echo "❌ 未找到 OpenSSL 库: ${OPENSSL_LIB_DIR}/libcrypto.so.1.1"
    exit 1
fi
if [[ ! -d "${OPENSSL_INCLUDE_DIR}" ]]; then
    echo "❌ 未找到 OpenSSL 头: ${OPENSSL_INCLUDE_DIR}"
    exit 1
fi

rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}"

# 禁止把本机 OpenSSL 绝对路径写进 RUNPATH；运行时由 SDK / LD_LIBRARY_PATH 找库
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
      -DOPENSSL_SSL_LIBRARY="${OPENSSL_LIB_DIR}/libssl.so.1.1" \
      -DOPENSSL_CRYPTO_LIBRARY="${OPENSSL_LIB_DIR}/libcrypto.so.1.1"

cmake --build "${BUILD_DIR}" --target paho-mqtt3as

if [ ! -f "${BUILD_DIR}/src/libpaho-mqtt3as.so" ]; then
    echo "❌ 未产出 libpaho-mqtt3as.so"
    exit 1
fi

mkdir -p "${LIB_DIR}" "${INCLUDE_DIR}"
cp -a "${BUILD_DIR}"/src/libpaho-mqtt3as.so* "${LIB_DIR}/"
if command -v patchelf >/dev/null 2>&1; then
    for so in "${LIB_DIR}"/libpaho-mqtt3as.so*; do
        [[ -f "$so" && ! -L "$so" ]] || continue
        patchelf --remove-rpath "$so" 2>/dev/null || true
    done
fi
cp "${SRC_DIR}"/src/*.h "${INCLUDE_DIR}/"

echo "✅ 库 -> ${LIB_DIR}"
echo "✅ 头 -> ${INCLUDE_DIR}"
