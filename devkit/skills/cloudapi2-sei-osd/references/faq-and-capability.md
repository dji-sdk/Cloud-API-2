# ISV SEI / OSD 问题解答与 FHSDK 能力对照

> **对应来源**：[ISV_SEI_OSD_需求与困惑汇总_2026-06-15.md](./ISV_SEI_OSD_需求与困惑汇总_2026-06-15.md)  
> **整理日期**：2026-06-15  
> **视角**：以上云 API 2 + **FHSDK（DJI CloudAPI2 SDK）** 为对外交付主线，汇总培训可答项、FHSDK 可闭环项、以及需产品/平台单独跟进的未决项。  

---

## 目录

- [0. 总览](#0-总览能不能逐条解答)
- [1. FHSDK 能力与边界](#1-fhsdk-能力与边界答复前先读)
- [2. 第一节「一页纸结论」——答复](#2-第一节一页纸结论答复)
- [3. 第二节 C1～C9 逐条解答](#3-第二节-c1c9-逐条解答)
  - [C1 详细解答：MQTT OSD vs DRC OSD vs SEI](#c1-详细解答mqtt-osd-vs-drc-osd-vs-sei)
  - [C2 详细解答：SEI 怎么取](#c2-详细解答sei-怎么取)
  - [C3 详细解答：哪代设备/哪个固件支持 SEI](#c3-详细解答哪代设备哪个固件支持-sei)
  - [C7 详细解答：视锥中心点 / 像素反算经纬度](#c7-详细解答视锥中心点--像素反算经纬度)
- [4. 第三节 逐家明细 ①～⑬ 逐条解答](#4-第三节-逐家明细-逐条解答)
  - [4.1 SEI / 帧级定位类（①~⑨）](#41-sei--帧级定位类)
  - [4.2 OSD 频率 / 负载 / 稳定性类（⑩~⑫）](#42-osd-频率--负载--稳定性类)
  - [4.3 开发交流会（⑬）](#43-开发交流会-)
- [5. 第四节 U1～U8 未决项](#5-第四节-u1u8-未决项fhsdk-能否代答)

---

## 0. 总览：能不能「逐条解答」？

**不能 100% 逐条闭环**，但可以对每一条给出**明确口径**（能答 / 部分能答 / 不能答 / 待产品确认）。

| 维度 | 结论 |
|------|------|
| 文档核心诉求（帧级位置对齐视频） | FHSDK **SEI 回调**是标准解法，可覆盖约 **40%～50%** 困惑的**技术闭环** |
| 培训可讲清的「Why / 概念」 | 约 **80%** 可讲（含 OSD vs SEI、为何别追高频 OSD） |
| 需产品/固件/平台单独答复 | 第四节 **U1～U8** 及若干逐家诉求 **FHSDK 无法代答** |
| 当前交付形态缺口 | **对外主交付 Java SDK（后端）**；前端 AR 需转发或待 Web/PaaS 组件正式暴露 SEI |

**一句话（对用户）**：帧级位姿请走 **FHSDK `LiveModule` + `subscribe*SeiInfo`**，不要指望提 OSD 频率；FHSDK 给**结构化 SEI 数据**，不给开箱 AR 投影；浏览器叠图需 **后端转发 JSON** 或 **官方 Web 直播能力（待产品确认是否正式交付）**。

---

## 1. FHSDK 能力与边界（答复前先读）

### 1.1 SEI：FHSDK 已做什么

- 路径：`dji_flight_hub_sdk/src/bll/module/live_module/`
- 从 RTC 视频帧 H.264 NAL 提取 DJI ext SEI → JSON（老机型）或 TLV（新机型）→ 回调：

| API | 数据 | 用途 |
|-----|------|------|
| `subscribeArSeiInfo` | `ArSeiInfo` | 相机 NED 姿态 + GND 位置 + 原点经纬度 |
| `subscribeFovSeiInfo` | `FovSeiInfo` | 传感器宽高的 μm 值 + 焦距 |
| `subscribeAiSeiInfo` | `AiSeiInfo` | AI 检测框 |
| `subscribeAssistantSeiInfo` | `AssistantSeiInfo` | 智能辅助朝向 |
| `subscribeEisSeiInfo` | `EisSeiInfo` | 电子防抖裁剪四角 |

- **不做**：PinPoint 投影、地图贴图、ArCore 绘制、AR+FOV 合并渲染结构、帧时间戳与 SEI 绑定。

### 1.2 OSD：FHSDK 已做什么

- 经 **Notification Center** 推送，例如：
  - `listenAircraftPayloadsOsdInfo`：云台角、变焦、**激光测距目标经纬高**（`measure_target_*`）等
  - 机场/飞机在线、位置等其它监听
- 监听策略多为 `NotificationFrequency_OnChanged`，**不是帧级**，也**不能在 FHSDK 层把 OSD 提到 10～20Hz**。

### 1.3 交付形态与前端 AR（内部决策摘要）

| 形态 | 状态 | 对 ISV 的含义 |
|------|------|----------------|
| **Java SDK** | ✅ 当前主交付 | SEI 在 JVM 回调；浏览器需自建 WebSocket 等转发 |
| **Web SDK**（`platform/web`，含 `subscribeArSei`） | ⚠️ 代码存在，接口列表标「支持」 | 是否正式发包、文档、SLA **需产品确认** |
| **PaaS 前端组件** | ⚠️ 未核实 | 是否暴露码流/SEI **需与组件 owner 对齐** |
| **自建流 / RTSP** | ❌ 非标准路径 | FHSDK `LiveModule` 不覆盖；不承诺 SEI |

**推荐对外策略（分层，非二选一）**：

1. **主路径**：标准 ISV 用 **FHSDK 结构化 API**，不默认发放 SEI 二进制协议全文。
2. **前端 AR**：**Java 解析 SEI → 业务 API/WebSocket → 前端 Canvas**（当前可落地）；或正式交付 Web LiveModule。
3. **例外路径**：自建流媒体用户声明**不在标准支持范围**；若必须支持，走单独立项，勿散发圈内 parser 源码。
4. **合规**：FHSDK 作为产品面交付 SEI 解析**无妨**；DUSS/Confluence/DJI 上云API 2同源实现**不宜**默认外发，协议附录需法务/信安评审。

---

## 2. 第一节「一页纸结论」——答复

| # | 原结论要点 | FHSDK / 培训口径 |
|---|------------|------------------|
| 1 | OSD 0.5～1Hz 无法对齐画面 | ✅ 认同。FHSDK **不提高 OSD 频率**；对齐画面用 **`subscribeArSeiInfo` 等** |
| 2 | 标准解法在 WebRTC i 帧 SEI，ISV 未接通 | ✅ FHSDK 已封装解析，ISV **无需自己抠 NAL**（走 FHSDK 拉流时） |
| 2a | 拉流方式不对拿不到 SEI | ✅ 培训必讲：**须 FHSDK RTC 拉流**；RTSP/未解析 SEI 的自建流 **FHSDK 不保证** |
| 2b | 不知哪代设备/固件支持 | ⚠️ **U1 未决**；代码仅有 `payload_index` 分支，**无官方矩阵**，培训前须产品补齐 |
| 3 | 培训重点是讲清已有能力 + 少量补能力 | ✅ FHSDK 接入教程 + 后端转发前端 AR 示例应作为培训核心产出 |
| 4 | 部分硬需求培训解决不了 | ✅ 见本文第四节、第六节 |

---

## 3. 第二节 C1～C9 逐条解答

### C1 详细解答：MQTT OSD vs DRC OSD vs SEI

ISV 常把"OSD"和"SEI"混为一谈，实际位置数据有**三个层级**：

| 通道 | 频率 | 条件 | 传输方式 | 内容 |
|------|------|------|----------|------|
| **MQTT OSD** | **0.5Hz**（2s 一次，`dji-cloud-access` 代码 `StartTimer(2000)` 写死） | 常态，无需额外建连 | MQTT 信令通道 | 飞机完整状态（GPS、高度、电量、姿态、云台角、激光测距等） |
| **DRC OSD** | **3Hz**（相机 OSD，协议标注）或更高 | 需建立 DRC 链路（进入指点飞行/虚拟摇杆等模式） | WebSocket DRC 通道 | 飞机状态 + 相机详细信息（含 FOV、各镜头参数） |
| **SEI** | **随帧（~25fps，每个 I 帧必有）** | 需拉流（FHSDK `LiveModule.start`） | 嵌在 H.264 视频帧 NAL 中 | 相机位姿(AR)、FOV、AI 检测框、EIS 裁剪、辅助影像朝向 |

**代码验证**（`dji-cloud-access/src/cloud_access_core/cloudaccessmgr.cpp`）：

```cpp
// 飞机 OSD：2000ms 定时器
push_fc_osd_id_ = cloud_worker_->StartTimer([this]() { PublishFcOSDInfo(false); }, 2000, true);
// 遥控器 OSD：2000ms
push_rc_osd_id_ = cloud_worker_->StartTimer([this]() { PublishRcOSDInfo(false); }, 2000, true);
// 机场中继 OSD：2000ms
push_relay_osd_id_ = cloud_worker_->StartTimer([this]() { PublishRelayOSDInfo(false); }, 2000, true);
```

**ISV 感知的"2 秒一次对不齐画面"就是 MQTT OSD 的 0.5Hz**——这是机场 MC 模块写死的上报节奏，SDK 侧无法调高。

**对 ISV 的标准回答**：

- 要**对齐视频画面**的位置 → 用 **SEI**（`subscribeArSeiInfo`），帧级
- 只看**设备状态监控** → 用 **MQTT OSD**（`listenAircraftPayloadsOsdInfo` 等），0.5Hz 够用
- 需要**中等频率（3Hz）+ 丰富相机参数**但不想拉流 → 建 **DRC 链路**，但有前置条件（进入 DRC 模式）
- **不要**试图把 MQTT OSD 提到高频来对齐画面——这条路不通

**FHSDK 接口映射**：

| 需求 | FHSDK 接口 | 频率 |
|------|-----------|------|
| 帧级位姿 | `subscribeArSeiInfo` / `subscribeFovSeiInfo` | ~25fps |
| 设备状态 | `listenAircraftPayloadsOsdInfo` 等 NC 监听 | 0.5Hz |
| 激光测距点 GPS | `listenAircraftPayloadsOsdInfo` → `measure_target_*` | 0.5Hz |

### C2～C9 汇总表

| # | 困惑 | 可否解答 | 标准答复（FHSDK 视角） |
|---|------|----------|------------------------|
| **C2** | SEI 怎么取 | ✅ 能 | 见下方 C2 详细解答 |
| **C3** | 哪代设备/固件支持 SEI | ❌ FHSDK 不能单独答 | **U1 待办**。代码已知：JSON 路径 `80-0-0`/`81-0-0`（M3D 等）、`66/67/129-0-0`（M3E 等）；二进制为其余；**跳过** `39-0-7`（FPV）、`165-0-7`（Dock Live）。**不能**代替「机场 1/2/3 代 + 固件」矩阵 |
| **C4** | OSD 频率为何调不了 | ✅ 能 | **见 C1 三层级表**。MQTT OSD 0.5Hz 是设备端 `dji-cloud-access` 硬编码 2000ms 定时器，SDK 无法调高。追帧级对齐应走 SEI |
| **C5** | 高频 OSD 一开就停航线 | ⚠️ 暂缓 | 疑似 bug，暂时无法回答。ISV 追帧级位置应走 SEI（见 C1），不需要开高频 OSD |
| **C6** | OSD 太多打挂服务器 | — 不处理 | 属于平台基建问题（MQTT 扇入/NC 订阅机制），不属于 SEI/CV 范畴 |
| **C7** | 视锥中心 / 像素反算经纬度 | ⚠️ 部分 | 见下方 C7 详细解答 |
| **C8** | 固件升级 OSD 枚举坏了 | — 不处理 | 固件兼容性问题，FHSDK 无能为力。U3 待办归固件/平台团队 |
| **C9** | 文档缺失 | ⚠️ 持续改善 | 对外仅 FHSDK + 前端 PaaS 组件两个产品。已完成：C++ / Java 字段注释补全；进行中：接入文档、逆投影示例、设备矩阵。缺：畸变表交付形式、前端组件 AR API 文档 |

### C7 详细解答：视锥中心点 / 像素反算经纬度

#### 问题本质

ISV 想做两件事：
1. **视锥中心点 GPS**：画面正中心对应的地面经纬度
2. **任意像素反算经纬度**：画面上任意一点 (u,v) → GPS

数学本质是投影的**逆问题**：从 2D 像素反推 3D 世界坐标。经典 CV 难点——一个像素对应一条射线，**深度未知则无唯一解**，必须引入额外约束（地面平面假设 / DEM / 激光测距）。

#### FHSDK 能提供的数据

| 数据 | 来源 | 用途 |
|------|------|------|
| 相机姿态 yaw/pitch/roll | `subscribeArSeiInfo` → `ArSeiInfo` | 构建外参旋转矩阵 |
| 相机 GND 位置 + 原点经纬高 | `ArSeiInfo` | 外参平移 + 世界坐标原点 |
| 成像面宽/高/焦距 | `subscribeFovSeiInfo` → `FovSeiInfo` | 构建内参矩阵 |
| 激光测距目标 GPS | OSD `measure_target_latitude/longitude/altitude` | 画面中心点 GPS（仅激光激活时有效） |
| DRC OSD `liveview_world_region` | DRC 链路 camera OSD | 可能是视锥地面投影区域（待确认语义） |

#### 解法层级

| 精度 | 方法 | 需要什么 | 谁有实现 |
|------|------|----------|---------|
| **粗略** | 假设地面平坦 + 飞行高度已知，射线与平面求交 | ArSeiInfo + FovSeiInfo + altitude | ISV 自研（~50 行代码） |
| **中等** | 在粗略基础上加畸变校正 | + 畸变 k1~p2 表 | ISV 自研 + DJI 提供畸变表 |
| **精确** | 射线与 DEM 地形求交 | + DEM 高程瓦片 | dji-arcore `decodeTerrainGeoPos` |

#### 各端已有的逆投影实现

| 端 | 正向（GPS→屏幕） | 逆向（屏幕→GPS） |
|---|---|---|
| CSDK PinPoint | ✅ `project_gps_point_to_liveview` | ❌ 无 |
| dji-arcore | ✅ `toScreen` | ✅ `getPointFromScreenPercent(x, y, distance)`、`decodePlaneGeoPos`（平面）、`decodeTerrainGeoPos`（DEM） |
| FHSDK | ❌ 无投影 API | ❌ 无 |
| 前端 PaaS 组件 | ✅ ArCore WASM 内置 | ✅ ArCore WASM 内置（是否暴露给 ISV 待确认） |

#### ISV 自研逆投影的步骤（平面假设，无畸变）

```
输入：像素坐标 (u, v)（归一化 [0,1]），ArSeiInfo，FovSeiInfo，假设地面高度 h_ground

1. 内参反算 → 相机系射线方向：
   x_cam = (u - 0.5) / fx     // fx = focal_length / fov_horizontal
   y_cam = (v - 0.5) / fy     // fy = focal_length / fov_vertical
   z_cam = 1.0
   ray_cam = normalize([x_cam, y_cam, z_cam])

2. DJI 轴重映射（相机系→云台系）：
   ray_gim = [ray_cam.z, ray_cam.x, ray_cam.y]

3. 外参旋转 → 世界系（NED）射线方向：
   q = quaternion_from_euler(yaw, pitch, roll)
   ray_ned = q * ray_gim

4. 射线与地面平面求交（地面在 Z_ned = (camera_altitude) 下方）：
   t = camera_altitude / ray_ned.z    // Z 向下为正
   point_ned = [ray_ned.x * t, ray_ned.y * t]  // 北/东方向偏移（米）

5. NED 偏移 → GPS：
   lat = origin_lat + point_ned.x / 6371393 * (180/π)
   lng = origin_lng + point_ned.y / (6371393 * cos(origin_lat_rad)) * (180/π)
```

注意：步骤 2 的轴重映射与 CSDK `liveview_ar.cpp` 的 `pos_tar2cam` 反向操作对应（CSDK 是 y→x, z→y, x→z，逆向则是 z→x, x→y, y→z）。

#### 对 ISV 的标准回答

1. **最简单的"视锥中心点"**：设备有激光测距时，直接用 OSD `measure_target_latitude/longitude/altitude`（设备已算好，无需自研）
2. **通用方案（无激光 / 任意像素）**：用 SEI 数据自研逆投影（见上方步骤），精度取决于是否加畸变 + 地面是否平坦
3. **前端 PaaS 组件用户**：ArCore WASM 内置 `getPointFromScreenPercent`，如果组件暴露该 API 则直接可用
4. **FHSDK 能力边界**：给数据（外参 + 内参动态部分），不给投影算法。ISV 需自研或等组件正式暴露逆投影 API

#### 待确认

- DRC OSD `liveview_world_region` 的具体语义（是否就是视锥四角地面投影？）
- 前端 PaaS 组件是否对 ISV 暴露了 ArCore 的 `getPointFromScreenPercent` API
- U5：OSD 中是否有"裁剪对应关系"隐藏参数（联云提出的 AI 框 GPS 偏差问题）

### C2 详细解答：SEI 怎么取

**核心认知**：SEI 嵌在 H.264/H.265 码流的 NAL 层中，**每帧都有**（不限 I 帧）。设备固件编码时写入 AR/AI/Assistant/EIS，Pilot 推流时追加 FOV + 端到端时延。只要码流不被转码破坏，SEI 就一直在。

**标准路径（推荐）**：

```java
ILiveModule live = sdk.getLiveModule(liveInfo);
live.start(callback);
live.subscribeArSeiInfo(info -> { /* 每帧位姿 ~25fps */ });
live.subscribeFovSeiInfo(info -> { /* 每帧 FOV */ });
```

FHSDK 内部完成 NAL 拆包 + TLV/JSON 解析 + 去竞争字节，ISV 无需关心协议细节。

**ISV 自建流媒体场景**：

| 转发方式 | SEI 是否保留 | 说明 |
|----------|-------------|------|
| **透传 / remux**（不重新编码） | ✅ 保留 | 码流 NAL 不变，SEI 原样存在 |
| **转码**（重新编码） | ❌ 丢失 | 转码器不会保留原始 DJI ext SEI |

**关键提醒**：ISV 自建流媒体转发时必须确保码流 pass-through（透传），不能转码。一旦重新编码，SEI 就没了。

**RTSP 点播（录像回放）场景**：

设备 SD 卡录像文件（MP4/MOV）是否包含 DJI ext SEI，**目前无代码证据确认**。ISV 实测反馈"RTSP 点播拿不到 SEI"，可能原因包括：
- 设备录像编码时未将 DJI ext SEI 写入媒体文件
- RTSP 服务器 remux 时剥离了 SEI NAL
- ISV 没有从点播码流中主动解析 SEI

此项需进一步确认设备固件的录像行为。**建议对外口径**：SEI 为实时直播能力，录像点播不承诺 SEI 可用。

**前端 PaaS 组件与 Java SDK 的 SEI 能力对比**：

ISV 同时拥有两个 SDK，SEI 能力互补：

| SDK | 能否获取 SEI 原始数据 | 能否做 AR 绘制 | 适用场景 |
|-----|---------------------|--------------|----------|
| **Java SDK（FHSDK）** | ✅ `subscribeArSeiInfo` / `subscribeFovSeiInfo` 等 | ❌ 需自研投影 | 后端数据消费、自研算法、转发给自有前端 |
| **前端 PaaS 组件（uranus-cockpit-app）** | ❌ 不直接暴露 SEI 结构体给 ISV | ✅ 内置 AR 点线面绘制（ArCore WASM） | 在直播画面上叠加 Pin/线/面标注 |

前端组件内部链路：RTC SDK 回调 → `parseRawSEIData` → `transformSeiDataForRender` + `HostDistortParam` → `ArInstance._onSeiV2` → Canvas 绘制。ISV 通过组件 API 设置 Pin/Line/Polygon 的 GPS 坐标，组件自动完成投影和绘制。

**注意**：前端组件启动时会发 `ar_info_switch: 1` 指令给设备（`openDroneSeiMessageSend`），SEI 需要设备端开启才推送。

**对外口径**：
- 只需 AR 标注绑定 → 用**前端 PaaS 组件**，不需要处理 SEI
- 需要 SEI 原始数据做自研算法 → 用 **Java SDK**
- 两者可组合使用
- 不教 ISV 自己写 NAL parser，不默认开放 TLV 协议文档
- 自建流媒体转发必须**保持码流透传（不转码）**，否则 SEI 丢失

### C3 详细解答：哪代设备/哪个固件支持 SEI

从多端代码（Pilot `camera_*.json`、DJI 上云API 2前端 `parse-data.ts` / `sei.ts`、FHSDK `live_module_impl.cpp`）可推导出较完整的机型支持矩阵。

#### Pilot 侧：ArOsdVersion（决定 SEI 位姿来源）

Pilot 按 `camera_*.json` 中 `arInfo.arOsdVersion` 选择位姿获取方式（默认值 = 3）：

| ArOsdVersion | 位姿来源 | 帧同步 | 适用机型 |
|---|---|---|---|
| **V3**（默认） | 视频帧 `extensionSei`（帧级 SEI） | ✅ | M4E、M4T、M4D、M4TD、CM82/83/84、PM440/EA340 |
| **V2** | 飞控 `CameraARInfoMsg` 推送 | ❌ 非帧级 | M3/M10 系（camera_100） |
| **V1** | 老 OSD `DeviceInfo` | ❌ | X20(camera_31)、CM50(camera_50) |

#### DJI 上云API 2前端侧：`parseRawSEIData` 解析路径

| 机型 | 解析路径 | SEI 格式 |
|---|---|---|
| M10E / M10T (EA220) | 主码流 `transformUtf8ToJson`；辅助影像 `decodeUtf8 → TLV` | JSON |
| M3E / M3T / M3TA (WM265) | `transformUtf8ToJson` | JSON |
| M4E / M4T / M4D / M4TD / PM440 | `transformSeiBinaryToJson`（标准二进制 TLV） | 二进制 |
| EA340T | `parseFullNalSeiData`（需自拆 NAL + 去竞争字节） | 二进制（完整 NAL） |
| FPV 码流 | `return`（跳过） | ❌ 不解析 |

#### FHSDK 侧：`payload_index` 分支

| payload_index | 机型 | 格式 | 状态 |
|---|---|---|---|
| `80-0-0` / `81-0-0` | M3D/3TD (EA220/M10) | JSON | ✅ |
| `66-0-0` / `67-0-0` / `129-0-0` | M3E/3T/3TA (WM265) | JSON | ✅ |
| `39-0-7` | FPV | — | ❌ 跳过 |
| `165-0-7` | Dock Live（机场画面） | — | ❌ 跳过 |
| 其余所有 | M4 系、EA340 等 | 二进制 TLV | ✅ |

#### 综合结论（对 ISV 的回答）

| 设备代际 | 代表机型 | SEI 支持 | SEI 帧同步 | 备注 |
|----------|---------|---------|-----------|------|
| **M3 系（二代机场）** | M3E/M3T/M3TA、M10E/M10T | ✅ | ❌（Pilot V2，飞控推送非帧级） | JSON 格式 SEI |
| **M4 系（三代机场）** | M4E/M4T/M4D/M4TD | ✅ | ✅（Pilot V3，帧级 SEI） | 二进制 TLV |
| **EA340（网联三代）** | EA340/EA340T | ✅ | ✅ | 二进制，RTC SDK 给完整 NAL |
| **老机型** | X20、CM50 | ⚠️ | ❌（Pilot V1，老 OSD） | 不走 SEI 路径 |
| **FPV / Dock Live** | 所有机型的 FPV 或机场画面码流 | ❌ | — | 代码硬编码跳过 |

#### 仍需产品/测试确认（U1 待办）

- 各机型从**哪个固件版本**开始支持 SEI（代码不做版本校验，无法从代码推断）
- 机场一代/二代/三代的**产品命名**与上述技术代际的精确对应
- M3 系的 V2 路径（飞控推送）频率是多少、是否满足 ISV 需求
- 飞控收到 `ap_ar_aei_info_enable_0=1` 后具体行为（见下方开关链路分析）

#### `ar_info_switch` 开关链路（代码验证）

**SEI 不是默认就推的，需要通过 shadow 设备影子主动开启。**

完整链路：

```
前端 PaaS 组件（ArCanvasContainer.vue）
  → openDroneSeiMessageSend()
    → POST /device/api/v1/service/shadows
      → body: { state: [{ desired: { ar_info_switch: 1 } }], device_sn, uuid }
        ↓ 后端 Shadow 服务（影子设备）
        ↓ MQTT 下发给机场
        ↓
机场 MC（industry_mission_center / dji_device_hub）
  → ARInfoFeatureImpl::SetOn(true)
    → protocol_setARInfoOn_v1(push_on=true)
      → FCConfigHelper.SetConfig(key="ap_ar_aei_info_enable_0", value=1)
        → DUSS 指令：写飞控 hash param
          ↓
设备飞控（FC）
  → 收到配置写入，开启 AR SEI 推送
  → 后续每帧编码时写入 ext SEI NAL
```

代码证据：

| 层 | 文件 | 关键代码 |
|---|---|---|
| 前端 | `uranus-cockpit-app/src/api/command-flight.ts:39` | `ar_info_switch: 1` 通过 shadow API |
| 类型定义 | `uranus-utils/types/device-setting.ts:59` | `ar_info_switch?: number; // 0关1开` |
| device_hub | `ar_info_feature_impl.cc:45` | `abs_->protocol_setARInfoOn(mode)` |
| 协议层 | `protocol_setARInfoOn_v1.cc:22` | `ConfigValueU08(push_on)` 写 FC config |
| FC config key | `fc_config_keys.cc:1006` | key=`"ap_ar_aei_info_enable_0"`, U08, min=0, max=1, default=1 |

**待圈内确认**：
- 飞控实际默认值是 0 还是 1（config 定义 default=1，但出厂固件可能不同）
- 该开关控制的是仅 AR SEI（type=4）还是所有 ext SEI（AR+AI+FOV+EIS+Assistant）
- FHSDK Java SDK 用户是否也需要调这个接口开启 SEI，还是拉流时自动开启

---

## 4. 第三节 逐家明细 ①～⑬ 逐条解答

### 4.1 SEI / 帧级定位类

#### ① 场景：机载 AI 计算平台 SEI 接入

**⚠️ 以下回答需进一步确认，基于开发者网站公开信息 + 代码推断。**

DJI 目前有两条机载 AI 识别结果上云的路径：

| 路径 | 方式 | 识别结果传输 | FHSDK 可获取 | 前端组件可渲染 |
|------|------|------------|-------------|--------------|
| **AI Inside（算力开放平台）** | 自训练模型部署到 M4 系列内置芯片 | 设备固件写入码流 **AI SEI（type=7）** | ✅ `subscribeAiSeiInfo` | ✅（AI 框叠加） |
| **妙算 3 + Payload SDK** | PSDK 应用运行在妙算，获取相机图像做推理 | PSDK "AI 识别结果回传显示"能力（具体通道待确认：可能是 SEI 注入或独立数据通道） | ⚠️ 待确认 | ✅ "支持DJI 上云API 2渲染妙算3 AI 应用识别结果"（PSDK 发布日志明确提及） |

**锋则原始诉求的回答**：

| 项 | 答复 |
|----|------|
| 妙算识别结果能否接入上云座舱 | ⚠️ **现在应该可以**——PSDK 3.9+ 已支持"DJI 上云API 2渲染妙算3 AI 应用识别结果"（2025-05 发布日志）。需确认：ISV 用的是妙算几代？是否升级了 PSDK 版本？ |
| 第三方边缘盒子（非妙算）识别结果 | ❌ **不走 SEI 通道**。FHSDK 不支持注入第三方 SEI 到设备码流。替代方案：算法结果走业务 API/WebSocket → 前端自行合成叠加 |
| FHSDK 侧能否拿到妙算 AI 结果 | ⚠️ 待确认：如果妙算 AI 结果走 AI SEI（type=7）注入码流，则 `subscribeAiSeiInfo` 可拿到；如果走独立通道则 FHSDK 拿不到 |

**待确认项**：
- 妙算 3 AI 结果回传的具体技术通道（SEI 注入 vs PSDK 小数据传输 vs 其它）
- AI Inside（机载算力）识别结果是否同时出现在 AI SEI 和其它通道
- ISV 如果用非 DJI 算力（第三方盒子），是否有标准接入方式

#### ② 场景：影像 GIS 贴图与快门同步

| 项 | 答复 |
|----|------|
| 每帧画面对应位置 | ⚠️ `subscribeArSeiInfo` 提供**高频相机位姿+原点经纬度**，优于 1Hz OSD；**无「快门同步」语义保证** |
| API 直接给 POV / 视锥中心 | ❌ FHSDK **无 POV 字段**；可用 SEI 自算中心射线，或 OSD 激光测距（打点目标，非任意像素） |
| OSD 提到 2Hz | ❌ **U2**，FHSDK 不能解决 |
| 「还要 ISV 再算一次」 | ⚠️ FHSDK 只给原始 SEI；**地图贴图/投影** SDK 不做，需 ISV 或未来 AR 组件 |
| 推荐路径 | Java：`subscribeArSeiInfo` + `subscribeFovSeiInfo` → 后端转发 → 前端 GIS 引擎 |

#### ③ 场景：视频叠加 AI 结果与地块投射

| 项 | 答复 |
|----|------|
| 帧级镜头/飞机位姿缺失 | ✅ 走 FHSDK SEI 可解决**数据面**（相对 2s OSD） |
| ① 地块边框投射 | ⚠️ 数据够，**投影与绘制 ISV 自研** |
| ② 任意像素反算经纬度 | ⚠️ 无畸变/EIS 封装，**精度有限** |
| ③ AI 车辆跟踪 | ✅ `subscribeAiSeiInfo`；跟车应用 SEI 帧率，非 OSD |
| 自建 WebRTC / 不用声网 | ⚠️ **须验证 FHSDK `LiveModule` 拉流**；纯自建转码且不经过 FHSDK → **不承诺 SEI** |
| 推荐 | 195 台规模建议统一 **FHSDK 标准拉流 + Java SEI**；勿并行自研 parser |

#### ④ 场景：实时视频叠加算法结果

| 项 | 答复 |
|----|------|
| OSD 2s 太慢 | ✅ 改用 `subscribeArSeiInfo` 等 |
| 不知道怎么接 SEI | ✅ 培训交付：**FHSDK LiveModule 示例**（不必先讲二进制） |
| 自建 ZLM | ⚠️ 若视频不经过 FHSDK RTC，需自建解析 — **非标准支持** |

#### ⑤ 场景：视频畸变校正与 OSD 异常处理

| # | 诉求 | 答复 |
|---|------|------|
| 1 | AI 框 GPS 偏差、裁剪对应 | ⚠️ **可解但需 ISV 自研**。`subscribeAiSeiInfo` 给 AI 框归一化坐标（万分比 cx/cy），要反算 GPS 需走 C7 逆投影流程：将 (cx/10000, cy/10000) 当作归一化像素 → 内参反算射线 → 旋转到世界系 → 与地面求交。偏差来源：①未加畸变校正（广角边缘偏差大）②未考虑 EIS 裁剪偏移。EIS 裁剪信息在 `subscribeEisSeiInfo` 中已有（归一化 0~65535）。**畸变数据**：DJI 内部有现成标定值（`HostDistortParam` / `camera_*.json`），但涉及公司内部数据，**是否提供给 ISV 需领导决策**；若不提供，ISV 可通过 OpenCV `calibrateCamera()` + 棋盘格自行标定 |
| 2 | SEI 跟随延迟 | ⚠️ 已识别为 FHSDK 技术债务。当前 SEI 被 Post 到 Main 线程异步处理，与视频帧无统一 timestamp。改进方向：提供静态 `SeiParser.parse(frameData)` 让用户同步解析（见讨论备忘第5节）。当前 ISV 可用时间近似对齐或做平滑插值 |
| 3 | 语音操控 0.1m 精度停 | ❌ 不属于 SEI/CV 范畴。靠高频 OSD 判停不可行（0.5Hz 精度不够）。建议改用 `FlyTo` 等指点飞行 API，由飞控做精确停靠 |
| 4 | OSD 漂移/中断 | ❌ **U7** 固件稳定性问题，非 SDK 可修复 |

#### ⑥ 场景：RTSP 点播取流

| 项 | 答复 |
|----|------|
| RTSP 拿 SEI | ❌ **不支持**。FHSDK SEI 绑 **RTC 直播** |
| 改法 | 改为 API 2 + **FHSDK `LiveModule.start`**；DJI 上云API 2 mock 仅作DJI 上云API 2场景参考 |
| 算法结果回传 | ❌ 非 SEI 范畴，需业务 API 设计 |

#### ⑦ 场景：视频与定位数据同步

| 项 | 答复 |
|----|------|
| 两通道不同步 | ⚠️ 统一走 **FHSDK 拉流 + SEI 回调**；若仍 OSD+视频分离，需 ISV 打时间戳 |
| i 帧无坐标 | ✅ 核对机型固件（**U1**）；用 `subscribeArSeiInfo` 验证 |
| 结构化数据 | ✅ FHSDK 直接给 `ArSeiInfo` 等结构体，无需自解析 JSON |

#### ⑧ 场景：开源文档与技术支持

| 项 | 答复 |
|----|------|
| SEI 踩坑 | ⚠️ 培训交付 FAQ + FHSDK Demo；**不建议**默认公开圈内 TLV 手册 |
| 开源 SDK | FHSDK 为商业 SDK；协议是否对外见 **1.3 合规** |

#### ⑨ 场景：SEI 解码集成

| 项 | 答复 |
|----|------|
| 机会 | ✅ **FHSDK 可省掉三方截帧回传**；Java 订 SEI 即可 |
| 培训 | 作为「标准路径」典型案例 |

### 4.2 OSD 频率 / 负载 / 稳定性类

#### ⑩ 场景：高频 OSD 数据采集（电力巡检）

| 项 | 答复 |
|----|------|
| 正常航线临时高频 OSD（10~20Hz） | ❌ FHSDK **不能**提供。MQTT OSD 2s 定时器硬编码在设备端，SDK 无法调高 |
| ISV 真实需求分析 | ISV 要的不是"实时高频 OSD"，而是**事后精确轨迹回溯**（炸机定位/理赔） |
| SEI 能否替代 | ❌ **SEI 不适用此场景**。SEI 是实时直播数据，不存储到设备/云端。飞行中如果没有拉流（或网络断了），SEI 数据就没了 |
| 替代方案 | ① **DJI 上云API 2飞行记录功能**（事后回溯轨迹）② **OSD**（0.5Hz 粗粒度轨迹）③ **DRC 高频 OSD**（3Hz，需建 DRC 链路，飞行中实时缓存可获得更高精度轨迹）④ ISV 自行在后端缓存 SEI 数据做轨迹记录（需飞行中拉流） |
| 状态 | **U4 待办**，归入"全生命周期—丢失场景如何找回"议题 |

#### ⑪ 众芯汉创（OSD 降载、可配置频率）

| 项 | 答复 |
|----|------|
| 按需订阅 | ⚠️ FHSDK 通过 NC **按主题监听**；是否可调频看 **NC/平台配置**，SDK 文档需写清 |
| 规模化 | 培训 **C6** + NC 最佳实践；FHSDK 无 MQTT 正则能力（平台侧） |

#### ⑫ 某测绘机构（固件升级枚举）

| 项 | 答复 |
|----|------|
| 5-8 更新后 OSD 收不到 | ❌ **U3** 后台枚举与兼容策略 |
| FHSDK 侧 | 只能保证 NC 监听 API 稳定；**枚举变更感知**需平台通告机制 |

### 4.3 开发交流会 ⑬

| 诉求 | 答复 |
|------|------|
| 自部署流媒体后怎么拿 OSD | OSD 走 MQTT NC 通道，与流媒体部署方式**无关**。`listenAircraftPayloadsOsdInfo` 等接口不受影响。自部署流媒体拿 SEI：须保证码流透传（见 C2） |
| 高频 OSD 接口 | ❌ 见 C1/C4——MQTT OSD 0.5Hz 硬编码不可调。需要帧级位置走 SEI |
| SEI 1/2/3 代支持 | ⚠️ C3 已从代码推导出机型矩阵：M3 系 ✅（JSON SEI，非帧级），M4 系 ✅（二进制 TLV，帧级），EA340 ✅。**固件版本号仍需 U1 确认** |
| 6 月底专题 | 建议以本文 C1~C9 为讲义骨架：三层级频率 → 接入路径 → 设备矩阵 → 逆投影算法 → 能力边界 |

---

## 5. 第四节 U1～U8 未决项——FHSDK 能否代答

| 编号 | 问题 | FHSDK 能否代答 | 当前口径 |
|------|------|----------------|----------|
| **U1** | SEI 1/2/3 代 + 固件矩阵 | ❌ | 培训前**必须**产品/测试给出；FHSDK 仅有 `payload_index` 实现分支 |
| **U2** | OSD 1Hz 上限能否优化 | ❌ | 短期无解；引导 SEI |
| **U3** | 固件升级 OSD 枚举兼容 | ❌ | 平台/后台/通告流程 |
| **U4** | 正常航线 10～20Hz OSD | ❌ | 无时间表 |
| **U5** | 裁剪/隐藏参数 | ❌ | 待核实；FHSDK 未暴露 |
| **U6** | 妙算 SEI / API 启动 | ❌ | 跨团队 |
| **U7** | OSD 异常稳定性 | ❌ | 固件长期项 |
| **U8** | 高频 OSD 形态 | ❌ | 平台评估中 |

---

## 6. 第五节培训材料清单——FHSDK 可承接部分

| # | 培训产出 | FHSDK 可交付 | 说明 |
|---|----------|--------------|------|
| 1 | 概念篇 OSD vs SEI | ✅ | 结合 `listenAircraftPayloadsOsdInfo` vs `subscribe*SeiInfo` |
| 2 | 接入篇（最重要） | ✅ | **FHSDK Java LiveModule 示例**，非默认「自写 NAL 解析」 |
| 2' | 拉流方式对照表 | ✅ | 见下表 |
| 3 | 设备/固件矩阵 | ❌ 依赖 U1 | FHSDK 不能先写死 |
| 4 | 频率与根因 | ✅ | |
| 5 | 高频 OSD 篇 | ✅ 概念 | 无 FHSDK API |
| 6 | 负载治理 | ⚠️ | NC 文档为主 |
| 7 | 进阶 AR | ⚠️ | 讲清 **数据有、投影无**；后端转发架构图 |
| 8 | 固件兼容 | ❌ 依赖 U3 | |
| 9 | 配套文档 | ⚠️ | 补 FAQ、Demo、字段说明；协议附录需评审 |

### 拉流方式与 FHSDK SEI（培训用对照表）

| 拉流方式 | FHSDK SEI | 说明 |
|----------|-----------|------|
| FHSDK `LiveModule.start`（RTC） | ✅ | **推荐标准路径** |
| Java 后端订 SEI → WebSocket → 前端 | ✅ | **当前前端 AR 推荐架构** |
| Web SDK `subscribeArSei`（若正式交付） | ✅ | 需产品确认 |
| DJI 上云API 2 + mock（DJI 上云API 2前端） | — | 非 FHSDK ISV 主路径，可并列介绍 |
| RTSP 点播 | ❌ | |
| 声网/自建 WebRTC，不经 FHSDK | ❌ | 不承诺 |
| 自建流媒体未解析 SEI | ❌ | |

---

## 7. 前端 AR 与交付决策（培训必讲附录）

### 7.1 为何「只有 Java」时前端仍可做 AR

```
设备 → 云端 RTC → Java FHSDK（subscribeArSeiInfo / subscribeFovSeiInfo）
                      ↓ WebSocket 或业务 REST（JSON）
                 浏览器 Canvas / WebGL（投影 + 绘制）
```

- FHSDK **不提供**投影公式与 ArCore；**提供** AR 所需输入字段（见 `message.h` `ArSeiInfo` / `FovSeiInfo`）。
- ISV 常见误解：「Java SDK = 做不了 AR」→ 应纠正为「**SEI 在服务端，AR 在客户端，中间要一条转发链**」。

### 7.2 是否教用户自解析码流？

| 策略 | 建议 |
|------|------|
| 标准 ISV | **不教**二进制 TLV；用 FHSDK API |
| 前端要强依赖浏览器拉流 | **产品决策**：正式交付 Web LiveModule，而非散发 parser |
| 自建流 ISV | 声明非标准；**不单独立项则不教**圈内 parser |
| 对外协议文档 | 默认**不发布**完整 DUSS/Confluence；以 SDK 为契约；例外走法务 |

### 7.3 PaaS 组件待确认项（对内）

- [ ] PaaS 播放器是否暴露 `subscribeArSei` 或等价事件？
- [ ] Web SDK 是否与 Java 同级 SLA？
- [ ] 培训讲义以哪条路径为准（避免接口列表写 Web 支持、对外只说 Java）

---

## 8. 常见 ISV 问法 → 推荐一句话回复

| ISV 问法 | 推荐回复 |
|----------|----------|
| OSD 太慢，能不能调到 5Hz？ | 不能。帧级位姿请用 **FHSDK SEI 回调**，不要提高 OSD 频率。 |
| 能不能把地图贴到画面上？ | FHSDK 给 **相机位姿+FOV**；贴图要你们 **自研投影** 或等 AR 组件；可参考激光测距 OSD 做打点。 |
| 我们只有 RTSP，能拿 SEI 吗？ | **标准不支持**。请改用 **FHSDK 直播拉流**。 |
| 能不能给 SEI 二进制协议文档？ | 对外以 **FHSDK 结构体**为准；底层协议不默认开放。 |
| 前端怎么做 AR？ | **后端 Java 订 SEI → 推 JSON 到前端**；或咨询是否已开放 **Web LiveModule**。 |
| 一二三代机场都支持 SEI 吗？ | **以产品发布的支持矩阵为准（U1）**；FHSDK 对 FPV/Dock Live 等镜头会跳过 SEI。 |

---

## 9. 文档修订记录

| 日期 | 说明 |
|------|------|
| 2026-06-15 | 初版：对照 ISV 汇总文档逐条答复，并入 FHSDK 能力边界与交付决策讨论 |

---

## 10. 内部待办（培训前建议闭环）

1. **U1**：产出 SEI 设备/固件矩阵（产品/测试），培训第三节才能写死。
2. **产品**：确认 Web SDK / PaaS 组件 SEI 是否对外及文档入口。
3. **研发**：准备 Java `LiveModule` + SEI 订阅 + WebSocket 转发最小 Demo。
4. **文档**：在 `dji_flight_hub_sdk_manual` 增加「SEI 与 OSD 选型」「前端 AR 架构」章节。
5. **法务/信安**：若坚持发布 SEI 二进制附录，单独评审；默认不发布圈内 Confluence/DUSS 链接。
