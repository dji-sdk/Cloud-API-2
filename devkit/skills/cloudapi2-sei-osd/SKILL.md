---
name: cloudapi2-sei-osd
description: Cloud API 2.0 SEI/OSD 实时遥测数据接入助手。当用户要获取无人机帧级位置数据（SEI，内嵌于视频流）、设备遥测数据（OSD，MQTT/DRC 通道）、在地图上可视化相机视锥/FOV/视频投影、接入 FHSDK LiveModule subscribeArSeiInfo/subscribeFovSeiInfo、或遇到 SEI 解析/OSD 频率/位置对齐视频等问题时调用。触发词：SEI、OSD、帧级位置、视锥、FOV、相机姿态、视频投影、ArSeiInfo、FovSeiInfo、subscribeArSeiInfo、subscribeFovSeiInfo、LiveModule、DRC OSD、MQTT OSD、osd_frequency、遥测数据、位置对齐、像素反算经纬度、视锥中心点、Cesium 地图、NED 坐标、相机位姿、畸变参数、HostDistortParam、帧同步、实时定位。
version: 1.2.0
author: DJI CloudAPI2 Team
---

# DJI CloudAPI2 SEI/OSD 实时遥测数据接入助手

## 这个 Skill 是干什么的

帮 ISV 开发者正确接入 DJI CloudAPI2 的 **SEI（帧级位置数据）** 和 **OSD（设备遥测数据）**，覆盖服务端 FHSDK 数据获取 + 前端地图可视化两端。

**核心价值**：ISV 最集中的痛点是"怎么拿到每一帧画面对应的、准确的、足够高频的位置数据"。本 Skill 把 SEI/OSD 的 What/Why/How 讲透。

## 何时触发

- 用户要获取帧级位置数据（SEI）
- 用户要获取设备遥测数据（OSD）
- 用户问 SEI vs OSD 区别、频率、前置条件
- 用户要在 Cesium/地图上可视化相机视锥、FOV、视频投影
- 用户要接入 `LiveModule`、`subscribeArSeiInfo`、`subscribeFovSeiInfo`
- 用户遇到"OSD 频率太低对不齐视频"问题
- 用户问像素反算经纬度、视锥中心点
- 用户问 DRC OSD 配置（`osd_frequency` 1~30Hz）
- 用户问哪代设备/固件支持 SEI

**不触发**：通用设备管理（→ `cloudapi2-device-management`）、前端 PaaS 组件接入（→ `cloudapi2-frontend-component`）、通用 SDK 规则（→ `cloudapi2-sdk-core`）。

## 工作流

### 第 1 步：判断场景

```
用户要什么？
├── 帧级位置/相机姿态/AR/视锥 → SEI 路径
├── 设备状态/位置/电量/速度   → OSD 路径（MQTT or DRC）
├── 前端地图可视化（Cesium）  → 前端 Demo 路径
├── 位置数据选型（不确定哪个）→ 先讲三层对比
└── 遇到 SEI 取不到/解析失败  → 排障路径
```

### 第 2 步：讲清三层位置数据

**必须先让用户理解三层选型**（来自 `references/training-guide.md`）：

| 通道 | 频率 | 前置条件 | 典型用途 |
|------|------|----------|---------|
| **MQTT OSD** | 0.5Hz（2s 一次，固定） | 设备在线即推 | 设备状态面板、粗轨迹 |
| **DRC OSD** | 默认 3Hz，可配 1~30Hz | 需建立 DRC 链路 | 操控反馈、中高频轨迹 |
| **SEI** | ~30fps（每帧都有） | 需拉流（WebRTC） | **画面+位置融合**（AR、视锥、像素反算） |

**判断流程**：
- 要对齐视频画面 → 用 SEI
- 需要中高频位置但不拉流 → DRC OSD（配 `osd_frequency`）
- 只看设备状态 → MQTT OSD
- **不要试图把 MQTT OSD 提到高频来对齐画面 — 这条路不通**

### 第 3 步：读对应 reference

| 场景 | 参考文档 |
|------|---------|
| 技术架构（SEI vs OSD 完整梳理） | `references/sei-osd-architecture.md` |
| 服务端 FHSDK 获取数据（Java） | `references/server-side-guide.md` |
| 前端 Cesium 地图可视化 Demo | `references/frontend-visualization.md` |
| ISV 常见问题与能力对照 | `references/faq-and-capability.md` |
| 培训内容（概念讲解） | `references/training-guide.md` |
| ISV 痛点汇总（需求背景） | `references/isv-pain-points.md` |

### 第 4 步：按路径输出

#### SEI 路径（服务端）

生成代码时遵循 `references/server-side-guide.md` 的完整生命周期：

```
[1] 初始化 SDK
[2] getDroneInstance(sn) → Drone
[3] 开直播：listenLiveAvailableRange → getLiveStream → LiveModule → start
[3.5] 开启 SEI 上报：openDroneSeiMessageSend（ar_info_switch:1）
[4] 订阅 SEI：live.subscribeArSeiInfo(cb) / subscribeFovSeiInfo(cb)
[5] 回调中处理 ArSeiInfo / FovSeiInfo
[6] 退订：live.unSubscribeArSeiInfo(id) / unSubscribeFovSeiInfo(id) + live.stop()
```

> **注意**：SEI 不依赖 DRC 链路。`disconnectDrcLink` 仅在使用了 DRC OSD 时才需要调用。

#### OSD 路径（服务端）

```
MQTT OSD：drone.getDroneOsdModule().listenAircraftXxx(cb)（无需额外前提）
DRC OSD：drone.getDrcModule().connectDrcLink(cb) → drone.getCameraControlModule().listenDrcCameraMeasureTarget(payload, cb)
```

#### 前端可视化路径

参考 `references/frontend-visualization.md`，核心架构：

```
后端 WebSocket 推送 SEI/OSD JSON
    ↓
前端 state.ts (reactive)
    ↓
Cesium 渲染：飞机模型 + 相机三轴 + 视锥线框 + 落地四边形 + 视频贴地投影
```

### 第 5 步：排障

SEI 常见问题：
- **拿不到 SEI** → 必须走 WebRTC 直推，RTSP/声网拿不到
- **浏览器限制** → 仅 Chrome/Edge 支持（`createEncodedStreams` Chromium 专有）
- **SEI 数据全零** → 检查是否调用了 `openDroneSeiMessageSend`（`ar_info_switch:1`）
- **机型差异** → M10E/M3T 走 UTF8 JSON，M4D/M4T 走二进制协议

## 核心约束（critical 级别）

1. **SEI 必须走 WebRTC**：RTSP 点播、声网拉流、自建流媒体均拿不到 i 帧 SEI
2. **listen/subscribe 必须配对 cancel/unSubscribe**：所有订阅返回 listenId，必须在退出时释放
3. **LiveModule 前提**：SEI 订阅前必须先 `getLiveStream` + `start` 开直播成功
4. **DRC 前提**：DRC OSD 需先 `connectDrcLink` 成功
5. **null guard**：`getDroneInstance(sn)` / `getLiveStream(liveInfo)` 均可能返回 null
6. **不要追高频 MQTT OSD**：频率固定 0.5Hz，不可调；需高频请走 DRC 或 SEI
7. **机型适配**：SEI 解析协议因机型不同（JSON vs 二进制），角度需乘 `CAM_COEFFICIENT`

## SEI 数据结构速查

### ArSeiInfo（相机姿态 + 位置）

| 字段 | 含义 |
|------|------|
| `cam_yaw/pitch/roll` | 相机欧拉角（NED 坐标系） |
| `gnd_lat/gnd_lng/gnd_alt` | 地面投影点经纬高 |
| `camera_pos_x/y/z` | 相机相对起飞点偏移（NED，米） |
| `liveview_source_cam` | 当前镜头（Wide=1/Zoom=2/IR=3） |

### FovSeiInfo（视场信息）

| 字段 | 含义 |
|------|------|
| `sensor_width/height` | 传感器尺寸（μm） |
| `focal_length` | 焦距（μm） |

### DRC OSD（快速通道）

| 字段 | 含义 |
|------|------|
| `latitude/longitude/altitude` | 飞机位置 |
| `attitude_head` | 飞机航向 |
| `gimbal_pitch/roll/yaw` | 云台姿态 |
| `speed_x/y/z` | 速度 |
| `home_distance` | 距 Home 距离 |

## 权威参考

`references/` 下的 6 份文档覆盖 SEI/OSD 接入的完整知识体系：
- 技术架构、服务端 API、前端可视化 Demo、ISV FAQ、培训内容、需求背景

## 参考文档已知局限（生成代码时注意）

以下是 references/ 中教学 Demo 代码的已知设计取舍，AI 生成生产代码时应改进：

1. **DeviceContext 的 listenId 字段缺 volatile**（`server-side-guide.md`）：
   `arSeiListenId` 等为普通 `int`，SDK 回调线程写入、应用线程读取。生产代码应使用
   `volatile int` 或 `AtomicInteger` 保证跨线程可见性。

2. **SDK 回调 error 为 null 时的处理**（`server-side-guide.md`）：
   部分 SDK 回调成功时传 `null` error。Demo 中 `if (error != null && error.isSuccess())`
   在此情况下不存结果。生产代码应改为 `if (error == null || error.isSuccess())`。

3. **subscribeSlowOsd 静默忽略订阅失败**（`server-side-guide.md`）：
   `putListenId` 中 `id < 0` 时只跳过存储，无日志。生产代码应记录 WARN 日志。

4. **Cesium 私有 API `_computeNearFarPlanes`**（`frontend-visualization.md`）：
   Demo 使用下划线前缀内部方法做视锥角点计算。生产项目应锁定 Cesium 版本或改用公开
   API（`FrustumGeometry` 的 `computeNearFarPlanes`）。

## 共享规范

> 本 Skill 的 constraints.yaml 仅包含 SEI/OSD 领域特有规则；通用 SDK 约束见 `skills/_shared/constraints-common.yaml`（生成代码时需同时遵守）。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载，不要一次性读取所有 references/。
