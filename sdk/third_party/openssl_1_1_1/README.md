# OpenSSL 1.1.1（预编译，Linux）

| | |
|--|--|
| 上游 | https://www.openssl.org / https://github.com/openssl/openssl |
| 版本 | **1.1.1f** |
| 许可证 | OpenSSL / SSLeay dual，见 [LICENSE](./LICENSE) |
| 类型 | 预编译动态库（仓内无 `src/` 源码树） |
| SDK 目标 | `cloud_api2::openssl`（**仅 Linux**） |
| 平台 | `linux/x86_64`：`libssl.so.1.1`、`libcrypto.so.1.1` |

mac 不走本目录（macOS 不作为官方支持平台）：本机开发时 CMake 用 Homebrew OpenSSL 3，`bin/build.sh` 把 `libssl.3.dylib` / `libcrypto.3.dylib` 打进 jar 的 `lib/macosx-arm64/`，`JniLibraryLoader` 先加载这两份。无 Homebrew 的机器也能跑。

Linux 上 FH / paho / lws / curl / aws / webrtc 共用本目录 ABI。jar 打进 `lib/linux-x86_64/` 的是 `.so.1.1`。

## 仓内布局

```text
include/openssl/
lib/linux/x86_64/libssl.so.1.1
lib/linux/x86_64/libcrypto.so.1.1
lib/linux/x86_64/libssl.so          # → so.1.1
lib/linux/x86_64/libcrypto.so
lib/linux/x86_64/libssl.a           # curl 等重编时用
lib/linux/x86_64/libcrypto.a
bin/build_openssl_1_1_1.sh
src/                                # gitignore
```

## 如何重编

只编 Linux x86_64。mac 本机开发请用 `brew install openssl@3`，不要往本目录写 dylib。

```bash
cd third_party/openssl_1_1_1
./bin/build_openssl_1_1_1.sh --build
```

脚本会 clone `OpenSSL_1_1_1f` 到本目录 `src/`（已 gitignore），产物写回 `lib/linux/x86_64/`。
