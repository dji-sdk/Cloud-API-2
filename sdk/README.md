# Cloud API 2 SDK

上云 API 2 的客户端 SDK（原 Flight Hub SDK）。在 PaaS 后端之上封装 HTTP、WebSocket 通知、直播拉流与 DRC 指令飞行，交付面为 **Java + C++**（Java 是 SWIG/JNI 绑定，C++ 是核心实现）。

## 目录

- [怎么编](#怎么编)
- [更新日志](CHANGELOG.md)
- [许可](#许可)
- [官方支持平台](#官方支持平台)
- [数据上报说明](#数据上报埋点说明)

## 怎么编

入口脚本在 `scripts/` 下跑（`ROOT_DIR=..`）：

```bash
cd scripts
./build_java.sh -j=N  or bash build_java.sh -j=N, 比如 "bash build_java.sh -j=12"
```

Java 侧说明见 [`platform/java/README.md`](platform/java/README.md)。Demo 配置模板是占位符（`xx.xx.xx.xx`）；本地填真实环境后不要提交。

## 许可

- 自研代码：Apache License 2.0，见 [`LICENSE`](LICENSE)。
- 三方组件与闭源预编译库（`libdrtc` / `libdrpc`）：见 [`NOTICE`](NOTICE) 与 [`platform/java/THIRD_PARTY_LICENSES/`](platform/java/THIRD_PARTY_LICENSES/)。

## 官方支持平台

- Linux x86_64
- Linux aarch64（Kylin V10）

特别说明, macOS 未全面支持, 不作为官方支持平台。

## 数据上报（埋点）说明

SDK 在调用关键接口（起飞/降落/返航、DRC 指令、航线任务、直播等）完成后，会向**集成方自己配置的 PaaS 服务地址**（`FlightHubSdkConfig.address`，路径 `/gateway/pf-beacon/ingest/inner/v1/report`）异步上报一条事件结果，用于平台侧运行统计与质量分析：

- **上报内容**：事件标识（如 `take_off`、`flight_task_pause`）、Unix 秒级时间戳、调用结果码（SDK 错误码 / HTTP 状态码 / 服务端业务码 / 设备端码）。完整事件清单见 `src/bll/http_interface/beacon_event.h`。
- **不包含**：个人数据、媒体内容、凭证密钥。
- **行为特征**：旁路异步上报（2~3 秒超时、可丢弃），不阻塞、不影响业务调用；**不会发往 DJI 服务器**。
- **关闭方式**：SDK 侧无独立开关；不希望接收上报的平台，可在服务端网关屏蔽上述路径。
