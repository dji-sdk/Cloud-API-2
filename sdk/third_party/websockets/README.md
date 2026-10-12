# libwebsockets（预编译）

| | |
|--|--|
| 上游 | https://github.com/warmcat/libwebsockets |
| 版本 | **v2.4.2**（SONAME 12） |
| 许可证 | LGPL-2.1（warmcat 例外条款），见 [LICENSE](./LICENSE) |
| 类型 | 预编译动态库（仓内无 `src/` 源码树） |
| SDK 目标 | `cloud_api2::websockets` |
| 平台 | `linux/x86_64` |

v2.4.2 是 LGPL，本 SDK 动态链接。业务 `#include <libwebsockets.h>`。jar 打进 `libwebsockets.so.12`，对仓内 OpenSSL 1.1.1。

源码以 tag `v2.4.2` 为准，不在本目录快照。mac 重编时脚本会给 `private-libwebsockets.h` 加 `MSG_NOSIGNAL` 守卫。

v2.4.2 公共头只有 **`libwebsockets.h` + 构建生成的 `lws_config.h`**（单文件时代，没有现代 `include/libwebsockets/*.h` 分包）。Linux / mac 脚本默认 **Release**；排障可 `NCSDK_LWS_BUILD_TYPE=Debug`。产物去掉本机路径：`LWS_BUILD_HASH` 固定为 `v2.4.2`；Linux **不写 RUNPATH**（运行时由 SDK/`LD_LIBRARY_PATH` 找 OpenSSL）。mac：动态库 id 为 `@rpath/libwebsockets.12.dylib`，OpenSSL 依赖为 `@rpath/libssl.3.dylib` / `@rpath/libcrypto.3.dylib`（不写 Homebrew 绝对路径）；jar 打包再改成 `@loader_path`。

## 仓内布局

```text
include/libwebsockets.h
include/lws_config.h
lib/linux/x86_64/libwebsockets.so.12
bin/build_libwebsockets_linux_x86_64.sh
bin/build_libwebsockets_macos_arm64.sh
src/                       # gitignore
```

官方槽只留 SONAME 12 的动态库。`cloud_api2::websockets` 在 `cmake/modules/cloud_api2_websockets.cmake`。

## 如何重编

```bash
cd third_party/websockets
git clone --branch v2.4.2 https://github.com/warmcat/libwebsockets.git src
./bin/build_libwebsockets_linux_x86_64.sh    # 或 ./bin/build_libwebsockets_macos_arm64.sh
```

`src/` 见 `.gitignore`。脚本会重建 `include/`（只装上述两个头），避免残留其它版本的分包头。
