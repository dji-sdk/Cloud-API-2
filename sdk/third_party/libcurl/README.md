# libcurl（预编译）

| | |
|--|--|
| 上游 | https://github.com/curl/curl |
| 版本 | 头文件 **8.15.0-DEV**（git 快照，不是打 tag 的 `curl-8_15_0`） |
| 许可证 | curl license（MIT/X 衍生），见 [COPYING](./COPYING) |
| 类型 | 预编译静态（仓内无 `src/` 源码树） |
| SDK 目标 | `cloud_api2::libcurl` |
| 平台 | `linux/x86_64` |

`#include "curl.h"`（头在 `include/`）。AWS 重编时自行包成 `<curl/curl.h>`。

链接 `libcurl.a`，TLS 走 OpenSSL `libssl.so.1.1`。jar 不另带 `libcurl.so`。

## 仓内布局

```text
include/*.h
lib/linux/x86_64/libcurl.a
bin/build_libcurl.sh
src/          # gitignore，clone 工作区
```

各平台头内容相同，只留一份 `include/`。

## 如何重编

```bash
cd third_party/libcurl
git clone --branch curl-8_15_0 https://github.com/curl/curl.git src
./bin/build_libcurl.sh --build
```

产物写回 `include/` 与 `lib/<os>/<arch>/libcurl.a`。

依赖：mac 用 Homebrew `openssl@3`；Linux 用 `third_party/openssl_1_1_1`。`src/` 见 `.gitignore`。

## 如何替换

换 `include/` 与 `lib/linux/x86_64/libcurl.a`，保持 `cloud_api2::libcurl`。
