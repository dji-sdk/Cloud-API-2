# ISV 开发者培训：SEI 与 OSD —— 如何获取帧级位置数据

> 培训形式：线上分享会  
> 目标受众：上云 API 2 ISV 开发者（GIS 应用、视频识别、巡检平台等）  
> 核心问题：**怎么拿到每一帧画面对应的、准确的、足够高频的位置数据**

---

## 目录

1. [为什么讲这个主题](#1-为什么讲这个主题)
2. [三层位置数据概览](#2-三层位置数据概览)
3. [SEI 是什么](#3-sei-是什么)
4. [SEI 包含哪些数据](#4-sei-包含哪些数据)
5. [怎么获取 SEI](#5-怎么获取-sei)
6. [设备支持情况](#6-设备支持情况)
7. [常见应用：视锥中心点与像素反算经纬度](#7-常见应用视锥中心点与像素反算经纬度)
8. [镜头畸变参数参考表](#8-镜头畸变参数参考表)
9. [高度基准与坐标系说明](#9-高度基准与坐标系说明)
10. [常见误区与 FAQ](#10-常见误区与-faq)
11. [总结](#11-总结)

---

## 1. 为什么讲这个主题

ISV 反馈最集中的痛点：

> "OSD 实时性太差，2 秒一次，拿到的 OSD 还停留在上一秒的位置。"  
> "这帧视频对应的镜头/相机角度位姿现在缺失，基于 OSD 信息 2 秒一帧很难对齐，整套功能就被删掉了。"  
> "SEI 试半天不行，不知道怎么接。"

**根因**：位置数据和视频画面走不同通道、频率不匹配。

**今天要解决**：把已有能力讲清楚，给可落地的接入路径。

---

## 2. 三层位置数据概览

位置数据不止一种，有三个层级可选：

| 通道 | 频率 | 前置条件 | 典型用途 |
|------|------|----------|---------|
| **MQTT OSD** | 0.5Hz（2s 一次，固定） | 无需额外操作，设备在线即推 | 设备状态监控面板 |
| **DRC OSD** | 默认 3Hz，**可配 1~30Hz** | 需建立 DRC 链路（进入指令飞行控制模式） | 操控反馈、中高频轨迹记录 |
| **SEI** | ~30fps（每帧都有） | 需拉流 | **画面 + 位置融合**（AR、地块贴图、像素反算；SDK 层非严格帧同步） |

**判断流程**：

```
要对齐视频画面的位置（高频位姿）？
  → 用 SEI（~30fps；SDK 层非严格帧同步，见 §3）

需要中高频位置但不想拉流？
  → 建 DRC 链路，配 osd_frequency（最高 30Hz）

只看设备状态（电量、在线、GPS 粗略位置）？
  → 用 MQTT OSD（0.5Hz 够用）

不要试图把 MQTT OSD 提到高频来对齐画面 —— 这条路不通
```

### 2.1 MQTT OSD

**什么是 MQTT OSD**：设备通过 MQTT 信令通道定时上报的完整状态消息，包含飞机 GPS、高度、速度、电量、云台角度、激光测距等。

- **频率**：0.5Hz（每 2 秒一次），设备端固定，无法调高
- **无需额外操作**：设备在线自动推送
- **数据量大**：一条消息含几十个字段，覆盖设备全量状态
- **适用场景**：设备列表面板、地图轨迹（粗粒度）、电量/信号监控

**FHSDK 接口**：`listenAircraftPayloadsOsdInfo` 等 Notification Center 监听接口。

### 2.2 DRC OSD

**什么是 DRC OSD**：通过 DRC（Dock Remote Control）链路上报的高频状态消息。DRC 链路是一条独立的 MQTT 连接，专用于指令飞行控制模式下的实时交互。

- **频率**：默认 3Hz，**可在进入 DRC 模式时配置为 1~30Hz**
- **前置条件**：需先调用 `drc_mode_enter` 建立 DRC 链路
- **数据内容**：飞机 OSD（位置/姿态/速度）+ 相机 OSD（云台角/FOV/激光测距/红外温度）+ HSI + 电池等
- **适用场景**：指点飞行操控、高频轨迹记录（如高危航线事后分析）

**频率配置方式**：在 `drc_mode_enter` 指令的 `data` 中指定：

```json
{
  "data": {
    "osd_frequency": 10,
    "hsi_frequency": 1,
    "mqtt_broker": { ... }
  }
}
```

| 参数 | 含义 | 范围 | 单位 |
|------|------|------|------|
| `osd_frequency` | 飞机 OSD 上报频率 | 1 ~ 30 | Hz |
| `hsi_frequency` | HSI 信息上报频率 | 1 ~ 30 | Hz |

**DRC OSD 包含的数据**（以相机 OSD 为例）：
- 云台 pitch/yaw/roll
- 各镜头 FOV 信息（width/height/focal_length）
- 激光测距目标 GPS（`measure_target_*`）
- 红外测温数据
- 变焦倍率
- 曝光/ISO/快门参数

**DRC OSD 飞机状态字段**（`osd_info_push`）：

| 字段 | 含义 | 单位 |
|------|------|------|
| `attitude_head` | 飞机航向角 | 度 |
| `latitude` / `longitude` | 飞机经纬度 | 度 |
| `altitude` / `height` | 飞机高度 | 米 |
| `speed_x` / `speed_y` / `speed_z` | 三轴速度 | m/s |
| `horizontal_speed` / `vertical_speed` | 水平/垂直速度 | m/s |
| `gimbal_pitch` / `gimbal_yaw` / `gimbal_roll` | 云台角度 | 度 |
| `ultrasonic_height` | 超声波测高 | 0.1m |
| `home_distance` | 返航距离 | 米 |
| `wind_direction` / `wind_speed` | 风向/风速 | — |

### 2.3 SEI

**什么是 SEI**：嵌在 H.264/H.265 视频码流中的补充增强信息，随每帧视频一起传输。

- **频率**：等于视频帧率（约 30fps），每帧都有（不限 I 帧）
- **前置条件**：需拉流（FHSDK `LiveModule.start`）
- **数据内容**：相机位姿（yaw/pitch/roll）、GND 位置、原点经纬高、FOV、AI 检测框、EIS 裁剪
- **适用场景**：画面 + 位置精确融合（AR 标注、地块贴图、像素反算经纬度）

**与 OSD 的本质区别**：SEI 数据嵌在视频码流中、频率远高于 OSD；但 SDK 交付时视频帧回调与 SEI 回调**分开投递**（见 §3），不能等同于「与画面严格帧同步」。

### 2.4 三者对比总结

| | MQTT OSD | DRC OSD | SEI |
|---|---|---|---|
| 频率 | 0.5Hz 固定 | 1~30Hz 可配 | ~30fps |
| 传输通道 | MQTT 信令 | DRC MQTT（独立连接） | 视频码流 |
| 与视频帧的关系 | 异步，不对齐 | 异步，不对齐 | 码流内同帧携带；**SDK 回调层均非严格对齐**（见 §3） |
| 是否需要拉流 | ❌ | ❌ | ✅ |
| 是否需要额外建连 | ❌ | ✅（DRC 链路） | ❌（拉流即有） |
| 数据丰富度 | 全量设备状态 | 飞机+相机+HSI+电池 | 相机位姿+FOV+AI+EIS |
| 可存储性 | ✅ 天然可存 | ✅ 天然可存 | 平台默认不持久化；飞行中持续拉流并后端缓存可得高密度轨迹，依赖链路与存储方案 |

---

## 3. SEI 是什么

**SEI（Supplemental Enhancement Information）** 是 H.264/H.265 视频编码标准中的补充增强信息。

在 DJI 无人机场景中，设备固件在编码视频时将相机位姿、FOV 等数据嵌入到每帧的 SEI NAL 单元中，随码流一起传输。

**关键特性**：

- **每帧都有**（不限 I 帧，P 帧也有）
- **码流层**：SEI 嵌在同一视频帧的 NAL 数据中
- **频率等于视频帧率**（约 30fps）
- **不需要额外信令通道**——数据就在视频码流里

**帧同步说明（三层，培训必讲）**：

| 层级 | 说明 |
|------|------|
| **码流层** | 设备编码时 SEI 与视频帧写在同一帧 NAL 中 |
| **SDK 交付层** | RTC / FHSDK 将**视频帧回调**与 **SEI 回调分开投递**，不保证同一 timestamp 严格对齐；各机型均存在**一定时间误差** |
| **应用层** | Web 前端 PaaS 组件可对位姿做平滑插值（见 Q9）缓解抖动，**无法从根上消除回调分离**；**FHSDK Java SDK 后续可提供带帧时间戳的同步解析接口**（规划中，非当前能力） |

因此：SEI 是「对齐画面」的**正确数据源**（频率远高于 OSD），但不要理解为「像素级严格帧同步、零误差」。

**浏览器限制**：

Web 端解析 SEI 需要读取原始编码帧，目前仅 **Chrome / Edge** 支持（基于 Chromium 的 `createEncodedStreams` API）。**Safari 和 Firefox 无法解析 SEI**，使用前端 PaaS 组件做 AR 渲染时请注意浏览器兼容性。

---

## 4. SEI 包含哪些数据

通过 FHSDK 可获取以下 5 类 SEI 数据：

### 4.1 AR SEI（相机位姿与位置）— 最常用

| 字段 | 含义 | 单位 |
|------|------|------|
| `cameraYaw` / `cameraPitch` / `cameraRoll` | 相机在 NED 坐标系下的欧拉角 | 度 |
| `cameraPosX` / `Y` / `Z` | 相机在 GND 坐标系下的位置（北/东/下） | mm |
| `latitude` / `longitude` | GND 坐标系原点经纬度 | 整型编码 |
| `gndOriginZ` | GND 原点 Z 轴高度 | mm，向下为正 |
| `altitude` | 相对起飞点高度 | mm，向上为正 |

**经纬度转换公式**：
```
lat_deg = latitude × (180.0 / 2^32)
lng_deg = longitude × (360.0 / 2^32)
```

**GND 坐标系**：以起飞点为原点，北为 X+，东为 Y+，向下为 Z+。

**FHSDK 使用说明**：对外请使用 `com.dji.flighthubsdk.bean.ArSeiInfo`（回调参数类型）。角度字段**已是度**；经纬度为整型编码时需按上式换算。**无需**按码流内 JSON 原始字段再乘机型系数（SDK 已完成解析与单位转换）。

### 4.2 FOV SEI（相机视场参数）

| 字段 | 含义 | 单位 |
|------|------|------|
| `fovHorizontal` | 有效成像宽度 | μm |
| `fovVertical` | 有效成像高度 | μm |
| `curActualFocalLength` | 当前实际焦距（÷1000 = mm） | 整型 |

**注意**：字段名虽含"fov"，但不是角度值，是传感器物理尺寸。视场角需自行计算：
```
水平视场角 = 2 × atan(fovHorizontal / (2 × curActualFocalLength)) × 180 / π
```

**从 FOV 算内参矩阵**：
```
fx = curActualFocalLength / fovHorizontal
fy = curActualFocalLength / fovVertical
cx = 0.5
cy = 0.5
```

### 4.3 AI SEI（AI 检测目标）

| 字段 | 含义 | 说明 |
|------|------|------|
| `targetId` | 目标跟踪 ID | 同一目标跨帧保持一致 |
| `targetType` | 目标类型 | 0=人, 1=车, 2=船, 3=运动目标 |
| `targetCx` / `targetCy` | 目标中心坐标 | 归一化万分比（÷10000 = 画面百分比） |
| `targetW` / `targetH` | 目标框尺寸 | 归一化万分比 |
| `targetDistance` | 目标距离 | mm |

### 4.4 EIS SEI（电子防抖裁剪区域）

四角坐标表示防抖后有效画面在原始传感器中的裁剪位置，值为归一化 0~65535（÷65535 = 百分比）。

### 4.5 Assistant SEI（辅助影像朝向）

辅助影像的自动朝向跟随状态和角度值。

---

## 5. 怎么获取 SEI

### 5.1 方式一：FHSDK Java SDK（推荐，获取原始数据）

```java
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.ArSeiInfo;
import com.dji.flighthubsdk.bean.FovSeiInfo;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.core.LiveModule;

// 1. 创建直播模块
LiveModule live = ...; // 由 SDK 按设备/镜头创建

// 2. 开始拉流
live.start(new FlightHubCallback.ResultCallbackWithError() {
    @Override
    public void onResult(FlightHubError err) {
        if (err.getErrorCode() == FlightHubSdkErrorCode.SUCCESS) {
            System.out.println("拉流成功");
        }
    }
});

// 3. 订阅 SEI 数据（回调参数为 bean 包下的 ArSeiInfo / FovSeiInfo）
live.subscribeArSeiInfo(new FlightHubCallback.ArSeiInfoCallback() {
    @Override
    public void onResult(ArSeiInfo arSeiInfo) {
        double yaw = arSeiInfo.getCameraYaw();
        double pitch = arSeiInfo.getCameraPitch();
        int altitude = arSeiInfo.getAltitude();
        // 处理位姿数据...
    }
});

live.subscribeFovSeiInfo(new FlightHubCallback.FovSeiInfoCallback() {
    @Override
    public void onResult(FovSeiInfo fovSeiInfo) {
        long width = fovSeiInfo.getFovHorizontal();
        long focalLen = fovSeiInfo.getCurActualFocalLength();
        // 处理 FOV 数据...
    }
});
```

**适用场景**：后端数据消费、自研投影算法、转发给自有前端做渲染。

### 5.2 方式二：前端 PaaS 组件（直接 AR 渲染）

前端组件内置了完整的 SEI 解析 + AR 投影 + Canvas 绘制能力。ISV 只需通过组件 API 设置 GPS 坐标点，组件自动完成画面上的 AR 标注绑定。

**适用场景**：在直播画面上叠加 Pin 点、航线、地块边界等 AR 标注。

**无需自己处理 SEI**——组件内部已完成。

**培训前待办（对内）**：

- [ ] 组件正式产品名称、对外文档与集成入口
- [ ] 对 ISV 暴露的 API 清单（Pin/线/面、是否含逆投影等）
- [ ] 与 Java SDK 组合部署的推荐架构图
- [ ] 浏览器限制（仅 Chrome/Edge，见 §3）的对外说明位置

### 5.3 两种方式对比

| | Java SDK | 前端 PaaS 组件 |
|---|---|---|
| 获取 SEI 原始数据 | ✅ | ❌（组件内部消费） |
| AR 点线面绘制 | ❌ 需自研 | ✅ 内置 |
| 自研投影/算法 | ✅ | ❌ |
| 典型架构 | 后端处理 → WebSocket → 自有前端 | 直接用组件 |

两者可组合使用。

### 5.4 自建流媒体场景注意事项

如果使用自建流媒体转发：

| 转发方式 | SEI 是否保留 |
|----------|-------------|
| **透传 / remux**（不重新编码） | ✅ 保留 |
| **转码**（重新编码） | ❌ 丢失 |

**关键**：转发时必须保持码流 pass-through，不能转码。一旦重新编码，SEI 就没了。

---

## 6. 设备支持情况

### 已确认支持 SEI 的机型

| 设备系列 | SEI 格式 | 与画面严格对齐 |
|---------|---------|----------------|
| **Matrice 4 系列**（M4E、M4T、M4D、M4TD） | 二进制 TLV | ⚠️ 非严格同步，存在一定误差 |
| **Matrice 3D / 3TD**（大疆机场 2） | JSON | ⚠️ 同上；部分机型位姿更新路径与视频帧进一步解耦，误差可能更大 |
| **Mavic 3 行业系列**（M3E、M3T、M3TA） | JSON | ⚠️ 同上 |
| **EA340 系列**（大疆机场 3） | 二进制 | ⚠️ 非严格同步，存在一定误差 |

### 不支持 SEI 的码流

| 码流类型 | 说明 |
|---------|------|
| FPV 画面 | 机身相机，不含业务 SEI |
| 机场自身画面（Dock Live） | 机场监控画面，不含 SEI |

### 注意事项

- 具体固件版本支持情况请以产品发布文档为准
- **SEI 推送开关**：部分场景需通过设备影子 API 设置 `ar_info_switch: 1`。**默认是否开启、Java 纯 SDK 用户是否必须自行调用，待产品/圈内逻辑确认**（培训口径以届时发布说明为准）

```json
POST /device/api/v1/service/shadows
{
  "state": [{ "desired": { "ar_info_switch": 1 } }],
  "uuid": "项目ID",
  "device_sn": "设备SN"
}
```

前端 PaaS 组件可能在直播启动时自动调用上述接口；纯 Java SDK 集成请以产品文档为准。

---

## 7. 常见应用：视锥中心点与像素反算经纬度

### 7.1 视锥中心点 GPS（最简方案）

如果设备有激光测距功能，OSD 中的 `measure_target_latitude` / `measure_target_longitude` / `measure_target_altitude` 直接就是画面中心点对应的地面 GPS 坐标。

**无需自己做投影计算。**

### 7.2 任意像素反算经纬度（通用方案）

当需要知道画面上任意一点对应的地面 GPS 时，需要自研逆投影。

**所需数据**：

| 数据 | 来源 |
|------|------|
| 相机位姿 yaw/pitch/roll | `subscribeArSeiInfo` → `ArSeiInfo`（已是度） |
| 相机高度、原点经纬度 | `ArSeiInfo` |
| 焦距与成像面尺寸 | `subscribeFovSeiInfo` → `FovSeiInfo` |
| 电子防抖裁剪（画面有 EIS 时建议） | `subscribeEisSeiInfo` → `EisSeiInfo` |
| 广角镜头畸变校正 | §8 畸变表（不在 SEI 中） |

**算法步骤**（平面地面假设；使用 FHSDK bean 字段，勿再按码流 JSON 解码）：

```
输入：归一化像素坐标 (u, v)，范围 [0, 1]

Step 0（建议）：按 §8 对 (u,v) 做镜头畸变校正；若开启 EIS，先按 EisSeiInfo 映射到有效成像区域

Step 1：像素 → 相机系射线
    fx = curActualFocalLength / fovHorizontal
    fy = curActualFocalLength / fovVertical
    ray_x = (u - 0.5) / fx
    ray_y = (v - 0.5) / fy
    ray_z = 1.0

Step 2：相机系 → 世界系（NED）
    用 ArSeiInfo 的 yaw/pitch/roll（度）构建旋转矩阵
    注意 DJI 相机系与 NED 的轴对应关系，勿直接当 OpenCV 默认相机系使用

Step 3：射线与地面求交（altitude 为相对起飞点高度，单位 mm → 米）
    t = (altitude / 1000.0) / ray_ned_z
    north_offset = ray_ned_x × t
    east_offset = ray_ned_y × t

Step 4：偏移 → GPS（原点经纬度需先按 §4.1 公式从整型转度）
    lat = origin_lat + north_offset / 6371393 × (180/π)
    lng = origin_lng + east_offset / (6371393 × cos(origin_lat_rad)) × (180/π)
```

**精度说明**：
- 画面中心区域精度较高
- **广角边缘务必结合 §8 畸变校正**，否则偏差明显
- 开启电子防抖时，需结合 `EisSeiInfo` 将像素映射到有效画面区域
- 地面不平坦时有误差（假设了平面地面）
- SDK 回调与视频帧非严格同步时，叠加渲染会有轻微抖动（见 §3、Q9）

---

## 8. 镜头畸变参数参考表

做精确像素反算经纬度或 AR 投影时，需要对镜头畸变进行校正。畸变参数是每个机型+镜头的**出厂标定固定值**，不在 SEI 中推送。

以下为各机型参考系数（径向 k1/k2/k3 + 切向 p1/p2），供 ISV 逆投影与 AR 使用；覆盖范围以表中机型/镜头为准，随产品迭代可能调整。

### Matrice 4 系列

| 机型 | 镜头 | k1 | k2 | k3 | p1 | p2 |
|------|------|-----|-----|-----|-----|-----|
| **M4E** | wide | -0.1026 | -0.0292 | 0 | 0 | 0 |
| **M4E** | zoom | 0 | 0 | 0 | 0 | 0 |
| **M4T** | wide | 0 | 0 | 0 | 0 | 0 |
| **M4T** | zoom | 0 | 0 | 0 | 0 | 0 |
| **M4T** | ir | 0 | 0 | 0 | 0 | 0 |

### Matrice 3 / M10 系列

| 机型 | 镜头 | k1 | k2 | k3 | p1 | p2 |
|------|------|-----|-----|-----|-----|-----|
| **M10E**（M3D） | wide | -0.1026 | -0.0292 | 0 | 0 | 0 |
| **M10E** | zoom | 0 | 0 | 0 | 0 | 0 |
| **M10T**（M3TD） | wide | 0.142257 | -0.267908 | 0 | -0.00103 | 0.000580 |
| **M10T** | zoom | -0.169256 | 0.283043 | 0 | -0.000851 | 0 |
| **M10T** | ir | 0 | 0 | 0 | 0 | 0 |

**说明**：
- 值为 0 表示畸变可忽略（多为变焦/红外镜头）
- 广角镜头（wide）畸变最显著，画面边缘偏差明显
- 未列出的机型默认全 0（无畸变校正）
- 畸变校正公式（径向 + 切向）：
```
r² = x² + y²
x_corrected = x(1 + k1·r² + k2·r⁴ + k3·r⁶) + 2·p1·x·y + p2·(r² + 2·x²)
y_corrected = y(1 + k1·r² + k2·r⁴ + k3·r⁶) + p1·(r² + 2·y²) + 2·p2·x·y
```
- 如果您的机型不在上表中，可通过 OpenCV `calibrateCamera()` + 棋盘格自行标定

---

## 9. 高度基准与坐标系说明

无人机数据涉及多套高度基准和坐标系，使用时请注意区分：

### 高度基准

| 高度类型 | 含义 | 来源 |
|----------|------|------|
| **相对起飞点高度** | 飞机相对起飞位置的高度差，向上为正 | SEI `ArSeiInfo.altitude`（mm） |
| **WGS84 椭球高** | GPS 原始高度，基于 WGS84 椭球面 | OSD `height` |
| **EGM96 大地高（海拔）** | 基于大地水准面的高度，接近实际海拔 | OSD `elevation`（需从椭球高转换） |

**换算关系**：`海拔 ≈ 椭球高 - 大地水准面差值`（各地不同，可用 EGM96 模型查表）

### 坐标系

| 坐标系 | 说明 | 使用场景 |
|--------|------|---------|
| **WGS84** | GPS 全球坐标系（经纬度+椭球高） | 地图显示、航线规划 |
| **GND** | 以起飞点为原点，北为 X+，东为 Y+，向下为 Z+ | SEI 中 `cameraPosX/Y/Z` |
| **NED** | 北-东-地（North-East-Down），与 GND 同方向 | SEI 中 `cameraYaw/Pitch/Roll` |

### 逆投影中的高度使用

第 7 节的逆投影算法使用的是 SEI 中的 `altitude`（相对起飞点高度，mm），不是 GPS 海拔。如果需要将反算结果与 GIS 地图（海拔高度）对齐，需要做基准转换。

---

## 10. 常见误区与 FAQ

### Q1：SEI 只有 I 帧才有？

**不是。** 每帧都有 SEI（P 帧也有），频率约等于视频帧率。

### Q2：能不能把 OSD 频率调高来替代 SEI？

**不能。** MQTT OSD 的 0.5Hz 是设备端固定的上报节奏，无法通过 SDK 或配置调高。要帧级位置对齐，请用 SEI。

### Q3：用 RTSP 点播能拿到 SEI 吗？

SEI 是实时直播能力。录像点播不承诺 SEI 可用。请使用标准直播拉流方式。

### Q4：自建流媒体转发后 SEI 消失了？

检查是否做了转码。流媒体转发必须 **透传码流（pass-through / remux）**，不能重新编码。转码会丢弃 SEI。

### Q5：前端怎么做 AR 叠加？

两种路径：
1. **用前端 PaaS 组件**：内置 AR 渲染，设置 GPS 坐标即可
2. **自研**：Java SDK 订阅 SEI → 后端推 JSON 到前端 → 前端 Canvas/WebGL 绘制

### Q6：视锥中心点 GPS 有现成接口吗？

- 有激光测距的设备：OSD 中 `measure_target_*` 字段即是
- 无激光测距：需用 SEI 数据自行做逆投影计算（见第 7 节）

### Q7：AI 检测框的坐标怎么用？

AI SEI 的坐标是**归一化万分比**：
```
实际像素 X = targetCx / 10000 × 画面宽度
实际像素 Y = targetCy / 10000 × 画面高度
```
如需反算 AI 目标的 GPS，将 (targetCx/10000, targetCy/10000) 代入第 7 节的逆投影算法。

### Q8：Web 前端做 AR 叠加有浏览器限制吗？

有。解析 SEI 需要读取原始编码帧，目前仅 **Chrome / Edge** 支持。Safari 和 Firefox 无法解析 SEI，AR 功能不可用。Java SDK 后端不受此限制。

### Q9：SEI 接收到的位姿数据会抖动怎么办？

根因是 **视频帧回调与 SEI 回调分开投递**（§3），各机型均无法做到严格帧同步。

- **Web / 前端 PaaS**：可对位姿做平滑插值作为权宜之计（例如位置约 150ms、角度约 70ms、pitch 约 40ms），**无法从根上消除回调分离**
- **FHSDK Java SDK**：当前为独立 SEI 回调；**后续计划提供带帧时间戳的同步解析接口**，便于与视频帧对齐（规划中）

### Q10：事后轨迹回溯（如炸机定位）能用 SEI 吗？

SEI 是实时直播数据，平台默认不持久化。若飞行中持续拉流并在后端缓存 SEI，可获得较高密度轨迹，但依赖网络与存储方案。仅做事后粗粒度定位时，可结合 **MQTT OSD**、**DRC 高频 OSD（飞行中缓存）** 或平台飞行记录能力。

---

## 11. 总结

| 要做什么 | 用什么 | 怎么接 |
|----------|--------|--------|
| 画面叠加 AR 标注 | 前端 PaaS 组件 | 组件 API 设置 GPS 坐标（见 §5.2 待办） |
| 后端获取高频位姿 | FHSDK Java SDK SEI | `subscribeArSeiInfo` 等（bean 包字段） |
| 设备状态监控 | MQTT OSD | `listenAircraftPayloadsOsdInfo` |
| 中高频轨迹（不拉流） | DRC OSD | `drc_mode_enter` + `osd_frequency` |
| 像素反算经纬度 | SEI + §8 畸变 + 自研逆投影 | AR SEI + FOV SEI（± EIS SEI） |
| 画面中心点 GPS | 激光测距 OSD | `measure_target_*` |

**一句话**：对齐画面用 SEI（高频、非严格帧同步），状态监控用 MQTT OSD，AR 绘制可用前端 PaaS 组件；精确反算需叠加 §8 畸变与 §7 逆投影。

---

## 修订记录

| 日期 | 说明 |
|------|------|
| 2026-06 | 初版培训文字稿 |
| 2026-06-15 | 统一帧同步口径（各机型非严格同步）；补全 Java bean API 示例；§7 畸变/EIS；`ar_info_switch` / PaaS 组件待办 |
