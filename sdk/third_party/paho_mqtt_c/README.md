# paho_mqtt_c（预编译）

| | |
|--|--|
| 上游 | https://github.com/eclipse/paho.mqtt.c |
| 版本 | **1.3.14**（`libpaho-mqtt3as`） |
| 许可证 | EPL 2.0 或 EDL 1.0（双许可），见 [LICENSE](./LICENSE) |
| 类型 | 预编译动态库（仓内无 `src/` 源码树） |
| SDK 目标 | `cloud_api2::paho` |
| 平台 | `linux/x86_64` |

EPL 必须动态链接。业务 `#include <MQTTAsync.h>`。jar 打进 `libpaho-mqtt3as.so.1`，对仓内 OpenSSL 1.1.1。

源码以 tag `v1.3.14` 为准，不在本目录快照。

## 仓内布局

```text
include/
lib/linux/x86_64/libpaho-mqtt3as.so.1
bin/build_linux_x86_64.sh
bin/build_macosx_arm64.sh
sample/mqtt_demo/          # SSL 异步客户端，只依赖 include + lib
src/                       # gitignore
```

官方槽只留 `libpaho-mqtt3as`（SSL 异步）。

## 如何重编

```bash
cd third_party/paho_mqtt_c
git clone --branch v1.3.14 https://github.com/eclipse/paho.mqtt.c.git src
./bin/build_linux_x86_64.sh    # 或 ./bin/build_macosx_arm64.sh
```

只编 `paho-mqtt3as`。`src/` 见 `.gitignore`。

Linux 脚本默认 **不写 RUNPATH**（运行时由 SDK / `LD_LIBRARY_PATH` 找仓内 OpenSSL 1.1.1）；若本机有 `patchelf` 会再清一遍。

mac 脚本把 OpenSSL 依赖改成 `@rpath/libssl.3.dylib` / `@rpath/libcrypto.3.dylib`（不写 Homebrew 绝对路径）。jar 打包还会再改成 `@loader_path` 并拷同目录的 OpenSSL。

## 如何替换

换 `include/` 与 `lib/linux/x86_64/libpaho-mqtt3as.so.1`，保持 `cloud_api2::paho`。
