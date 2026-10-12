#!/bin/bash

# AWS S3 SDK 构建脚本
# 自动检测当前平台并构建，可编 macOS (arm64/x86_64) 与 Linux (x86_64/aarch64)
# - macOS: 使用系统 OpenSSL 3.5.2（通过 Homebrew 安装）
# - Linux: 使用项目 OpenSSL 1.1.1f

set -e

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AWS_S3_ROOT="$(dirname "$SCRIPT_DIR")"
PROJECT_ROOT="$(dirname "$(dirname "$AWS_S3_ROOT")")"
THIRD_PARTY_ROOT="$PROJECT_ROOT/third_party"
# 路径卫生：编译期映射本机/CI 路径
PATH_HYGIENE_FLAGS="-ffile-prefix-map=${THIRD_PARTY_ROOT}=/build/vendor/third_party -ffile-prefix-map=${HOME}=/build/vendor/home -ffile-prefix-map=/home/jenkins=/build/vendor"

# 获取 CPU 核心数
# 资源门控：优先用上层 export 的 FHSDK_MAX_JOBS（scripts/build_java.sh 统一定版）；
# 单独跑本脚本时用"核数但封顶 8"，避免在 20 核机器上 -j20 打满 CPU 把 SSH 挤断。详见 scripts/README.md。
PARALLEL_JOBS="${FHSDK_MAX_JOBS:-$(n=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4); [ "$n" -gt 8 ] && echo 8 || echo "$n")}"

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

# 显示使用说明
show_usage() {
    echo "AWS S3 SDK 构建脚本"
    echo "==================="
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --build, -b            构建 AWS S3 SDK"
    echo "  --clean                清理构建目录"
    echo "  --help, -h             显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0                     # 显示此帮助信息"
    echo "  $0 --build             # 构建 AWS S3 SDK"
    echo "  $0 -b                  # 构建 AWS S3 SDK (简写)"
    echo "  $0 --clean             # 清理构建目录"
    echo "  $0 --help              # 显示此帮助信息"
    echo ""
    echo "本脚本可构建的平台:"
    echo "  macOS ARM64 (Apple Silicon)"
    echo "  macOS x86_64 (Intel)"
    echo "  Linux x86_64"
    echo "  Linux ARM64"
    echo ""
}

# 检测当前平台
detect_platform() {
    local os=$(uname -s | tr '[:upper:]' '[:lower:]')
    local arch=$(uname -m)
    
    case $os in
        "darwin")
            case $arch in
                "arm64"|"aarch64")
                    echo "macosx arm64"
                    ;;
                "x86_64")
                    echo "macosx x86_64"
                    ;;
                *)
                    log_error "不支持的 macOS 架构: $arch"
                    exit 1
                    ;;
            esac
            ;;
        "linux")
            case $arch in
                "x86_64")
                    echo "linux x86_64"
                    ;;
                "aarch64"|"arm64")
                    echo "linux aarch64"
                    ;;
                *)
                    log_error "不支持的 Linux 架构: $arch"
                    exit 1
                    ;;
            esac
            ;;
        *)
            log_error "不支持的操作系统: $os"
            exit 1
            ;;
    esac
}

# 检查依赖
check_deps() {
    log_info "检查依赖项..."
    
    local missing_deps=()
    
    # 检查基本工具
    command -v cmake >/dev/null 2>&1 || missing_deps+=("cmake")
    command -v make >/dev/null 2>&1 || missing_deps+=("make")
    
    # 检查编译器
    if command -v clang++ >/dev/null 2>&1; then
        COMPILER="clang++"
    elif command -v g++ >/dev/null 2>&1; then
        COMPILER="g++"
    else
        missing_deps+=("c++ compiler")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        log_error "缺少以下依赖项: ${missing_deps[*]}"
        log_info "请安装缺少的依赖项后重试"
        exit 1
    fi
    
    # 检查 CMake 版本
    local cmake_version=$(cmake --version | head -n1 | cut -d' ' -f3)
    local required_version="3.10"
    if ! printf '%s\n%s\n' "$required_version" "$cmake_version" | sort -V -C; then
        log_error "CMake 版本过低: $cmake_version (需要 >= $required_version)"
        exit 1
    fi
    
    log_success "依赖项检查通过"
}

# 检查第三方库
check_third_party_libs() {
    local os=$1
    local arch=$2
    
    log_info "检查第三方库..."
    
    local missing_libs=()
    
    # 根据平台检查不同的 OpenSSL
    if [ "$os" = "macosx" ]; then
        # macOS: 检查系统 OpenSSL 3.5.2
        local openssl_root="/opt/homebrew/opt/openssl@3"
        local openssl_lib_dir="$openssl_root/lib"
        local openssl_include_dir="$openssl_root/include"
        
        if [ ! -d "$openssl_include_dir" ]; then
            missing_libs+=("系统 OpenSSL 头文件 (请安装: brew install openssl@3)")
        fi
        if [ ! -f "$openssl_lib_dir/libssl.dylib" ] && [ ! -f "$openssl_lib_dir/libssl.a" ]; then
            missing_libs+=("系统 OpenSSL SSL 库 (请安装: brew install openssl@3)")
        fi
        if [ ! -f "$openssl_lib_dir/libcrypto.dylib" ] && [ ! -f "$openssl_lib_dir/libcrypto.a" ]; then
            missing_libs+=("系统 OpenSSL Crypto 库 (请安装: brew install openssl@3)")
        fi
    else
        # Linux: 检查项目 OpenSSL 1.1.1
        local openssl_root="$THIRD_PARTY_ROOT/openssl_1_1_1"
        local openssl_lib_dir="$openssl_root/lib/$os/$arch"
        if [ ! -d "$openssl_lib_dir" ]; then
            missing_libs+=("项目 OpenSSL 1.1.1 for $os/$arch")
        fi
        if [ ! -f "$openssl_lib_dir/libssl.so.1.1" ] && [ ! -f "$openssl_lib_dir/libssl.so" ]; then
            missing_libs+=("项目 OpenSSL SSL 库 for $os/$arch")
        fi
        if [ ! -f "$openssl_lib_dir/libcrypto.so.1.1" ] && [ ! -f "$openssl_lib_dir/libcrypto.so" ]; then
            missing_libs+=("项目 OpenSSL Crypto 库 for $os/$arch")
        fi
        if [ ! -d "$openssl_root/include" ]; then
            missing_libs+=("项目 OpenSSL 头文件")
        fi
    fi
    
    # 检查 libcurl（库 + 头文件）
    if [ ! -d "$THIRD_PARTY_ROOT/libcurl/lib/$os/$arch" ]; then
        missing_libs+=("libcurl for $os/$arch")
    fi
    
    if [ ! -d "$THIRD_PARTY_ROOT/libcurl/include" ]; then
        missing_libs+=("libcurl headers")
    fi
    
    if [ ${#missing_libs[@]} -ne 0 ]; then
        log_error "缺少以下第三方库: ${missing_libs[*]}"
        if [ "$os" = "macosx" ]; then
            log_info "macOS: 请安装系统 OpenSSL: brew install openssl@3"
        else
            log_info "Linux: 请确保第三方库已正确构建并放置在 $THIRD_PARTY_ROOT 目录下"
        fi
        exit 1
    fi
    
    log_success "第三方库检查通过"
}


# 构建 AWS S3 SDK
build_aws_s3() {
    local os=$1
    local arch=$2
    local build_dir="$AWS_S3_ROOT/build"
    local install_dir="$AWS_S3_ROOT/lib/$os/$arch"
    
    log_info "开始构建 AWS S3 SDK for $os $arch..."
    
    # 干净配置：
    # 1) 清 build，避免 CMakeCache 残留系统 crypto 路径
    # 2) 清 install：上一轮 organize_output 会把 .a 挪到顶层，但留下
    #    lib/*/cmake 仍指向已搬走的 lib/libs2n.a，子工程 find_package 会踩坑
    rm -rf "$build_dir" "$install_dir"
    mkdir -p "$build_dir" "$install_dir"
    cd "$build_dir"
    
    # 根据平台设置不同的 OpenSSL 路径
    local openssl_root=""
    local openssl_lib_dir=""
    local openssl_include_dir=""
    local openssl_crypto_lib=""
    local openssl_ssl_lib=""
    
    if [ "$os" = "macosx" ]; then
        # macOS: 使用系统 OpenSSL 3.5.2
        openssl_root="/opt/homebrew/opt/openssl@3"
        openssl_lib_dir="$openssl_root/lib"
        openssl_include_dir="$openssl_root/include"
        
        # macOS 使用 .dylib 文件
        openssl_crypto_lib="$openssl_lib_dir/libcrypto.dylib"
        openssl_ssl_lib="$openssl_lib_dir/libssl.dylib"
        
        # 如果 .dylib 不存在，尝试使用 .a 静态库
        if [ ! -f "$openssl_crypto_lib" ] && [ -f "$openssl_lib_dir/libcrypto.a" ]; then
            openssl_crypto_lib="$openssl_lib_dir/libcrypto.a"
        fi
        if [ ! -f "$openssl_ssl_lib" ] && [ -f "$openssl_lib_dir/libssl.a" ]; then
            openssl_ssl_lib="$openssl_lib_dir/libssl.a"
        fi
        
        log_info "macOS: 使用系统 OpenSSL 3.5.2"
    else
        # Linux: 使用项目 OpenSSL 1.1.1
        openssl_root="$THIRD_PARTY_ROOT/openssl_1_1_1"
        openssl_lib_dir="$openssl_root/lib/$os/$arch"
        openssl_include_dir="$openssl_root/include"
        
        # Linux 使用 .so.1.1 文件
        openssl_crypto_lib="$openssl_lib_dir/libcrypto.so.1.1"
        openssl_ssl_lib="$openssl_lib_dir/libssl.so.1.1"
        
        # 如果 .so.1.1 不存在，尝试使用符号链接 .so
        if [ ! -f "$openssl_crypto_lib" ] && [ -L "$openssl_lib_dir/libcrypto.so" ]; then
            openssl_crypto_lib="$openssl_lib_dir/libcrypto.so"
        fi
        if [ ! -f "$openssl_ssl_lib" ] && [ -L "$openssl_lib_dir/libssl.so" ]; then
            openssl_ssl_lib="$openssl_lib_dir/libssl.so"
        fi
        
        log_info "Linux: 使用项目 OpenSSL 1.1.1"
    fi
    
    local curl_lib="$THIRD_PARTY_ROOT/libcurl/lib/$os/$arch/libcurl.a"
    # 仓库里头文件在 libcurl/include/*.h，AWS 需要 <curl/curl.h>，包一层 curl/ 目录
    local curl_include_wrap="$build_dir/curl_include_wrap"
    mkdir -p "$curl_include_wrap"
    ln -sfn "$THIRD_PARTY_ROOT/libcurl/include" "$curl_include_wrap/curl"
    local curl_include_dir="$curl_include_wrap"
    
    log_info "OpenSSL 根目录: $openssl_root"
    log_info "OpenSSL 库目录: $openssl_lib_dir"
    log_info "OpenSSL 头文件目录: $openssl_include_dir"
    
    # 检查库文件是否存在
    if [ ! -f "$openssl_crypto_lib" ] && [ ! -L "$openssl_crypto_lib" ]; then
        log_error "OpenSSL crypto 库不存在: $openssl_crypto_lib"
        log_error "请检查 $openssl_lib_dir 目录"
        exit 1
    fi
    
    if [ ! -f "$openssl_ssl_lib" ] && [ ! -L "$openssl_ssl_lib" ]; then
        log_error "OpenSSL SSL 库不存在: $openssl_ssl_lib"
        log_error "请检查 $openssl_lib_dir 目录"
        exit 1
    fi
    
    if [ ! -d "$openssl_include_dir" ]; then
        log_error "OpenSSL 头文件目录不存在: $openssl_include_dir"
        exit 1
    fi
    
    if [ ! -f "$curl_lib" ]; then
        log_error "libcurl 库不存在: $curl_lib"
        exit 1
    fi
    
    if [ ! -f "$AWS_S3_ROOT/src/CMakeLists.txt" ]; then
        log_error "没有源码: $AWS_S3_ROOT/src"
        log_info "git clone --branch 1.11.642 --recurse-submodules https://github.com/aws/aws-sdk-cpp.git $AWS_S3_ROOT/src"
        exit 1
    fi

    # 配置项目
    log_info "配置项目..."
    
    local cmake_args=(
        "$AWS_S3_ROOT/src/"
        "-DCMAKE_BUILD_TYPE=Release"
        "-DBUILD_ONLY=s3"
        "-DCMAKE_INSTALL_PREFIX=$install_dir"
        "-DBUILD_SHARED_LIBS=OFF"
        "-DENABLE_TESTING=OFF"
        "-DFORCE_SHARED_CRT=OFF"
        "-DENFORCE_SUBMODULE_VERSIONS=OFF"
        "-DENABLE_OPENSSL_ENCRYPTION=Yes"
        "-DOPENSSL_CRYPTO_LIBRARY=$openssl_crypto_lib"
        "-DOPENSSL_SSL_LIBRARY=$openssl_ssl_lib"
        "-DOPENSSL_INCLUDE_DIR=$openssl_include_dir"
        "-DOPENSSL_ROOT_DIR=$openssl_root"
        "-DOPENSSL_FOUND=TRUE"
        "-DOPENSSL_LIBRARIES=$openssl_ssl_lib;$openssl_crypto_lib"
        "-DOPENSSL_INCLUDE_DIRS=$openssl_include_dir"
        "-DENABLE_CURL_CLIENT=Yes"
        "-DCURL_INCLUDE_DIR=$curl_include_dir"
        "-DCURL_LIBRARY=$curl_lib"
        "-DCMAKE_C_FLAGS=${PATH_HYGIENE_FLAGS}"
        "-DCMAKE_CXX_FLAGS=${PATH_HYGIENE_FLAGS}"
    )

    # Linux: s2n / aws-c-cal 走 Findcrypto，默认会捡到系统 OpenSSL 3（/usr/include），
    # 与工程锁的 openssl_1_1_1 混用 → libs2n 编出 EVP_MD_fetch 等 3.x 符号，最终链 1.1.1 失败。
    # 强制 crypto_* 与 OPENSSL_* 同源。macOS 全程 openssl@3，无需此项。
    if [ "$os" = "linux" ]; then
        local openssl_crypto_static="$openssl_lib_dir/libcrypto.a"
        if [ ! -f "$openssl_crypto_static" ]; then
            log_error "OpenSSL crypto 静态库不存在: $openssl_crypto_static（s2n 静态链接需要）"
            exit 1
        fi
        cmake_args+=(
            "-Dcrypto_INCLUDE_DIR=$openssl_include_dir"
            "-Dcrypto_SHARED_LIBRARY=$openssl_crypto_lib"
            "-Dcrypto_STATIC_LIBRARY=$openssl_crypto_static"
            "-Dcrypto_LIBRARY=$openssl_crypto_static"
        )
        log_info "Linux: 强制 s2n/Findcrypto 使用 openssl_1_1_1（避免系统 OpenSSL 3）"
    fi
    
    # 添加平台特定配置
    if [ "$os" = "macosx" ]; then
        cmake_args+=(
            "-DTARGET_ARCH=APPLE"
            "-DCMAKE_OSX_SYSROOT=/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk"
            "-DCMAKE_SYSTEM_NAME=Darwin"
        )
    elif [ "$os" = "linux" ]; then
        cmake_args+=(
            "-DTARGET_ARCH=LINUX"
            "-DCMAKE_SYSTEM_NAME=Linux"
        )
    fi
    
    cmake "${cmake_args[@]}"
    
    # 编译
    log_info "编译项目 (使用 $PARALLEL_JOBS 个并行任务)..."
    nice -n "${FHSDK_NICE:-15}" make install -j "$PARALLEL_JOBS"
    
    cd - > /dev/null
    
    log_success "AWS S3 SDK 构建完成"
}


# 组织输出文件
organize_output() {
    local os=$1
    local arch=$2
    local lib_dir="$AWS_S3_ROOT/lib/$os/$arch"
    
    log_info "组织输出文件..."
    
    # 确保目录存在
    mkdir -p "$lib_dir"
    
    # 查找并移动库文件
    if [ -d "$lib_dir/lib" ]; then
        # 移动所有 .a 文件到 lib 目录
        find "$lib_dir/lib" -name "*.a" -exec mv {} "$lib_dir/" \;
        
        # 移动所有 .so 和 .dylib 文件到 lib 目录
        find "$lib_dir/lib" -name "*.so*" -o -name "*.dylib" | while read -r lib; do
            if [ -f "$lib" ]; then
                mv "$lib" "$lib_dir/"
            fi
        done
        
        # 清理空的 lib 目录
        if [ -d "$lib_dir/lib" ] && [ -z "$(ls -A "$lib_dir/lib")" ]; then
            rmdir "$lib_dir/lib"
            log_info "已清理空的 lib 目录"
        fi
    fi
    
    # 查找并移动头文件
    if [ -d "$lib_dir/include" ]; then
        log_info "头文件位置: $lib_dir/include/"
    fi
    
    # 查找并移动 CMake 配置文件
    if [ -d "$lib_dir/lib/cmake" ]; then
        if [ -d "$lib_dir/cmake" ]; then
            # 如果目标目录已存在，先删除它
            rm -rf "$lib_dir/cmake"
        fi
        mv "$lib_dir/lib/cmake" "$lib_dir/"
        log_info "CMake 配置文件位置: $lib_dir/cmake/"
    fi

    # 搬走 .a 后，lib/ 下常残留各子模块 cmake（路径已失效）；整棵删掉避免下次 find_package 踩坑
    if [ -d "$lib_dir/lib" ]; then
        rm -rf "$lib_dir/lib"
        log_info "已清理 install 残留 lib/（含失效 cmake 配置）"
    fi
    
    log_success "输出文件组织完成"
}

# 显示构建信息
show_build_info() {
    local os=$1
    local arch=$2
    local lib_dir="$AWS_S3_ROOT/lib/$os/$arch"
    
    log_success "构建完成！"
    echo ""
    log_info "构建信息:"
    log_info "  平台: $os $arch"
    log_info "  产物目录: $lib_dir"
    log_info "  库文件:"
    
    if [ -d "$lib_dir" ]; then
        find "$lib_dir" -name "*.a" -o -name "*.so*" -o -name "*.dylib" | while read -r lib; do
            local filename=$(basename "$lib")
            local size=$(ls -lh "$lib" | awk '{print $5}')
            log_info "    $filename ($size)"
        done
    fi
    
    log_info "  头文件: $lib_dir/include/"
    log_info "  CMake 配置: $lib_dir/cmake/"
    echo ""
    log_info "使用方法:"
    log_info "  在 CMakeLists.txt 中添加:"
    log_info "    find_package(aws-cpp-sdk-s3 REQUIRED)"
    log_info "    target_link_libraries(your_target aws-cpp-sdk-s3)"
}

# 清理构建目录
clean_build() {
    log_info "清理构建目录..."
    
    if [ -d "$AWS_S3_ROOT/build" ]; then
        rm -rf "$AWS_S3_ROOT/build"
        log_success "已清理构建目录"
    else
        log_info "构建目录不存在，无需清理"
    fi
}

# 主函数
main() {
    # 设置错误时清理
    trap cleanup EXIT
    
    # 解析命令行参数
    if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
        show_usage
        exit 0
    elif [ "$1" = "--clean" ]; then
        clean_build
        exit 0
    elif [ "$1" = "--build" ] || [ "$1" = "-b" ]; then
        # 执行构建
        log_info "开始构建 AWS S3 SDK..."
        
        # 检测平台
        local platform_info=$(detect_platform)
        read -r os arch <<< "$platform_info"
        log_info "检测到平台: $os $arch"
        
        # 检查依赖
        check_deps
        
        # 检查第三方库
        check_third_party_libs "$os" "$arch"
        
        # 构建 AWS S3 SDK
        build_aws_s3 "$os" "$arch"
        
        # 组织输出文件
        organize_output "$os" "$arch"
        
        # 显示构建信息
        show_build_info "$os" "$arch"
    else
        # 默认显示帮助信息
        show_usage
        exit 0
    fi
}

# 清理函数
cleanup() {
    if [ $? -ne 0 ]; then
        log_error "构建过程中发生错误，正在清理..."
    fi
}

# 运行主函数
main "$@"