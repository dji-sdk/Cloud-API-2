# MQTT SSL Demo

用仓内 `libpaho-mqtt3as` 做 SSL 异步客户端的小例子：验库、学用法。

## 构建

```bash
cd third_party/paho_mqtt_c/sample/mqtt_demo
./build_demo.sh
# 或
mkdir -p build && cd build && cmake .. && cmake --build .
```

CMake 会相对路径找到：

- `third_party/paho_mqtt_c/{include,lib/<os>/<arch>}`
- Linux：仓内 `third_party/openssl_1_1_1`（与 so 的 `NEEDED: libssl.so.1.1` 一致）
- macOS arm64：Homebrew OpenSSL 3 + `macosx/arm64` 的 dylib

不要依赖系统 OpenSSL 3 来链 Linux 产物，也不要写死本机绝对路径。

## 运行

Broker 地址由调用方提供，不要把内网地址写进源码。

```bash
./build/mqtt_demo tcp://127.0.0.1:1883
# 或
MQTT_SERVER_URI=tcp://127.0.0.1:1883 ./build/mqtt_demo
```

只想确认能编过，构建成功即可。

## 依赖

- `libpaho-mqtt3as`（EPL，动态）
- OpenSSL（Linux：仓内 1.1.1；mac：Homebrew openssl@3）
- pthread、dl
