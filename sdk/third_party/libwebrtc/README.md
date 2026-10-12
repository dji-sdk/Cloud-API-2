# libwebrtc（预编译）

| | |
|--|--|
| 上游 | https://webrtc.googlesource.com/src |
| 版本 | **branch-heads/6477**（约 Chromium M114） |
| 许可证 | BSD 3-clause，见 [LICENSE](./LICENSE)；`include/third_party/abseil-cpp` 为 Apache-2.0 |
| 类型 | 预编译静态 + 仓内头文件切片 `include/` |
| SDK 目标 | `cloud_api2::libwebrtc` |
| 平台 | `linux/x86_64` |

ME 直播走 WebRTC。链接 `libwebrtc.a`，TLS 走外部 OpenSSL（`openssl_1_1_1`），不是自带 BoringSSL。jar 不另带 so。

头从 `include/`。ME 另外编进 4 个源文件：`rtc_base/log_sinks.cc`、`file_rotating_stream.cc`、`strings/string_builder.cc`、`system/file_wrapper.cc`。

`include/` 是 **ME + sample + 上述 4 个 `.cc` 的编译闭包**（约 280 个 WebRTC 头 + 4 个 `.cc`，加上 Abseil 用到的头），不是完整 Chromium 树。`include/third_party/abseil-cpp` 同样按闭包裁过，只留编译用到的 `absl/**` 头和 `LICENSE` / `AUTHORS`（Apache-2.0）。测试、Android/iOS SDK、Chromium `base/`、Abseil 的 CI/测试树等已去掉。

## 仓内布局

```text
include/                         # 编译闭包头 + 上述 4 个 .cc；Abseil 同样是闭包
lib/linux/x86_64/libwebrtc.a
bin/setup_webrtc.sh
bin/build_webrtc_macos_arm64.sh
bin/build_webrtc_linux_x86_64.sh
bin/verify_no_boringssl.sh
sample/                          # 只依赖 include/ + lib/<本机>/
src/                             # gitignore：完整 WebRTC 树
```

## 如何重编

仓内 `include/` 不够用来 GN 重编。需要 depot_tools + 完整树，切到 `branch-heads/6477`。

```bash
cd third_party/libwebrtc
./bin/setup_webrtc.sh                 # 拉 depot_tools + src/（约十几～三十 GB）
./bin/build_webrtc_linux_x86_64.sh    # mac 用 build_webrtc_macos_arm64.sh
# 默认只产出 src/out/Release/obj/libwebrtc.a，不覆盖仓内 lib/
# verify 通过后再安装：
./bin/build_webrtc_linux_x86_64.sh --install
```

`setup` / 构建只写 `src/` 与 `depot_tools/`，**不覆盖** `include/`。  
**默认不替换**仓内 `lib/<os>/<arch>/libwebrtc.a`；加 `--install` 才拷贝。

`gclient sync` 若因网络中断失败，或重跑 setup 时因本地补丁导致脏树，在本目录重新执行 `./bin/setup_webrtc.sh`（脚本会 `gclient sync --reset`），或：

```bash
export PATH="$PWD/depot_tools:$PATH"
gclient sync --reset
```

关键 GN：`rtc_build_ssl = false`、`rtc_use_h264 = true`、`use_custom_libcxx = false`、`use_rtti = true`、`rtc_enable_grpc = false`、`libsrtp_build_boringssl = false`。

- mac：`target_os = "mac"` `target_cpu = "arm64"`；`rtc_ssl_root` = Homebrew `openssl@3`
- Linux：`target_os = "linux"` `target_cpu = "x64"`；`rtc_ssl_root` = `third_party/openssl_1_1_1/include`；用 gclient 自带 Clang

不要把完整 `src/` / `depot_tools/` 提交进仓；重编产物未经验证也不要覆盖提交官方槽。

## 如何替换

换 `lib/linux/x86_64/libwebrtc.a`。若 API 变了，从 `src/` 按 ME + sample + 那 4 个 `.cc` 重算 include 闭包再同步头（不要整树拷回 `include/`），保持 `cloud_api2::libwebrtc`。Abseil 也按同一闭包同步，并保留 `LICENSE` / `AUTHORS`。
