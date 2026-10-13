# Cloud API 2 SDK

上云 API 2 的客户端 SDK（原 Flight Hub SDK）。在 PaaS 后端之上封装 HTTP、WebSocket 通知、直播拉流与 DRC 指令飞行，交付面为 **Java + C++**（Java 是 SWIG/JNI 绑定，C++ 是核心实现）。

## 目录

- [怎么编](#怎么编)
- [更新日志](CHANGELOG.md)
- [许可](#许可)
- [官方支持平台](#官方支持平台)

## 怎么编

入口脚本在 `scripts/` 下跑（`ROOT_DIR=..`）：

```bash
cd scripts
./build_java.sh
```

Java 侧说明见 [`platform/java/README.md`](platform/java/README.md)。Demo 配置模板是占位符（`xx.xx.xx.xx`）；本地填真实环境后不要提交。

## 许可

- 自研代码：Apache License 2.0，见 [`LICENSE`](LICENSE)。
- 三方组件与闭源预编译库（`libdrtc` / `libdrpc`）：见 [`NOTICE`](NOTICE) 与 [`platform/java/THIRD_PARTY_LICENSES/`](platform/java/THIRD_PARTY_LICENSES/)。

## 官方支持平台

- Linux x86_64

特别说明, macOS 未全面支持, 不作为官方支持平台。另demo代码仅用于本地测试，不允许直接用于生产环境。
