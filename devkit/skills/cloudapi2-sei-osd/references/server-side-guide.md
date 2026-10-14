# FHSDK 服务端数据获取指南（OSD / DRC OSD / SEI）

> 目标：教会开发者在**服务端（Java）**通过 `com.dji.flighthubsdk.core.Drone` 与
> `com.dji.flighthubsdk.core.LiveModule` 拿到无人机的 **MQTT OSD**、**DRC 快通道 OSD** 与 **SEI 帧级数据**。
>
> 本文是生成 Skill 的源文档，自包含。代码片段均为**完整可编译的真实方法**（非伪代码），
> 并区分「SDK 必需调用」与「业务承载对象」（后者可替换）。

---

## 0. TL;DR

- **MQTT OSD**（位置/姿态/速度/电量/风/云台/机场位置）：从 **`Drone` / `Dock`** 拿，
  方法是 `drone.getDroneOsdModule().listenAircraftXxx(callback)`，走 notification/MQTT 通道，**秒级、不与画面对齐**。
- **DRC OSD**（激光测距等高频相机数据）：`drone.getDrcModule().connectDrcLink` 后 `drone.getCameraControlModule().listenDrcCameraMeasureTarget(payload, cb)`，
  **较高频**，需先建 DRC 链路。
- **SEI**（相机姿态 + GND 偏移 + 视场/焦距）：从 **`LiveModule`** 拿，
  方法是 `live.subscribeArSeiInfo / subscribeFovSeiInfo`，内嵌于视频码流，**帧级、与画面逐帧对齐**。
- 前提不同：MQTT OSD 选到设备即可；**SEI 必须先开直播拿到 `LiveModule`**；DRC 需 `drone.getDrcModule().connectDrcLink`。
- 所有 `listen/subscribe` 都返回 **listenId**，必须保存并在退出时 `cancel/unSubscribe`，否则回调泄漏。

---

## 1. 心智模型：三条独立链路

```
┌──────────────────────────────────────────────────────────────────────┐
│  平台 (DJI CloudAPI2 Server)                                               │
│     │                                                                 │
│     ├── notification / MQTT ──► SDK(JNI) ──► Drone/Dock               │  ← MQTT OSD（秒级）
│     │                                                                 │
│     ├── DRC 链路 ────────────► SDK(JNI) ──► Drone                     │  ← DRC OSD（较高频）
│     │                                                                 │
│     └── 视频码流(H.264+SEI) ──► SDK(JNI) ──► LiveModule               │  ← SEI（帧级）
└──────────────────────────────────────────────────────────────────────┘
                                   │
                          你的代码从「句柄」开始介入：
                          drone.getDroneOsdModule().listenAircraftXxx(cb)       ← MQTT OSD
                          drone.getCameraControlModule().listenDrcCameraMeasureTarget(payload, cb) ← DRC OSD（需 getDrcModule().connectDrcLink）
                          live.subscribeArSeiInfo(cb)         ← SEI（需 LiveModule）
```

| 维度 | MQTT OSD | DRC OSD | SEI |
|------|----------|---------|-----|
| SDK 对象 | `Drone` / `Dock` | `Drone` | `LiveModule` |
| 取数据方法 | `getDroneOsdModule().listenAircraftXxx` / `getDockOsdModule().listenDockLocation` | `getCameraControlModule().listenDrcCameraMeasureTarget` | `subscribeArSeiInfo` / `subscribeFovSeiInfo` |
| 前提 | 选到设备 | **`getDrcModule().connectDrcLink` 成功** | **`getLiveStream` + `start` 开直播** |
| 数据源 | notification / MQTT | DRC 通道 | 视频码流内嵌 SEI |
| 频率 | 秒级（~0.5Hz） | 较高频 | 帧级（~视频帧率） |
| 与画面对齐 | 否 | 否 | **是** |
| 典型用途 | 地图飞机模型、状态面板 | 高频激光测距落点 | 相机姿态、视锥、像素反算 |

---

## 2. 完整生命周期

```
[1] 初始化 SDK (全程一次)
      JniLibraryLoader.loadLibrary()  →  FlightHubSDK.initialize(config)
              │
[2] 拿句柄
      设备拓扑 → droneSn / gatewaySn
      FlightHubSDK.getDroneInstance(droneSn) → Drone
      FlightHubSDK.getDockInstance(gatewaySn) → Dock
              │
[3] 订阅 OSD ────────────────► drone.getDroneOsdModule().listenAircraftXxx(cb)  (返回 listenId)
              │
[4] 开直播 (SEI 前提)
      drone.getLiveDeviceModule().listenLiveAvailableRange(cb) → 选一个 LiveInfo
      drone.getLiveStream(liveInfo) → LiveModule
      live.start(cb)  (等成功)
              │
[5] 建 DRC 链路 (DRC OSD 前提；Demo 亦在此订阅 SEI)
      drone.getDrcModule().connectDrcLink(cb)  (等成功)
      drone.getCameraControlModule().listenDrcCameraMeasureTarget(payload, cb)   ← DRC 激光测距
      live.subscribeArSeiInfo/FovSeiInfo(cb)         ← SEI（SDK 仅需 LiveModule；时机见 §6.3）
              │
      ... 回调持续触发，你在 callback 里处理数据 ...
              │
[6] 退订清理 (与 3/5 对称)
      drone.getCameraControlModule().cancelListenDrcCameraMeasureTarget(payload, id)
      live.unSubscribeArSeiInfo(id) / unSubscribeFovSeiInfo(id)
      drone.getDrcModule().disconnectDrcLink(cb)
      drone.getDroneOsdModule().cancelListenAircraftXxx(id)
      live.stop()
```

### 业务承载对象约定（贯穿全文）

下文代码出现三个**与 SDK 无关、可自由替换**的承载对象，理解它们才能看懂片段：

- **`DeviceContext ctx`** — 单会话的状态袋：存 `Drone`/`Dock`/`LiveModule` 句柄、所有 listenId、
  直播信息。完整定义见 §11。
- **`publisher`** — 数据出口。片段里所有 `publisher.publishXxx(...)`
  你都可替换成「打印 / 落库 / 推 MQ / WebSocket」。
- **`TelemetryMapper`** — 把 SDK bean 转成扁平 `Map`。完整类见 §12。

---

## 3. 前置：初始化 SDK（完整方法）

进程级单例初始化，`synchronized` + 双检保证只初始化一次。

```java
@Component
public class FhsdkBootstrap {

    private static final Logger log = LoggerFactory.getLogger(FhsdkBootstrap.class);

    private final FhsdkProperties properties;   // 配置：address/port/notificationServer/log*
    private volatile boolean initialized;

    public FhsdkBootstrap(FhsdkProperties properties) {
        this.properties = properties;
    }

    public synchronized void ensureInitialized() {
        if (initialized && FlightHubSDK.isInitialized()) {
            return;
        }
        // 关键点 1：必须先加载 JNI 原生库
        JniLibraryLoader.loadLibrary();

        // 关键点 2：组装配置
        FlightHubSdkConfig sdkConfig = new FlightHubSdkConfig();
        sdkConfig.setAddress(properties.getAddress());                       // http://<fhsdk-server-host>
        sdkConfig.setPort(properties.getPort());                            // 30812
        sdkConfig.setNotificationServerAddress(properties.getNotificationServer()); // ws://.../notifications?x-auth-token=<your-auth-token>
        sdkConfig.setLogLevel(properties.getLogLevel());
        sdkConfig.setLogToConsole(properties.isLogToConsole());
        sdkConfig.setLogFilePath(properties.getLogFilePath());
        sdkConfig.setLogMaxFileSize(4 * 1024 * 1024);
        sdkConfig.setLogMaxFiles(100);

        // 可选：自定义鉴权/HTTP 头
        List<HeaderKeyValue> headers = new ArrayList<>();
        headers.add(new HeaderKeyValue("x-demo-uuid", "fhsdk-demo-server"));
        FlightHubSDK.setWebSocketAuthHeaders(headers);
        FlightHubSDK.setHttpHeaders(headers);

        // 关键点 3：初始化。token / binding_uuid 由 SDK ConfigReader 读取；
        // 若工作目录存在 ./flight_hub_config.json 则从中加载，否则使用 SDK 内置默认值。
        FlightHubSdkErrorCode result = FlightHubSDK.initialize(sdkConfig);
        if (result != FlightHubSdkErrorCode.SUCCESS) {
            throw new IllegalStateException("FHSDK initialize failed: " + result);
        }
        initialized = true;
        log.info("FHSDK initialized, version={}", FlightHubSDK.getVersion());
    }

    public boolean isInitialized() {
        return initialized && FlightHubSDK.isInitialized();
    }
}
```

所需 import：
```java
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.core.JniLibraryLoader;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
```

---

## 4. 拿句柄：Drone / Dock

### 4.1 列设备拓扑 → 解析 droneSn（完整方法，含异步压平）

`workspace.getDeviceManageModule().getDeviceList` 是异步回调，用 `CountDownLatch + AtomicReference` 压平成同步
（这套模板见 §8）。

```java
public List<DeviceVo> listDevices() {
    bootstrap.ensureInitialized();
    Workspace workspace = FlightHubSDK.getWorkspaceInstance();

    AtomicReference<List<DeviceTopo>> result = new AtomicReference<>(List.of());
    AtomicReference<FlightHubError> errorRef = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);

    workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
        if (error != null && error.isSuccess()) {
            result.set(devices);
        } else {
            errorRef.set(error);
        }
        latch.countDown();
    });

    try {
        if (!latch.await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("getDeviceList timeout");
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("getDeviceList interrupted", e);
    }
    if (errorRef.get() != null) {
        throw new IllegalStateException("getDeviceList failed: " + errorRef.get());
    }

    // 拓扑结构：topo.getDevice() = 网关(机场/遥控)，topo.getSubDevices() = 子设备(含 drone)
    List<DeviceVo> vos = new ArrayList<>();
    for (DeviceTopo topo : result.get()) {
        Device main = topo.getDevice();
        if (main == null) continue;

        DeviceVo vo = new DeviceVo();
        vo.setGatewaySn(main.getDeviceSn());
        vo.setGatewayName(firstNonBlank(main.getNickname(), main.getDeviceName(), main.getDeviceSn()));
        vo.setOnline(main.isOnline());

        List<Device> subs = topo.getSubDevices();
        if (subs != null && !subs.isEmpty()) {
            // 取 deviceClass == "drone" 的子设备
            Device sub = subs.stream()
                    .filter(s -> "drone".equalsIgnoreCase(s.getDeviceClass()))
                    .findFirst()
                    .orElse(subs.get(0));
            vo.setDroneSn(sub.getDeviceSn());
            vo.setDroneName(firstNonBlank(sub.getNickname(), sub.getDeviceName(), sub.getDeviceSn()));
            vo.setDroneOnline(sub.isOnline());
        }
        vos.add(vo);
    }
    return vos;
}

private static String firstNonBlank(String... values) {
    for (String v : values) {
        if (v != null && !v.isBlank()) return v;
    }
    return "";
}
```

按 gatewaySn 找原始 `DeviceTopo`（拿 subDevices 列表）的方法（同样的压平套路）：

```java
private Optional<DeviceTopo> findTopoRaw(String gatewaySn) {
    bootstrap.ensureInitialized();
    Workspace workspace = FlightHubSDK.getWorkspaceInstance();
    AtomicReference<DeviceTopo> found = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);
    workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
        if (error != null && error.isSuccess()) {
            for (DeviceTopo topo : devices) {
                if (topo.getDevice() != null && gatewaySn.equals(topo.getDevice().getDeviceSn())) {
                    found.set(topo);
                    break;
                }
            }
        }
        latch.countDown();
    });
    try {
        latch.await(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    return Optional.ofNullable(found.get());
}
```

所需 import：
```java
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.Workspace;
```

### 4.2 用 SN 换句柄（校验在线 + 找 drone 子设备 + 换句柄）

```java
DeviceTopo topo = findTopoByGatewaySn(gatewaySn).orElseThrow(...);
Device main = topo.getDevice();
if (main == null || !main.isOnline()) {
    throw new IllegalStateException("device offline: " + gatewaySn);   // 离线收不到数据
}

// 找出 drone 子设备
Device droneDevice = topo.getSubDevices().stream()
        .filter(s -> "drone".equalsIgnoreCase(s.getDeviceClass()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("no drone sub-device under gateway: " + gatewaySn));
String droneSn = droneDevice.getDeviceSn();

// 换句柄
Drone drone = FlightHubSDK.getDroneInstance(droneSn);     // 飞机句柄：OSD + 直播入口
if (drone == null) throw new IllegalStateException("drone instance not found: " + droneSn);
Dock dock = FlightHubSDK.getDockInstance(gatewaySn);       // 机场句柄(可选)

// 存入承载对象，供后续订阅使用
ctx.setGatewaySn(gatewaySn);
ctx.setDroneSn(droneSn);
ctx.setDrone(drone);
ctx.setDock(dock);
```

所需 import：
```java
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.Dock;
```

---

## 5. 获取 MQTT OSD 数据（Drone / Dock 路径，完整方法）

订阅与退订**成对出现**，listenId 统一用 `ctx.putListenId(key, id)` 存（见 §11），退订时按 key 取回。

### 5.1 订阅飞机 7 路 OSD

```java
private void subscribeSlowOsd(DeviceContext ctx) {
    String sid = ctx.getHttpSessionId();   // 仅用于把数据路由回对应会话；非 SDK 必需
    String gw  = ctx.getGatewaySn();
    Drone drone = ctx.getDrone();
    DroneOsdModule osd = drone.getDroneOsdModule();

    // 位置：经纬度 / 相对高 / 海拔
    ctx.putListenId("location", osd.listenAircraftLocation(loc ->
            publisher.publishMqttOsd(sid, gw, "location", TelemetryMapper.location(loc))));

    // 姿态：偏航 / 俯仰 / 横滚
    ctx.putListenId("attitude", osd.listenAircraftAttitude(att ->
            publisher.publishMqttOsd(sid, gw, "attitude", TelemetryMapper.attitude(att))));

    // 速度：水平 / 垂直
    ctx.putListenId("speed", osd.listenAircraftSpeed(spd ->
            publisher.publishMqttOsd(sid, gw, "speed", TelemetryMapper.speed(spd))));

    // 电量：百分比 / 剩余飞行时间 / 返航电量 / 降落电量
    ctx.putListenId("battery", osd.listenAircraftBatteryInfo(bat ->
            publisher.publishMqttOsd(sid, gw, "battery", TelemetryMapper.battery(bat))));

    // 风速 / 风向
    ctx.putListenId("wind", osd.listenAircraftWindSpeed(wind ->
            publisher.publishMqttOsd(sid, gw, "wind", TelemetryMapper.wind(wind))));

    // 云台姿态（每个 payload 一组 pitch/roll/yaw）
    ctx.putListenId("gimbal", osd.listenAircraftPayloadsGimbalAttitude(gimbal ->
            publisher.publishMqttOsd(sid, gw, "gimbalAttitude", TelemetryMapper.gimbalAttitude(gimbal))));

    // 激光测距目标（从 payload OSD 信息里取，可能多 payload）
    ctx.putListenId("measure", osd.listenAircraftPayloadsOsdInfo(list -> {
        if (list.getPayloadsInfoList() == null) return;
        for (var osdInfo : list.getPayloadsInfoList()) {
            publisher.publishMqttOsd(sid, gw, "measureTarget", TelemetryMapper.measureTargetFromOsd(osdInfo));
        }
    }));
}
```

> ⚠️ **MQTT 激光测距可用性**：`listenAircraftPayloadsOsdInfo` 能否收到测距，取决于设备/负载/平台配置，
> **现场常不推送**。不要把它当作必然可用的数据源；**高频激光测距请走 DRC** `listenDrcCameraMeasureTarget`（§5.4）。

### 5.2 订阅机场位置（Dock 路径，可选）

```java
private void subscribeDockOsd(DeviceContext ctx) {
    Dock dock = ctx.getDock();
    if (dock == null) return;
    String sid = ctx.getHttpSessionId();
    String gw  = ctx.getGatewaySn();
    ctx.putListenId("dockLocation", dock.getDockOsdModule().listenDockLocation(loc ->
            publisher.publishMqttOsd(sid, gw, "dockLocation", TelemetryMapper.dockLocation(loc))));
}
```

### 5.3 退订 OSD（与 5.1/5.2 对称）

```java
private void unsubscribeSlowOsd(DeviceContext ctx) {
    Drone drone = ctx.getDrone();
    if (drone == null) return;
    DroneOsdModule osd = drone.getDroneOsdModule();
    cancel(ctx, "location", osd::cancelListenAircraftLocation);
    cancel(ctx, "attitude", osd::cancelListenAircraftAttitude);
    cancel(ctx, "speed",    osd::cancelListenAircraftSpeed);
    cancel(ctx, "battery",  osd::cancelListenAircraftBatteryInfo);
    cancel(ctx, "wind",     osd::cancelListenAircraftWindSpeed);
    cancel(ctx, "gimbal",   osd::cancelListenAircraftPayloadsGimbalAttitude);
    cancel(ctx, "measure",  osd::cancelListenAircraftPayloadsOsdInfo);
}

private void unsubscribeDockOsd(DeviceContext ctx) {
    Dock dock = ctx.getDock();
    if (dock == null) return;
    Integer id = ctx.getListenIds().remove("dockLocation");
    if (id != null) {
        try { dock.getDockOsdModule().cancelListenDockLocation(id); } catch (Exception ignored) {}
    }
}

// 退订小工具：按 key 取回 listenId 并调用对应 cancel 方法
private interface CancelOp { void cancel(int listenId); }

private void cancel(DeviceContext ctx, String key, CancelOp op) {
    Integer id = ctx.getListenIds().remove(key);
    if (id != null) {
        try { op.cancel(id); } catch (Exception ignored) {}
    }
}
```

> ⚠️ 回调在 SDK 线程触发，**不要在回调里阻塞/做重活**；上面只做了「取值→转交 publisher」。

### 5.4 DRC 快通道：激光测距（完整方法）

DRC 链路建立后，可通过 `getCameraControlModule().listenDrcCameraMeasureTarget` 拿到**较高频**的激光测距落点，
与 MQTT `getDroneOsdModule().listenAircraftPayloadsOsdInfo` 的测距字段语义相近，但推送更及时。

**前提**：`getDrcModule().connectDrcLink` 成功；`payloadIndex` 与当前直播负载一致。

```java
public void connectDrc(DeviceContext ctx) {
    if (ctx.isDrcConnected()) return;
    Drone drone = ctx.getDrone();
    if (drone == null) throw new IllegalStateException("drone not set");

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<FlightHubError> errorRef = new AtomicReference<>();
    drone.getDrcModule().connectDrcLink(error -> {
        errorRef.set(error);
        latch.countDown();
    });
    await(latch, "connectDrcLink");
    if (errorRef.get() == null || !errorRef.get().isSuccess()) {
        throw new IllegalStateException("connectDrcLink failed: " + errorRef.get());
    }
    ctx.setDrcConnected(true);

    // 可选：拿负载控制权（杆量/云台复位前通常需要）
    String payload = ctx.getPayloadIndex();
    drone.getControlAuthModule().acquirePayloadControlAuthority(payload, err -> { /* 记录日志 */ });

    subscribeDrcChannels(ctx);
}

private void subscribeDrcChannels(DeviceContext ctx) {
    String sid = ctx.getHttpSessionId();
    String gw  = ctx.getGatewaySn();
    String payload = ctx.getPayloadIndex();
    Drone drone = ctx.getDrone();

    // DRC 快通道激光测距
    int fastId = drone.getCameraControlModule().listenDrcCameraMeasureTarget(payload, target ->
            publisher.publishDrcCamera(sid, gw, TelemetryMapper.measureTarget(target)));
    ctx.putListenId("drc_measure", fastId);

    // Demo 在 DRC 连上后订阅 SEI（见 §6.3 说明）；SDK 本身不强制此顺序
}

public void disconnectDrc(DeviceContext ctx) {
    if (ctx == null || !ctx.isDrcConnected()) return;
    unsubscribeDrcChannels(ctx);
    CountDownLatch latch = new CountDownLatch(1);
    ctx.getDrone().getDrcModule().disconnectDrcLink(error -> latch.countDown());
    await(latch, "disconnectDrcLink");
    ctx.setDrcConnected(false);
}

private void unsubscribeDrcChannels(DeviceContext ctx) {
    Drone drone = ctx.getDrone();
    Integer fastId = ctx.getListenIds().remove("drc_measure");
    if (fastId != null) {
        try { drone.getCameraControlModule().cancelListenDrcCameraMeasureTarget(ctx.getPayloadIndex(), fastId); }
        catch (Exception ignored) {}
    }
    // SEI 退订见 §6.3 / §7
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `latitude` / `longitude` / `altitude` | double | 激光落点 WGS84 |
| `distance` | double | 斜距（米） |
| `laserMeasureState` | int | 测距状态（0=正常等，见 SDK 枚举） |
| `payloadIndex` | String | 负载索引 |

---

## 6. 获取 SEI 数据（LiveModule 路径，完整方法）

SEI 嵌在视频码流里，**必须先开直播拿到 `LiveModule`**。三步：拿 LiveInfo → 开直播 → 订阅 SEI。

### 6.1 监听可用直播范围，拿到 LiveInfo（完整方法）

`LiveInfo` 含 `payloadIndex / deviceSn / videoId`，是开直播的入参。

```java
public void startWatch(DeviceContext ctx) {
    stopWatch(ctx);
    Drone drone = ctx.getDrone();
    if (drone == null) return;

    ctx.setAvailablePayloads(new CopyOnWriteArrayList<>());
    ctx.setSelectedLiveInfo(null);

    int listenId = drone.getLiveDeviceModule().listenLiveAvailableRange(ranges -> {
        // ranges: List<LiveInfo>，按 droneSn 过滤出本机的直播负载
        List<LiveInfo> filtered = filterDronePayloads(ranges, ctx.getDroneSn());
        ctx.setAvailablePayloads(new CopyOnWriteArrayList<>(filtered));
        for (LiveInfo info : filtered) {
            log.info("payload available payload={} deviceSn={} videoId={}",
                    info.getPayloadIndex(), info.getDeviceSn(), info.getVideoId());
        }
    });
    ctx.setPayloadRangeListenId(listenId);
}

public void stopWatch(DeviceContext ctx) {
    if (ctx == null || ctx.getDrone() == null) return;
    if (ctx.getPayloadRangeListenId() >= 0) {
        try { ctx.getDrone().getLiveDeviceModule().cancelListenLiveAvailableRange(ctx.getPayloadRangeListenId()); }
        catch (Exception ignored) {}
        ctx.setPayloadRangeListenId(-1);
    }
}

private static List<LiveInfo> filterDronePayloads(List<LiveInfo> ranges, String droneSn) {
    if (ranges == null || ranges.isEmpty()) return List.of();
    List<LiveInfo> matched = new ArrayList<>();
    for (LiveInfo info : ranges) {
        if (droneSn != null && droneSn.equals(info.getDeviceSn())) matched.add(info);
    }
    return matched.isEmpty() ? List.copyOf(ranges) : matched;
}
```

> 从 `ctx.getAvailablePayloads()` 里选一个 `LiveInfo`（按 `payloadIndex` 匹配）存为
> `ctx.setSelectedLiveInfo(found)`，即可用于下一步开直播。

### 6.2 开直播 → 得到 LiveModule（完整方法）

`getLiveStream` 拿模块，`start` 异步等成功。

```java
public void startLive(DeviceContext ctx) {
    Drone drone = ctx.getDrone();
    if (drone == null) throw new IllegalStateException("drone not set");
    if (ctx.getLiveModule() != null) throw new IllegalStateException("live already started");

    LiveInfo liveInfo = ctx.getSelectedLiveInfo();   // 来自 §6.1
    if (liveInfo == null) throw new IllegalStateException("payload 未选择");

    // (a) 拿直播模块
    LiveModule liveModule = drone.getLiveStream(liveInfo);
    ctx.setLiveModule(liveModule);

    // (b) 启动（异步 → latch 等成功）
    CountDownLatch startLatch = new CountDownLatch(1);
    AtomicReference<FlightHubError> startError = new AtomicReference<>();
    liveModule.start(error -> {
        startError.set(error);
        startLatch.countDown();
    });
    try {
        if (!startLatch.await(20, TimeUnit.SECONDS)) {
            throw new IllegalStateException("live start timeout");
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("live start interrupted", e);
    }
    if (startError.get() == null || !startError.get().isSuccess()) {
        ctx.setLiveModule(null);
        throw new IllegalStateException("live start failed: " + startError.get());
    }

    // (c) 可选：订阅 H.264 裸帧（喂 ffmpeg / 落盘 / 转推）
    AtomicLong frameCount = new AtomicLong();
    int streamListenId = liveModule.subscribeLiveStream((data, frameInfo) -> {
        if (data == null || data.length == 0) return;
        long n = frameCount.incrementAndGet();
        if (n == 1 || n % 100 == 0) {
            log.info("live frame #{} bytes={}", n, data.length);
        }
        // data = H.264 Annex-B 裸码流
    });
    ctx.setLiveStreamListenId(streamListenId);

    // 至此 liveModule 可用，可在此或别处订阅 SEI（见 §6.3）
}
```

### 6.3 订阅 SEI（AR + FOV，完整方法）

**前提 `live != null`**，否则静默跳过。listenId 用 `ctx` 上的独立字段保存
（`arSeiListenId` / `fovSeiListenId`）。

> 💡 **订阅时机**：SDK 只要求 `LiveModule` 已 `start` 成功即可订阅 SEI，**不依赖 DRC**。
> ISV Demo 选择在 `connectDrcLink` 成功后一并订阅 SEI（与快激光同入口），便于前端在 DRC 就绪后再画相机三轴；
> 你也可以在 `startLive` 成功后立即订阅。

```java
private void subscribeSei(DeviceContext ctx) {
    String sid = ctx.getHttpSessionId();
    String gw  = ctx.getGatewaySn();

    LiveModule live = ctx.getLiveModule();
    if (live == null) return;   // ⚠️ 没开直播 → SEI 无法订阅

    // AR SEI：相机姿态(yaw/pitch/roll) + 相机位置(X/Y/Z) + 目标经纬高
    int arId = live.subscribeArSeiInfo(ar -> {
        Map<String, Object> arMap = Map.of(
                "cameraYaw",   ar.getCameraYaw(),
                "cameraPitch", ar.getCameraPitch(),
                "cameraRoll",  ar.getCameraRoll(),
                "cameraPosX",  ar.getCameraPosX(),
                "cameraPosY",  ar.getCameraPosY(),
                "cameraPosZ",  ar.getCameraPosZ(),
                "latitude",    ar.getLatitude(),
                "longitude",   ar.getLongitude(),
                "altitude",    ar.getAltitude()
        );
        publisher.publishSeiAr(sid, gw, arMap);
    });
    ctx.setArSeiListenId(arId);

    // FOV SEI：水平/垂直视场角 + 当前实际焦距
    int fovId = live.subscribeFovSeiInfo(fov -> {
        Map<String, Object> fovMap = Map.of(
                "fovHorizontal",        fov.getFovHorizontal(),
                "fovVertical",          fov.getFovVertical(),
                "curActualFocalLength", fov.getCurActualFocalLength()
        );
        publisher.publishSeiFov(sid, gw, fovMap);
    });
    ctx.setFovSeiListenId(fovId);
}
```

> 💡 AR 与 FOV 是**两路独立回调**，若要合并成「一帧完整 SEI」，需各自缓存最新值再合并发出，
> 见 §6.4。

### 6.4 （可选）AR/FOV 合并为一条 SEI（完整方法）

两路异步到达，各自缓存最新值，任一到达即合并下发。

```java
private final ConcurrentHashMap<String, Map<String, Object>> pendingSeiAr  = new ConcurrentHashMap<>();
private final ConcurrentHashMap<String, Map<String, Object>> pendingSeiFov = new ConcurrentHashMap<>();

public void publishSeiAr(String sessionId, String gatewaySn, Map<String, Object> arData) {
    pendingSeiAr.put(sessionId, arData);
    tryFlushSei(sessionId, gatewaySn);
}

public void publishSeiFov(String sessionId, String gatewaySn, Map<String, Object> fovData) {
    pendingSeiFov.put(sessionId, fovData);
    tryFlushSei(sessionId, gatewaySn);
}

private void tryFlushSei(String sessionId, String gatewaySn) {
    Map<String, Object> ar  = pendingSeiAr.get(sessionId);
    Map<String, Object> fov = pendingSeiFov.get(sessionId);
    if (ar == null && fov == null) return;
    HashMap<String, Object> merged = new HashMap<>();
    if (ar  != null) merged.putAll(ar);
    if (fov != null) merged.putAll(fov);
    // 下发一条 type="sei" 的合并消息（这里换成你的出口）
    send(sessionId, /* build message type=sei, data=merged */ merged);
}
```

### 6.5 AR / FOV 字段语义与单位（必读）

SDK 在 C++ 层解析 SEI 裸包后，Java `ArSeiInfo` / `FovSeiInfo` 已是**物理量**。
下游做地图/视锥计算前须理解单位，避免把 FOV 字段当成「角度」。

#### AR SEI（`ArSeiInfo`）

| 字段 | SDK 类型 | 单位 / 编码 | 说明 |
|------|----------|-------------|------|
| `cameraYaw` / `cameraPitch` / `cameraRoll` | double | **度** | NED 下相机欧拉角；原始 int32 ÷ 10⁷ |
| `cameraPosX` / `cameraPosY` / `cameraPosZ` | int | **mm** | GND 坐标系下相机位置；低 16 + 高 16 拼成 int32 |
| `latitude` / `longitude` | int | **整型编码** | 度 = `lat × 180 / 2³²`，`lng × 360 / 2³²` |
| `gndOriginZ` | int | **mm** | GND 原点 Z，向下为正 |
| `altitude` | int | **mm** | 相对起飞点高度，向上为正；SDK 内 `gndOriginZ − cameraPosZ` |

**增量 SEI（incremental）**：码流首字节 `incremental=1` 时，该帧**只更新欧拉角**（及时间戳），
`cameraPosHigh` 与 `gnd_origin_lla` **不携带**。因此 `cameraPosX/Y/Z` 与 `latitude/longitude`
可能停留在上一完整帧，而姿态每帧刷新——合并缓存时注意「位置字段可能滞后于姿态」。

**地图打点常用换算**（以机场为 GND 原点，与 Demo `computeSeiCameraWgs84` 一致）：

```text
北向偏移(m) = cameraPosX / 1000
东向偏移(m) = cameraPosY / 1000
相机高度(m) = dockHeight + altitude / 1000
lat = dockLat + north / R × (180/π)
lng = dockLng + east / (R × cos(dockLat)) × (180/π)
```

无有效 `cameraPosX/Y` 时可回退飞机 MQTT 位置（秒级）。

#### FOV SEI（`FovSeiInfo`）

| 字段 | SDK 类型 | 单位 | 说明 |
|------|----------|------|------|
| `fovHorizontal` / `fovVertical` | long | **μm（微米）** | 有效成像宽/高，**不是视场角度数** |
| `curActualFocalLength` | long | **μm** | 当前实际焦距 |

视场角（度）需针孔模型自行换算：

```text
hFovDeg = 2 × atan(fovHorizontal / (2 × curActualFocalLength)) × 180 / π
vFovDeg = 2 × atan(fovVertical   / (2 × curActualFocalLength)) × 180 / π
```

所需 import（§6 全部）：
```java
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveModule;
```

---

## 7. 退订与清理 SEI / 直播（完整方法）

SEI 退订点可能有多个（停直播、断链路），用「id>=0 才退、退后置 -1」做成幂等。

```java
public void stopLive(DeviceContext ctx) {
    if (ctx == null) return;
    LiveModule live = ctx.getLiveModule();

    // 退裸帧
    if (ctx.getLiveStreamListenId() >= 0 && live != null) {
        try { live.unSubscribeLiveStream(ctx.getLiveStreamListenId()); } catch (Exception ignored) {}
        ctx.setLiveStreamListenId(-1);
    }
    // 退 AR SEI
    if (ctx.getArSeiListenId() >= 0 && live != null) {
        try { live.unSubscribeArSeiInfo(ctx.getArSeiListenId()); } catch (Exception ignored) {}
        ctx.setArSeiListenId(-1);
    }
    // 退 FOV SEI
    if (ctx.getFovSeiListenId() >= 0 && live != null) {
        try { live.unSubscribeFovSeiInfo(ctx.getFovSeiListenId()); } catch (Exception ignored) {}
        ctx.setFovSeiListenId(-1);
    }
    // 停直播
    if (live != null) {
        try { live.stop(); } catch (Exception ignored) {}
        ctx.setLiveModule(null);
    }
}
```

> 完整的会话拆除（退 OSD + 退 SEI + 停直播 + 停 payload 监听）见 §10 的 `deselect()`。

---

## 8. 必备工具：异步回调压平为同步

SDK 几乎所有**一次性请求**都是异步回调。用 `CountDownLatch + AtomicReference` 压平。

```java
CountDownLatch latch = new CountDownLatch(1);
AtomicReference<T> result = new AtomicReference<>();
AtomicReference<FlightHubError> errorRef = new AtomicReference<>();

someSdkApi.call((data, error) -> {
    if (error != null && error.isSuccess()) result.set(data);
    else errorRef.set(error);
    latch.countDown();
});

try {
    if (!latch.await(30, TimeUnit.SECONDS)) throw new IllegalStateException("timeout");
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new IllegalStateException("interrupted", e);
}
if (errorRef.get() != null) throw new IllegalStateException("failed: " + errorRef.get());
T value = result.get();
```

> 区分：**一次性请求**（getDeviceList / start）用 latch 压平；
> **持续订阅**（listenXxx / subscribeXxx）**不要 latch**，回调会反复触发。

---

## 9. 坑点清单（Skill 的价值所在）

1. **先 `loadLibrary()` 再 `initialize()`**，且初始化是进程级单例，不要重复 init。
2. **SEI 强依赖直播**：必须 `getLiveStream` → `start` 成功后再 `subscribeArSeiInfo`，
   否则 `LiveModule` 为 null，订阅被静默跳过 → 永远收不到 SEI。
3. **DRC 与 SEI 无硬依赖**：SEI 仅需 `LiveModule`；Demo 在 DRC 后才订阅 SEI 是产品编排，非 SDK 限制。
4. **MQTT 激光测距不保证可用**：`listenAircraftPayloadsOsdInfo` 现场常无测距；高频落点用 `listenDrcCameraMeasureTarget`。
5. **FOV 字段是 μm 不是角度**：`fovHorizontal/fovVertical` 须配合 `curActualFocalLength` 换算视场角。
6. **AR 增量帧**：`incremental=1` 时位置/经纬可能不更新，仅姿态刷新；合并缓存勿假设每帧位置都变。
7. **listenId 必须保存并退订**，否则换设备/退出后回调泄漏。
8. **回调在 SDK 线程执行**，不要阻塞或做重活；取值后尽快转交业务线程。
9. **离线设备**拿到句柄也收不到数据，订阅前先判 `isOnline()`。
10. **MQTT OSD ≠ SEI**：MQTT 秒级、不对齐画面；SEI 帧级、对齐画面。画面上像素反算经纬度只能用 SEI。
11. **一次性请求用 latch 压平，持续订阅不要 latch**。
12. **退订做成幂等**：SEI 在「停直播」和「断 DRC」两处都可清理，id 用后置 -1 防重复退订。

---

## 10. 端到端编排（完整 select / deselect）

把 §4~§7 串起来的真实编排，展示「选设备即订阅 OSD」「退设备即对称拆除」。

```java
public synchronized void select(String sessionId, String gatewaySn) {
    deselect(sessionId);   // 先清旧的

    // —— §4：解析拓扑 + 换句柄 ——
    DeviceTopo topo = deviceService.findTopoByGatewaySn(gatewaySn)
            .orElseThrow(() -> new IllegalArgumentException("device not found: " + gatewaySn));
    Device main = topo.getDevice();
    if (main == null || !main.isOnline()) throw new IllegalStateException("device offline");
    Device droneDevice = topo.getSubDevices().stream()
            .filter(s -> "drone".equalsIgnoreCase(s.getDeviceClass()))
            .findFirst().orElseThrow(() -> new IllegalStateException("no drone sub-device"));
    String droneSn = droneDevice.getDeviceSn();
    Drone drone = FlightHubSDK.getDroneInstance(droneSn);
    if (drone == null) throw new IllegalStateException("drone instance not found");

    DeviceContext ctx = new DeviceContext();
    ctx.setHttpSessionId(sessionId);
    ctx.setGatewaySn(gatewaySn);
    ctx.setDroneSn(droneSn);
    ctx.setDrone(drone);
    ctx.setDock(FlightHubSDK.getDockInstance(gatewaySn));
    registry.setDeviceContext(sessionId, ctx);

    // —— §5：订阅 OSD ——
    subscribeSlowOsd(ctx);
    subscribeDockOsd(ctx);

    // —— §6.1：开始监听可用直播范围（为后续开直播/SEI 备料）——
    payloadService.startWatch(ctx);

    // 开直播、建 DRC、订阅 SEI 由业务 API 触发（Demo：POST /live/start → POST /control/drc/connect）
    log.info("Selected gateway={} drone={}", gatewaySn, droneSn);
}

public synchronized void deselect(String sessionId) {
    DeviceContext ctx = registry.getDeviceContext(sessionId);
    if (ctx == null) return;

    // 对称拆除：断链路(含 SEI) → 退 OSD → 停直播(含 SEI/裸帧) → 停 payload 监听
    try { controlService.disconnectDrc(sessionId); } catch (Exception ignored) {}
    unsubscribeSlowOsd(ctx);
    unsubscribeDockOsd(ctx);
    liveService.stopLive(ctx);          // §7
    payloadService.stopWatch(ctx);      // §6.1
    ctx.clear();
    registry.clearDeviceContext(sessionId);
}
```

---

## 11. 承载对象：DeviceContext（完整定义）

单会话的状态袋。**`listenIds` Map + 独立 listenId 字段** 是「订阅—记 id—取消」模式的存储核心。

```java
public class DeviceContext {

    private String httpSessionId;          // 路由用，非 SDK 必需
    private String gatewaySn;
    private String droneSn;

    // —— SDK 句柄 ——
    private Drone drone;
    private Dock dock;
    private LiveModule liveModule;

    // —— 直播选择 ——
    private LiveInfo selectedLiveInfo;
    private List<LiveInfo> availablePayloads = new CopyOnWriteArrayList<>();

    // —— listenId 管理 ——
    // (a) 多路同质订阅(OSD)：用 key→id 的 Map
    private final Map<String, Integer> listenIds = new ConcurrentHashMap<>();
    // (b) 单一/特殊生命周期订阅：独立字段，便于单独判空与幂等退订
    private int liveStreamListenId   = -1;
    private int payloadRangeListenId = -1;
    private int arSeiListenId        = -1;
    private int fovSeiListenId        = -1;
    private boolean drcConnected      = false;

    // 存 OSD listenId（id>=0 才存，过滤 SDK 失败的负值）
    public void putListenId(String key, int id) {
        if (id >= 0) listenIds.put(key, id);
    }
    public Map<String, Integer> getListenIds() { return listenIds; }

    // ... 其余 getter/setter 省略 ...

    // 一次性归零（deselect 时调用）
    public void clear() {
        drone = null; dock = null; liveModule = null;
        selectedLiveInfo = null;
        availablePayloads = new CopyOnWriteArrayList<>();
        listenIds.clear();
        liveStreamListenId = payloadRangeListenId = arSeiListenId = fovSeiListenId = -1;
        drcConnected = false;
    }
}
```

所需 import：
```java
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.LiveModule;
```

---

## 12. 字段映射：TelemetryMapper（完整类）

把 SDK bean 转成扁平 `Map`。**这是「某路数据有哪些字段、getter 叫什么」的权威速查表。**

```java
public final class TelemetryMapper {
    private TelemetryMapper() {}

    public static Map<String, Object> location(AircraftLocation loc) {
        return Map.of(
                "latitude",  loc.getLatitude(),
                "longitude", loc.getLongitude(),
                "height",    loc.getHeight(),
                "elevation", loc.getElevation());
    }

    public static Map<String, Object> attitude(AircraftAttitude att) {
        return Map.of(
                "attitudeHead",  att.getAttitudeHead(),
                "attitudePitch", att.getAttitudePitch(),
                "attitudeRoll",  att.getAttitudeRoll());
    }

    public static Map<String, Object> speed(AircraftSpeed spd) {
        return Map.of(
                "horizontalSpeed", spd.getHorizontalSpeed(),
                "verticalSpeed",   spd.getVerticalSpeed());
    }

    public static Map<String, Object> battery(AircraftBatteryInfo bat) {
        return Map.of(
                "capacityPercent",  bat.getCapacityPercent(),
                "remainFlightTime", bat.getRemainFlightTime(),
                "returnHomePower",  bat.getReturnHomePower(),
                "landingPower",     bat.getLandingPower());
    }

    public static Map<String, Object> wind(AircraftWindSpeed wind) {
        return Map.of(
                "windSpeed",     wind.getWindSpeed(),
                "windDirection", wind.getWindDirection());
    }

    public static Map<String, Object> dockLocation(DockLocation loc) {
        return Map.of(
                "latitude",  loc.getLatitude(),
                "longitude", loc.getLongitude(),
                "height",    loc.getHeight(),
                "heading",   loc.getHeading());
    }

    public static Map<String, Object> gimbalAttitude(AircraftPayloadsGimbalAttitude gimbal) {
        List<Map<String, Object>> payloads = new ArrayList<>();
        if (gimbal.getPayloadsGimbalAttitude() != null) {
            for (AircraftPayloadGimbalAttitude p : gimbal.getPayloadsGimbalAttitude()) {
                Map<String, Object> item = new HashMap<>();
                item.put("payloadIndex", nullSafe(p.getPayloadIndex()));
                item.put("gimbalPitch",  p.getGimbalPitch());
                item.put("gimbalRoll",   p.getGimbalRoll());
                item.put("gimbalYaw",    p.getGimbalYaw());
                payloads.add(item);
            }
        }
        return Map.of("payloads", payloads);
    }

    // 快速通道测距（来自 listenDrcCameraMeasureTarget，DRC 链路）
    public static Map<String, Object> measureTarget(CameraMeasureTarget t) {
        return Map.of(
                "payloadIndex",     nullSafe(t.getPayloadIndex()),
                "latitude",         t.getLatitude(),
                "longitude",        t.getLongitude(),
                "altitude",         t.getAltitude(),
                "distance",         t.getDistance(),
                "laserMeasureState", t.getLaserMeasureState());
    }

    // OSD 通道测距（来自 listenAircraftPayloadsOsdInfo）
    public static Map<String, Object> measureTargetFromOsd(AircraftPayloadsOsdInfo osd) {
        return Map.of(
                "payloadIndex",     nullSafe(osd.getPayloadIndex()),
                "latitude",         osd.getMeasureTargetLatitude(),
                "longitude",        osd.getMeasureTargetLongitude(),
                "altitude",         osd.getMeasureTargetAltitude(),
                "distance",         osd.getMeasureTargetDistance(),
                "laserMeasureState", (int) osd.getMeasureTargetErrorState());
    }

    public static Map<String, Object> sei(ArSeiInfo ar, FovSeiInfo fov) {
        HashMap<String, Object> map = new HashMap<>();
        if (ar != null) {
            map.put("cameraYaw",   ar.getCameraYaw());
            map.put("cameraPitch", ar.getCameraPitch());
            map.put("cameraRoll",  ar.getCameraRoll());
            map.put("cameraPosX",  ar.getCameraPosX());
            map.put("cameraPosY",  ar.getCameraPosY());
            map.put("cameraPosZ",  ar.getCameraPosZ());
            map.put("latitude",    ar.getLatitude());
            map.put("longitude",   ar.getLongitude());
            map.put("altitude",    ar.getAltitude());
        }
        if (fov != null) {
            map.put("fovHorizontal",        fov.getFovHorizontal());
            map.put("fovVertical",          fov.getFovVertical());
            map.put("curActualFocalLength", fov.getCurActualFocalLength());
        }
        return map;
    }

    private static String nullSafe(String v) { return v == null ? "" : v; }
}
```

所需 import：
```java
import com.dji.flighthubsdk.bean.*;   // AircraftLocation / AircraftAttitude / ... / ArSeiInfo / FovSeiInfo
```

---

## 13. SDK API 速查（仅 Drone / LiveModule / Dock / Workspace）

> 自包含清单，方便 Skill 直接索引。签名为「方法 → 返回 → 说明」。

### Workspace
- `FlightHubSDK.getWorkspaceInstance()` → `Workspace`
- `workspace.getDeviceManageModule().getDeviceList(cb(List<DeviceTopo>, FlightHubError))` → 异步，列设备拓扑

### Drone（`FlightHubSDK.getDroneInstance(droneSn)`）
| 方法 | 返回 | 说明 |
|------|------|------|
| `getDroneOsdModule().listenAircraftLocation(cb)` | int listenId | 位置（MQTT） |
| `getDroneOsdModule().listenAircraftAttitude(cb)` | int | 姿态 |
| `getDroneOsdModule().listenAircraftSpeed(cb)` | int | 速度 |
| `getDroneOsdModule().listenAircraftBatteryInfo(cb)` | int | 电量 |
| `getDroneOsdModule().listenAircraftWindSpeed(cb)` | int | 风 |
| `getDroneOsdModule().listenAircraftPayloadsGimbalAttitude(cb)` | int | 云台 |
| `getDroneOsdModule().listenAircraftPayloadsOsdInfo(cb)` | int | payload OSD（含测距，**不保证有数据**） |
| `getLiveDeviceModule().listenLiveAvailableRange(cb)` | int | 可用直播负载 |
| `getDrcModule().connectDrcLink(cb(FlightHubError))` | — | 建 DRC 链路（异步） |
| `getDrcModule().disconnectDrcLink(cb(FlightHubError))` | — | 断 DRC |
| `getCameraControlModule().listenDrcCameraMeasureTarget(payload, cb)` | int | **DRC 快通道**激光测距 |
| `getCameraControlModule().cancelListenDrcCameraMeasureTarget(payload, id)` | — | 退订 DRC 测距 |
| `getControlAuthModule().acquirePayloadControlAuthority(payload, cb)` | — | 拿负载控制权（杆量/云台前） |
| `getFlightControlModule().drcSendStickControl(StickControlData)` | — | 杆量控制（DRC 链路） |
| `getCameraControlModule().drcResetGimbal(payload, mode, cb)` | — | 云台复位 |
| `getDroneOsdModule().cancelListenAircraftXxx(id)` | — | 对应退订 MQTT OSD |
| `getLiveDeviceModule().cancelListenLiveAvailableRange(id)` | — | 退订直播范围 |
| `getLiveStream(LiveInfo)` | `LiveModule` | 拿直播模块 |

### LiveModule（`drone.getLiveStream(liveInfo)`）
| 方法 | 返回 | 说明 |
|------|------|------|
| `start(cb(FlightHubError))` | — | 启动直播（异步） |
| `stop()` | — | 停止 |
| `subscribeLiveStream(cb(byte[], frameInfo))` | int | H.264 裸帧 |
| `subscribeArSeiInfo(cb(ArSeiInfo))` | int | AR SEI（姿态+GND 偏移，见 §6.5） |
| `subscribeFovSeiInfo(cb(FovSeiInfo))` | int | FOV SEI（μm 成像尺寸+焦距，见 §6.5） |
| `unSubscribeLiveStream(id)` | — | 退裸帧 |
| `unSubscribeArSeiInfo(id)` | — | 退 AR SEI |
| `unSubscribeFovSeiInfo(id)` | — | 退 FOV SEI |

### Dock（`FlightHubSDK.getDockInstance(gatewaySn)`）
| 方法 | 返回 | 说明 |
|------|------|------|
| `getDockOsdModule().listenDockLocation(cb)` | int | 机场位置 |
| `getDockOsdModule().cancelListenDockLocation(id)` | — | 退订 |
</content>
</invoke>
