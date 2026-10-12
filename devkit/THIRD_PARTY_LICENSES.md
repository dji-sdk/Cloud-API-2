# THIRD_PARTY_LICENSES — 三方组件与再分发声明

本文件记录随本仓库分发的三方组件及其来源、许可证状态，供接受方核对各厂商的再分发授权。

> **状态说明（2026-10-10，SDK 3.3.7）**：SDK 3.1.5+ 已移除 Agora / VolcEngine / ByteRTC
> 等商业组件（CL-02 解除）。以下列表反映 `libs/flight-hub-sdk-3.3.7.jar` 的实际内容，
> 与 JAR 内 `THIRD_PARTY_LICENSES/README.md` 保持一致。

## 一、随 libs/flight-hub-sdk-3.3.7.jar 分发的组件

以下 native 库内嵌于私有 SDK JAR，随 JAR 一并分发。

### 开源组件

| 组件 | 版本 | 许可证 | 说明 |
|------|------|--------|------|
| OpenSSL | 1.1.1n (Linux) / 3.x (macOS) | OpenSSL License + SSLeay License (1.1.x) / Apache 2.0 (3.x) | TLS 能力 |
| libwebsockets | v2.4.2 (SONAME 12) | LGPL-2.1（warmcat 静态链接豁免条款） | WebSocket 底层 |
| paho-mqtt-c | 1.3.14 | EPL 2.0 / EDL 1.0 (dual) | MQTT 客户端 |
| plog | — | MIT | Header-only 日志库 |
| AIGCJson | — | MIT-style | Header-only JSON mapper |
| aws-cpp-sdk-s3 | 1.11.642 | Apache 2.0 | S3 对象存储客户端 |
| libcurl | 8.15.0-DEV | curl license（MIT/X 衍生） | HTTP 客户端 |
| libwebrtc | branch-heads/6477 | BSD 3-clause | WebRTC 底层（新增于 3.3.5） |

### DJI 专有组件

| 组件 | 说明 |
|------|------|
| `libflighthubsdk_jni.*` | SDK 官方 JNI 桥接层 |
| `libdrtc_device_sdk` (Linux) | DJI DRTC device SDK |
| `libdrpc` (Linux) | DJI DRTC RPC |

> DJI 专有组件随 SDK 授权使用，再分发无需第三方授权。

### 已移除组件（SDK 3.1.5+ 不再包含）

以下组件曾存在于 SDK 3.1.4 及更早版本，自 3.1.5 起已全部移除：

- Agora RTC SDK (`libagora_rtc_sdk.so`)
- VolcEngine RTC / ByteRTC (`libVolcEngineRTC.*`, `libbytertc_*`)
- ByteEffect (`libeffect.*`)
- ByteNN (`libbytenn`)
- ByteVC0 (`libbytevc0`)
- FDK-AAC (`libagora-fdkaac.so`, `libbytertc_fdk-aac_extension.*`)

## 二、随 skeleton/ 分发的第三方运行时

| 组件 | 来源 | 许可证 | 说明 |
|------|------|--------|------|
| Wibu CodeMeter Runtime (`codemeter-lite_*.deb`) | Wibu Systems 官方发布包 | 专有 EULA | 仅私有化构建使用，见 `skeleton/packages/x86/NOTICE` |

> **注意**：CodeMeter .deb 未随 git 仓库分发（已加入 `.gitignore`），ISV 需从 Wibu 官方或 DJI 安装包单独获取。

## 三、已知开源许可的依赖

构建产物（MCP / skeleton BFF）依赖的标准开源组件（Spring Boot、Project Reactor、
Checkstyle、JaCoCo 等）在各自 POM 声明中遵循原始许可证，不在此赘述。

---

## 待办（发布前必须完成）

- [x] 向 Agora / 字节跳动（VolcEngine、ByteEffect）逐项取得再分发授权 — **不再需要（SDK 3.1.5+ 已移除）**
- [x] 确认 FDK-AAC 扩展的再分发条款 — **不再需要（SDK 3.1.5+ 已移除）**
- [x] 确认 libwebsockets MIT 许可证合规 — **2026-09-28 已更正**：实际为 v2.4.2（SONAME 12），许可证为 **LGPL-2.1**（含 warmcat 静态链接豁免条款），并非 MIT。SDK 3.3.5 vendor notice 纠正了此前 3.1.6 notice 的错误标注；已核对二进制文件名/SONAME 在 3.1.6→3.3.5 之间未变化，判定该库实际从未变更，此前"MIT"标注系 vendor 文档错误，非许可证变更。本表已按 LGPL-2.1 记录。
- [ ] 核实完成后，将本文件标记为最终版

## 更新记录

- 2026-08-21：初始骨架创建（CL-02 开放项），商业组件再分发授权待业务/法务核实。
- 2026-08-31：按 SDK 3.1.6 JAR 内实际内容重写。移除已从 SDK 删除的 Agora/VolcEngine/ByteRTC 商业组件，更新为 OpenSSL/libwebsockets/paho-mqtt 等实际包含的开源组件。
- 2026-09-28：按 SDK 3.3.5 JAR 内实际内容更新。新增 libwebrtc（BSD 3-clause）静态链接组件，补全 aws-cpp-sdk-s3（1.11.642）/libcurl（8.15.0-DEV）版本号；**更正 libwebsockets 许可证标注：MIT → LGPL-2.1（+ warmcat 静态链接豁免条款），版本 v4.x → v2.4.2（SONAME 12 未变，判定为此前 vendor notice 标注错误，非本次升级引入的变化）**。
