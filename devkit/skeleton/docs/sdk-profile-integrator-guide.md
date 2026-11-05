# SDK Profile 切换集成商操作手册

> **适用版本**：fh-skeleton v0.1 (Plan A3 完成后)
> **背景**：Plan A3 提供 `mock` 和 `sdk` 两个 Spring profile。默认 profile 为 `sdk`，`mock` 为本地开发的可选项，需显式启用（开发态体验）；`sdk` 留作真 SDK 适配层（后续 A3b plan）。本文档说明集成商如何在两者之间切换。

---

## TL;DR

> **前置**：本页所有 `mvn -pl app/fh-skeleton-app ...` 命令只构建 `app` 模块，`com.dji.fh:*:0.1.0`
> 的同项目模块从 `~/.m2` 解析。首次使用请先在 `skeleton/` 执行 `mvn clean install -DskipTests`
> （并按 §5.1 安装与本机架构匹配的 `flight-hub-sdk`），否则会报
> `Could not resolve dependencies ... kernel-impl:jar:0.1.0`。
> `-am` 不能替代：加上后 `spring-boot:*` 前缀会解析不到插件。

| 场景 | profile | 启动命令 |
|---|---|---|
| 本地开发 / Demo / 集成测试 | `mock`（需显式启用） | `mvn -pl app/fh-skeleton-app spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mock"` |
| 生产 / 接真飞行平台（**需等 A3b plan**） | `sdk`（默认 profile） | `java -jar fh-skeleton-app-0.1.0.jar --spring.profiles.active=sdk` |

---

## 1. 两个 profile 的核心差异

### `mock` profile（需显式启用 / 开发态）

装配以下 bean（来自 `kernel-mock-sdk` 模块）：

- `MockDeviceRegistry` — 虚拟设备注册表（1 drone + 1 dock）
- `MockFlightHubSdk` — 伪 SDK 入口（5 callback 类型：listen/cancel/fire）
- `MockOsdScheduler` — `@Scheduled` 1Hz 推送 OSD
- `MockSdkAdapter` — Mock 5 callback → KernelEvent (`source=mock-sdk`)
- `MockEventTrigger` — 触发 API（脚本 + REST 共用）
- `MockEventScripter` — 启动脚本回放（默认 3s drone 上线 / 4s dock 上线 / 30s 飞行任务）
- `MockRestController` — POST `/pf-devkit/ai-plugin/skeleton/mock/devices/{sn}/online|offline|osd|flight-task|hms`
- `MockSdkStarter` — `@EventListener(ApplicationReadyEvent)` 启动序列
- `KernelReadyEvent(mode=MOCK)` 在 ApplicationReady 后被发布

### `sdk` profile（生产 / 真 SDK）

`@ConditionalOnProperty("fh.skeleton.sdk.mode", havingValue="mock", matchIfMissing=false)` 切到 `sdk` 后，**整个 `MockSdkAutoConfiguration` 不装配**：

- 上述全部 Mock bean **不存在**
- `MockRestController` 不暴露任何端点（请求会返回 Spring 默认 `404 Not Found`）
- **没有 SDK adapter bean，KernelReadyEvent 也不会被发布**（这是 v0.1 的已知 gap，A3b plan 会补）
- 生产 0 泄漏面：mock 相关类完全不入运行时上下文

---

## 2. 切换方式（4 种，等价）

### 2.1 命令行 `--spring.profiles.active`

```bash
java -jar fh-skeleton-app-0.1.0.jar --spring.profiles.active=sdk
```

### 2.2 环境变量 `SPRING_PROFILES_ACTIVE`

```bash
export SPRING_PROFILES_ACTIVE=sdk
java -jar fh-skeleton-app-0.1.0.jar
```

### 2.3 集成商别名环境变量 `FH_SKELETON_PROFILE`（推荐 ops）

`application.yml` 中 `spring.profiles.active=${FH_SKELETON_PROFILE:sdk}`，所以：

```bash
export FH_SKELETON_PROFILE=sdk
java -jar fh-skeleton-app-0.1.0.jar
```

### 2.4 JVM 系统属性

```bash
java -Dspring.profiles.active=sdk -jar fh-skeleton-app-0.1.0.jar
```

---

## 3. 验证 profile 是否生效

### 验证 mock profile

```bash
$ mvn -pl app/fh-skeleton-app spring-boot:run \
    -Dspring-boot.run.arguments="--spring.profiles.active=mock"
# 5 秒后：
$ curl -s http://localhost:8080/pf-devkit/ai-plugin/skeleton/actuator/health
{"status":"UP"}

# 启动日志应含：
The following 1 profile is active: "mock"
Mock SDK 正在初始化（device sn=MOCK-DRONE-001, dock sn=MOCK-DOCK-001）
mock script 启动，回放 3 条事件
mock OSD tick：向 2 个在线设备推送（[MOCK-DOCK-001, MOCK-DRONE-001]）  ← DEBUG，每秒一拍

# REST 端点存在：
$ curl -i -X POST http://localhost:8080/pf-devkit/ai-plugin/skeleton/mock/devices/MOCK-DRONE-001/online
HTTP/1.1 202
{"status":"fired"}
```

### 验证 sdk profile

```bash
$ mvn -pl app/fh-skeleton-app spring-boot:run \
    -Dspring-boot.run.arguments="--spring.profiles.active=sdk"
# 启动日志应含：
The following 1 profile is active: "sdk"
Started FhSkeletonApplication in ... seconds

# 启动日志 NO "Mock SDK 正在初始化"

# REST 端点不存在（Spring 默认 404）：
$ curl -i -X POST http://localhost:8080/pf-devkit/ai-plugin/skeleton/mock/devices/MOCK-DRONE-001/online
HTTP/1.1 404
{"timestamp":"...", "status":404, "error":"Not Found", "path":"/pf-devkit/ai-plugin/skeleton/mock/devices/..."}
```

---

## 4. 切到 sdk profile 后的行为（A3b 完成后）

✅ **A3b 已提供真 SDK 适配层**。切到 `sdk` profile 后（且 Maven 用 `-Psdk-real` 打包）：

| 行为 | 现状 | 备注 |
|---|---|---|
| App 正常启动 | ✅ | 同 mock |
| `actuator/health` 返回 UP | ✅ | 即使 SDK initialize 失败也不影响 health（降级路径） |
| Mock bean 全不装配 | ✅ | 0 泄漏面（mock 与 real 互斥） |
| `KernelReadyEvent` 发布 | ✅ | SDK initialize 成功 → workspace listen 注册 → 拉初始设备清单 → publish ready |
| `DeviceOnlineEvent` / `DeviceOfflineEvent` | ✅ | 通过 `Workspace.listenWorkspaceDeviceChanged` callback 转换 |
| `OsdUpdateEvent` | ✅ | 3 listen（location / battery / attitude）合并 + 1Hz Tick 推送 |
| `HmsAlertEvent` | ✅ | 每个 drone listen HMS，一次 callback 可能 publish 多条 |
| `FlightTaskProgressEvent` | ❌ v0.1 不发 | mock profile 下 `MockEventScripter` 脚本回放可触发；sdk profile 留给 A3c plan（需对接 `Workspace.getFlightTaskModule`） |

详见 §8 「v0.1 限制」。

---

## 5. 自定义真 SDK adapter（扩展 5 标准事件之外的能力）

> ✅ **A3b 已提供 `kernel-real-sdk` 模块** —— 绝大多数集成商不需要自己实现 adapter。
> 本节适用场景：需要监听 5 标准 KernelEvent 之外的 SDK 能力（如设备相机、wayline、
> FlightTaskProgress 等）。

### 5.1 在自家 module 引入 SDK jar

#### Maven 依赖

```xml
<dependency>
    <groupId>com.dji.flighthubsdk</groupId>
    <artifactId>flight-hub-sdk</artifactId>
    <version>3.3.7</version>
</dependency>
```

> 💡 **推荐方式**：使用 A3b 提供的 `kernel-real-sdk` 模块（见 §6），无需集成商自己处理 SDK 引入和 listen/cancel 生命周期。
> 仅当你需要绕开 `RealSdkAdapter` 直接调 SDK 时（如调 `getMediaServiceModule` / `getFlightTaskModule` / `TerraBuildModule` 等），才需要在自家 module 显式引 SDK。

#### 平台支持

`flight-hub-sdk:3.3.7` 按 CPU 架构分 amd64 / arm64 两个包（`libs/amd64/`、`libs/arm64/`），Maven 坐标相同，jar 内各自打包对应平台的 native lib：

| 平台 | 状态 | native lib 路径（jar 内） |
|---|---|---|
| Linux x86_64 | ✅ 支持（需 `libs/amd64` 包） | `lib/linux-x86_64/*.so` |
| Linux aarch64 | ✅ 支持（需 `libs/arm64` 包） | `lib/linux-aarch64/*.so` |
| macOS arm64 (Apple Silicon) | ✅ 支持（mock 或直跑，两个包均含） | `lib/macosx-arm64/*.dylib` |
| macOS x86_64 (Intel Mac) | ❌ 不支持 | — |
| Windows | ❌ 不支持 | — |

→ **开发态**：Linux x86_64 / Linux aarch64 / Apple Silicon Mac 可直接 `mvn -pl app/fh-skeleton-app spring-boot:run`，无需 Docker（需先安装与架构匹配的包到 `~/.m2`）
→ **生产态**：Linux x86_64 或 Linux aarch64 均可，但**镜像的 `SDK_ARCH` 必须与目标架构一致**（`--build-arg SDK_ARCH=amd64|arm64`）
→ **不支持的平台**：Windows / Intel Mac 必须用 Docker（A4 plan 提供 Docker 镜像）

#### jar 体积警示

`flight-hub-sdk:3.3.7` jar 因为内置 native lib，amd64 包约 **36 MB**、arm64 包约 **40 MB**（`.so` 解压后各约 350~400 MB）：

- 引入后会显著增大本地 Maven 仓库占用
- `mvn package -Psdk-real` 打 fat jar 时，最终包体积会显著增大
- CI / Docker 构建首次拉取需要 30s ~ 2min（取决于网络）

`kernel-real-sdk` 模块默认不出现在 `fh-skeleton-app` 的依赖中 —— 只有显式启用 Maven profile `sdk-real` 时才拼入（见 §6.1）。

### 5.2 实现自家 SDK adapter

#### 真 SDK 调用层级（重要！）

真 SDK 不是扁平的 listenXxx API，而是按实例分层：

```
                FlightHubSDK (static)
                       │
            ┌──────────┼──────────┐
            ▼          ▼          ▼
       Workspace    Drone      Dock
       (1 个全局)  (per sn)   (per sn)
            │          │          │
       listenWork-  listen-    listen-
       spaceDevice  Aircraft   Dock
       Changed     {Location,  {Cover,
                    Battery,    Silent
                    Attitude,   Mode,
                    HMS, ...}    ...}
```

→ 想 listen 设备相关事件，必须先 `FlightHubSDK.getDroneInstance(sn)` 拿到 `Drone` 实例，再调 `drone.listenXxx(callback)`。

#### SDK callback 约定（重要！）

- **listen 类 callback 单参数**（无 FlightHubError）：订阅成功 / 失败信息在 `listenXxx()` 返回的 **int** listen_id 上（`< 0` 表示订阅失败），订阅成功后 callback 只推数据；
- **action 类 callback 才带 `(data, FlightHubError)`**（如 `DeviceListCallback`）；
- `initialize` 是 **同步** 的：`FlightHubSDK.initialize(config)` 直接返回 `FlightHubSdkErrorCode`，不要用 `CompletableFuture.thenAccept`；
- `getDroneInstance(sn)` / `getDockInstance(sn)` 可能返回 null（设备未注册 / SDK 未 init）。

#### 真 SDK adapter 最小骨架

```java
package com.example.isv.adapter;

import com.dji.fh.kernel.spi.event.*;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "fh.skeleton.sdk.mode", havingValue = "sdk")
public class MyExtraSdkAdapter {

    private static final String SOURCE = "my-isv-extra";

    private final SdkEventBus bus;

    /** 每设备 listenId 列表，key=sn，value=kind→id */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> perDeviceIds =
            new ConcurrentHashMap<>();

    public MyExtraSdkAdapter(SdkEventBus bus) {
        this.bus = bus;
    }

    @EventListener
    public void onReady(ApplicationReadyEvent event) {
        // 1. 同步 initialize（直接返回 errorCode，不是 CompletableFuture）
        //    如果你的应用已经引入 kernel-real-sdk，initialize 已经由 RealSdkStarter
        //    完成，这里不需要重复调；本示例展示完整流程仅供集成商完全自家场景参考。
        FlightHubSdkErrorCode code = FlightHubSDK.initialize(buildConfig());
        if (code != FlightHubSdkErrorCode.SUCCESS) {
            return;  // 失败降级
        }

        // 2. 获取 Workspace（必须 initialize 后才可用，否则返回 null）
        Workspace workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            return;
        }

        // 3. 注册自家 listen（kernel-real-sdk 没覆盖的能力）
        //    例：listenAircraftAttitude 已被 kernel-real-sdk 接管，
        //    这里 listen 别的字段，如 listenAircraftBatteryWarningThresholds
        // ...
    }

    @PreDestroy
    public void shutdown() {
        // 反向 cancel 所有 listen（参考 RealSdkStarter.shutdown 模板）
        perDeviceIds.forEach((sn, ids) -> ids.forEach((kind, id) -> {
            try {
                Drone drone = FlightHubSDK.getDroneInstance(sn);
                if (drone == null) return;
                // 按 kind 调对应 cancel 方法
                // 例：drone.cancelListenAircraftBatteryWarningThresholds(id);
            } catch (Exception ignored) { }
        }));
        perDeviceIds.clear();
    }

    private FlightHubSdkConfig buildConfig() {
        FlightHubSdkConfig config = new FlightHubSdkConfig();
        config.setToken("...");
        config.setAddress("...");
        config.setPort("443");
        config.setNotificationServerAddress("wss://...");
        return config;
    }
}
```

**关键约定**（与项目根 CLAUDE.md 一致）：

1. **同步 initialize**：`FlightHubSDK.initialize(config)` 直接返回 `FlightHubSdkErrorCode`
2. **listen / cancel lifecycle**：`listenXxx` 返回 **int** listenId（`< 0` 表示失败），必须存入 map，`@PreDestroy` 时调对应 `cancelListenXxx(id)`
3. **实例 null 检查**：`getDroneInstance(sn)` / `getDockInstance(sn)` / `getWorkspaceInstance()` 可能返回 null
4. **flight-control authority**（如果调命令）：命令前 `acquireFlightControlAuthority()`，命令后 `releaseFlightControlAuthority()`（甚至失败路径）
5. **重复 initialize 安全**：如果应用同时引了 `kernel-real-sdk`，**不要在集成商 adapter 里再调 `initialize`**，直接用 `getWorkspaceInstance()` 即可

### 5.3 何时用 kernel-real-sdk vs 何时自家 adapter

| 维度 | 用 kernel-real-sdk（推荐） | 自家实现 adapter |
|---|---|---|
| 5 标准 KernelEvent | ✅ 已实现 | 需自己写转换 |
| JNI native lib 处理 | ✅ jar 已打包 | 同 |
| listen/cancel 生命周期管理 | ✅ `RealSdkStarter` 已封装 | 必须自己写 listenIds map + @PreDestroy |
| 配置 properties | ✅ `fh.skeleton.sdk.real.*` 完整体系 | 自己写 properties |
| 测试支持 | ✅ `SdkInvoker` mock 抽象 + 集成测试样板 | 自己写测试 |
| 升级负担 | ✅ Skeleton 维护，跟随 plan 演进 | 集成商自负 |
| 5 标准事件之外的 SDK 调用 | ❌ 不暴露 | ✅ 自由调任意 SDK API |
| 自定义事件源（聚合 / 过滤 / 转换）| ❌ 仅 5 标准事件 | ✅ 可发任意 KernelEvent |
| Docker 启动模板 | 🚧 A4 plan 提供 | 同 |

**判断方法**：

- 想用 5 个标准 KernelEvent（DeviceOnline / DeviceOffline / OsdUpdate / HmsAlert / KernelReady）?
  → 直接用 `kernel-real-sdk`：**Maven profile 打包**：`mvn package -Psdk-real`，**运行时设 property**：`--spring.profiles.active=sdk`
- 还想监听设备相机 / wayline / FlightTaskProgress?
  → 在集成商自家 module 加一个 adapter（如 `MyExtraSdkAdapter`），跟 `kernel-real-sdk` 共存（同 SpringContext 内）
- 完全不用 5 个标准事件，想自己定义事件模型?
  → 完全自己实现 adapter，**不引** `kernel-real-sdk`

---

## 6. 启动模式切换（Maven profile + Spring profile 协同）

A3b 引入了 Maven profile `sdk-real`，**与 Spring profile 协同**控制启动模式：

| 维度 | Maven profile | Spring profile |
|---|---|---|
| 何时决定 | 构建期 | 运行期 |
| 控制内容 | jar 包是否包含 `kernel-real-sdk` + SDK jar | bean 是否装配 |
| 关键开关 | `mvn package -Psdk-real` | `--spring.profiles.active=sdk` |

### 6.1 启动场景矩阵

| 场景 | Maven 命令 | 运行命令 | 行为 |
|---|---|---|---|
| 开发态 mock | `mvn -pl app/fh-skeleton-app spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mock"` | （内置） | mock SDK，不含真 SDK jar |
| 生产 mock 演示 | `mvn package` | `java -jar app.jar --spring.profiles.active=mock` | mock SDK，jar ~20MB |
| 真 SDK 集成测试 | `mvn package -Psdk-real` | `java -jar app.jar --spring.profiles.active=sdk` | 真 SDK，jar ~200MB |
| 切回 mock | — | `java -jar app.jar --spring.profiles.active=mock` | 默认 profile 为 `sdk`；即使含 -Psdk-real 打包的 jar，激活 mock profile 后真 SDK 类不入装配路径 |

> ⚠️ **反过来不行**：没用 `-Psdk-real` 打包的 jar，sdk profile 启动会 `ClassNotFoundException`（真 SDK 类不在 classpath）。

### 6.2 推荐的集成商工作流

```bash
# 日常开发 / 单元测试 / mock 演示
mvn -pl app/fh-skeleton-app spring-boot:run \
    -Dspring-boot.run.arguments="--spring.profiles.active=mock"

# CI（不含真 SDK，快）
mvn clean install

# 集成测试 / 预生产
mvn package -Psdk-real -DskipTests
java -jar app/fh-skeleton-app/target/*.jar \
    --spring.profiles.active=sdk \
    --fh.skeleton.sdk.real.token=<token> \
    --fh.skeleton.sdk.real.address=api.example.com \
    --fh.skeleton.sdk.real.port=443 \
    --fh.skeleton.sdk.real.notification-server-address=wss://your-server/ws

# 生产部署：建议用 Docker（等 A4 plan）
```

### 6.3 验证 profile 是否生效

```bash
# 验证 mock profile（需显式激活 --spring.profiles.active=mock）
curl http://localhost:8080/pf-devkit/ai-plugin/skeleton/actuator/health
# {"status":"UP"}
# 日志含 "Mock SDK 正在初始化"

# 验证 sdk profile（真 SDK）
curl http://localhost:8080/pf-devkit/ai-plugin/skeleton/actuator/health
# {"status":"UP"}
# 日志含 "真 SDK initialize 成功，版本=3.3.7"
# 或 "真 SDK initialize 失败：errorCode=..."（token 错误时降级，app 仍 UP）
```

---

## 7. 常见问题（FAQ）

### Q1：切到 sdk profile 后集成商 feature 没收到任何事件？

A：先确认两件事：(a) `mvn package -Psdk-real` 是否激活，含真 SDK jar；(b) 启动日志是否含 "真 SDK initialize 成功"。如果 initialize 失败（token / endpoint 错误），KernelReadyEvent 不会发，下游 listener 不会收到任何事件。

### Q2：集成商想覆盖 `MockOsdScheduler` 的 1Hz 频率？

A：`application.yml` 设 `fh.skeleton.mock.osd.interval-millis=500` (2Hz) 或更慢。
（仅 mock profile 生效；sdk profile 用 `fh.skeleton.sdk.real.osd-interval-millis`）

### Q3：集成商想关闭启动脚本回放但保留 Mock SDK？

A：`fh.skeleton.mock.script.enabled=false`。设备依然 register 在 registry 里，可通过 REST 手动触发上线。

### Q4：sdk profile 下我能不能复用 `MockEventTrigger` 接口直接 publish 假事件？

A：不能。`MockEventTrigger` 是 mock-sdk 模块的内部 API，sdk profile 下 Mock bean 完全不装配，类不在运行时 classpath 行为预期路径上。

生产正确做法见 §5：要么自家 SDK adapter publish 真事件，要么集成商 feature inject `SdkEventBus` 直接 `bus.publish(...)`。

### Q5：mock profile 在生产里启用会有什么风险？

A：`POST /mock/devices/{sn}/*` 5 个端点完全开放，**无鉴权**（NoOpSecurityFilter）。任何能访问 8080 的人都能伪造 device-online / OSD / 飞行任务事件。**绝对禁止在生产或公网启用 mock profile**。`application-mock.yml` 顶部已加警告。

### Q6：sdk profile 启动失败 NoClassDefFoundError flight-hub-sdk 怎么办？

A：你没用 `-Psdk-real` 打包。检查 `mvn package` 命令是否含 `-Psdk-real`。也可以 `unzip -l target/*.jar | grep flighthubsdk` 验证 jar 内是否含 SDK 类。

### Q7：sdk profile 下能不能修改 OSD 字段（只要 location 不要 attitude）?

A：不行（v0.1）。`OsdAggregator` 总是合并 3 source 全字段。集成商 listener 可以自己过滤：`event.osd().get("head")` 返回的 Optional 直接 ignore 即可。如果想从根本上不接 attitude listen，需要绕开 `kernel-real-sdk` 自家实现 adapter。

### Q8：sdk profile 下看到 SDK initialize 失败 `ERROR_INIT_FAILED`/`ERROR_INVALID_PARAM` 怎么排错？

A：按下表逐项检查：

| 错误码 | 最常见原因 | 自查方式 |
|---|---|---|
| `ERROR_INVALID_PARAM` | 4 个必填 env var 漏配（token / address / port / notification-server-address）→ 留空串传给 SDK | `printenv \| grep FH_SKELETON_SDK_` 看是否 4 项全有值 |
| `ERROR_INIT_FAILED` | token 失效 / endpoint 不可达 / 证书校验失败 | `curl -v https://<address>:<port>/...` 测网络可达；token 找发放方核对 |
| `ERROR_ALREADY_INITIALIZED` | 同 JVM 内重复 init（热重载常见） | 完全重启 JVM；A3b v0.1 不支持热重启（gap §8.2 第 7 项） |
| `Failed to load dependency library: libwebsockets.12.dylib` | Spring Boot fat jar JNI 嵌套加载（gap §8.2 第 8 项） | 本地用 `mvn -pl app/fh-skeleton-app spring-boot:run -Psdk-real`；生产等 A4 Docker |

参考集成商指南 §6.2 推荐工作流 + §8.2 v0.1 限制清单。

### Q9：mock profile 和 sdk profile 切换后集成商 feature 行为不一致怎么办？

A：A3b 已设计两 profile 行为对齐（除 v0.1 限制清单 §8.2 列出的 7 项 gap）。`source` 字段是关键诊断信号：

- mock profile 下 KernelEvent 的 `source()` = `"mock-sdk"` / `"mock-script"` / `"mock-rest"`
- sdk profile 下 KernelEvent 的 `source()` = `"real-sdk"`

集成商 feature **不要** 在业务逻辑里 hard-code 检查 `source.equals("real-sdk")`——这违背了 SPI 抽象。仅日志可用 source 区分。

---

## 8. A3b 完成后的能力清单 / v0.1 限制（必读）

### 8.1 已提供（A3b ✅）

- ✅ 真 SDK `FlightHubSDK.initialize` / `shutdown` 全自动（启动 + 优雅关闭）
- ✅ 设备进出工作空间自动产生 `DeviceOnlineEvent` / `DeviceOfflineEvent`（通过 `Workspace.listenWorkspaceDeviceChanged`）
- ✅ 设备 HMS 告警自动产生 `HmsAlertEvent`（按 drone listen `listenAircraftHMS`，一次 callback 可能 publish 多条）
- ✅ 设备 OSD 自动产生 `OsdUpdateEvent`（3 listen 合并：`listenAircraftLocation` + `listenAircraftBatteryInfo` + `listenAircraftAttitude`，1Hz Tick 推送，与 mock 对齐）
- ✅ `KernelReadyEvent` 在 SDK initialize 成功后自动 publish
- ✅ `initialize` 失败、listen 失败、cancel 失败均**降级处理**，app 不崩
- ✅ 完整的 `fh.skeleton.sdk.real.*` properties 体系
- ✅ Mac arm64 / Linux x86_64 / Linux aarch64 三平台支持（后两者按架构选包）
- ✅ `SdkInvoker` 抽象让集成商在测试中可 mock 真 SDK

### 8.2 v0.1 已知限制（A3c 或后续补齐）

| # | 限制 | 影响 | 临时方案 |
|---|---|---|---|
| 1 | **启动后新上线设备的 per-device listen 不自动注册** | t2 之后进入工作空间的设备的 OsdUpdateEvent / HmsAlertEvent 不会触发（仅 DeviceOnlineEvent 触发）| 重启 app；或集成商自家 ws callback hook（参考 §5.2）|
| 2 | **不发 `FlightTaskProgressEvent`** | sdk profile 下飞行任务无事件 | 集成商自己 inject `SdkInvoker` 调 `Workspace.getFlightTaskModule()` |
| 3 | **HMS message 字段固定 null** | SDK HmsDataBasics 不含 message 文本，需通过 `Workspace.queryHmsTrans` 二次查询 | 集成商用 `alertCode()` 自行查翻译表 |
| 4 | OSD payload 启动早期字段可能不全 | location/battery/attitude 3 source 不同步到达 | 等 1-2 Tick 后字段补全；集成商 listener 需 null check |
| 5 | `getDeviceList` 超时 → 启动时已在线设备无 per-device listen | 设备需重启 SDK 才会被识别 | 调大 `device-list-timeout` 或检查网络 |
| 6 | 仅 Linux x86_64 / Linux aarch64 + Mac arm64 | Windows / Intel Mac 必须 Docker；Linux 需选对 `SDK_ARCH` | 等 A4 plan |
| 7 | 不支持热重启 SDK | dev 热重载可能 `ERROR_ALREADY_INITIALIZED` | 完全重启 JVM |
| 8 | Spring Boot fat jar 嵌套加载 SDK native lib 失败 | `java -jar app.jar` (`-Psdk-real` 打包) 启动时 SDK initialize 抛 `Failed to load dependency library: libwebsockets.12.dylib` | A4 Docker 镜像把 native lib 解压到独立路径解决；目前本地用 `mvn -pl app/fh-skeleton-app spring-boot:run` 或解压 jar 后 `java -cp` 启动 |

### 8.3 推荐集成商升级路径

1. **当前**（A3b 合并后）：用 `mock` profile 开发 feature；feature 接口设计要假设事件源可能是 `"mock-sdk"` 或 `"real-sdk"` 都能 work（用 `source` 字段区分日志即可）
2. **真 SDK 集成测试期**：切 `sdk` profile + `-Psdk-real` 打包，跑端到端集成测试
3. **生产部署**：等 A4 Docker plan 合入

---

## 9. 路线图

- ✅ **A3**（已合）：Mock SDK + Spring Boot 启动入口 + profile 切换框架
- ✅ **A3b**（本期）：真 SDK 适配层（`kernel-real-sdk` 模块 + 5 KernelEvent 映射 + `SdkInvoker` 抽象 + Maven profile `sdk-real`）
- 🚧 **A3c**（待规划）：启动后新上线设备的 per-device listen 动态注册 + `FlightTaskProgressEvent` 适配 + Workspace 级模块 listen
- 🚧 **A4**（待规划）：Docker 镜像 + Compose + 12-factor（生产部署）
- 🚧 **Plan B**（待规划）：feature-device 等首批 first-party feature

