# Flight Hub SDK 模块化 Demo 设计文档

> 对应实现：`platform/java/new_sdk_demo`　| 　参考方案：`OtherProjects/sdk_design/docs/sdk_demo/sdk_demo_design_v1.md`

本文档说明新 demo 的设计目标、架构分层、核心抽象与关键决策。实现严格对照 SDK 真实 Java API（`platform/java/flight_hub_sdk/src`）编写并编译通过。当前规模：40 个 Java 源文件、约 2 万行，19 个模块 demo + 4 个场景 demo 全部深度实现（无脚手架），并经全量代码评审整改（`tests/demo_review_report.md`）。

---

## 1. 目标与约束

**目标**（同参考方案）：给接入方一个标准官方用法示例，代码经少量修改即可用于业务；同时可作为问题定位辅助手段。

**约束**：
1. **并存**：不改动旧 `FlightHubSDKDemo.java`（~1.6 万行巨类），新建于独立 gradle 工程 `new_sdk_demo`，包名 `com.dji.flighthubsdk.demo`。
2. **零第三方依赖**：仅 JDK + SDK jar（不引入 Jackson/SLF4J/picocli）。手写 JSON、自封装日志、数据驱动菜单。
3. **每模块给示例**：19 个功能模块 demo + 4 个场景 case，全部深度实现、编译通过。
4. **源码 & 产物 Java 8**（无 `var`/`List.of`/文本块等 9+ 特性），可在 Java 8+ 运行；无 Nashorn → 手写 JSON 而非 `JSON.parse`。
   构建用 JDK≥9 时以 `options.release = 8` 链接**真正的 Java 8 API 面**（`build.gradle`），把 Java 9+ API 误用挡在编译期；用 JDK 8 构建则由 `source/target 1.8` 原生保证。
   **原生库平台**：SDK jar 官方交付只内置 linux-x86_64；其它平台（Windows / macOS / linux-aarch64）加载失败，`SdkRuntime` 会给出可读错误（含 `os.name/os.arch`）。

## 2. 在参考方案上的改进

参考方案的 `DemoBase`/`Precondition`/裸 `listenId` 写法会保留旧 demo 的部分反模式（手动 cancel、忙等）。本设计在**保留其目录结构与场景流程**的前提下，用一套更优的瘦运行时替换基类实现：

| 参考方案 | 本设计 | 解决的问题 |
|---|---|---|
| `DemoBase.waitForCallback`（AtomicBoolean+sleep） | `Await`（CountDownLatch） | 忙等 → 事件驱动、可超时 |
| 裸 `int id = listen(...)` + 手动 cancel | `Subscription`(AutoCloseable) + `SubscriptionRegistry` | ~210 泄漏监听器 → 自动逆序退订 |
| 成功/失败二值判定 | `Outcome` 三态（SUCCESS/FAILED/TIMEOUT） | 超时 ≠ 未生效：飞行类指令需按「可能已生效」保守收尾 |
| `TestParamsConfig`（隐含 POJO/反射） | 手写 `Json` + `Node` 类型化视图 | 零依赖、支持嵌套 |
| 分散 setup/teardown | `SdkRuntime`/`DeviceSession` 均 AutoCloseable | try-with-resources 保证清理 |
| `System.exit(1)` | 抛 `FlightHubException` | 库/示例不杀宿主进程 |

## 3. 架构分层

```
┌──────────────────────────────────────────────────────────────────┐
│  DemoEntry                 数据驱动注册表 + 交互/非交互入口          │
├──────────────────────────────────────────────────────────────────┤
│  module/*  (19)            scenario/*  (4)                        │
│  一模块一类，继承 ModuleDemo   一场景一类，继承 Scenario            │
├──────────────────────────────────────────────────────────────────┤
│  base/                                                            │
│   ModuleDemo   注入式 runInSession()：run→自动清理(只关 session)   │
│                call()同步动作 / subscribe()自动退订 / requireSuccess│
│                Outcome 三态 + callOutcome/syncOutcome/runOutcome   │
│   Scenario     steps() 步骤编排 + step/syncResult/quiet 收尾兜底   │
│   Precondition acquireFlightControl / takeoff(Result) / release ...│
├──────────────────────────────────────────────────────────────────┤
│  runtime/  （瘦运行时，与 SDK 解耦的通用能力）                      │
│   SdkRuntime        initialize/shutdown + 鉴权头注入（AutoCloseable）│
│   DeviceSession     已选 topo→drone/dock/workspace 句柄 + registry │
│   Subscription      listenId + cancel 打包为 AutoCloseable          │
│   SubscriptionRegistry  LIFO 逆序释放全部订阅/资源                  │
│   Await             回调→同步（CountDownLatch，可超时）             │
│   Devices           设备列表拉取 + 按角色取 drone/dock 句柄         │
│   DemoConfig        config.json（server/auth）+ 环境变量覆盖        │
│   DemoParams/Node   demo_params.json 类型化只读视图                 │
│   Json              零依赖递归下降 JSON 解析器                      │
│   DemoLog           自封装滚动文件日志（不裸 System.out）           │
│   FlightHubException 统一异常                                       │
├──────────────────────────────────────────────────────────────────┤
│  utils/ Utils       控制台格式化 / readLine / describe(Throwable)   │
├──────────────────────────────────────────────────────────────────┤
│  SDK: FlightHubSDK / Workspace / Drone / Dock / *Module            │
└──────────────────────────────────────────────────────────────────┘
```

## 4. 核心抽象

### 4.1 Subscription + SubscriptionRegistry（消除监听器泄漏）

旧 demo 最大反模式：`int id = listenXxx(cb)` 后极少 `cancelListenXxx(id)`。本设计把「listenId + 对应 cancel 动作」打包为 `AutoCloseable`：

```java
Subscription sub = subscribe("droneOsd",
        osd.listenAircraftPayloadsOsdInfo(cb),   // 返回 int id
        osd::cancelListenAircraftPayloadsOsdInfo); // Canceller
// 自动注册进 session.registry()，会话结束 LIFO 逆序全部退订
```

`SubscriptionRegistry` 持有所有订阅与可关闭资源（含文件流），`DeviceSession.close()` 时统一释放。`listenId == -1` 直接抛异常，避免静默失败。

**订阅规范**（评审整改后的统一约定）：
1. 优先经基类 `subscribe()` 建立订阅（自动登记 registry 兜底）；
2. 必须直接 `Subscription.of(...)` 构造的（如需要在关闭时机上精细控制的进度订阅、拉流 range/stream 订阅），构造后立即 `registry().add(sub)` 登记——`close()` 幂等（CAS），主动关闭与会话兜底不冲突；
3. 「建立订阅 → 下发指令 → 等待推送」的临时订阅段用 `try/finally` 包裹，finally 中 `close()` 后同步 `registry().remove(sub)`，防止长会话累积死引用；
4. 参考实现：`DeviceDebugDemo` 的 9 个进度订阅方法、`LiveStreamDemo` 的 rangeSub/streamSub。

### 4.2 Await（消除忙等）

```java
Await<FlightHubError> a = new Await<>("returnHome");
fc.returnHome(err -> a.set(err));      // 回调线程只做 set + countDown
requireSuccess("返航", a.await(10_000)); // 主线程等待，超时抛异常
```

`Await.set()` 用独立 CAS 标志保证「首个结果生效」（返回 boolean 供调用方判定结果是否被采纳，避免迟到回调打误导日志）；`await(timeoutMs)` 对 `timeoutMs ≤ 0` fail-fast。封装在基类 `call(label, cb -> module.action(cb), timeout)` 中，一行完成「下发→等待→校验错误码」。错误校验保留 `serverCode/deviceCode/serverMessage`，避免旧 demo 把设备错误码压平成不透明失败。

### 4.3 Outcome 三态与 SUCCESS 门控

`ModuleDemo` 定义 `enum Outcome { SUCCESS, FAILED, TIMEOUT }`，并提供三个同步化骨架：

- `callOutcome(label, action, timeoutMs)` —— `ResultCallbackWithError` 回调版；
- `syncOutcome(label, action, timeoutMs)` —— `ResultCallback` 回调版；
- `runOutcome(label, timeoutMs, await, dispatch)` —— 通用骨架，供带业务结果对象的特殊回调复用。

**语义约定**：
- `TIMEOUT` = 应答未到，**命令可能已在设备侧生效**——飞行/物理动作类指令须按保守策略处理（标志位保持置位、收尾兜底补偿）；
- `FAILED` = 服务端/设备明确拒绝，确认未生效——可安全复位标志位；
- **结果解析与缓存必须做 SUCCESS 门控**：`Outcome o = runOutcome(...); if (o == Outcome.SUCCESS && ref.get() != null) { 解析/缓存 }`。SDK 的 C++ 失败路径可能回调空结构体（SWIG 落地为非 null 空 bean），不门控会缓存脏值污染后续串联操作。参考实现：`MaintenanceDemo`、`TerraBuildDemo`、`AIBoxDemo`。

### 4.4 生命周期

入口 `DemoEntry.session()` 统一负责 SDK 生命周期与选设备，各 demo 经 `ModuleDemo.runInSession()` 注入运行，保证无论成功/异常都清理：

```
入口(一次): load config/params → SdkRuntime.start（init+鉴权） → pickDevice（选设备）
每个 demo:  DeviceSession.of（按已选 topo 解析 drone/dock/机场句柄）
            → runInSession → run()（子类逻辑）
            → finally: 只 session.close()（退订本轮订阅，不 shutdown SDK）
退出时:     入口统一 SdkRuntime.close()（shutdown SDK）
```

`SdkRuntime`、`DeviceSession`、`Subscription`、`SubscriptionRegistry` 全部 `AutoCloseable`，杜绝漏 shutdown/漏退订。

**JNI Error 隔离策略**（全工程统一）：SDK 经 JNI 调用，原生层可能抛 `Error`（如 `UnsatisfiedLinkError`）而非 `Exception`。所有**清理/收尾路径**一律 `catch (Throwable)`（`Scenario.quiet`、`Precondition.releaseFlightControl`、各模块私有 `quietStep`），只 catch `Exception` 会让 Error 穿透并中断后续收尾。主流程由基类 `runBody()` 兜底隔离。

**设备状态还原**：demo 自动更改的设备状态须在退出前还原——记录「本 demo 曾更改」标志，`run()` 用 try/finally 在收尾还原（兜 Throwable、不掩盖主异常）。参考实现：`FlySafeUnlockDemo` 的 `debugModeOpenedByDemo` + `restoreDebugModeIfOpened()`（自动开启的机场远程调试模式退出时 `closeDebugMode` 关闭）。

### 4.5 参数与配置文件化

- `config.json`（连接 + 鉴权，**不入库**）→ `DemoConfig`；`auth.auth_key_values` 为单键值对象数组，支持同时配置多个鉴权头，任一项 value 留空可用 `FLIGHTHUB_AUTH_VALUE` 环境变量覆盖。
- `demo_params.json`（可变业务参数，30 个模块/场景分组节）→ `DemoParams` + `Node` 类型化读取，解决「手输慢/易超时」与「硬编码不灵活」两难。
- `Node` 对缺失分组/键**静默回退默认值**（设计意图：配置残缺不阻断运行）——代价是「代码读的分组名必须真实存在于 demo_params.json」，否则用户改配置不生效且无感。**约定：新增 `params.node("xxx")` 读取时必须同步在 demo_params.json 中添加对应分组节**（含 `_comment` 注释）。
- 均由零依赖 `Json` 解析（递归下降，支持嵌套对象/数组、`\uXXXX`、转义、BOM、深度封顶），替代旧 demo 的正则解析（嵌套即坏）。

### 4.6 缓存串联的一致性约定

菜单式 demo 常见「[1] 查询缓存 → [2] 使用缓存」的接口串联。评审整改后的约定：

1. **缓存须与其归属主体（如设备 SN）成对记录**，使用前比对主体一致才生效，避免 A 设备的能力/证书错用于 B 设备（参考 `MaintenanceDemo.lastRepairConfigSn`）；
2. **每次成功查询先复位缓存**再按本次结果填充，空结果不残留上一主体的旧值（参考 `FlySafeUnlockDemo.lastLicenseId`）；
3. 「已缓存」类提示用**本次是否写入**的局部标志判断，不用缓存字段旧值判断。

## 5. 场景编排

`Scenario` 基类在 `ModuleDemo` 之上提供：`step(title)` 步骤分段日志、`syncResult(label, action, timeout)`（`ResultCallback` 同步化）、`quiet(label, step)`（收尾单步 Throwable 兜底，一步失败不阻断后续收尾）。

以 FlyToScenario 为例：

```
前置: acquireFlightControl + takeoff（仅判断指令下发成功，不等飞完）
  → start 指点飞行（拿 flyToId）
  → subscribe 飞行进度（登记进 registry）
  → 收到「飞行中」→ updateTargetPoints（参数来自配置，立即下发）；否则跳过
  → returnHome
finally → releaseFlightControl（订阅由会话自动退订）
```

**飞行安全关键决策**（四个场景统一采用）：

1. **「下发即置位」+「明确拒绝复位」**：可能改变飞行状态的指令（起飞/start/进入 POI）在下发**前**置位标志（回调超时不代表未生效，收尾据此兜底补偿）；回调返回**服务端明确业务码拒绝（serverCode != 0）**时复位标志（确认未生效，避免对未起飞的飞机补发返航/对未创建的任务补发急停）。`Precondition.takeoffResult(...)` 为此返回原始 `FlightHubError` 供调用方精细判定；`Precondition.takeoff(...)` 是其「失败即抛」的简化包装。
2. **交互操控循环内单指令失败不终止场景**：`DrcFlightScenario` 的起飞/返航分支各自 try/catch(Throwable)，失败告警后回菜单——飞行中因一次网络抖动踢出操控菜单会让操作者失去对空中飞机的手动操控。
3. **返航前摇杆置中**：DRC 摇杆输入优先于自动飞行，下发返航前先把杆量置中并使 hold 失效，避免残留杆量帧打断返航。
4. **finally 分步收尾**：每步经 `quiet(...)` 各自兜异常，顺序为「停调度器 → 中性尾帧 → 补返航（如已起飞未返航）→ 断 DRC → 释放控制权 → 退订」，一步失败不阻断后续。

## 6. 鉴权模型（版本相关）

本 SDK Java 版本的 `FlightHubSdkConfig` **无 `setToken`/`setBindingUuid`**，采用 **gateway header 注入**：把 `auth.auth_key_values`（单键值对象数组，可配置多个）解析出的一组 key/value 同时注入 HTTP 与 WebSocket 头（`FlightHubSDK.setHttpHeaders` / `setWebSocketAuthHeaders`），必须在 `initialize` 之前。设备实例走单参门面 `getDroneInstance(sn)` / `getWorkspaceInstance()`（非 session 版）。

## 7. 验证与质量保障

- 全量 `javac`（40 类）对 `dist/flight-hub-sdk.jar` 编译通过（仅 SDK 自身 API 的 deprecation note）。
- 入口菜单冒烟运行通过（UTF-8 输出字节校验正确）；真机联调留痕见 `tests/`（日志、码流落盘样本、测试记录）。
- 所有 API 签名对照 `flight_hub_sdk/src` 真实源码核对（回调方法名/参数类型 bean vs generated、枚举 `fromValue`、`ResultCallback` vs `ResultCallbackWithError` 等）。
- 完成一轮 41 文件全量代码评审（`tests/demo_review_report.md`，27 项问题分高/中/低三档），全部高、中级别问题已整改：DrcFlightScenario 飞行安全三项、AIBoxDemo SUCCESS 门控、订阅登记/finally 规范化、`wayline` 配置节、调试模式状态还原、缓存主体错配等。第 4 章的「订阅规范 / SUCCESS 门控 / 状态还原 / 缓存一致性」约定即整改沉淀。

## 8. 待办（增量）

1. 处理评审报告中剩余的低级别问题（提示文案、边界校验、`registry().remove` 全量补齐等，见 `tests/demo_review_report.md` #12-#27）。
2. 可选：为每个 demo 类补 `public static void main` 以支持 `-PmainClass` 单类运行。
3. 可选：RTMP 推流场景复用旧 demo 的 `FFmpegPipeStreamer` 思路，但去掉 `System.exit`。
