# 由各 webrtc 脚本 source。不要直接执行。

_LIBWEBRTC_BIN="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIBWEBRTC_ROOT="$(cd "$_LIBWEBRTC_BIN/.." && pwd)"
SDK_ROOT="$(cd "$LIBWEBRTC_ROOT/../.." && pwd)"
WEBRTC_SRC="${WEBRTC_SRC:-$LIBWEBRTC_ROOT/src}"
DEPO="${DEPO:-$LIBWEBRTC_ROOT/depot_tools}"
SDK_HEADERS="$LIBWEBRTC_ROOT/include"
WEBRTC_BRANCH="${WEBRTC_BRANCH:-branch-heads/6477}"

webrtc_apply_patches() {
    local CONFIG_H="$WEBRTC_SRC/modules/congestion_controller/goog_cc/loss_based_bwe_v2.h"
    local CONFIG_CC="$WEBRTC_SRC/modules/congestion_controller/goog_cc/loss_based_bwe_v2.cc"
    local LIBSRTP_GN="$WEBRTC_SRC/third_party/libsrtp/BUILD.gn"

    if [ ! -f "$CONFIG_H" ] || [ ! -f "$CONFIG_CC" ]; then
        echo "错误: 不是完整 WebRTC 树: $WEBRTC_SRC"
        exit 1
    fi

    if ! grep -q '^[[:space:]]*Config();' "$CONFIG_H"; then
        python3 - "$CONFIG_H" "$CONFIG_CC" <<'PY'
from pathlib import Path
import sys
header, source = Path(sys.argv[1]), Path(sys.argv[2])
h = header.read_text()
h = h.replace("  struct Config {\n", "  struct Config {\n    Config();\n", 1)
header.write_text(h)
s = source.read_text()
s = s.replace("namespace webrtc {\n", "namespace webrtc {\n\nLossBasedBweV2::Config::Config() = default;\n", 1)
source.write_text(s)
PY
        echo "已应用 LossBasedBweV2::Config 默认构造补丁"
    else
        echo "LossBasedBweV2::Config 补丁已存在"
    fi

    if [ -f "$LIBSRTP_GN" ] && ! grep -q 'libsrtp_build_boringssl' "$LIBSRTP_GN"; then
        python3 - "$LIBSRTP_GN" <<'PY'
from pathlib import Path
import sys
path = Path(sys.argv[1])
text = path.read_text()
text = text.replace(
    "  build_libsrtp_tests = false\n}",
    """  build_libsrtp_tests = false

  # Select bundled BoringSSL or external OpenSSL.
  libsrtp_build_boringssl = true
  libsrtp_ssl_root = ""
}""",
    1,
)
text = text.replace(
    """config("libsrtp_warnings") {
  cflags = []
  if (is_win) {
    cflags += [ "/wd4018" ]  # Signed/unsigned mismatch in comparison
  }
}

static_library("libsrtp") {""",
    """config("libsrtp_warnings") {
  cflags = []
  if (is_win) {
    cflags += [ "/wd4018" ]  # Signed/unsigned mismatch in comparison
  }
}

config("libsrtp_external_ssl") {
  assert(libsrtp_ssl_root != "",
         "libsrtp_ssl_root is required when BoringSSL is disabled.")
  include_dirs = [ libsrtp_ssl_root ]
}

static_library("libsrtp") {""",
    1,
)
text = text.replace(
    """  public_deps = [
    "//third_party/boringssl:boringssl",
  ]""",
    """  if (libsrtp_build_boringssl) {
    public_deps = [ "//third_party/boringssl:boringssl" ]
  } else {
    configs += [ ":libsrtp_external_ssl" ]
  }""",
    1,
)
path.write_text(text)
PY
        echo "已应用 libsrtp 外部 OpenSSL 补丁"
    else
        echo "libsrtp 外部 OpenSSL 补丁已存在（或无此文件）"
    fi
}

webrtc_install_lib() {
    local plat="$1"
    local src_a="$WEBRTC_SRC/out/Release/obj/libwebrtc.a"
    local dst="$LIBWEBRTC_ROOT/lib/${plat}/libwebrtc.a"
    if [ ! -f "$src_a" ]; then
        echo "错误: 没有编译产物 $src_a"
        exit 1
    fi
    mkdir -p "$(dirname "$dst")"
    cp "$src_a" "$dst"
    echo "已安装: $dst"
    echo "未改 include/。头有 API 变化时按 README 重算编译闭包再同步，不要整树拷。"
}
