# FHSDK 集成商自研 Demo — 从需求到落地的实战案例

> **读者**：集成商开发者、FHSDK 培训讲师  
> **定位**：概念培训之外，本文记录 **Demo 从需求到落地的完整过程**，包括架构决策、分阶段交付与常见问题处理。  
> **配套幻灯片**：[`slides/index.html`](./slides/index.html)（SVG + HTML，浏览器打开）  
> **参考实现**：本仓库 `fov-demo` 工程

---

## 目录

1. [背景：集成商自研集成](#1-背景集成商自研集成)
2. [需求阶段](#2-需求阶段)
3. [架构设计](#3-架构设计)
4. [分阶段开发与文档对照](#4-分阶段开发与文档对照)
5. [常见问题](#5-常见问题)
6. [文档与代码如何对照阅读](#6-文档与代码如何对照阅读)
7. [验收与交付物](#7-验收与交付物)
8. [后续：阶段 D′](#8-后续阶段-d)

---

## 1. 背景：集成商自研集成

### 1.1 行业共性痛点

来自多场集成商技术交流的共识：

- **MQTT OSD ~0.5Hz**，无法与 30fps 画面对齐 → 地块投射、像素反算、AI 框叠加全部卡住。
- **不知道 SEI 怎么接**：RTSP 点播、转码流媒体、先连 DRC 再开流等错误路径导致「SEI 没有」。
- **不嵌入平台驾驶舱组件**：集成商采用 **FHSDK Java + 自研页面** 集成。

### 1.2 本 Demo 要证明什么

| 目标 | 说明 |
|------|------|
| 可行性 | FHSDK Java 能支撑直播、三层遥测、地图几何、手动操控 |
| 可复制 | 完整前后端 + 文档，集成商对照改 |
| 可讲解 | 三层数据差异、双落点对比、订阅生命周期 |

### 1.3 刻意不做什么

- 不嵌入 PaaS 驾驶舱组件（另一条集成路线见平台官方 PaaS 文档）。
- 不在 `<video>` 上叠 AR（空间理解全部在 **Cesium 地图**）。
- 不用 MQTT 慢通道激光当地图验收项（现场通常无推送）。

**参考文档**：`docs/0_整体方案.md` §2（三层数据选型）

---

## 2. 需求阶段

### 2.1 需求输入

- UI 参考：作业台风格（`reference.png`），**不要求 1:1**。
- 能力清单：设备、直播、MQTT / DRC / SEI、地图视锥、杆量、云台。
- 集成边界：**前端不握 Token、不连 MQTT/DRC**，只调 Demo 后端。

### 2.2 核心需求拆解

| 模块 | 需求要点 | 文档 |
|------|----------|------|
| 慢通道 | 飞机位姿、电量、风速 → 信息条 + 模型 | 需求 §3.4 |
| 快通道 | DRC 激光落点 → 地图绿点 | 需求 §3.5 |
| SEI | 开直播后帧级位姿 + FOV → 三轴/视锥/光轴 | 需求 §3.6 |
| 直播 | WHEP 稳定播放，画面无业务叠加 | 需求 §4.3 |
| 操控 | 杆量、云台拖拽，依赖 DRC | 需求 §3.7、§4.4 |

### 2.3 需求阶段的关键决策

1. **培训对比表**：MQTT / SEI / DRC 三列并排，列头写频率与前置条件。
2. **双落点**：DRC 实测（绿）vs SEI 光轴几何（橙），不做 MQTT 激光对比。
3. **分阶段交付**：A 链路 → B 光轴 → C 视锥 → D 操控 → D′ 飞行任务。

**参考文档**：`docs/0_整体方案.md`、`docs/requirement/需求说明.md`

---

## 3. 架构设计

### 3.1 总体架构

```text
集成商前端（Vue 3 + Cesium）
  ├─ WHEP            播放直播
  ├─ WebSocket       mqtt_osd / drc_camera / sei
  └─ REST            杆量、DRC、开直播

Java BFF（Spring Boot + FHSDK）
  ├─ LiveModule + FFmpeg WHIP → Oryx
  ├─ listen / subscribe → WebSocket 广播
  └─ REST → Drone / LiveModule API
```

**第一原则**：控制 REST / 遥测 WebSocket / 视频 WHEP **三分离**。

### 3.2 数据通道映射

| 通道 | 频率 | FHSDK | WS type |
|------|------|-------|---------|
| MQTT OSD | ~0.5Hz | `listenAircraft*` 分项 | `mqtt_osd` + `field` |
| DRC 激光 | 高频 | `listenCameraMeasureTarget` | `drc_camera` |
| SEI | ~30fps | `subscribeAr/FovSeiInfo` | `sei` |

### 3.3 会话与状态机

Demo 采用 **分步按钮**，不自动串联，方便培训讲解：

```text
session/init → devices/select → WS connect → payload 确认
  → live/start（SEI） → drc-link（激光+操控）
```

**参考文档**：`docs/0_整体方案.md` §3、`docs/1_后端方案.md` §3.4

### 3.4 后端模块

| 模块 | 职责 |
|------|------|
| `DeviceSessionService` | 选设备、MQTT 慢通道订阅 |
| `LiveService` | 开流、FFmpeg、**触发 SEI 订阅** |
| `SeiSubscriptionService` | AR/FOV SEI，与 DRC 解耦 |
| `ControlService` | DRC、快激光、杆量 |
| `TelemetryPublisher` | WS JSON 信封 |

---

## 4. 分阶段开发与文档对照

### 阶段 A — 基础链路 ✅

**交付**：设备列表、WS、Payload 下拉、WHEP 直播、MQTT 信息条、飞机 glTF、DRC 绿点、SEI RGB 三轴。

| 步骤 | 实现要点 | 参考文档 |
|------|----------|----------|
| SDK init | `POST /session/init` | `docs/1_后端方案.md` |
| MQTT 分项 listen | 不用聚合 topic | `docs/0_整体方案.md` §6 |
| 开流 + WHEP | Oryx + FFmpeg 透传 | `docs/1_后端方案.md` §3.6 |
| SEI 三轴 | WS `sei` → Cesium | `docs/turtorial/SEI相机坐标系说明.md` |

### 阶段 B — 光轴落点 ✅

**交付**：SEI 光轴射线与地面/地形求交，橙点与 DRC 绿点同屏对比。

| 步骤 | 实现要点 | 参考文档 |
|------|----------|----------|
| NED 欧拉 + 相机系 | +Z 光轴、+Y 画面下 | `docs/turtorial/SEI相机坐标系说明.md` |
| 射线求交 | `globe.pick` | `docs/turtorial/Cesium地图动态几何渲染说明.md` |
| 逆投影原理 | 画面中心 = 光轴 | `docs/0_整体方案.md` §6 |

### 阶段 C — 视锥与贴地 ✅

**交付**：FPV 视锥线框、地面黄色交线、可选 WHEP 纹理贴地（默认关）。

| 步骤 | 实现要点 | 参考文档 |
|------|----------|----------|
| FOV μm → 角度 | 勿把 fov 字段当度数 | `docs/0_整体方案.md` §6 |
| Primitive 刷新 | 复用 instance 降开销 | `docs/turtorial/Cesium地图动态几何渲染说明.md` |
| 贴地 | 仅地图侧，非 video AR | `docs/2_前端方案.md` |

### 阶段 D — 操控补全 ✅

**交付**：R/F 升降、Q/E 偏航、直播区拖拽云台（`gimbalPitch` + `yaw`）。

| 步骤 | 实现要点 | 参考文档 |
|------|----------|----------|
| 50ms stick | `useStickControl` | `docs/2_前端方案.md` |
| 拖拽映射 | pointer → REST stick | `frontend/.../useGimbalDrag.ts` |
| 禁用态 | 未连 DRC 灰掉 | `docs/0_整体方案.md` §7.4 |

### 横切 — 培训说明 UI ✅

**交付**：顶栏「培训说明」开关、`TrainingBanner`、`MapLegend`、遥测表频率脚注。

**参考文档**：`docs/0_整体方案.md` §9（培训话术）

### 阶段 D′ — 飞行控制 ⏳

一键起飞、急停、返航、取消返航 — 规划中。

---

## 5. 常见问题

> 线性总览见 [`slides/svg/development-timeline.svg`](./slides/svg/development-timeline.svg) 或 PNG [`slides/svg/png/development-timeline.png`](./slides/svg/png/development-timeline.png)。

### 5.0 线性开发历程（一图速览）

```text
需求 → 架构 → A 基础链路 → B 光轴落点 → C 视锥贴地 → D 操控 → 文档沉淀 → D′ 规划中
         │         │              │              │           │
         │    [问题] SEI绑DRC   [问题] 欧拉角≠关节角  [问题] 单位/透传  [问题] drcConnected
         │    [问题] MQTT无激光      │              [问题] MapLegend
         └─ BFF 三分离 ────────────┴── SeiSubscriptionService 修复链 ──┘
```

| 时间点 | 里程碑 | 同期问题 | 修复 / 文档 |
|--------|--------|----------|-------------|
| 起步 | 需求 + 架构定稿 | — | 整体方案 |
| 阶段 A | WS/WHEP/DRC/SEI 三轴 | SEI 订在 DRC；MQTT 激光无数据 | `SeiSubscriptionService` · 后端方案 §3 |
| 阶段 B | 橙绿双落点 | SEI roll 与 gimbal 混淆 | `SEI相机坐标系说明.md` |
| 阶段 C | 视锥 + 贴地 | dm/s·mm·μm；转码丢 SEI | `formatOsd.ts` · FFmpeg 透传 |
| 联调 | SEI 与 DRC 解耦验收 | 前端图层绑错 `drcConnected` | `liveStarted` · 订阅生命周期 |
| 培训 UI | Banner/图例/对比表 | MapLegend grid 竖条 | flex 布局 |
| 收尾 | 实战案例 + 幻灯片 | — | 本文档 |

> 每条记录：**现象 → 根因 → 修复**，便于集成商自查。

### 问题 1：SEI 必须先连 DRC 才有？

| | |
|---|---|
| **现象** | 先连 DRC 再开直播，地图无 SEI；先开直播再 DRC 则正常 |
| **根因** | SEI 订阅写在 `connectDrc` 里，且 `live == null` 时跳过 |
| **修复** | 抽出 `SeiSubscriptionService`，在 `LiveService.startLive` 成功后订阅 |
| **参考** | 见 `docs/1_后端方案.md` · `SeiSubscriptionService` |

### 问题 2：前端用 `drcConnected` 控制 SEI 图层

| | |
|---|---|
| **现象** | 未连 DRC 时看不到视锥，误以为 SEI 依赖 DRC |
| **根因** | 显示条件绑错状态 |
| **修复** | SEI 图层改绑 `liveStarted`；关直播 `clearSei()` |
| **参考** | `docs/2_前端方案.md`、`docs/0_整体方案.md` §7.4 |

### 问题 3：风速 / 高度 / FOV 展示「像错了」

| | |
|---|---|
| **现象** | 风速 17 被当成 17 m/s；FOV 当角度；SEI 高度当米 |
| **根因** | SDK 原始单位：wind **dm/s**、SEI altitude **mm**、FOV **μm** |
| **修复** | `formatOsd.ts` 直接展示原始值 + 脚注；FOV 另算 HFOV/VFOV |
| **参考** | `docs/0_整体方案.md` §6、`frontend/.../formatOsd.ts` |

### 问题 4：SEI pitch/roll 与云台角对不上

| | |
|---|---|
| **现象** | 水平朝前时 SEI roll≈90°、MQTT gimbal_pitch≈90°，以为表格写反 |
| **根因** | SEI 是 NED **相机欧拉角**，MQTT 是**云台关节角** |
| **修复** | 培训文档 + 对比表旁注；地图三轴与画面对齐即正确 |
| **参考** | `docs/turtorial/SEI相机坐标系说明.md` |

### 问题 5：MQTT 慢通道激光没有绿点

| | |
|---|---|
| **现象** | 订阅了 `measureTarget` 但地图无激光 |
| **根因** | 现场 MQTT 通常不推激光测距 |
| **修复** | 验收改用 DRC `listenCameraMeasureTarget` |
| **参考** | `docs/0_整体方案.md` §6.2 |

### 问题 6：FFmpeg 转码后 SEI 消失

| | |
|---|---|
| **现象** | 推流后 SDK 有 SEI，前端 WHEP 流无 SEI |
| **根因** | 重新编码丢弃 SEI NAL |
| **修复** | WHIP 透传 H.264 裸流（remux） |
| **参考** | `docs/1_后端方案.md` §3.6、根 README §依赖环境 |

### 问题 7：图例布局三列挤成竖条

| | |
|---|---|
| **现象** | `MapLegend` 2 列 grid + 3 子元素，说明文字 14px 宽竖排 |
| **根因** | CSS grid 子项顺序与列数不匹配 |
| **修复** | 改 flex 分组布局 |
| **参考** | `frontend/.../MapLegend.vue` |

### 问题 8：改后端 SEI 逻辑不生效

| | |
|---|---|
| **现象** | 代码已改，行为仍旧 |
| **根因** | Spring Boot 未重启 |
| **修复** | 后端变更后 `mvn spring-boot:run` 重启 :8080 |
| **参考** | 根 README §快速启动 |

---

## 6. 文档与代码如何对照阅读

集成商自研集成时，推荐按以下顺序阅读本仓库：

```text
1. 读需求说明 / 整体方案     → 确认能力与边界
2. 问数据选型                  → 三层通道（MQTT / DRC / SEI）
3. 写后端订阅 / WS             → backend/fhsdk-demo-server
4. 开直播 / WHEP               → LiveService + Oryx
5. 地图视锥 / 光轴             → frontend Cesium 模块
6. 杆量 / DRC                  → ControlService
```

| 主题 | 文档 |
|------|------|
| 总览 | `docs/0_整体方案.md` |
| 后端 | `docs/1_后端方案.md` |
| 前端 | `docs/2_前端方案.md` |
| SEI 坐标系 | `docs/turtorial/SEI相机坐标系说明.md` |
| Cesium 渲染 | `docs/turtorial/Cesium地图动态几何渲染说明.md` |

---

## 7. 验收与交付物

| # | 验收项 | 状态 |
|---|--------|------|
| 1 | 选设备后信息条、模型更新 | ✅ |
| 2 | WHEP 稳定播放，video 无叠加 | ✅ |
| 3 | 开直播即有 SEI（无需 DRC） | ✅ |
| 4 | DRC 后绿点激光 | ✅ |
| 5 | 橙点光轴 + 视锥 | ✅ |
| 6 | WASD/R/F/Q/E + 拖拽云台 | ✅ |
| 7 | 前端仅调 BFF | ✅ |
| 8 | 飞行控制按钮 | D′ 规划中 |

**交付物清单**：后端、前端、`docs/`、本案例 + HTML 幻灯片。

---

## 8. 后续：阶段 D′

- **D′**：对接一键起飞 / 急停 / 返航 API，扩展 `ControlService`。
- **文档**：常见问题条目持续并入教程文档；Demo 变更同步各方案文档。
- **培训分工**：
  - 概念培训 → 平台官方 SEI/OSD 材料
  - **实战案例** → 本文 + `slides/index.html`

---

## 相关链接

| 资源 | 路径 |
|------|------|
| 需求说明 | [`../requirement/需求说明.md`](../requirement/需求说明.md) |
| 整体方案 | [`../0_整体方案.md`](../0_整体方案.md) |
| SEI 坐标系 | [`../turtorial/SEI相机坐标系说明.md`](../turtorial/SEI相机坐标系说明.md) |
| HTML 幻灯片 | [`./slides/index.html`](./slides/index.html) |
| **开发历程图** | SVG [`./slides/svg/development-timeline.svg`](./slides/svg/development-timeline.svg) · PNG [`./slides/svg/png/development-timeline.png`](./slides/svg/png/development-timeline.png) |
