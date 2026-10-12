#!/bin/bash

# Flight Hub Java SDK Demo 构建脚本
# 作者: DJI
# 版本: 1.0

# Exit immediately if a command exits with a non-zero status.
set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# --- Configuration ---
# 获取当前脚本所在目录
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

# 检查SDK JAR包是否存在
check_sdk_jar() {
    log_info "检查SDK JAR包..."
    
    # 检查配置文件中的路径
    local config_file="$DEMO_DIR/config.properties"
    local sdk_jar_path=""
    
    if [ -f "$config_file" ]; then
        sdk_jar_path=$(grep "^sdk.jar.path=" "$config_file" | cut -d'=' -f2 | tr -d ' ')
        if [ -n "$sdk_jar_path" ]; then
            sdk_jar_path="$(cd "$DEMO_DIR" && realpath "$sdk_jar_path")"
            log_info "从配置文件读取SDK JAR路径: $sdk_jar_path"
        fi
    fi
    
    # 如果配置文件没有指定，使用默认路径
    if [ -z "$sdk_jar_path" ]; then
        sdk_jar_path="$DIST_DIR/flight-hub-sdk.jar"
        log_info "使用默认SDK JAR路径: $sdk_jar_path"
    fi
    
    if [ ! -f "$sdk_jar_path" ]; then
        log_error "SDK JAR包不存在: $sdk_jar_path"
        log_error "请先构建SDK项目，或检查配置文件中的路径设置"
        return 1
    fi
    
    log_success "SDK JAR包存在: $sdk_jar_path"
    return 0
}

# 清理构建目录
clean_build() {
    log_info "清理Demo构建目录..."
    
    cd "$DEMO_DIR"
    run_gradle clean
    
    log_success "Demo构建目录清理完成"
}

# 构建Demo项目
build_demo() {
    log_info "构建Demo项目..."
    
    cd "$DEMO_DIR"
    
    # 检查SDK JAR包
    check_sdk_jar || return 1
    
    # 使用Gradle构建
    run_gradle build
    
    if [ $? -eq 0 ]; then
        log_success "Demo项目构建成功"
        
        # 显示构建产物
        log_info "构建产物位置:"
        log_info "  - JAR包: $DEMO_DIR/build/libs/"
        ls -la "$DEMO_DIR/build/libs/"
    else
        log_error "Demo项目构建失败"
        return 1
    fi
}

# 运行Demo
run_demo() {
    log_info "运行Demo..."
    
    cd "$DEMO_DIR"
    
    # 检查SDK JAR包
    check_sdk_jar || return 1
    
    # 使用Gradle运行
    # 注意：这里刻意不走 run_gradle —— demo 是实时控制类交互程序，
    # 不能让它继承编译用的 nice 15 低优先级。
    ./gradlew run
}

# 创建可执行JAR
create_executable_jar() {
    log_info "创建可执行JAR..."
    
    cd "$DEMO_DIR"
    
    # 检查SDK JAR包
    check_sdk_jar || return 1
    
    # 使用Gradle创建可执行JAR（finalizedBy copyDemoConfigs：拷贝 config*.json + drtc_config_node*.json）
    run_gradle executableJar
    
    if [ $? -eq 0 ]; then
        log_success "可执行JAR创建成功"
        
        # 显示构建产物
        log_info "可执行JAR位置:"
        log_info "  - JAR包: $DEMO_DIR/build/libs/"
        ls -la "$DEMO_DIR/build/libs/" | grep executable
        if [ -f "$DEMO_DIR/build/libs/drtc_config_node.json" ]; then
            log_success "已拷贝 DRTC 配置: build/libs/drtc_config_node.json"
        else
            log_error "未找到 build/libs/drtc_config_node.json（检查 copyDemoConfigs 任务）"
        fi
        if [ -f "$DEMO_DIR/build/libs/config.json" ] || ls "$DEMO_DIR/build/libs"/config_*.json >/dev/null 2>&1; then
            log_success "已拷贝 Demo 环境配置 JSON 到 build/libs/"
        fi
        log_info "运行示例:"
        log_info "  cd build/libs && java -jar flight-hub-sdk-demo-*-executable.jar config_204.json"
    else
        log_error "可执行JAR创建失败"
        return 1
    fi
}

# 显示帮助信息
show_help() {
    echo "Flight Hub Java SDK Demo 构建脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help          显示此帮助信息"
    echo "  -c, --clean         清理构建目录"
    echo "  -b, --build         构建Demo项目"
    echo "  -r, --run           运行Demo"
    echo "  -e, --executable    创建可执行JAR"
    echo "  -a, --all           清理、构建并创建可执行JAR"
    echo ""
    echo "示例:"
    echo "  $0 --clean          # 清理构建目录"
    echo "  $0 --build          # 构建Demo项目"
    echo "  $0 --run            # 运行Demo"
    echo "  $0 --executable     # 创建可执行JAR"
    echo "  $0 --all            # 完整构建流程"
}

# 主函数
main() {
    local clean_only=false
    local build_only=false
    local run_only=false
    local executable_only=false
    local all_tasks=false
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -c|--clean)
                clean_only=true
                shift
                ;;
            -b|--build)
                build_only=true
                shift
                ;;
            -r|--run)
                run_only=true
                shift
                ;;
            -e|--executable)
                executable_only=true
                shift
                ;;
            -a|--all)
                all_tasks=true
                shift
                ;;
            *)
                log_error "未知选项: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 如果没有指定任何选项，显示帮助信息
    if [ "$clean_only" = false ] && [ "$build_only" = false ] && \
       [ "$run_only" = false ] && [ "$executable_only" = false ] && \
       [ "$all_tasks" = false ]; then
        log_warning "未指定任何构建选项"
        echo ""
        show_help
        exit 0
    fi
    
    log_info "开始Flight Hub Java SDK Demo构建..."
    
    # 执行清理
    if [ "$clean_only" = true ] || [ "$all_tasks" = true ]; then
        clean_build
        if [ "$clean_only" = true ]; then
            exit 0
        fi
    fi
    
    # 执行构建
    if [ "$build_only" = true ] || [ "$all_tasks" = true ]; then
        build_demo || exit 1
        if [ "$build_only" = true ]; then
            exit 0
        fi
    fi
    
    # 执行运行
    if [ "$run_only" = true ]; then
        run_demo || exit 1
        exit 0
    fi
    
    # 创建可执行JAR
    if [ "$executable_only" = true ] || [ "$all_tasks" = true ]; then
        create_executable_jar || exit 1
    fi
    
    log_success "Flight Hub Java SDK Demo构建完成！"
}

# 运行主函数
main "$@"