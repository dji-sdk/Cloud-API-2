# FHSDK ISV 培训 Demo

演示 ISV 基于 **FHSDK Java** 自研前后端的直播、OSD、SEI、地图与手动操控。

**路线图**（详见 [需求说明 §6](docs/requirement/需求说明.md#6-实现阶段)）：A～D 已完成（地图 SEI 视锥/贴地、杆量与云台拖拽）；D′ 飞行控制规划中。

## 文档

| 文档 | 说明 |
|------|------|
| [需求说明](docs/requirement/需求说明.md) | 做什么 |
| [整体方案](docs/0_整体方案.md) | 架构与协议 |
| [后端方案](docs/1_后端方案.md) | `backend/` 工程结构 |
| [前端方案](docs/2_前端方案.md) | `frontend/` 工程结构 |
| [**实战案例**](docs/case-study/ISV自研Demo实战案例.md) | 从需求到落地全过程 + [HTML 幻灯片](docs/case-study/slides/index.html) |

## 工程目录

```text
fov-demo/
├── LICENSE                    # Apache-2.0
├── backend/
│   ├── docker/
│   │   ├── oryx/              # Oryx 拉流（Docker）
│   │   └── demo-server/       # 后端容器镜像与运行时配置
│   └── fhsdk-demo-server/     # Spring Boot + FHSDK 源码
├── frontend/
│   ├── prototype/             # UI 交互原型（静态 HTML）
│   └── fhsdk-demo-web/        # Vue 3 工程
└── docs/
```

## 首次配置（运行前必做，后端容器化）

1. **FHSDK JAR**：将 `flight-hub-sdk.jar` 放到 `backend/fhsdk-demo-server/fhsdk/`（见该目录 [`README.md`](backend/fhsdk-demo-server/fhsdk/README.md)）。
2. **后端容器环境变量**：复制 `backend/docker/demo-server/.env.example` 为 `.env`，填写 `FHSDK_*`、`ORYX_PUBLISH_SECRET`、`LIVE_*`。
3. **DRTC 配置**：在 `backend/docker/demo-server/` 下，复制 `drtc_config_node.json.example` 为 `config/drtc_config_node.json`，将 `YOUR_FH_SERVER_HOST` 改为平台地址；或设置环境变量 `SDK_DRTC_CONFIG` 指向 FHSDK 包内 demo 配置。随后在 `demo-server/` 目录执行 `./prepare.sh`。
4. **Oryx 推流密钥**：启动 Oryx 后运行 `backend/docker/oryx/init-oryx.sh`，将输出的 secret 填入 `.env` 的 `ORYX_PUBLISH_SECRET`。
5. **Cesium 地形（可选）**：复制 `frontend/fhsdk-demo-web/.env.example` 为 `.env.local`，填入 `VITE_CESIUM_ION_TOKEN`。

## 许可证

- Demo 源码：[Apache License 2.0](LICENSE)
- `flight-hub-sdk.jar` 及 native 库为 FHSDK 专有组件，需按 DJI 分发说明单独获取

## 使用说明

- 本 Demo 面向**本地开发/培训**：REST `/api/v1/*` 使用浏览器 Cookie Session，无独立用户登录；请在可信网络内使用（默认 `127.0.0.1:5173` / `:18080`）。
- 平台若要求 HTTP/WebSocket 鉴权头，在 `backend/docker/demo-server/.env` 中配置 `FHSDK_AUTH_HEADER_*`；部分环境可留空。

## 依赖环境

运行完整 Demo（直播 + OSD + SEI + DRC）需要以下环境。**仅看 UI 原型**只需浏览器，无需后端。

### 本机工具

| 组件 | 要求 | 用途 |
|------|------|------|
| **Java 17 + Maven** | 构建 fat jar | `backend/fhsdk-demo-server/` |
| **Node.js + npm** | 建议 LTS | `frontend/fhsdk-demo-web`（Vite 6） |
| **Docker / Docker Compose** | 跑 Oryx + 后端容器 | `backend/docker/oryx/` + `backend/docker/demo-server/` |
| **浏览器** | Chrome / Edge 等现代浏览器 | Vue + Cesium + WHEP 播放 |

### 本地服务与端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Oryx（Docker） | `12022`（宿主信令，映射容器 2022）、`8000/udp`（WebRTC） | WHIP 收流 / WHEP 播放；首次须 `./init-oryx.sh` 获取 `publish-secret` |
| Java 后端（Docker） | `18080`（宿主，映射容器 8080） | REST `/api/v1` + WebSocket 遥测 |
| Vite 前端 | `5173` | `/api` → `:18080`，`/oryx` → `:12022` |

### 远端平台（FHSDK）

后端通过 **FHSDK Java** 连接 FlightHub 平台，配置由 `backend/docker/demo-server/.env` 传入容器：

- HTTP：`FHSDK_ADDRESS` + `FHSDK_PORT`
- Notification WebSocket：`FHSDK_NOTIFICATION_SERVER`
- 需能访问该平台，且工作空间内有**在线设备**（机场 + 飞机）

### 数据流（简要）

```text
平台设备 → FHSDK LiveModule → FFmpeg WHIP → Oryx → 浏览器 WHEP
                └→ listen/subscribe → WebSocket → 前端（OSD / SEI / DRC）
```

### 不依赖

- 不嵌入 PaaS 驾驶舱前端
- 前端不直连 FHSDK、MQTT、DRC

更细说明：[`backend/fhsdk-demo-server/README.md`](backend/fhsdk-demo-server/README.md)、[`backend/docker/oryx/README.md`](backend/docker/oryx/README.md)、[`docs/1_后端方案.md`](docs/1_后端方案.md) §3–§5。

## UI 原型预览

浏览器打开：

[`frontend/prototype/index.html`](frontend/prototype/index.html)

或：

```bash
npx --yes serve frontend/prototype -p 5174
```

## 快速启动

```bash
# 0. 完成上方「首次配置」

# 1. Oryx（首次需 init-oryx.sh 获取 publish-secret）
cd backend/docker/oryx && docker compose up -d && ./init-oryx.sh

# 2. 构建后端 fat jar（需 Java 17）
cd ../../fhsdk-demo-server && mvn -DskipTests package

# 3. 准备并启动后端容器（prepare 依赖上一步 jar + config/drtc_config_node.json）
cd ../docker/demo-server && ./prepare.sh && docker compose up -d --build

# 4. 前端
cd ../../../frontend/fhsdk-demo-web && npm install && npm run dev
# 浏览器打开 http://127.0.0.1:5173/
```

**换机型直播**：页面连接 WS 后从 Payload 下拉列表选择（如 M3TD `81-0-0`、M3D `80-0-0`）并确认，无需改 yml。可选在 `application.yml` 配置 `demo.drone-payload-index` 作为默认高亮项。
