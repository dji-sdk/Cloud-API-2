# fhsdk-demo-server

Spring Boot + FHSDK Java BFF。

## 前置

- Java **17**
- **`fhsdk/flight-hub-sdk.jar`**：自行放置，见 [`fhsdk/README.md`](fhsdk/README.md)
- 系统已安装 **FFmpeg ≥ 8.0**（WHIP muxer 自 8.0 "Huffman" 2025-08 进入主线，须支持 `-f whip`，直播 WHIP 推流）
- **Oryx**（WHIP 收流 + WHEP 播放，见 `../docker/oryx/`，首次运行 `./init-oryx.sh`）

## 配置

编辑 `src/main/resources/application.yml`：填入平台 `fhsdk.address`、`port`、`notification-server` 与 `oryx.publish-secret`（或环境变量 `ORYX_PUBLISH_SECRET`）。

## 启动（推荐：容器化后端）

推荐使用 `../docker/demo-server/` 启动 linux/amd64 后端（适配 DRTC）：

```bash
# 1) 先起 Oryx（首次需 init）
cd ../docker/oryx && docker compose up -d && ./init-oryx.sh

# 2) 构建 fat jar
cd ../../fhsdk-demo-server && mvn -DskipTests package

# 3) 复制 jar + drtc_config 并启动容器
cd ../docker/demo-server && ./prepare.sh && docker compose up -d --build
```

服务地址（宿主机）：`http://127.0.0.1:18080`

## 启动（本机模式，可选）

```bash
# 1. Oryx（WHIP 收流 + WHEP 播放）
cd ../docker/oryx && docker compose up -d && ./init-oryx.sh

# 2. 后端
mvn spring-boot:run
```

服务地址：`http://127.0.0.1:8080`

## API

统一前缀 `/api/v1`，详见 `docs/0_整体方案.md` §7。

首次请求 `POST /session/init` 会加载 FHSDK 原生库并连接平台。`token` / `binding_uuid` 由 SDK 内置 `ConfigReader` 提供，无需额外配置文件。

## 飞机直播 Payload

选设备后 `listenLiveAvailableRange` 推送列表；前端 `POST /devices/payload` 确认后方能 `POST /live/start` / DRC。`demo.drone-payload-index` 可选，仅作下拉默认高亮。

## 包结构

见 `docs/1_后端方案.md`
