#!/bin/bash
# 验证 libwebrtc.a 中是否还有 BoringSSL 库的符号。
# IsBoringSsl 等是 WebRTC 自己的代码，不算失败。
# mac 的 nm 对 C 符号带下划线，用 _? 兼容。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=webrtc_common.sh
source "$SCRIPT_DIR/webrtc_common.sh"

LIBWEBRTC="${1:-}"
if [ -z "$LIBWEBRTC" ]; then
    if [ -f "$WEBRTC_SRC/out/Release/obj/libwebrtc.a" ]; then
        LIBWEBRTC="$WEBRTC_SRC/out/Release/obj/libwebrtc.a"
    else
        case "$(uname -s)" in
            Darwin) LIBWEBRTC="$LIBWEBRTC_ROOT/lib/macosx/arm64/libwebrtc.a" ;;
            Linux)  LIBWEBRTC="$LIBWEBRTC_ROOT/lib/linux/x86_64/libwebrtc.a" ;;
            *) echo "错误: 请传入 libwebrtc.a 路径"; exit 1 ;;
        esac
    fi
fi

echo "正在检查 $LIBWEBRTC 中的 BoringSSL 库符号..."
echo ""

if [ ! -f "$LIBWEBRTC" ]; then
    echo "错误: 找不到 $LIBWEBRTC"
    exit 1
fi

echo "1. 检查 BoringSSL 库特有的符号 (这些应该不存在):"
echo "----------------------------------------"

BORING_SYMBOLS=(
    "BORINGSSL_self_test"
    "boringssl_getrandom"
    "boringssl_self_test_sha256"
    "boringssl_self_test_sha512"
    "boringssl_self_test_hmac_sha256"
    "kBoringSSLRSASqrtTwo"
    "BORINGSSL_keccak"
)

FOUND_COUNT=0
for symbol in "${BORING_SYMBOLS[@]}"; do
    if nm "$LIBWEBRTC" 2>/dev/null | grep -Eq "T _?${symbol}$"; then
        echo "  找到 BoringSSL 符号: $symbol"
        FOUND_COUNT=$((FOUND_COUNT + 1))
    fi
done

if [ "$FOUND_COUNT" -eq 0 ]; then
    echo "  未找到 BoringSSL 库符号"
else
    echo "  找到 $FOUND_COUNT 个 BoringSSL 库符号"
fi

echo ""
echo "2. WebRTC 自己的 SSL 检测函数 (这些是正常的):"
echo "----------------------------------------"
# head 会提前关管道；在 pipefail 下会导致 nm 收到 SIGPIPE → 整脚本 exit 141
set +o pipefail
nm "$LIBWEBRTC" 2>/dev/null | grep "IsBoringSsl" | head -5 || true
echo "  (这些符号是 WebRTC 代码，不是 BoringSSL 库)"

echo ""
echo "3. 检查 OpenSSL 符号引用 (应该有未定义的外部引用):"
echo "----------------------------------------"
OPENSSL_SYMBOLS=$(nm "$LIBWEBRTC" 2>/dev/null | grep -E "U _?(SSL_|OPENSSL_|EVP_|X509_)" | wc -l | tr -d ' ')
echo "  找到 $OPENSSL_SYMBOLS 个 OpenSSL 外部引用符号"
if [ "$OPENSSL_SYMBOLS" -gt 0 ]; then
    echo "  正确引用外部 OpenSSL"
    nm "$LIBWEBRTC" 2>/dev/null | grep -E "U _?(SSL_|OPENSSL_|EVP_|X509_)" | head -10 || true
else
    echo "  未找到 OpenSSL 外部引用"
fi
set -o pipefail

echo ""
echo "4. 总结:"
echo "----------------------------------------"
if [ "$FOUND_COUNT" -eq 0 ] && [ "$OPENSSL_SYMBOLS" -gt 0 ]; then
    echo "  成功：使用外部 OpenSSL，不包含 BoringSSL 库"
    exit 0
fi
echo "  仍存在问题，需要进一步检查"
exit 1
