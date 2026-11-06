---
name: cloudapi2-sdk-core
description: |
  仅在需要编写、生成或审查 DJI Cloud API 2 Java SDK 集成代码时使用。
  不要在用户请求连接 DJI Cloud API 2、查看设备、控制无人机、查询遥测等运维操作时加载本 skill
  （运维操作由 DJI Cloud API 2 MCP Server 直接处理，无需本 skill 介入）。
  适用场景：写 Java 代码调用 SDK API、实现飞控逻辑、
  生成 Spring Boot Service 集成层、审查 SDK 调用代码质量。
  关键词：DJI Cloud API 2, DJI, 无人机, 机场, drone, dock, listen, cancel,
  acquireFlightControlAuthority, releaseFlightControlAuthority, errorCode,
  initialize, getWorkspaceInstance, getDroneInstance, getDockInstance,
  getControlAuthModule, getFlightControlModule, getTakeoffModule,
  getDroneOsdModule, getDeviceStateModule, getDeviceHMSModule,
  getDrcModule, getFlyToPointModule, getCameraControlModule,
  getWaylineManageModule, submitWaylineInfo, getWaylineEditorSts, importKmz,
  listenImportWaylineEvent, 航线资产, 航线管理, wayline management,
  listenAircraftLocation, listenAircraftBatteryInfo, listenAircraftSpeed,
  listenAircraftAttitude, listenAircraftModeCode, listenAircraftHMS, listenDeviceHMS,
  cancelListenAircraftLocation, cancelListenAircraftBatteryInfo, livestream, live,
  flight task, wayline, POI, HMS, media, 飞控, 返航, 起飞,
  急停, returnHome, returnHomeCancel, takeoff, drcEmergencyStop, FlightHubSDK,
  FlightHubSdkConfig, Workspace, DeviceTopo, FlightHubError,
  HeaderKeyValue, setHttpHeaders, setWebSocketAuthHeaders, JniLibraryLoader, loadLibrary, auth, 鉴权
author: DJI Cloud API 2 Team
version: 1.6.0
---

# DJI Cloud API 2 SDK 接入 Skill

## 适用场景

当开发者描述以下场景时触发本 Skill：

- 集成 / 接入 DJI Cloud API 2 SDK（Java）
- 监听无人机或机场状态（OSD、在线状态、电量等）
- 控制无人机（起飞、返航/取消返航、急停、指点飞行；降落通过 returnHome 实现）
- 开始 / 停止直播流
- 管理飞行任务（创建、暂停、恢复航线）
- 遇到 SDK 相关的报错（errorCode、初始化失败、null 返回）
- 管理设备（列表、绑定、解绑、详情查询）

## SDK 心智模型

DJI Cloud API 2 Java SDK 是**事件驱动**的，核心模式是 `listen / cancel`：

```
FlightHubSDK（静态入口）
  → initialize(FlightHubSdkConfig)                ← 同步阻塞，返回 ErrorCode，必须检查
    → getWorkspaceInstance()                       ← 可能返回 null
      → workspace.getDeviceManageModule().getDeviceList(cb)
        → getDroneInstance(sn)                     ← 可能返回 null
        → getDockInstance(sn)                      ← 可能返回 null
          → drone.getXxxModule()                   ← 通过子模块 getter 访问能力
            → module.listenXxx(callback)           ← 返回 int listenId (≥0 成功, <0 失败)
            → module.cancelListenXxx(id)           ← 服务关闭时必须调用
```

**注意**：`listenDroneOnlineStatus` 在 `DeviceStateModule`（非 DroneOsdModule），`listenDeviceHMS` 在 `DeviceHMSModule`（非 DroneOsdModule）。

**模块化访问模式**（v3.1.0）——设备实例通过 getter 方法暴露子模块：
- `drone.getControlAuthModule()` — 飞控/负载控制权
- `drone.getFlightControlModule()` — 返航、急停、摇杆、DRC 实时遥测（drcOsdInfo/drcBatteriesInfo）
- `drone.getTakeoffModule()` — 起飞、指令任务事件（listenCommandTaskEvent）
- `drone.getDroneOsdModule()` — WebSocket 遥测监听（位置/电量/速度/姿态/负载等，推送通道走 `notificationServerAddress` 配置的 WebSocket，不是 MQTT）
- `drone.getDeviceStateModule()` — 在线状态（listenDroneOnlineStatus）
- `drone.getDeviceHMSModule()` — 健康管理告警（listenDeviceHMS）
- `drone.getDrcModule()` — DRC 链路连接/断开、链路状态监听
- `drone.getFlyToPointModule()` — 指点飞行（start/drcStop/updateTargetPoints）
- `drone.getCameraControlModule()` — 相机控制、DRC 激光测距（listenDrcCameraMeasureTarget）、DRC 相机状态
- `drone.getLiveDeviceModule()` — 直播设备管理（listenLiveAvailableRange）
- `drone.getPoiModule()` — 兴趣点环绕
- `dock.getDeviceStateModule()` — 机场在线状态（listenDockOnlineStatus）
- `dock.getDeviceHMSModule()` — 机场健康管理告警
- `dock.getDockOsdModule()` — 机场遥测（位置/气象/存储/网络等）
- `dock.getDockControlModule()` — 机场辅助设备控制（摄像头切换/空调/补光灯/充电/舱盖），完整方法清单+枚举见 `cloudapi2-device-management/references/device-lifecycle.md`
- `workspace.getWaylineManageModule()` — 航线资产管理（列表/详情/上传导入/重命名/复制/拼接/批量下载删除），详见 `prompts/add-wayline-management.md`

**回调参数顺序不统一**（高频踩坑，**不要按"查询/命令"二分推断**）：

> ⚠️ **没有统一规律**。同为查询类 API，`getDeviceList` 是 `(data, error)`，而 `getWaylineList` / `getWaylineDetail` 却是 `(error, result)`。按 API 名或"查询类/命令类"推断顺序**一定会写错**。

| API | 真实签名 | 归类别 |
|-----|----------|--------|
| `getDeviceList` | `(deviceList, error)` —— **data 在前** | 查询（例外） |
| `getWaylineList` | `(error, result)` —— **error 在前** | 查询（违反"查询=data 在前"） |
| `getWaylineDetail` | `(error, result)` —— **error 在前** | 查询（违反"查询=data 在前"） |
| `takeoff` / `returnHome` 等命令类 | `(error, result)` —— **error 在前** | 命令 |

- 生成代码时**必须**用 `javap -p <Callback 接口>` 或直接让编译器报错来确认实际签名（参数顺序写反时形参名不会报错，但把 `error` 当结果用会在调用处 NPE / 类型不匹配——务必核对 `FlightHubCallback` 中的具体内部接口）。
- 不确定时优先按 `(error, result)` 写，再用编译器验证；`getDeviceList` 是全仓少见的 data-first 例外。

**错误响应统一结构**（所有回调共用）：

```java
FlightHubError {
    FlightHubSdkErrorCode errorCode;  // SDK 层枚举
    int httpCode;                      // HTTP 状态码（0=网络层失败）
    int serverCode;                    // 服务端业务码（0=成功）
    int deviceCode;                    // 设备端错误码（0=无设备错误）
    String serverMessage;              // 服务端错误描述
}
```

## 标准 import 清单（复制即用，禁止凭记忆猜包路径）

SDK 的类分布在四个包下，**同名类在多个包中重复存在**，导入错的包会被编译器
以 `cannot find symbol` 或 `incompatible types` 直接拦下：

| 包 | 内容 | 代表类 |
|---|---|---|
| `com.dji.flighthubsdk` | 静态入口与回调接口 | `FlightHubSDK`、`FlightHubCallback` |
| `com.dji.flighthubsdk.bean` | 配置 / 错误 / 数据传输对象（**唯一正确来源**） | `FlightHubSdkConfig`、`HeaderKeyValue`、`FlightHubError`、`DeviceTopo` |
| `com.dji.flighthubsdk.generated` | 错误码枚举、native 接口与回调参数 | `FlightHubSdkErrorCode` |
| `com.dji.flighthubsdk.core` | 设备实例与能力模块 | `Workspace`、`Drone`、`DroneOsdModule` 等 |
| `com.dji.flighthubsdk.value` | OSD / 参数里的业务枚举（40+ 个） | `DroneModeCode`、`CameraMode`、`DockAirConditionerState` |

> ⚠️ `FlightHubSdkConfig` / `HeaderKeyValue` / `FlightHubError` / `DeviceTopo`
> 在 `generated` 包里也有同名副本，但 **`FlightHubSDK` 的方法签名和回调参数
> 用的是 `bean` 包**。导入 `generated` 版本会在回调 lambda 里报
> `incompatible types`。
>
> ⚠️ `FlightHubSdkErrorCode` 在 `generated` 和 `value` 包里**各有一个同名枚举，
> 二者不兼容**。`FlightHubSDK.initialize()` / `setHttpHeaders()` 的返回值、
> `FlightHubError.getErrorCode()` 用的都是 **`generated` 包**；`value` 包那个
> 只出现在部分 OSD 字段里（带 `getValue()` / `fromValue()`）。判断初始化结果时
> 一律 import `com.dji.flighthubsdk.generated.FlightHubSdkErrorCode`。
>
> ⚠️ `value` 包里的枚举（如 `AircraftModeCode.getModeCode()` 返回的
> `DroneModeCode`）**不需要也不能用 int 硬编码替代**，需要比较时直接
> `import com.dji.flighthubsdk.value.DroneModeCode` 后按枚举常量写。
>
> ⚠️ 回调接口（`DeviceListCallback`、`AircraftLocationCallback` 等）都是
> `FlightHubCallback` 的**内部接口**，不需要单独 import；需要显式写类型时
> 使用 `FlightHubCallback.XxxCallback`。

```java
// 静态入口 + 回调接口
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.FlightHubCallback;

// 配置 / 错误 / 数据传输对象
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.DeviceTopo;

// 错误码枚举
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;

// 实例与 JNI 加载器
import com.dji.flighthubsdk.core.JniLibraryLoader;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.Dock;

// 能力模块（通过实例 getter 获取）
import com.dji.flighthubsdk.core.DeviceManageModule;
import com.dji.flighthubsdk.core.DroneOsdModule;
import com.dji.flighthubsdk.core.DeviceStateModule;
import com.dji.flighthubsdk.core.DeviceHMSModule;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.core.TakeoffModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.FlyToPointModule;
import com.dji.flighthubsdk.core.CameraControlModule;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import com.dji.flighthubsdk.core.PoiModule;
import com.dji.flighthubsdk.core.DockOsdModule;
import com.dji.flighthubsdk.core.DockControlModule;
import com.dji.flighthubsdk.core.WaylineManageModule;
```

> Spring Boot 3 的注解统一来自 `jakarta.*`（如 `jakarta.annotation.PostConstruct`、
> `jakarta.annotation.PreDestroy`），不是 `javax.*`。

## 6 条核心规则（硬约束，生成代码时必须遵守）

### 规则 1：鉴权头注入 → initialize → 检查返回值

SDK 采用 **gateway header 注入**鉴权模型（无 setToken/setAppId），完整初始化分四步：

**第零步：先显式加载 JNI（否则鉴权头注入静默失效）**

```java
import com.dji.flighthubsdk.core.JniLibraryLoader;

JniLibraryLoader.loadLibrary();   // 必须先调用，再注入鉴权头
```

> ⚠️ `setHttpHeaders`/`setWebSocketAuthHeaders` 走 native，JNI 未加载时二者只返回 `ERROR_NOT_INITIALIZED`（**不会抛异常**，鉴权头静默不注入 → WebSocket 握手 401）。这是最常见的"配置都对但仍 401"根因。

**第一步：注入鉴权头（必须在 initialize 之前）**

```java
// SDK 鉴权通过 HTTP + WebSocket 双通道头注入，不是 appId/appSecret
// key/value 是网关约定的头名与头值，由 FH2 服务端决定（测试环境常见 user_id / paas_test），
// 不一定是 Authorization / Bearer —— 以服务端提供的鉴权头对为准。
List<HeaderKeyValue> headers = new ArrayList<>();
headers.add(new HeaderKeyValue(authKey, authValue));   // 例如 ("user_id", "paas_test")

FlightHubSDK.setHttpHeaders(headers);            // HTTP 请求鉴权
FlightHubSDK.setWebSocketAuthHeaders(headers);    // WebSocket 推送鉴权
// 必须检查返回值：两者任一非 SUCCESS，说明注入失败，后续推送必然 401
```

> ⚠️ 不注入鉴权头就 initialize，WebSocket 握手会返回 **401 Unauthorized**，所有推送功能失效。

**第二步：构建 config 并 initialize**

```java
FlightHubSdkConfig config = new FlightHubSdkConfig();
config.setAddress("https://<fh2-host>");                                                    // HTTP 地址，含协议前缀
config.setPort("30812");                                                                     // HTTP 端口，注意是 String 类型
config.setNotificationServerAddress("wss://<fh2-host>:30812/openapi/v0.9/notification/ws/v1/notifications"); // WebSocket 推送地址
config.setLogLevel("info");                                                                  // 可选：debug / info / warn / error
```

> ⚠️ `notificationServerAddress` 不设会导致 WebSocket URL 解析失败，OSD 监听、设备状态推送等功能全部不可用。`port` 是 **String** 类型，不是 int。

**第三步：检查返回值**

```java
// ✅ 正确：检查返回值
FlightHubSdkErrorCode code = FlightHubSDK.initialize(config);
if (code != FlightHubSdkErrorCode.SUCCESS) {
    throw new IllegalStateException("SDK init failed: " + code);
}

// ❌ 错误：未检查返回值
FlightHubSDK.initialize(config); // 可能返回 ERROR_INIT_FAILED
Workspace ws = FlightHubSDK.getWorkspaceInstance(); // null
```

### 规则 2：所有回调必须检查 errorCode

```java
// ✅ 正确（注意：回调参数顺序是 data 在前、error 在后）
workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        log.error("获取设备列表失败: sdk={}, server={}", error.getErrorCode(), error.getServerCode());
        return;
    }
    // 安全使用 devices
});

// ❌ 错误：errorCode 非 SUCCESS 时 devices 可能为 null
workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
    devices.forEach(d -> ...); // NPE
});
```

### 规则 3：listenId 必须保存，关闭时必须 cancel

```java
// ✅ 正确（Spring Boot）— 订阅位置 + 电量（OSD 由多个独立 listener 组成）
@Component
public class DroneTelemetryListener {
    private int locationListenId = -1;
    private int batteryListenId  = -1;

    @PostConstruct
    public void start() {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone == null) return;

        DroneOsdModule osd = drone.getDroneOsdModule();
        locationListenId = osd.listenAircraftLocation(loc -> handleLocation(loc));
        if (locationListenId < 0) log.error("订阅位置失败");

        batteryListenId = osd.listenAircraftBatteryInfo(bat -> handleBattery(bat));
        if (batteryListenId < 0) log.error("订阅电量失败");
    }

    @PreDestroy
    public void stop() {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone == null) return;
        DroneOsdModule osd = drone.getDroneOsdModule();
        if (locationListenId >= 0) osd.cancelListenAircraftLocation(locationListenId);
        if (batteryListenId  >= 0) osd.cancelListenAircraftBatteryInfo(batteryListenId);
    }
}

// ❌ 错误：不保存 listenId → 无法 cancel → 回调泄漏
drone.getDroneOsdModule().listenAircraftLocation(loc -> { ... }); // listenId 被丢弃
```

### 规则 4：飞控指令必须先抢权、用完释放

```java
// ✅ 正确：acquire → 指令 → release（无论成败）
ControlAuthModule auth = drone.getControlAuthModule();
auth.acquireFlightControlAuthority(acquireError -> {
    if (acquireError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        log.error("获取控制权失败: {}", acquireError.getErrorCode());
        return;
    }
    drone.getFlightControlModule().returnHome(cmdError -> {
        // 无论成败都释放控制权
        auth.releaseFlightControlAuthority(e -> {});
        if (cmdError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
            log.error("返航失败: {}", cmdError.getErrorCode());
        }
    });
});

// ❌ 错误：未抢权直接发指令 → 返回权限错误
drone.getFlightControlModule().returnHome(error -> { ... });
```

### 规则 5：getDroneInstance / getDockInstance / 模块 getter 可能返回 null

```java
// ✅ 正确
Drone drone = FlightHubSDK.getDroneInstance(sn);
if (drone == null) {
    log.warn("Drone not found or offline: {}", sn);
    return;
}

// 模块 getter 也可能返回 null（如设备不支持该模块）
CameraControlModule cam = drone.getCameraControlModule();
if (cam == null) {
    log.warn("CameraControlModule not available: {}", sn);
    return;
}

// ❌ 错误：NPE
FlightHubSDK.getDroneInstance(sn).getDroneOsdModule().listenAircraftLocation(...);
```

### 规则 6：危险操作必须先输出风险警告（输出行为规则）

对以下操作生成代码时，**必须在代码块之前**输出结构化警告块，禁止直接给出代码：

**适用操作**：`takeoff` / `returnHome` / `drcEmergencyStop` / `drcEmergencyLanding` / `drcForceLanding` / `flyToPoint`

**强制输出格式**（必须逐项覆盖）：

```
⚠️ **危险操作警告**
- 操作类型：[takeoff / returnHome / drcEmergencyStop / drcEmergencyLanding / drcForceLanding / flyToPoint]
- 不可逆性：[具体描述，如"drcForceLanding 强制降落并停转螺旋桨，不可撤销"或"drcEmergencyStop 立即悬停，螺旋桨保持运转"]
- 物理影响：此代码将控制真实无人机执行物理动作，操作失误可能导致坠机或财产损失
- 生产建议：务必实现前端二次确认弹窗（confirm dialog）；建议支持 dry_run 参数做预校验
```

**规则约束**：
- ❌ **禁止**直接输出飞控代码而不附带上述警告
- ❌ **禁止**仅在代码注释中写警告（用户可能不读注释）
- ✅ 警告必须在代码块**外部**，以独立段落形式呈现
- ✅ 如果用户在同一轮对话中已确认知晓风险，后续同类操作可省略警告

> 每条规则的详细"为什么"和更多正反例，见 `references/核心规则详解.md`

## 规则适用范围

| 规则 | 适用操作 | 不适用 |
|------|----------|--------|
| 规则 1 initialize | 应用启动时（一次） | — |
| 规则 2 errorCode | **所有回调** | — |
| 规则 3 listenId/cancel | `listenXxx()` 订阅类 | 命令式操作（returnHome/takeoff 等一次性回调） |
| 规则 4 acquire/release | 飞控命令（takeoff/returnHome/returnHomeCancel/drcEmergencyStop/drcEmergencyLanding/drcForceLanding/flyToPoint） | 查询类、监听类、航线任务（自管理控制权） |
| 规则 5 null 检查 | getDroneInstance / getDockInstance / getWorkspaceInstance / 模块 getter（getCameraControlModule 等） | — |
| 规则 6 危险操作警告 | takeoff/returnHome/drcEmergencyStop/drcEmergencyLanding/drcForceLanding/flyToPoint 代码生成 | 查询类、监听类、代码 review（非生成场景） |

## 异步回调桥接模式（REST 接口必读）

SDK 所有操作均为异步回调，但 Spring Boot Controller 需要同步返回。标准桥接方式：

```java
public void executeWithCallback(Drone drone) {
    CompletableFuture<Void> future = new CompletableFuture<>();

    drone.getFlightControlModule().returnHome(error -> {
        if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
            future.completeExceptionally(new RuntimeException("失败: " + error.getErrorCode()));
        } else {
            future.complete(null);
        }
    });

    // Controller 层等待结果（带超时保护）
    try {
        future.get(30, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
        throw new RuntimeException("SDK 回调超时");
    }
}
```

> ⚠️ SDK 回调在**非主线程**执行（SDK 内部线程池），操作共享状态需要线程安全保护。

## 决策树路由

```
用户问题
├─ getDroneInstance / getDockInstance / getWorkspaceInstance 返回 null
│    → 加载 references/设备管理流程.md#常见问题 + references/错误码诊断.md#常见问题速查
├─ 报错 / errorCode / httpCode / serverCode
│    → 加载 references/错误码诊断.md，按 4 层优先级定位
├─ 接入新功能 / 添加模块 / 订阅数据
│    → 先判断是订阅式(规则3)还是命令式(规则4)，按对应规则生成代码
├─ 质量检查 / 自检 / review
│    → 加载 references/接入质量检查清单.md
└─ SDK 概念 / 架构 / "xxx 是什么"
     → 加载 references/文档检索与问答.md
```

## 反幻觉规则（强制）

1. **示例代码必须来源于本 Skill 的 references/ 或用户提供的 SDK 文档**，不得凭模型记忆生成 API 签名、字段名、枚举值或代码片段
2. **禁止凭记忆编造**——如果不确定某个 API 是否存在或参数是否正确，必须明确告知用户"需要确认 SDK 文档"
3. **引用必须可溯源**——任何 SDK 调用建议必须能追溯到 references/ 文档路径或官方协议文档

## 未覆盖模块（已知缺口，禁止编造）

以下官方 SDK 模块本 Skill **明确不覆盖**，遇到相关需求时必须告知用户"需要确认 SDK 文档"，不得凭记忆编造调用方式：

| 模块 | 说明 |
|---|---|
| `DeployModule` | 部署相关能力，走 `cloudapi2-skeleton-deploy` / `cloudapi2-mcp-deploy` Skill |
| `TerraBuildModule` | 三维重建，仅 MCP 侧（`cloudapi2-mcp-guide`）有覆盖 |
| `TestFlightModule` | 试飞功能 |
| `CustomSEIModule` | 自定义 SEI 下发 |
| `FlashLedModule` | 补光灯控制 |
| `LiveSyncModule` | 直播同步 |
| `PullStreamModule` | 拉流 |
| `RelayControlModule` | 中继控制 |


## 共享规范

> 本 Skill 的 6 条核心规则同时收录在 `skills/_shared/constraints-common.yaml`（跨 Skill 共享约束 SSOT）。其他 Skill 通过引用该文件获得对这些规则的感知。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载，不要一次性读取所有 references/。
