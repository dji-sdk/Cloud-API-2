#!/bin/bash
# 编当前机的 libcurl 静态库，写回 lib/<os>/<arch>/libcurl.a 与 include/。
# 不 clone。源码放在 third_party/libcurl/src/ 。
# mac: Homebrew openssl@3；Linux: 仓内 third_party/openssl_1_1_1。

set -euo pipefail

GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CURL_ROOT="$(dirname "$SCRIPT_DIR")"
PROJECT_ROOT="$(dirname "$(dirname "$CURL_ROOT")")"
THIRD_PARTY_ROOT="$PROJECT_ROOT/third_party"
SRC_DIR="$CURL_ROOT/src"
# 资源门控：优先用上层 export 的 FHSDK_MAX_JOBS（scripts/build_java.sh 统一定版）；
# 单独跑本脚本时用"核数但封顶 8"，避免在 20 核机器上 -j20 打满 CPU 把 SSH 挤断。详见 scripts/README.md。
JOBS="${FHSDK_MAX_JOBS:-$(n=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4); [ "$n" -gt 8 ] && echo 8 || echo "$n")}"
PATH_HYGIENE_FLAGS="-ffile-prefix-map=${THIRD_PARTY_ROOT}=/build/vendor/third_party -ffile-prefix-map=${HOME}=/build/vendor/home -ffile-prefix-map=/home/jenkins=/build/vendor"

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_ok() { echo -e "${GREEN}[OK]${NC} $1"; }
log_err() { echo -e "${RED}[ERROR]${NC} $1"; }

show_usage() {
    cat <<EOF
用法: $0 [--build|-b] [--clean] [--help|-h]

先 clone 源码到 ${CURL_ROOT}/src ，再 --build。
产物：include/*.h 与 lib/<os>/<arch>/libcurl.a（只静态，不进仓 .so/.dylib）。
EOF
}

detect_platform() {
    local os arch
    os="$(uname -s | tr '[:upper:]' '[:lower:]')"
    arch="$(uname -m)"
    case "$os" in
        darwin)
            case "$arch" in
                arm64|aarch64) echo "macosx arm64" ;;
                x86_64) echo "macosx x86_64" ;;
                *) log_err "不支持的 macOS 架构: $arch"; exit 1 ;;
            esac
            ;;
        linux)
            case "$arch" in
                x86_64) echo "linux x86_64" ;;
                aarch64|arm64) echo "linux aarch64" ;;
                *) log_err "不支持的 Linux 架构: $arch"; exit 1 ;;
            esac
            ;;
        *) log_err "不支持的 OS: $os"; exit 1 ;;
    esac
}

setup_openssl() {
    local os="$1" arch="$2"
    if [ "$os" = "macosx" ]; then
        OPENSSL_PREFIX="/opt/homebrew/opt/openssl@3"
        if [ ! -d "$OPENSSL_PREFIX/include" ] || [ ! -f "$OPENSSL_PREFIX/lib/libssl.dylib" ]; then
            log_err "未找到 Homebrew openssl@3: $OPENSSL_PREFIX（brew install openssl@3）"
            exit 1
        fi
        log_info "OpenSSL: Homebrew $OPENSSL_PREFIX"
        return
    fi

    local ossl_root="$THIRD_PARTY_ROOT/openssl_1_1_1"
    local ossl_lib="$ossl_root/lib/${os}/${arch}"
    if [ ! -d "$ossl_root/include/openssl" ] || [ ! -f "$ossl_lib/libssl.so.1.1" ]; then
        log_err "未找到仓内 openssl_1_1_1: $ossl_lib"
        exit 1
    fi
    OPENSSL_PREFIX="$CURL_ROOT/build/openssl_prefix"
    rm -rf "$OPENSSL_PREFIX"
    mkdir -p "$OPENSSL_PREFIX/lib"
    ln -sfn "$ossl_root/include" "$OPENSSL_PREFIX/include"
    ln -sfn "$ossl_lib/libssl.so.1.1" "$OPENSSL_PREFIX/lib/libssl.so.1.1"
    ln -sfn "$ossl_lib/libcrypto.so.1.1" "$OPENSSL_PREFIX/lib/libcrypto.so.1.1"
    ln -sfn libssl.so.1.1 "$OPENSSL_PREFIX/lib/libssl.so"
    ln -sfn libcrypto.so.1.1 "$OPENSSL_PREFIX/lib/libcrypto.so"
    if [ -f "$ossl_lib/libssl.a" ]; then
        ln -sfn "$ossl_lib/libssl.a" "$OPENSSL_PREFIX/lib/libssl.a"
        ln -sfn "$ossl_lib/libcrypto.a" "$OPENSSL_PREFIX/lib/libcrypto.a"
    fi
    log_info "OpenSSL: wrap $ossl_lib -> $OPENSSL_PREFIX"
}

do_clean() {
    if [ -d "$SRC_DIR" ]; then
        ( cd "$SRC_DIR" && make distclean >/dev/null 2>&1 || make clean >/dev/null 2>&1 || true )
    fi
    rm -rf "$CURL_ROOT/build"
    log_ok "已清理 curl 构建残留"
}

do_build() {
    local os arch
    read -r os arch <<<"$(detect_platform)"
    log_info "平台: $os/$arch"

    if [ ! -x "$SRC_DIR/configure" ] && [ ! -x "$SRC_DIR/buildconf" ]; then
        log_err "没有源码: $SRC_DIR"
        log_info "git clone --branch curl-8_15_0 https://github.com/curl/curl.git $SRC_DIR"
        exit 1
    fi

    setup_openssl "$os" "$arch"

    cd "$SRC_DIR"
    if [ ! -x ./configure ]; then
        ./buildconf
    fi
    make distclean >/dev/null 2>&1 || true

    # HTTP-only，与仓内 8.15.0-DEV 产物同一套开关；只编静态。
    ./configure \
        --prefix=/usr/local \
        --disable-shared \
        --enable-static \
        --disable-dependency-tracking \
        --disable-ares \
        --disable-cookies \
        --disable-dict \
        --disable-file \
        --disable-ftp \
        --disable-gopher \
        --disable-imap \
        --disable-ldap \
        --disable-ldaps \
        --disable-manual \
        --disable-netrc \
        --disable-ntlm \
        --disable-pop3 \
        --disable-proxy \
        --disable-rtsp \
        --disable-smb \
        --disable-smtp \
        --disable-sspi \
        --disable-telnet \
        --disable-tftp \
        --disable-unix-sockets \
        --disable-verbose \
        --disable-versioned-symbols \
        --enable-http \
        --enable-ipv6 \
        --with-openssl="${OPENSSL_PREFIX}" \
        --without-brotli \
        --without-libidn2 \
        --without-libpsl \
        --without-librtmp \
        --without-libssh2 \
        --without-nghttp2 \
        --without-zlib \
        --without-zstd \
        CFLAGS="-fPIC -O2 ${PATH_HYGIENE_FLAGS}" \
        CPPFLAGS="-I${OPENSSL_PREFIX}/include" \
        LDFLAGS="-L${OPENSSL_PREFIX}/lib"

    nice -n "${FHSDK_NICE:-15}" make -j"$JOBS"

    if [ ! -f lib/.libs/libcurl.a ]; then
        log_err "未产出 lib/.libs/libcurl.a"
        exit 1
    fi

    local inc_dir="$CURL_ROOT/include"
    local lib_dir="$CURL_ROOT/lib/${os}/${arch}"
    mkdir -p "$inc_dir" "$lib_dir"
    find include/curl -name '*.h' -type f -exec cp {} "$inc_dir/" \;
    cp lib/.libs/libcurl.a "$lib_dir/libcurl.a"
    log_ok "头 -> $inc_dir"
    log_ok ".a  -> $lib_dir/libcurl.a"
}

case "${1:-}" in
    --build|-b) do_build ;;
    --clean) do_clean ;;
    --help|-h|"") show_usage ;;
    *) log_err "未知参数: $1"; show_usage; exit 1 ;;
esac
