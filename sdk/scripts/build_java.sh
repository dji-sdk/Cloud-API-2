#!/bin/bash

# Copyright (c) 2026 SZ DJI Technology Co., Ltd.
# SPDX-License-Identifier: Apache-2.0


# Java 产物构建脚本;

# 当前目录
ORIGIN_DIR=$(pwd)
# 根目录
ROOT_DIR=${ORIGIN_DIR}/..

# 用 BASH_SOURCE 定位, 保证从任意 cwd 调用本脚本都能找到自身目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 并行度：--jobs=N / -j=N 为必传参数(不再按核数推默认值), 下限 1。
# 注意函数是直接改全局 FHSDK_JOBS 而不是 echo 结果：若写成 FHSDK_JOBS=$(fhsdk_require_jobs), 
# 函数里的 exit 1 只退出命令替换的子 shell, 主脚本会带着空并行度继续往下跑。
fhsdk_require_jobs() {
    if [ -z "${FHSDK_JOBS}" ]; then
        echo "错误: 缺少必传参数 --jobs=N / -j=N(构建并行度)" >&2
        echo "      例: ./build_java.sh -r -j=10(本机核数可用 nproc 查询)" >&2
        echo "      详见: ./build_java.sh --help" >&2
        exit 1
    fi
    # 只收十进制整数;负数带 '-'、小数带 '.'、字母等一律落到这里报错, 不做猜测性纠正
    case "${FHSDK_JOBS}" in
        *[!0-9]*)
            echo "错误: --jobs / -j 必须是十进制整数, 收到: '${FHSDK_JOBS}'" >&2
            exit 1
            ;;
    esac
    # 去前导零：'07' 原样传下去会被某些消费方按八进制解析;此处已确定全是数字, 10# 转换安全
    FHSDK_JOBS=$((10#${FHSDK_JOBS}))
    # 保证至少为 1：make -j0 / gradle --max-workers=0 会直接报错
    if [ "${FHSDK_JOBS}" -lt 1 ]; then
        echo "警告: --jobs=${FHSDK_JOBS} 无效, 已修正为下限 1" >&2
        FHSDK_JOBS=1
    fi
}

show_help() {
    cat <<'EOF'
Flight Hub Java SDK 打包构建脚本

用法: ./build_java.sh [选项]

选项:
  -h, --help          显示此帮助信息
  -v=X.Y.Z            覆盖版本号(默认读仓根 VERSION;-v=v3.1.0 与 -v=3.1.0 等价)
  -d, --debug         构建 Debug 版本
  -r, --release       构建 Release 版本(默认)
  --shell-protect     对 libflighthubsdk_jni.so 启用 VMP 加固
  --jobs=N, -j=N      【必传】本轮构建并行度, N 为整数且下限 1(无默认值, 必须显式指定)

环境变量:
  FHSDK_KEEP_DAEMON     设为 1 则构建结束后保留 Gradle daemon(默认停掉)
  (FHSDK_JOBS / FHSDK_MAX_JOBS 由本脚本按 --jobs 定版后 export 给下游子脚本, 
    不再作为本脚本的并行度输入——并行度只认 --jobs / -j)

示例:
  ./build_java.sh -r -j=10              # Release, 10 路并行
  ./build_java.sh -d --jobs=4           # Debug, 4 路并行
  ./build_java.sh -r -j=$(nproc)        # 吃满核(Linux;核数查询见 nproc / lscpu)
EOF
}

# --help 先于 VERSION 读取处理：缺 VERSION 文件时也要能打出帮助
for arg in "$@"; do
    if [[ $arg == -h ]] || [[ $arg == --help ]]; then
        show_help
        exit 0
    fi
done

# 并行度定版：与 --help 一样先于 VERSION 读取, 让「忘了传 -j」这类参数错误优先报出来, 
# 而不是先被一句无关的 "VERSION 文件不存在" 盖过去。
FHSDK_JOBS=""
for arg in "$@"; do
    if [[ $arg == --jobs=* ]]; then
        FHSDK_JOBS="${arg#--jobs=}"  # 与 -v= 同风格
    elif [[ $arg == -j=* ]]; then
        FHSDK_JOBS="${arg#-j=}"
    fi
done
fhsdk_require_jobs

# 与 platform/java/lib/<plat>、CMake CLOUD_API2_JAVA_PLAT 对齐：macosx-arm64 / linux-x86_64 / linux-aarch64
get_platform() {
    local OS
    OS=$(uname -s)
    local ARCH
    ARCH=$(uname -m)
    case "$OS" in
        Linux)
            case "$ARCH" in
                x86_64) echo "linux-x86_64" ;;
                aarch64) echo "linux-aarch64" ;;
                *) echo "linux-unknown" ;;
            esac
            ;;
        Darwin)
            case "$ARCH" in
                arm64) echo "macosx-arm64" ;;
                *) echo "macosx-unknown" ;;
            esac
            ;;
        *) echo "unknown-platform" ;;
    esac
}

PLATFORM=$(get_platform)
OUTPUT_DIR=${ROOT_DIR}/build/java/${PLATFORM}

# 版本处理逻辑：
#   1) 默认从 dji_flight_hub_sdk/VERSION 读(全局唯一 SoT;人手只改这一处)
#   2) 命令行 -v= / CI 可覆盖(不必先手改再提交)
#   3) 归一化：去空白 + 剥离可选前导 v;zip 名统一用无 v 的 X.Y.Z
#   4) 定版后写回仓根 VERSION, 再编 CMake/Gradle, 保证本轮 zip/MANIFEST/version.h 一致
#   5) 打包时把 VERSION 复制进用户 demo/、new_sdk_demo/(用户无整仓也能就地读版本)
VERSION_FILE="${ROOT_DIR}/VERSION"
if [ -f "${VERSION_FILE}" ]; then
    version=$(tr -d '[:space:]' < "${VERSION_FILE}")
else
    echo "错误: VERSION 文件不存在: ${VERSION_FILE}" >&2
    exit 1
fi
build_type="release"  # 默认构建类型
shell_protect=false  # 是否启用 VMP 加固

# 检查命令行参数
for arg in "$@"; do
    if [[ $arg == -v=* ]]; then
        version="${arg#-v=}"  # -v= 覆盖 VERSION 文件的值
    elif [[ $arg == -d ]] || [[ $arg == --debug ]]; then
        build_type="debug"
    elif [[ $arg == -r ]] || [[ $arg == --release ]]; then
        build_type="release"
    elif [[ $arg == --shell-protect ]]; then
        shell_protect=true
    fi
done

# 归一化：去空白 + 剥离可选前导 v(允许 -v=v3.1.0 与 -v=3.1.0 等价)
version=$(echo "${version}" | tr -d '[:space:]')
version="${version#v}"

# 门禁: 版本号必须是 X.Y.Z 三段式, 防止格式异常静默传播到 zip/MANIFEST/version.h
if ! [[ "${version}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "错误: 版本号格式非法: '${version}'(应为 X.Y.Z, 如 3.3.7)" >&2
    exit 1
fi

# 写回 VERSION：让本轮编译的 CMake / Gradle 都读到这一份, 防止只改 zip 名的偏差
echo "${version}" > "${VERSION_FILE}"

set -e

# 并行度已在参数解析阶段定版并校验(fhsdk_require_jobs), 这里只 export 给下游脚本沿用同一值。
export FHSDK_JOBS
# 兼容仍读旧变量名的下游/内部工具
export FHSDK_MAX_JOBS="$FHSDK_JOBS"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}开始构建 Flight Hub Java SDK...${NC}"
echo -e "${YELLOW}使用版本: ${version}${NC}"
echo -e "${YELLOW}构建类型: ${build_type}${NC}"
echo -e "${YELLOW}平台: ${PLATFORM}${NC}"
echo -e "${YELLOW}产物目录: ${OUTPUT_DIR}${NC}"
echo -e "${YELLOW}VMP 加固: ${shell_protect}${NC}"
echo -e "${YELLOW}并行度: ${FHSDK_JOBS}${NC}"

## change directory to java platform & exec build script;
cd ${ROOT_DIR}/platform/java/bin

./build.sh -c
if [ "$build_type" = "debug" ]; then
    ./build.sh -d
else
    ./build.sh -r
fi

## buid flight_hub_sdk_demo;
# VMP 加固(在 demo 打包前替换 so, 使 jar 中包含加固版本)
if [ "${shell_protect}" = "true" ]; then
    SO_PATH="${ROOT_DIR}/platform/java/lib/${PLATFORM}/libflighthubsdk_jni.so"
    VMP_SCRIPT="${ORIGIN_DIR}/vmp/vmp_protect.sh"
    if [ ! -f "${VMP_SCRIPT}" ]; then
        echo -e "${RED}[ERROR] vmp_protect.sh 不存在: ${VMP_SCRIPT}${NC}"
        exit 1
    fi
    echo -e "${YELLOW}[VMP] 开始加固 libflighthubsdk_jni.so...${NC}"
    bash "${VMP_SCRIPT}" "${SO_PATH}" "${SO_PATH}"
    echo -e "${GREEN}[VMP] 加固完成${NC}"
fi

cd ${ROOT_DIR}/platform/java/flight_hub_sdk_demo

./build.sh -c
./build.sh -e

rm -rf ${OUTPUT_DIR}/*

mkdir -p ${OUTPUT_DIR}/dist
mkdir -p ${OUTPUT_DIR}/demo
cp -r ${ROOT_DIR}/platform/java/dist/flight-hub-sdk.jar ${OUTPUT_DIR}/dist/flight-hub-sdk.jar
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/build/libs/flight-hub-sdk-demo-executable.jar ${OUTPUT_DIR}/demo/flight-hub-sdk-demo-executable.jar
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/config_empty.json ${OUTPUT_DIR}/demo/config.json
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/config.properties ${OUTPUT_DIR}/demo/config.properties
# gradle.properties 必须随包发：用户包里没有它的话, demo 构建会走 Gradle 默认 max-workers = 用户机器核数, 
# 且丢失 UTF-8 / zh-CN 的 systemProp(demo 控制台输出是中文, 缺了会乱码)。
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/gradle.properties ${OUTPUT_DIR}/demo/gradle.properties
# DRTC 空模板(与 config_empty 同风格, 与 config.json 同级);不打包 *_204 等真实环境配置
cp ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/drtc_config_node.json ${OUTPUT_DIR}/demo/drtc_config_node.json
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/gradle ${OUTPUT_DIR}/demo/
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/src ${OUTPUT_DIR}/demo/
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/build.gradle ${OUTPUT_DIR}/demo/
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/build.sh ${OUTPUT_DIR}/demo/
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/gradlew ${OUTPUT_DIR}/demo/
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/gradlew.bat ${OUTPUT_DIR}/demo/
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/settings.gradle ${OUTPUT_DIR}/demo/
cp -r ${ROOT_DIR}/platform/java/flight_hub_sdk_demo/README.md ${OUTPUT_DIR}/demo/
# 用户包无整仓：把全局 VERSION 复制到 demo 根, 供 build.gradle 就地读取(人手只维护仓根 VERSION)
cp "${VERSION_FILE}" ${OUTPUT_DIR}/demo/VERSION

## build new_sdk_demo(模块化新版 demo, 与旧 demo 隔离并存, 互不影响)
## 说明：new_sdk_demo 是独立 gradle 工程, 产物名为 new-sdk-demo-executable.jar(区别于旧 demo), 
##       打包进独立的 new_sdk_demo/ 目录, 与旧 demo 的 demo/ 目录互不覆盖。
cd ${ROOT_DIR}/platform/java/new_sdk_demo

./build.sh -c
./build.sh -e

mkdir -p ${OUTPUT_DIR}/new_sdk_demo
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/build/libs/new-sdk-demo-executable.jar ${OUTPUT_DIR}/new_sdk_demo/new-sdk-demo-executable.jar
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/config.json.example ${OUTPUT_DIR}/new_sdk_demo/config.json
# DRTC 占位模板：config.json 里 sdk.drtc_config_path 默认就写 "drtc_config_node.json"(jar 同级/CWD 相对路径), 
# 所以必须落地成这个文件名才能被解析到;host 为 xx.xx.xx.xx 占位, 用户改成真实环境即可。
# 内容与 demo/drtc_config_node.json 保持一致——含 verify_ssl 全为 true(产物安全审查要求, 见 8d3485ec1);
# 本地无 CA 联调要关校验, 改自己那份 gitignore 的 drtc_config_node.json, 别改 .example, 否则会随包发出去。
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/drtc_config_node.json.example ${OUTPUT_DIR}/new_sdk_demo/drtc_config_node.json
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/config.properties ${OUTPUT_DIR}/new_sdk_demo/config.properties
# 同 demo/：随包发 gradle.properties(max-workers 兜底 + UTF-8 systemProp)
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/gradle.properties ${OUTPUT_DIR}/new_sdk_demo/gradle.properties
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/gradle ${OUTPUT_DIR}/new_sdk_demo/
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/src ${OUTPUT_DIR}/new_sdk_demo/
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/build.gradle ${OUTPUT_DIR}/new_sdk_demo/
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/build.sh ${OUTPUT_DIR}/new_sdk_demo/
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/gradlew ${OUTPUT_DIR}/new_sdk_demo/
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/gradlew.bat ${OUTPUT_DIR}/new_sdk_demo/
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/settings.gradle ${OUTPUT_DIR}/new_sdk_demo/
cp -r ${ROOT_DIR}/platform/java/new_sdk_demo/README.md ${OUTPUT_DIR}/new_sdk_demo/
# 用户包无整仓：复制全局 VERSION(与 demo/ 同策略)
cp "${VERSION_FILE}" ${OUTPUT_DIR}/new_sdk_demo/VERSION
# 注意：不拷贝 tests/(含大体积冒烟 jar 与日志), 也不拷贝本地 config.json / drtc_config_node.json
#       (两者都不入库, 只发 *.example 并在包内落地成去掉 .example 的文件名)

cd ${OUTPUT_DIR}

# 根据构建类型和加固标识生成 zip 文件名
if [ "$build_type" = "debug" ]; then
    zip_name="flight_hub_sdk_v${version}_debug_$(date +%Y%m%d)_${PLATFORM}"
else
    zip_name="flight_hub_sdk_v${version}_$(date +%Y%m%d)_${PLATFORM}"
fi
zip -r "${zip_name}.zip" ./*

# 回收 Gradle daemon：三个 Gradle 工程各会拉起 daemon, 且默认存活 3 小时。
# 打包构建是一次性动作, 不停的话 daemon 会逐轮累积(实测曾有 8 个常驻、合计 3.6G RSS), 
# 挤占的正是下一轮编译要用的内存。FHSDK_KEEP_DAEMON=1 可保留(迭代开发时想要热 daemon)。
# 注意 --stop 按 Gradle 版本生效, 只能停掉与本仓 wrapper 同版本(8.5)的 daemon;
# 机器上 IDE / 其他工程起的别版本 daemon 不归本脚本管, 也不该由构建脚本代杀。
if [ "${FHSDK_KEEP_DAEMON}" != "1" ]; then
    echo -e "${YELLOW}回收 Gradle daemon(FHSDK_KEEP_DAEMON=1 可跳过)...${NC}"
    "${ROOT_DIR}/platform/java/gradlew" --stop >/dev/null 2>&1 || true
fi

echo "Java SDK build completed."



