#!/bin/bash

# OpenSSL 1.1.1 Build Script
# 构建 OpenSSL 1.1.1 静态库和动态库

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OPENSSL_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$OPENSSL_ROOT/build"
INSTALL_DIR="$OPENSSL_ROOT"
OPENSSL_SOURCE="$OPENSSL_ROOT/src"
THIRD_PARTY_ROOT="$(cd "$OPENSSL_ROOT/.." && pwd)"
# 路径卫生：编译期映射本机/CI 路径；openssldir 用中性前缀，再用 DESTDIR 安装
PATH_HYGIENE_FLAGS="-ffile-prefix-map=${THIRD_PARTY_ROOT}=/build/vendor/third_party -ffile-prefix-map=${HOME}=/build/vendor/home -ffile-prefix-map=/home/jenkins=/build/vendor"
OPENSSL_NEUTRAL_PREFIX="/build/vendor/openssl_1_1_1"

# OpenSSL 1.1.1 版本信息
OPENSSL_VERSION="1.1.1f"
OPENSSL_TAG="OpenSSL_1_1_1f"

# Get CPU core count
# 资源门控：优先用上层 export 的 FHSDK_MAX_JOBS（scripts/build_java.sh 统一定版）；
# 单独跑本脚本时用"核数但封顶 8"，避免在 20 核机器上 -j20 打满 CPU 把 SSH 挤断。详见 scripts/README.md。
PARALLEL_JOBS="${FHSDK_MAX_JOBS:-$(n=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4); [ "$n" -gt 8 ] && echo 8 || echo "$n")}"

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to detect current platform
detect_platform() {
    local os=$(uname -s | tr '[:upper:]' '[:lower:]')
    local arch=$(uname -m)
    if [[ "$os" == "linux" && "$arch" == "x86_64" ]]; then
        echo "linux/x86_64"
    else
        echo "unsupported"
    fi
}

# Function to show usage
show_usage() {
    echo "OpenSSL 1.1.1 Build Script"
    echo "=========================="
    echo ""
echo "This script builds OpenSSL 1.1.1 for Linux x86_64 (JNI ABI)."
echo "macOS uses Homebrew openssl@3 and does not write into this directory."
    echo ""
    echo "Usage: $0 [--build] [--clean]"
    echo ""
    echo "Options:"
    echo "  --build    Build OpenSSL 1.1.1 (required to start compilation)"
    echo "  --clean    Clean build directory before building"
    echo ""
    echo "Examples:"
    echo "  $0                    Show this help message"
    echo "  $0 --build           Build OpenSSL 1.1.1 for current platform"
    echo "  $0 --build --clean   Clean and build OpenSSL 1.1.1"
    echo ""
    echo "The script will:"
    echo "  - Download OpenSSL 1.1.1 source code if not present"
    echo "  - Detect the current platform automatically"
    echo "  - Use all available CPU cores for parallel compilation"
    echo "  - Install headers to: $INSTALL_DIR/include/"
    echo "  - Install libraries to: $INSTALL_DIR/lib/{platform}/"
    echo ""
echo "Supported platforms:"
echo "  - linux/x86_64"
    echo ""
    echo "Current platform: $(detect_platform)"
    echo "CPU cores: $PARALLEL_JOBS"
    echo "OpenSSL version: $OPENSSL_VERSION"
    echo ""
}

# Function to download OpenSSL 1.1.1 source
download_openssl() {
    if [[ -d "$OPENSSL_SOURCE" ]]; then
        print_info "OpenSSL source already exists at: $OPENSSL_SOURCE"
        return 0
    fi
    
    print_info "Downloading OpenSSL $OPENSSL_VERSION source code..."
    
    # Create source directory
    mkdir -p "$OPENSSL_SOURCE"
    cd "$OPENSSL_SOURCE"
    
    # Download and extract OpenSSL 1.1.1
    local openssl_url="https://github.com/openssl/openssl/archive/refs/tags/$OPENSSL_TAG.tar.gz"
    local openssl_tarball="$OPENSSL_TAG.tar.gz"
    
    print_info "Downloading from: $openssl_url"
    if command -v wget >/dev/null 2>&1; then
        wget -O "$openssl_tarball" "$openssl_url"
    elif command -v curl >/dev/null 2>&1; then
        curl -L -o "$openssl_tarball" "$openssl_url"
    else
        print_error "Neither wget nor curl found. Please install one of them."
        exit 1
    fi
    
    print_info "Extracting OpenSSL source..."
    tar -xzf "$openssl_tarball"
    
    # Move contents to the correct location
    local extracted_dir="openssl-$OPENSSL_TAG"
    if [[ -d "$extracted_dir" ]]; then
        mv "$extracted_dir"/* .
        rmdir "$extracted_dir"
    fi
    
    # Clean up tarball
    rm -f "$openssl_tarball"
    
    print_success "OpenSSL $OPENSSL_VERSION source downloaded successfully"
}

# Function to setup platform-specific configurations
setup_platform_config() {
    local platform=$1
    
    case $platform in
        "linux/x86_64")
            CONFIG_OPTS="linux-x86_64"
            ;;
        *)
            print_error "Unsupported platform: $platform"
            print_error "This directory only ships linux/x86_64. macOS: brew install openssl@3"
            exit 1
            ;;
    esac
}

# Function to build OpenSSL 1.1.1
build_openssl() {
    local platform=$1
    local build_dir="$BUILD_DIR"
    local install_dir="$INSTALL_DIR"
    
    print_info "Building OpenSSL $OPENSSL_VERSION for $platform..."
    
    # Check if OpenSSL source exists
    if [[ ! -d "$OPENSSL_SOURCE" ]]; then
        print_error "OpenSSL source not found at: $OPENSSL_SOURCE"
        print_error "Please run with --build to download the source first."
        exit 1
    fi
    
    # Create build directory
    mkdir -p "$build_dir"
    cd "$build_dir"
    
    # Copy source to build directory
    if [[ ! -d "openssl" ]]; then
        cp -r "$OPENSSL_SOURCE" "openssl"
    fi
    
    cd "openssl"
    
    # Configure OpenSSL 1.1.1
    # 使用中性 --prefix/--openssldir，避免 ct_log_list.cnf 等串写入真实本机/CI 路径；
    # 再用 DESTDIR 装到仓库目录。CFLAGS 带 -ffile-prefix-map。
    local stage_dir="$build_dir/destdir"
    rm -rf "$stage_dir"
    mkdir -p "$stage_dir"

    print_info "Configuring OpenSSL $OPENSSL_VERSION with: $CONFIG_OPTS"
    print_info "Neutral prefix/openssldir: $OPENSSL_NEUTRAL_PREFIX"
    print_info "Building both static and shared libraries..."
    ./Configure $CONFIG_OPTS \
        --prefix="$OPENSSL_NEUTRAL_PREFIX" \
        --openssldir="$OPENSSL_NEUTRAL_PREFIX" \
        no-asm \
        enable-ec_nistp_64_gcc_128 \
        -DOPENSSL_USE_LEGACY_PROVIDER=1 \
        $PATH_HYGIENE_FLAGS
    
    # Build OpenSSL
    print_info "Building OpenSSL with $PARALLEL_JOBS parallel jobs..."
    nice -n "${FHSDK_NICE:-15}" make -j"$PARALLEL_JOBS"
    
    # Install into DESTDIR then promote into OPENSSL_ROOT layout
    print_info "Installing OpenSSL (DESTDIR=$stage_dir)..."
    make install_sw DESTDIR="$stage_dir"

    local staged_root="$stage_dir$OPENSSL_NEUTRAL_PREFIX"
    if [[ ! -d "$staged_root" ]]; then
        print_error "Staged install missing: $staged_root"
        exit 1
    fi

    mkdir -p "$install_dir/include" "$install_dir/lib"
    # headers
    if [[ -d "$staged_root/include" ]]; then
        rm -rf "$install_dir/include"
        cp -a "$staged_root/include" "$install_dir/"
    fi
    # libs → temporary lib/ root; organize_output moves to lib/$platform
    find "$staged_root" \( -name '*.a' -o -name 'libssl.so*' -o -name 'libcrypto.so*' -o -name 'libssl*.dylib' -o -name 'libcrypto*.dylib' \) \( -type f -o -type l \) | while read -r lib; do
        cp -a "$lib" "$install_dir/lib/"
        print_info "Staged lib → $install_dir/lib/$(basename "$lib")"
    done
    
    print_success "OpenSSL $OPENSSL_VERSION build completed for $platform"
}

# Function to organize output files
organize_output() {
    local platform=$1
    local install_dir="$INSTALL_DIR"
    local platform_lib_dir="$install_dir/lib/$platform"
    
    print_info "Organizing output files for $platform..."
    
    # Create platform-specific lib directory
    mkdir -p "$platform_lib_dir"
    
    # Move libraries from lib directory to platform-specific directory.
    # 条件必须整体括起：否则 -maxdepth 1 只绑定第一个 -name，后续分支会递归到
    # lib/$platform/ 里。符号链接（libssl.so -> libssl.so.1.1）在其目标先被移走后
    # 会变成悬空链接，用 -f 判断会漏掉它，留在 lib/ 根污染仓库。
    if [[ -d "$install_dir/lib" ]]; then
        find "$install_dir/lib" -maxdepth 1 \
            \( -name "*.a" -o -name "*.so*" -o -name "*.dylib" -o -name "*.dll" \) \
            \( -type f -o -type l \) -print0 | while IFS= read -r -d '' lib; do
            mv -f "$lib" "$platform_lib_dir/"
            print_info "Moved $(basename "$lib") to $platform_lib_dir/"
        done
    fi
    
    # Move libraries from lib64 directory to platform-specific directory (for Linux systems)
    if [[ -d "$install_dir/lib64" ]]; then
        find "$install_dir/lib64" -maxdepth 1 \
            \( -name "*.a" -o -name "*.so*" -o -name "*.dylib" -o -name "*.dll" \) \
            \( -type f -o -type l \) -print0 | while IFS= read -r -d '' lib; do
            mv -f "$lib" "$platform_lib_dir/"
            print_info "Moved $(basename "$lib") from lib64 to $platform_lib_dir/"
        done
        # Remove empty lib64 directory if it exists
        if [[ -d "$install_dir/lib64" ]] && [[ -z "$(ls -A "$install_dir/lib64")" ]]; then
            rmdir "$install_dir/lib64"
            print_info "Removed empty lib64 directory"
        fi
    fi
    
    # Keep include directory at root level (platform-independent)
    if [[ -d "$install_dir/include" ]]; then
        print_info "Headers are available in $install_dir/include/"
    fi
    
    print_success "Output organized for $platform"
}

# Function to clean build directory
clean_build() {
    print_info "Cleaning build directory..."
    if [[ -d "$BUILD_DIR" ]]; then
        rm -rf "$BUILD_DIR"
        print_success "Build directory cleaned"
    else
        print_info "Build directory does not exist, nothing to clean"
    fi
}

# Main function
main() {
    local clean_build_flag=false
    local build_flag=false
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --build)
                build_flag=true
                shift
                ;;
            --clean)
                clean_build_flag=true
                shift
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Show usage if no build flag
    if [[ "$build_flag" != true ]]; then
        show_usage
        exit 0
    fi
    
    # Clean if requested
    if [[ "$clean_build_flag" == true ]]; then
        clean_build
    fi
    
    # Detect platform
    local platform=$(detect_platform)
    
    # Validate platform
    if [[ "$platform" != "linux/x86_64" ]]; then
        print_error "Could not detect linux/x86_64 (got: $platform). macOS uses Homebrew openssl@3."
        exit 1
    fi
    
    # Setup platform configuration
    setup_platform_config "$platform"
    
    # Download OpenSSL source if needed
    download_openssl
    
    # Create necessary directories
    mkdir -p "$BUILD_DIR"
    mkdir -p "$INSTALL_DIR/lib"
    mkdir -p "$INSTALL_DIR/include"
    
    # Build OpenSSL
    build_openssl "$platform"
    
    # Organize output files
    organize_output "$platform"
    
    print_success "OpenSSL $OPENSSL_VERSION build completed successfully!"
    print_info "Headers: $INSTALL_DIR/include/"
    print_info "Libraries: $INSTALL_DIR/lib/$platform/"
    print_info "Ready for AWS S3 SDK integration"
}

# Run main function
main "$@"
