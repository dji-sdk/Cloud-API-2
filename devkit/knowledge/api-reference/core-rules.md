---
title: "SDK 集成核心规则"
module: api-reference
tags: [core-rules, initialize, callback, listen-cancel, null-guard, control-authority]
audience: [agent, developer]
complexity: intermediate
summary: "6 条必须遵守的 SDK 集成核心规则"
last_updated: 2026-08-05
sdk_version: "3.3.7"
related:
  - api-reference/flight-control.md
  - api-reference/error-codes.md
  - api-reference/device-management.md
---

FlightHub SDK 有 6 条必须遵守的核心规则，违反任何一条都会导致静默失败或未定义行为。

# 核心规则详解

> **定位**：本文件是 `SKILL.md` 中 6 条核心规则的"为什么 + 完整示例"扩展，由 AI Agent 按需加载。  
> **来源**：`docs/sdk-protocol/01-FlightHubSDK.md`、`05-Drone-飞行控制.md`、`07-Drone-状态监听.md`、`14-Workspace.md`、`00-通用约定.md`

---

## 规则 1：initialize 必须先完成且检查返回值

### 为什么

FlightHub Java SDK 底层通过 C++/JNI 实现核心逻辑。调用 `initialize` 时，SDK 依次执行：

1. 加载 15+ 个平台原生库（.so / .dll / .dylib）
2. 建立 WebSocket 长连接（`notificationServerAddress`）
3. 注入网关鉴权头（gateway header）并拉取工作空间初始配置
4. 初始化内部 MQTT 路由表、设备注册表、回调分发器

**在 `initialize` 完成之前**，SDK 内部状态为"未就绪"：
- `getWorkspaceInstance()` 返回 `null`
- `getDroneInstance(sn)` / `getDockInstance(sn)` 返回 `null`
- 任何 listen / cancel 调用均无效

如果 `initialize` 返回非 `SUCCESS`，SDK 处于部分初始化状态，继续调用任何接口均属于未定义行为。

### API 签名

```java
// 主要重载（必填参数版本）
static FlightHubSdkErrorCode initialize(FlightHubSdkConfig config)

// 辅助方法
static FlightHubSdkErrorCode shutdown()    // 释放所有资源
static boolean isInitialized()             // 查询当前状态
```

**返回值枚举**：

| 枚举值 | 含义 | 处理方式 |
|--------|------|----------|
| `SUCCESS` | 初始化成功 | 继续调用其他接口 |
| `ERROR_INIT_FAILED` | 初始化失败（网络/库加载问题） | 检查地址、端口、鉴权头配置 |
| `ERROR_ALREADY_INITIALIZED` | 重复初始化 | 可忽略或先调用 `shutdown()` |

### FlightHubSdkConfig 必填字段

```java
FlightHubSdkConfig config = new FlightHubSdkConfig();
// ✅ 必填字段（缺少任何一项将返回 ERROR_INIT_FAILED）
config.setAddress("http://10.0.0.1");                         // 含协议前缀
config.setPort("6789");                                         // 端口号字符串
config.setNotificationServerAddress("ws://10.0.0.1:6790/ws"); // ws:// 或 wss:// 开头

// 可选字段（有合理默认值）
config.setLogLevel("info");           // debug / info / warn / error
config.setLogToConsole(true);
config.setLogFilePath("sdk.log");     // 文件路径或目录
config.setLogMaxFileSize(4194304L);   // 4 MB
config.setLogMaxFiles(1000);
config.setShowVersionInfo(true);
```

> ⚠️ **鉴权不在 config 里**：`FlightHubSdkConfig` 没有 `setToken` 方法。鉴权通过 **gateway header 注入**完成——在 `initialize` 之前调用 `FlightHubSDK.setHttpHeaders(...)` 与 `FlightHubSDK.setWebSocketAuthHeaders(...)`（见下方"第零步"）。

### Spring Boot 正确写法

```java
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.core.JniLibraryLoader;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import java.util.ArrayList;
import java.util.List;

/**
 * SDK 生命周期管理配置类。
 * 保证 Spring 容器启动时 SDK 已就绪，关闭时资源被释放。
 *
 * @author your-name
 */
@Configuration
@Slf4j
public class SdkLifecycleConfig {
    // ⚠️ 类名避开 SDK 的 com.dji.flighthubsdk.bean.FlightHubSdkConfig，防止命名冲突

    /** SDK 服务地址，含协议前缀，如 http://10.0.0.1 */
    @Value("${flighthub.sdk.address}")
    private String address;

    /** 服务端口 */
    @Value("${flighthub.sdk.port}")
    private String port;

    /** 鉴权头 key（网关约定的头名，如 user_id，不一定是 Authorization） */
    @Value("${flighthub.sdk.auth-key}")
    private String authKey;

    /** 鉴权头 value（网关约定的头值，如 paas_test） */
    @Value("${flighthub.sdk.auth-value}")
    private String authValue;

    /** WebSocket 通知地址，ws:// 或 wss:// 开头 */
    @Value("${flighthub.sdk.notification-server-address}")
    private String notificationServerAddress;

    /**
     * 应用启动后同步初始化 SDK。
     * 使用 @PostConstruct 保证在依赖该 Bean 的其他 Bean 初始化之前完成。
     *
     * @throws IllegalStateException SDK 初始化失败时抛出，阻止应用启动
     */
    @PostConstruct
    public void initSdk() {
        // 第零步：显式加载 JNI（否则鉴权头注入静默失效）
        // setHttpHeaders/setWebSocketAuthHeaders 走 native，JNI 未加载时二者
        // 只返回 ERROR_NOT_INITIALIZED（不抛异常）→ 鉴权头静默不注入 → 握手 401。
        // SDK 仅内置 linux-x86_64 与 macOS arm64 原生库，其它平台会在此加载失败。
        try {
            JniLibraryLoader.loadLibrary();
        } catch (Exception | LinkageError e) {
            // loadLibrary 可能抛 UnsatisfiedLinkError（Error），必须用 LinkageError 一并捕获
            log.error("加载 JNI 原生库失败（平台={}/{}）: {}",
                    System.getProperty("os.name"), System.getProperty("os.arch"), e.getMessage(), e);
            return;
        }

        // 第一步：注入鉴权头（必须在 initialize 之前，且检查返回值）
        List<HeaderKeyValue> headers = new ArrayList<>();
        headers.add(new HeaderKeyValue(authKey, authValue));

        FlightHubSdkErrorCode httpRc = FlightHubSDK.setHttpHeaders(headers);
        FlightHubSdkErrorCode wsRc = FlightHubSDK.setWebSocketAuthHeaders(headers);
        if (httpRc != FlightHubSdkErrorCode.SUCCESS || wsRc != FlightHubSdkErrorCode.SUCCESS) {
            throw new IllegalStateException(
                "鉴权头注入失败: http=" + httpRc + ", ws=" + wsRc);
        }

        // 第二步：构建 config 并 initialize
        com.dji.flighthubsdk.bean.FlightHubSdkConfig sdkConfig =
                new com.dji.flighthubsdk.bean.FlightHubSdkConfig();
        sdkConfig.setAddress(address);
        sdkConfig.setPort(port);
        sdkConfig.setNotificationServerAddress(notificationServerAddress);

        FlightHubSdkErrorCode code = FlightHubSDK.initialize(sdkConfig);

        // ✅ 必须检查返回值
        if (code == FlightHubSdkErrorCode.ERROR_ALREADY_INITIALIZED) {
            log.warn("SDK 已初始化，跳过重复初始化");
            return;
        }
        if (code != FlightHubSdkErrorCode.SUCCESS) {
            // 抛出异常阻止 Spring 启动，避免服务以半初始化状态对外提供服务
            throw new IllegalStateException(
                "FlightHub SDK 初始化失败，errorCode=" + code
                + "，请检查 address/port/notificationServerAddress 及鉴权头配置");
        }

        log.info("FlightHub SDK 初始化成功，版本：{}", FlightHubSDK.getVersion());
    }

    /**
     * 应用关闭时释放 SDK 资源（关闭 WebSocket 连接、取消所有订阅）。
     */
    @PreDestroy
    public void shutdownSdk() {
        if (FlightHubSDK.isInitialized()) {
            FlightHubSdkErrorCode code = FlightHubSDK.shutdown();
            if (code != FlightHubSdkErrorCode.SUCCESS) {
                log.error("FlightHub SDK shutdown 失败，errorCode={}", code);
            } else {
                log.info("FlightHub SDK 已正常关闭");
            }
        }
    }
}
```

### 非 Spring 正确写法（普通 Java 应用）

```java
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.core.JniLibraryLoader;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import java.util.ArrayList;
import java.util.List;

public class Application {

    private static final String SDK_ADDRESS  = "http://10.0.0.1";
    private static final String SDK_PORT     = "6789";
    private static final String SDK_AUTH_KEY = System.getenv("FH_SDK_AUTH_KEY");
    private static final String SDK_AUTH_VAL = System.getenv("FH_SDK_AUTH_VALUE");
    private static final String SDK_WS_ADDR  = "ws://10.0.0.1:6790/ws";

    public static void main(String[] args) {
        initSdkOrExit();

        // 注册 JVM 关闭钩子，确保即使 Ctrl+C 退出也能释放资源
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FlightHubSDK.shutdown();
            System.out.println("SDK 资源已释放");
        }));

        // 正常业务逻辑...
    }

    /**
     * 同步初始化 SDK，失败则打印错误并退出进程。
     */
    private static void initSdkOrExit() {
        // 第零步：显式加载 JNI（否则鉴权头注入静默失效 → 握手 401）
        JniLibraryLoader.loadLibrary();

        // 第一步：注入鉴权头（必须在 initialize 之前）
        List<HeaderKeyValue> headers = new ArrayList<>();
        headers.add(new HeaderKeyValue(SDK_AUTH_KEY, SDK_AUTH_VAL));
        FlightHubSDK.setHttpHeaders(headers);
        FlightHubSDK.setWebSocketAuthHeaders(headers);

        // 第二步：构建 config 并 initialize
        com.dji.flighthubsdk.bean.FlightHubSdkConfig config =
                new com.dji.flighthubsdk.bean.FlightHubSdkConfig();
        config.setAddress(SDK_ADDRESS);
        config.setPort(SDK_PORT);
        config.setNotificationServerAddress(SDK_WS_ADDR);

        FlightHubSdkErrorCode code = FlightHubSDK.initialize(config);
        if (code != FlightHubSdkErrorCode.SUCCESS) {
            System.err.println("SDK 初始化失败: " + code);
            System.exit(1); // 非 Spring 环境直接退出
        }
    }
}
```

### 错误写法（3 种）

```java
// ❌ 错误写法 1：不检查返回值
FlightHubSDK.initialize(config); // 可能返回 ERROR_INIT_FAILED，被静默忽略
Workspace ws = FlightHubSDK.getWorkspaceInstance(); // 返回 null
ws.getDeviceManageModule().getDeviceList(...); // NPE！

// ❌ 错误写法 2：异步线程初始化导致竞态
new Thread(() -> FlightHubSDK.initialize(config)).start();
// 主线程可能在初始化完成前继续执行
Drone drone = FlightHubSDK.getDroneInstance(sn); // null（初始化还没完成）

// ❌ 错误写法 3：不调 shutdown，资源泄漏
// 应用退出后 WebSocket 连接不被释放
// 下次启动可能收到 ERROR_ALREADY_INITIALIZED（进程内 JVM 重用场景）
```

---

## 规则 2：所有回调必须检查 errorCode

### 为什么

`FlightHubError` 是 4 层错误信息的聚合体，每一层描述不同维度的失败：

```
SDK 层（errorCode）
  └─ HTTP 层（httpCode）        ← 网络 / 服务可达性
       └─ 服务端层（serverCode） ← 业务逻辑错误
            └─ 设备层（deviceCode） ← 设备端执行错误
```

**关键点**：当 `errorCode != SUCCESS` 时，回调的 `result` 参数**不保证有效**：
- 可能为 `null`（请求未到达服务端或服务端未返回数据）
- 可能是部分填充的对象（serverCode 有值但 result 字段为空）
- 直接使用 `result` 而不检查 `error` 必然导致 NPE 或逻辑错误

### 完整 FlightHubError 结构

```java
FlightHubError error = ...; // 回调中收到

// 层 1：SDK 层枚举（首先检查）
FlightHubSdkErrorCode errorCode = error.getErrorCode();
// 可能值：SUCCESS / ERROR_INIT_FAILED / ERROR_NOT_INITIALIZED
//         ERROR_INVALID_PARAM / ERROR_OPERATION_FAILED

// 层 2：HTTP 状态码（errorCode == SUCCESS 但 httpCode != 200 时表示服务端异常）
int httpCode = error.getHttpCode(); // 0=网络层失败，200=正常

// 层 3：服务端业务码（0=成功，非0=业务逻辑错误）
int serverCode = error.getServerCode();

// 层 4：设备端错误码（0=无设备错误）
int deviceCode = error.getDeviceCode();

// 补充描述（服务端返回的错误文本）
String serverMessage = error.getServerMessage();
```

### 完整检查模式

```java
workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
    // ✅ 步骤 1：检查 SDK 层错误
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        log.error("获取设备列表失败（SDK层）: errorCode={}", error.getErrorCode());
        return;
    }

    // ✅ 步骤 2：检查服务端业务码（SDK层成功并不意味着服务端成功）
    if (error.getServerCode() != 0) {
        log.error("获取设备列表失败（服务端）: serverCode={}, message={}",
                  error.getServerCode(), error.getServerMessage());
        return;
    }

    // ✅ 步骤 3：防御性 null 检查（部分接口在成功时也可能返回空列表）
    if (devices == null) {
        log.warn("设备列表为空");
        return;
    }

    // ✅ 安全使用数据
    log.info("获取到 {} 台设备", devices.size());
    devices.forEach(topo -> processDevice(topo));
});
```

### 带设备层错误检查的飞控指令模式

```java
drone.getFlightControlModule().returnHome(error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        log.error("返航指令失败（SDK层）: errorCode={}", error.getErrorCode());
        return;
    }
    if (error.getServerCode() != 0) {
        log.error("返航指令被服务端拒绝: serverCode={}, message={}",
                  error.getServerCode(), error.getServerMessage());
        return;
    }
    if (error.getDeviceCode() != 0) {
        // 设备端拒绝执行：可能是电量不足、任务锁定等
        log.error("返航指令被设备拒绝: deviceCode={}", error.getDeviceCode());
        return;
    }
    log.info("返航指令已成功下发并执行");
});
```

### 错误写法

```java
// ❌ 错误写法 1：完全不检查 error，直接使用 result
workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
    devices.forEach(d -> ...); // 当请求失败时 devices=null → NPE
});

// ❌ 错误写法 2：只检查 errorCode，忽略 serverCode
workspace.getWaylineManageModule().getWaylineList(params, (error, waylines) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return; // 看似正确
    }
    // 问题：errorCode=SUCCESS 但 serverCode=400（鉴权失败）时
    // waylines 为 null，接下来的 waylines.size() 仍然 NPE
    log.info("航线数量：{}", waylines.size());
});

// ❌ 错误写法 3：用 try-catch 掩盖 null（错误方向）
workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
    try {
        devices.forEach(d -> ...);
    } catch (NullPointerException e) {
        // 吞掉异常，根本原因是没检查 error，不是 NPE
    }
});
```

---

## 规则 3：listenId 必须保存，关闭时必须 cancel

### 为什么

`listenXxx(callback)` 注册的是**持续推送**订阅，不是一次性回调：
- SDK 内部维护一张"订阅映射表"，每次数据变化都遍历表中所有回调并触发
- 如果不调用 `cancelListenXxx`，该条目**永远留在表中**
- 长期运行的服务会面临：回调泄漏（内存）、CPU 浪费（无效触发）、重复处理（同一事件多次触发）

`listenId` 是 SDK 用来定位和删除特定订阅的唯一凭证：
- `≥ 0`：注册成功，必须保存
- `< 0`：注册失败（如 SDK 未初始化、设备实例无效）

### listen / cancel 协议

```java
// 订阅接口（通过 DroneOsdModule 访问，每对接口共用一个 listenId 命名空间）
DroneOsdModule osd = drone.getDroneOsdModule();
int listenId = osd.listenAircraftLocation(callback);  // 返回 int，≥0 成功

// 取消接口
osd.cancelListenAircraftLocation(listenId);           // 无返回值

// 常见 listen/cancel 对（部分举例，均通过 getDroneOsdModule() 访问）：
// osd.listenAircraftLocation    / osd.cancelListenAircraftLocation
// osd.listenAircraftBatteryInfo / osd.cancelListenAircraftBatteryInfo
// osd.listenAircraftSpeed       / osd.cancelListenAircraftSpeed
// HMS 告警通过 getDeviceHMSModule()：
// hms.listenDeviceHMS            / hms.cancelListenDeviceHMS
// 控制权监听通过 getControlAuthModule()：
// auth.listenFlightControlAuthority / auth.cancelListenFlightControlAuthority
```

### 单设备 Spring Boot 模式

```java
/**
 * 单架无人机遥测订阅组件。
 * 演示最基础的 listenId 保存与 @PreDestroy cancel 模式。
 *
 * @author your-name
 */
@Component
@Slf4j
public class DroneTelemetryListener {

    /** 无人机序列号，从配置读取 */
    @Value("${flighthub.drone.sn}")
    private String droneSn;

    /** 保存 listenId，-1 表示未订阅 */
    private int locationListenId = -1;
    private int batteryListenId  = -1;

    /**
     * 服务启动后订阅位置和电量数据。
     */
    @PostConstruct
    public void subscribe() {
        Drone drone = FlightHubSDK.getDroneInstance(droneSn);
        if (drone == null) {
            log.warn("Drone 实例不存在，SN={}，跳过订阅", droneSn);
            return;
        }

        DroneOsdModule osd = drone.getDroneOsdModule();

        // 订阅位置数据（纬度/经度/高度/海拔）
        locationListenId = osd.listenAircraftLocation(loc -> {
            log.debug("位置更新：lat={}, lng={}, height={}m",
                      loc.getLatitude(), loc.getLongitude(), loc.getHeight());
        });
        if (locationListenId < 0) {
            log.error("订阅位置失败，listenId={}，SN={}", locationListenId, droneSn);
        } else {
            log.info("位置订阅成功，listenId={}", locationListenId);
        }

        // 订阅电量数据
        batteryListenId = osd.listenAircraftBatteryInfo(bat -> {
            log.debug("电量更新：capacity={}%，remainTime={}s",
                      bat.getCapacityPercent(), bat.getRemainFlightTime());
        });
        if (batteryListenId < 0) {
            log.error("订阅电量失败，listenId={}", batteryListenId);
        }
    }

    /**
     * 服务关闭时取消所有订阅，防止回调泄漏。
     */
    @PreDestroy
    public void unsubscribe() {
        Drone drone = FlightHubSDK.getDroneInstance(droneSn);
        if (drone == null) {
            return;
        }

        DroneOsdModule osd = drone.getDroneOsdModule();
        if (locationListenId >= 0) {
            osd.cancelListenAircraftLocation(locationListenId);
            log.info("位置订阅已取消，listenId={}", locationListenId);
        }
        if (batteryListenId >= 0) {
            osd.cancelListenAircraftBatteryInfo(batteryListenId);
            log.info("电量订阅已取消，listenId={}", batteryListenId);
        }
    }
}
```

### 多设备管理模式（生产推荐）

```java
/**
 * 多设备遥测订阅管理器。
 * 用 ConcurrentHashMap 维护每台设备的所有订阅，支持动态添加/移除设备。
 *
 * @author your-name
 */
@Component
@Slf4j
public class DeviceTelemetryManager {

    /**
     * Key: 设备 SN；Value: 该设备所有订阅的取消操作列表。
     * 使用 Runnable 封装 cancelXxx 调用，统一执行接口。
     */
    private final ConcurrentHashMap<String, List<Runnable>> cancelActions =
            new ConcurrentHashMap<>();

    /**
     * 为指定无人机注册所有遥测订阅。
     *
     * @param sn 无人机序列号
     * @throws IllegalArgumentException 设备实例不存在时
     */
    public void trackDrone(String sn) {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone == null) {
            throw new IllegalArgumentException("Drone 实例不存在或已离线: " + sn);
        }

        List<Runnable> cancels = cancelActions.computeIfAbsent(sn,
                k -> new java.util.concurrent.CopyOnWriteArrayList<>());

        DroneOsdModule osd = drone.getDroneOsdModule();

        // 订阅速度
        int speedId = osd.listenAircraftSpeed(speed -> handleSpeed(sn, speed));
        if (speedId >= 0) {
            cancels.add(() -> osd.cancelListenAircraftSpeed(speedId));
            log.info("[{}] 速度订阅成功，listenId={}", sn, speedId);
        }

        // 订阅位置
        int locId = osd.listenAircraftLocation(loc -> handleLocation(sn, loc));
        if (locId >= 0) {
            cancels.add(() -> osd.cancelListenAircraftLocation(locId));
        }

        // 订阅电池
        int batId = osd.listenAircraftBatteryInfo(bat -> handleBattery(sn, bat));
        if (batId >= 0) {
            cancels.add(() -> osd.cancelListenAircraftBatteryInfo(batId));
        }

        log.info("[{}] 共注册 {} 个遥测订阅", sn, cancels.size());
    }

    /**
     * 移除指定设备的所有订阅。
     *
     * @param sn 设备序列号
     */
    public void removeDrone(String sn) {
        List<Runnable> cancels = cancelActions.remove(sn);
        if (cancels != null) {
            cancels.forEach(Runnable::run);
            log.info("[{}] 已取消 {} 个遥测订阅", sn, cancels.size());
        }
    }

    /**
     * 应用关闭时取消所有设备的所有订阅。
     */
    @PreDestroy
    public void removeAll() {
        log.info("正在取消所有设备订阅，设备数={}", cancelActions.size());
        cancelActions.keySet().forEach(this::removeDrone);
    }

    // --- 私有事件处理方法 ---

    private void handleSpeed(String sn, Object speed) {
        // 处理速度数据...
    }

    private void handleLocation(String sn, Object loc) {
        // 处理位置数据...
    }

    private void handleBattery(String sn, Object bat) {
        // 处理电池数据...
    }
}
```

### 错误写法

```java
// ❌ 错误写法 1：不保存 listenId → 无法 cancel → 回调永久泄漏
drone.getDroneOsdModule().listenAircraftLocation(loc -> log.info("位置: {}", loc)); // listenId 被丢弃

// ❌ 错误写法 2：只在特定条件下 cancel，导致分支遗漏
@PreDestroy
public void stop() {
    if (someCondition) { // 条件不满足时 cancel 永远不执行
        drone.getDroneOsdModule().cancelListenAircraftLocation(listenId);
    }
}

// ❌ 错误写法 3：不检查 listenId < 0，对无效 listenId 调用 cancel
DroneOsdModule osd = drone.getDroneOsdModule();
int listenId = osd.listenAircraftLocation(callback); // 返回 -1（失败）
osd.cancelListenAircraftLocation(listenId); // 传入 -1，行为未定义

// ❌ 错误写法 4：服务重启后重复订阅，没有先 cancel 旧的
// 每次服务启动都新增一条订阅，同一事件被回调 N 次
public void restart() {
    subscribe(); // 未 cancel 之前的订阅
}
```

---

## 规则 4：飞控指令必须先抢权、用完释放

### 为什么

FlightHub 支持多操作员同时连接（多个 BFF 实例、Web 控制台、遥控器），飞控权限是**互斥资源**：

- 任何时刻只有一个操作员持有飞控权
- 未抢权就发送指令，服务端返回权限错误（`errorCode = ERROR_OPERATION_FAILED`，`serverCode` 为权限相关码）
- 抢权后不释放，其他操作员将持续等待；遥控器也无法接管
- 特殊情况：遥控器切回手动档会强制夺取控制权，此时 `listenFlightControlAuthority` 回调触发 `isGranted=false`

### API 签名

```java
// 通过 ControlAuthModule 访问（drone.getControlAuthModule()）
// 获取飞控权（异步回调）
void acquireFlightControlAuthority(ResultCallbackWithError callback)

// 释放飞控权（异步回调）
void releaseFlightControlAuthority(ResultCallbackWithError callback)

// 监听飞控权状态变化（持续推送，返回 listenId）
int listenFlightControlAuthority(ControlAuthorityStatusCallback callback)
void cancelListenFlightControlAuthority(int listenId)

// 回调接口：
// ResultCallbackWithError: void onResult(FlightHubError error)
// ControlAuthorityStatusCallback: void onControlAuthorityStatus(boolean isGranted)
```

### 完整模板：safeExecuteFlightCommand

```java
/**
 * 飞控指令执行模板类。
 * 封装"抢权 → 指令 → 释放"完整流程，保证权限在任何情况下都被释放。
 *
 * @author your-name
 */
@Service
@Slf4j
public class FlightCommandService {

    /**
     * 安全执行飞控指令的通用模板。
     * 流程：acquireFlightControlAuthority → 执行 command → releaseFlightControlAuthority
     *
     * @param drone   目标无人机实例（必须非 null）
     * @param command 飞控指令 lambda（在抢权成功后被调用）
     */
    public void safeExecuteFlightCommand(Drone drone, FlightCommand command) {
        log.info("开始抢占飞控权...");

        ControlAuthModule auth = drone.getControlAuthModule();
        auth.acquireFlightControlAuthority(acquireError -> {
            // ✅ 检查抢权结果
            if (acquireError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log.error("获取飞控权失败: errorCode={}, serverCode={}",
                          acquireError.getErrorCode(), acquireError.getServerCode());
                return; // 抢权失败，不执行指令，也无需释放
            }

            log.info("飞控权已获取，执行指令...");

            // ✅ 执行具体飞控指令，并在回调中确保释放权限
            command.execute(cmdError -> {
                // ✅ 无论指令成败，always 释放飞控权（等效于 finally）
                releaseSafely(drone);

                if (cmdError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                    log.error("飞控指令失败: errorCode={}, serverCode={}, deviceCode={}",
                              cmdError.getErrorCode(),
                              cmdError.getServerCode(),
                              cmdError.getDeviceCode());
                } else {
                    log.info("飞控指令执行成功");
                }
            });
        });
    }

    /**
     * 安全释放飞控权（忽略释放失败日志，但记录 warn）。
     *
     * @param drone 无人机实例
     */
    private void releaseSafely(Drone drone) {
        drone.getControlAuthModule().releaseFlightControlAuthority(releaseError -> {
            if (releaseError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log.warn("释放飞控权失败（可能已被其他人抢占）: errorCode={}",
                         releaseError.getErrorCode());
            } else {
                log.info("飞控权已释放");
            }
        });
    }

    /**
     * 飞控指令函数式接口。
     */
    @FunctionalInterface
    public interface FlightCommand {
        /**
         * 执行飞控指令，并在指令完成后通过 resultCallback 通知结果。
         *
         * @param resultCallback 指令完成回调
         */
        void execute(ResultCallbackWithError resultCallback);
    }
}
```

### 使用模板执行常见指令

```java
@Service
@Slf4j
public class DroneFlightService {

    private final FlightCommandService flightCommandService;

    public DroneFlightService(FlightCommandService flightCommandService) {
        this.flightCommandService = flightCommandService;
    }

    /**
     * 执行返航指令。
     *
     * @param sn 无人机序列号
     */
    public void returnHome(String sn) {
        Drone drone = requireDrone(sn);
        flightCommandService.safeExecuteFlightCommand(
            drone,
            callback -> drone.getFlightControlModule().returnHome(callback) // ✅ 模板保证先抢权再调
        );
    }

    /**
     * 执行紧急停止指令。
     *
     * @param sn 无人机序列号
     */
    public void emergencyStop(String sn) {
        Drone drone = requireDrone(sn);
        flightCommandService.safeExecuteFlightCommand(
            drone,
            callback -> drone.getFlightControlModule().drcEmergencyStop(callback)
        );
    }

    /**
     * 执行起飞指令（带参数）。
     *
     * @param sn     无人机序列号
     * @param params 起飞参数
     */
    public void takeoff(String sn, TakeoffParams params) {
        Drone drone = requireDrone(sn);
        flightCommandService.safeExecuteFlightCommand(
            drone,
            callback -> drone.getTakeoffModule().takeoff(params, (takeoffError, result) -> {
                if (takeoffError.getErrorCode() == FlightHubSdkErrorCode.SUCCESS) {
                    log.info("起飞成功，flightId={}", result.getFlightId());
                }
                callback.onResult(takeoffError); // 转发给模板的 release 逻辑
            })
        );
    }

    /**
     * 获取 Drone 实例，不存在则抛出异常。
     *
     * @param sn 序列号
     * @return Drone 实例
     * @throws IllegalArgumentException 设备不存在或离线
     */
    private Drone requireDrone(String sn) {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone == null) {
            throw new IllegalArgumentException("Drone 不存在或已离线: " + sn);
        }
        return drone;
    }
}
```

### 需要抢权的操作 vs 不需要抢权的操作

| 类别 | 方法 | 是否需要 acquireFlightControlAuthority |
|------|------|---------------------------------------|
| **飞行控制（需要抢权）** | `getTakeoffModule().takeoff()` | ✅ 是 |
| | `getFlightControlModule().returnHome()` / `returnHomeCancel()` | ✅ 是 |
| | `getFlightControlModule().drcEmergencyStop()` / `drcEmergencyLanding()` / `drcForceLanding()` | ✅ 是 |
| | `getFlightControlModule().drcSendStickControl()` (DRC 摇杆) | ✅ 是（且需先 `connectDrcLink`） |
| | `getFlyToPointModule()` 指点飞行 | ✅ 是 |
| | 暂停/恢复航线任务 | ✅ 是 |
| **状态监听（不需要抢权）** | 所有 `listenXxx` / `cancelListenXxx` | ❌ 否 |
| **查询类（不需要抢权）** | `getDeviceList` / `getDeviceDetailInfo` | ❌ 否 |
| **负载控制（不同权限）** | 相机拍照/录像/云台控制 | ❌ 需 `getControlAuthModule().acquirePayloadControlAuthority(payloadIndex, cb)` |

### 错误写法

```java
// ❌ 错误写法 1：未抢权直接发指令
drone.getFlightControlModule().returnHome(error -> {
    // 服务端返回权限错误：serverCode 表示无飞控权
    if (error.getServerCode() != 0) {
        log.error("返航失败"); // 根因是没有先抢权
    }
});

// ❌ 错误写法 2：抢权后忘记释放（常见于指令成功路径忘写 release）
ControlAuthModule auth = drone.getControlAuthModule();
auth.acquireFlightControlAuthority(acquireError -> {
    if (acquireError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) return;

    drone.getFlightControlModule().returnHome(cmdError -> {
        if (cmdError.getErrorCode() == FlightHubSdkErrorCode.SUCCESS) {
            log.info("返航成功");
            // ❌ 没有调用 releaseFlightControlAuthority！
            // 其他操作员将无法获取控制权，直到超时
        }
        // 失败路径同样没有释放
    });
});

// ❌ 错误写法 3：在相机操作时用飞控权（应用负载控制权）
auth.acquireFlightControlAuthority(acquireError -> { // 错误权限类型
    if (acquireError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) return;
    drone.getCameraControlModule().drcStartRecord("81-0-0", error -> {}); // 相机操作应用 acquirePayloadControlAuthority
});
```

---

## 规则 5：getDroneInstance / getDockInstance / 模块 getter 可能返回 null

### 为什么

`getDroneInstance(sn)` / `getDockInstance(sn)` 返回 `null` 的 4 种条件：

| 条件 | 说明 | 排查方式 |
|------|------|----------|
| **SDK 未初始化** | `initialize()` 未调用或失败 | 检查 `FlightHubSDK.isInitialized()` |
| **SN 不在工作空间设备列表** | 设备未绑定到当前 workspace | 调用 `workspace.getDeviceManageModule().getDeviceList()` 确认 SN |
| **设备当前离线** | 设备失联或关机 | 检查 `DeviceTopo.device.isOnline` |
| **SN 拼写错误** | 序列号大小写错误或多余空格 | 打印 SN 逐字符对比 |

**模块 getter**（如 `drone.getCameraControlModule()`、`drone.getDeviceHMSModule()` 等）也可能返回 `null`——
当设备类型/固件不支持该模块时会如此。生产代码**必须对模块 getter 返回值做 null 检查**再调用其方法。

### 防御模式：requireDrone / requireDock 工具方法

```java
/**
 * 设备工具类，提供防御性的设备实例获取方法。
 * 遵循快速失败原则：实例不存在时立即抛出异常，避免 NPE 在深层代码中爆发。
 *
 * @author your-name
 */
public final class DeviceUtil {

    private DeviceUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 获取无人机实例，不存在则抛出异常。
     *
     * @param sn 无人机序列号
     * @return Drone 实例（非 null）
     * @throws DeviceNotFoundException 设备不存在或已离线
     */
    public static Drone requireDrone(String sn) {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone == null) {
            throw new DeviceNotFoundException(
                "Drone 实例不存在：sn=" + sn + "，请确认设备已绑定工作空间且在线");
        }
        return drone;
    }

    /**
     * 获取机场实例，不存在则抛出异常。
     *
     * @param sn 机场序列号
     * @return Dock 实例（非 null）
     * @throws DeviceNotFoundException 设备不存在或已离线
     */
    public static Dock requireDock(String sn) {
        Dock dock = FlightHubSDK.getDockInstance(sn);
        if (dock == null) {
            throw new DeviceNotFoundException(
                "Dock 实例不存在：sn=" + sn + "，请确认机场已绑定工作空间且在线");
        }
        return dock;
    }

    /**
     * 安全获取无人机实例，不存在时返回空 Optional。
     * 适合不需要快速失败的场景（如轮询、批量查询）。
     *
     * @param sn 无人机序列号
     * @return 包含 Drone 的 Optional，或 Optional.empty()
     */
    public static Optional<Drone> findDrone(String sn) {
        return Optional.ofNullable(FlightHubSDK.getDroneInstance(sn));
    }

    /**
     * 安全获取机场实例，不存在时返回空 Optional。
     *
     * @param sn 机场序列号
     * @return 包含 Dock 的 Optional，或 Optional.empty()
     */
    public static Optional<Dock> findDock(String sn) {
        return Optional.ofNullable(FlightHubSDK.getDockInstance(sn));
    }
}
```

### DeviceNotFoundException 自定义异常

```java
/**
 * 设备实例不存在异常。
 * 当请求的无人机或机场未绑定、离线或 SN 有误时抛出。
 *
 * @author your-name
 */
public class DeviceNotFoundException extends RuntimeException {

    /**
     * @param message 包含 SN 和原因的详细描述
     */
    public DeviceNotFoundException(String message) {
        super(message);
    }

    /**
     * @param message 详细描述
     * @param cause   原始异常
     */
    public DeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 配合设备列表获取有效 SN

```java
/**
 * 获取工作空间内所有在线设备的 SN 列表。
 * 正确的 SN 来源：从 getDeviceList 返回的 DeviceTopo 中读取，不要硬编码。
 *
 * @author your-name
 */
@Service
@Slf4j
public class DeviceDiscoveryService {

    /**
     * 查询所有设备并打印在线无人机列表。
     * DeviceTopo 层级：机场（dock）→ subDevices（挂载的无人机）
     */
    public void discoverDevices() {
        Workspace workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log.error("Workspace 实例为 null，SDK 可能未初始化");
            return;
        }

        workspace.getDeviceManageModule().getDeviceList((deviceTopos, error) -> {
            // ✅ 先检查 error（规则 2）
            if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log.error("获取设备列表失败: {}", error.getErrorCode());
                return;
            }
            if (deviceTopos == null || deviceTopos.isEmpty()) {
                log.warn("工作空间内无设备");
                return;
            }

            // 遍历设备拓扑：顶层是机场，subDevices 是挂载的无人机
            for (DeviceTopo topo : deviceTopos) {
                String dockSn = topo.getDevice().getDeviceSn();
                boolean dockOnline = topo.getDevice().isOnline();
                log.info("机场 SN={}, 在线={}", dockSn, dockOnline);

                // ✅ 从 subDevices 获取无人机 SN（不要猜测或硬编码）
                if (topo.getSubDevices() != null) {
                    for (Device subDevice : topo.getSubDevices()) {
                        String droneSn = subDevice.getDeviceSn();
                        boolean droneOnline = subDevice.isOnline();
                        log.info("  └─ 无人机 SN={}, 在线={}", droneSn, droneOnline);

                        // ✅ 只对在线设备调用 getDroneInstance
                        if (droneOnline) {
                            Drone drone = FlightHubSDK.getDroneInstance(droneSn);
                            if (drone != null) {
                                log.info("     Drone 实例获取成功，可开始订阅");
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 安全地为所有在线无人机执行操作。
     *
     * @param action 对每台在线无人机执行的操作
     */
    public void forEachOnlineDrone(java.util.function.Consumer<Drone> action) {
        Workspace workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            return;
        }

        workspace.getDeviceManageModule().getDeviceList((deviceTopos, error) -> {
            if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS
                    || deviceTopos == null) {
                return;
            }

            deviceTopos.stream()
                // 展开 subDevices（无人机）
                .flatMap(topo -> topo.getSubDevices() == null
                        ? java.util.stream.Stream.empty()
                        : topo.getSubDevices().stream())
                // 只处理在线设备
                .filter(Device::isOnline)
                .forEach(device -> {
                    String sn = device.getDeviceSn();
                    // ✅ 仍然需要 null 检查（并发场景下设备可能在查询后离线）
                    Drone drone = FlightHubSDK.getDroneInstance(sn);
                    if (drone == null) {
                        log.warn("设备列表标记在线但实例为 null，SN={}（可能刚离线）", sn);
                        return;
                    }
                    action.accept(drone);
                });
        });
    }
}
```

### 错误写法

```java
// ❌ 错误写法 1：直接调用，无 null 检查 → NPE
FlightHubSDK.getDroneInstance(sn).getDroneOsdModule().listenAircraftLocation(callback);

// ❌ 错误写法 2：检查了 null 但仍在 null 分支继续调用
Drone drone = FlightHubSDK.getDroneInstance(sn);
if (drone == null) {
    log.warn("Drone 为 null");
}
drone.getFlightControlModule().returnHome(error -> {}); // ❌ drone 仍可能是 null！（应该在 warn 后 return）

// ❌ 错误写法 3：硬编码 SN，设备迁移后失效
String sn = "1ZNBJ1A0021234"; // 硬编码
Drone drone = FlightHubSDK.getDroneInstance(sn); // 可能 null（SN 换了/拼写错）

// ❌ 错误写法 4：不检查 isOnline 就获取实例（离线设备返回 null）
deviceTopos.forEach(topo -> {
    topo.getSubDevices().forEach(sub -> {
        // 离线设备的 getDroneInstance 返回 null，未加 null 检查
        Drone drone = FlightHubSDK.getDroneInstance(sub.getDeviceSn());
        drone.getDroneOsdModule().listenAircraftLocation(loc -> {}); // NPE（设备离线）
    });
});

// ✅ 正确：检查在线状态 + 检查实例是否 null
deviceTopos.forEach(topo -> {
    if (topo.getSubDevices() == null) return;
    topo.getSubDevices().stream()
        .filter(Device::isOnline)
        .forEach(sub -> {
            Drone drone = FlightHubSDK.getDroneInstance(sub.getDeviceSn());
            if (drone == null) {
                log.warn("设备在线但实例为 null: {}", sub.getDeviceSn());
                return;
            }
            drone.getDroneOsdModule().listenAircraftLocation(loc -> {});
        });
});
```

---

## 速查汇总

| 规则 | 核心记忆 | 最常见错误 |
|------|----------|------------|
| 1. initialize | 同步调用 + 检查返回值 + @PreDestroy shutdown | 异步线程初始化竞态 / 不检查返回值 |
| 2. errorCode | 先检查 errorCode，再检查 serverCode，再用数据 | 跳过检查直接调用 result 的方法 |
| 3. listenId | 订阅必须保存 listenId，关闭必须 cancel | listenId 被丢弃 / @PreDestroy 忘写 cancel |
| 4. 飞控权 | acquire → 指令 → release（无论成败） | 不 acquire / acquire 后不 release |
| 5. null 检查 | getDroneInstance / getDockInstance 必须判 null | 直接链式调用 / 只检查 isOnline 不检查实例 |
