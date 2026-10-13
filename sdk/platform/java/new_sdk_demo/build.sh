#!/bin/bash

# Flight Hub Java SDK 模块化 Demo 构建脚本
# 作者: DJI
# 版本: 1.0
#
# 说明:
#       本脚本是 ./gradlew 的薄封装(与老 demo 工程 flight_hub_sdk_demo/build.sh 风格一致)。
#       可执行 JAR 由 build.gradle 的 executableJar 任务产出(内含 SDK 类与原生库)。

set -eo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEMO_DIR="$SCRIPT_DIR"
PLATFORM_JAVA_DIR="$(dirname "$DEMO_DIR")"
DIST_DIR="$PLATFORM_JAVA_DIR/dist"

# 调 gradlew：并行度默认满核；上层可 export FHSDK_JOBS / FHSDK_MAX_JOBS。
run_gradle() {
    local jobs c
    if [ -n "${FHSDK_JOBS:-}" ] && [ "$FHSDK_JOBS" -gt 0 ] 2>/dev/null; then
        jobs="$FHSDK_JOBS"
    elif [ -n "${FHSDK_MAX_JOBS:-}" ] && [ "$FHSDK_MAX_JOBS" -gt 0 ] 2>/dev/null; then
        jobs="$FHSDK_MAX_JOBS"
    else
        c="$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4)"
        case "$c" in ""|*[!0-9]*) c=4 ;; esac
        jobs="$c"
    fi
    ./gradlew "$@" --max-workers="$jobs"
}

# 检查 SDK JAR 包
check_sdk_jar() {
    log_info "检查 SDK JAR 包..."
    local config_file="$DEMO_DIR/config.properties"
    local sdk_jar_path=""
    if [ -f "$config_file" ]; then
        sdk_jar_path=$(grep "^sdk.jar.path=" "$config_file" | cut -d'=' -f2 | tr -d ' ')
        if [ -n "$sdk_jar_path" ]; then
            sdk_jar_path="$(cd "$DEMO_DIR" && realpath "$sdk_jar_path" 2>/dev/null || echo "")"
        fi
    fi
    if [ -z "$sdk_jar_path" ]; then
        sdk_jar_path="$DIST_DIR/flight-hub-sdk.jar"
    fi
    if [ ! -f "$sdk_jar_path" ]; then
        log_error "SDK JAR 包不存在: $sdk_jar_path"
        log_error "请先构建 SDK 项目, 或检查 config.properties 中的 sdk.jar.path"
        return 1
    fi
    log_success "SDK JAR 包存在: $sdk_jar_path"
    return 0
}

# 清理构建目录
clean_build() {
    log_info "清理构建目录..."
    cd "$DEMO_DIR"
    run_gradle clean
    log_success "清理完成"
}

# 构建 Demo 项目
build_demo() {
    log_info "构建 Demo 项目..."
    cd "$DEMO_DIR"
    check_sdk_jar || return 1
    run_gradle build || return 1
    log_success "构建成功, 产物: $DEMO_DIR/build/libs/"
    ls -la "$DEMO_DIR/build/libs/"
}

# 运行 Demo
run_demo() {
    log_info "运行 Demo..."
    cd "$DEMO_DIR"
    check_sdk_jar || return 1
    # 刻意不走 run_gradle —— demo 是实时控制类交互程序，不能让它继承编译用的 nice 15 低优先级。
    ./gradlew run
}

# 创建可执行 JAR
create_executable_jar() {
    log_info "创建可执行 JAR..."
    cd "$DEMO_DIR"
    check_sdk_jar || return 1
    run_gradle executableJar
    log_success "可执行 JAR 创建成功:"
    ls -la "$DEMO_DIR/build/libs/" | grep executable
    echo ""
    log_info "运行方式(先准备 config.json, 见 README): "
    log_info "  java -Dfile.encoding=UTF-8 -jar build/libs/new-sdk-demo-executable.jar config.json"
    log_info "  java -Dfile.encoding=UTF-8 -jar build/libs/new-sdk-demo-executable.jar config.json FlyToScenario"
}

show_help() {
    echo "Flight Hub Java SDK 模块化 Demo 构建脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help          显示此帮助信息"
    echo "  -c, --clean         清理构建目录"
    echo "  -b, --build         构建 Demo 项目"
    echo "  -r, --run           运行 Demo"
    echo "  -e, --executable    创建可执行 JAR"
    echo "  -a, --all           清理、构建并创建可执行 JAR"
    echo ""
    echo "示例:"
    echo "  $0 --executable     # 创建可执行 JAR"
    echo "  $0 --all            # 完整构建流程"
}

main() {
    local clean_only=false build_only=false run_only=false executable_only=false all_tasks=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help) show_help; exit 0 ;;
            -c|--clean) clean_only=true; shift ;;
            -b|--build) build_only=true; shift ;;
            -r|--run) run_only=true; shift ;;
            -e|--executable) executable_only=true; shift ;;
            -a|--all) all_tasks=true; shift ;;
            *) log_error "未知选项: $1"; show_help; exit 1 ;;
        esac
    done

    if [ "$clean_only" = false ] && [ "$build_only" = false ] && \
       [ "$run_only" = false ] && [ "$executable_only" = false ] && \
       [ "$all_tasks" = false ]; then
        log_warning "未指定任何构建选项"; echo ""; show_help; exit 0
    fi

    log_info "开始 Flight Hub Java SDK 模块化 Demo 构建..."

    if [ "$clean_only" = true ] || [ "$all_tasks" = true ]; then
        clean_build
        [ "$clean_only" = true ] && exit 0
    fi
    if [ "$build_only" = true ] || [ "$all_tasks" = true ]; then
        build_demo || exit 1
        [ "$build_only" = true ] && exit 0
    fi
    if [ "$run_only" = true ]; then
        run_demo || exit 1
        exit 0
    fi
    if [ "$executable_only" = true ] || [ "$all_tasks" = true ]; then
        create_executable_jar || exit 1
    fi

    log_success "Flight Hub Java SDK 模块化 Demo 构建完成！"
}

main "$@"
