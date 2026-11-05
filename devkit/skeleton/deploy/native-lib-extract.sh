#!/usr/bin/env bash
# ============================================================================
# 从 fh-skeleton fat jar 中提取 SDK JNI 原生库（libflighthubsdk_jni.so 及依赖 .so）
#
# 背景：SDK native lib 内嵌于 fat jar 的 BOOT-INF/lib/flight-hub-sdk-*.jar
# 的双层嵌套 jar 中，JVM 的 JniLibraryLoader 无法直接加载（jar-in-jar 不可见）。
# 本脚本将 native lib 解压到 /opt/sdk-libs，配合 systemd 的
# -Djava.library.path=/opt/sdk-libs 使用（与 Docker 镜像内处理方式一致）。
#
# 用法：
#   sudo ./deploy/native-lib-extract.sh /path/to/fh-skeleton-app-0.1.0.jar
#
# 依赖：unzip（sudo apt install -y unzip）
# ============================================================================
set -euo pipefail

JAR="${1:-}"
if [[ -z "$JAR" || ! -f "$JAR" ]]; then
  echo "用法: $0 <fh-skeleton-app-*.jar>" >&2
  exit 1
fi

TARGET_DIR="${SDK_LIBS_DIR:-/opt/sdk-libs}"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

echo "提取 SDK native lib 到 ${TARGET_DIR} ..."
mkdir -p "$TARGET_DIR"

# 1. 从 fat jar 内提取内嵌的 flight-hub-sdk-*.jar
unzip -j -o "$JAR" 'BOOT-INF/lib/flight-hub-sdk-*.jar' -d "$WORK_DIR" >/dev/null

# 2. 按本机架构选择 SDK jar 内的原生库目录（SDK 分 amd64/arm64 两个包，
#    每个包只带自己那一份 lib/linux-*，用错架构会解压出空目录或加载失败）
case "$(uname -m)" in
  x86_64 | amd64) SDK_LIB_DIR=linux-x86_64 ;;
  aarch64 | arm64) SDK_LIB_DIR=linux-aarch64 ;;
  *)
    echo "不支持的本机架构: $(uname -m)（仅支持 x86_64 / aarch64）" >&2
    exit 1
    ;;
esac

if ! unzip -l "$WORK_DIR"/flight-hub-sdk-*.jar "lib/${SDK_LIB_DIR}/*" >/dev/null 2>&1; then
  echo "SDK jar 内不含 lib/${SDK_LIB_DIR}/，请改用与本机架构匹配的 SDK 包重新构建" >&2
  exit 1
fi

unzip -j -o "$WORK_DIR"/flight-hub-sdk-*.jar "lib/${SDK_LIB_DIR}/*" -d "$TARGET_DIR" >/dev/null

# 3. 设置执行权限（部分 .so 需要）
chmod 755 "$TARGET_DIR"/*.so*

echo "完成。native lib 数量：$(ls -1 "$TARGET_DIR" | wc -l | tr -d ' ')"
echo "请确认 systemd unit 的 ExecStart 包含 -Djava.library.path=${TARGET_DIR}"
