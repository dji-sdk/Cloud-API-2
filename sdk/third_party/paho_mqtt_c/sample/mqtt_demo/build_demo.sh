#!/bin/bash
# 构建 MQTT demo。不自动连 broker。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="${SCRIPT_DIR}/build"

echo "构建 MQTT Demo → ${BUILD_DIR}"

rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}"
cd "${BUILD_DIR}"

cmake -DCMAKE_BUILD_TYPE=Release "${SCRIPT_DIR}"

# 资源门控：优先用上层 export 的 FHSDK_MAX_JOBS；单独跑时用核数但封顶 8。
# 详见 scripts/README.md。
PARALLEL_JOBS="${FHSDK_MAX_JOBS:-$(n=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4); [ "$n" -gt 8 ] && echo 8 || echo "$n")}"
nice -n "${FHSDK_NICE:-15}" cmake --build . -j"$PARALLEL_JOBS"

echo "构建成功: ${BUILD_DIR}/mqtt_demo"
echo "运行示例: ${BUILD_DIR}/mqtt_demo tcp://127.0.0.1:1883"
