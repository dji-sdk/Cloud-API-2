# Flight Hub SDK 模块化 Demo

> 位置：`platform/java/new_sdk_demo`　| 　源码 & 产物：Java 8(字节码 class v52)　| 　构建/开发 JDK：8 或 21 均可　| 　依赖：仅 SDK jar(零第三方)

一个按模块拆分、场景化、参数文件化、无监听器泄漏的 Java SDK demo, 作为接入方的**官方标准用法示例**, 也可作为问题定位辅助手段。它与旧的 `flight_hub_sdk_demo/FlightHubSDKDemo.java`(~1.6 万行巨类)**并存互不影响**。

当前规模：**41 个 Java 源文件、约 2.1 万行**——20 个功能模块 demo + 4 个场景 demo **全部深度实现**(无脚手架/TODO), 并已通过一轮全量代码评审与整改(评审报告见 `tests/demo_review_report.md`)。

设计细节见 [`DESIGN.md`](./DESIGN.md)。

---

## 1. 快速开始

构建/运行方式与老 demo 工程 `flight_hub_sdk_demo` 一致：自带 `gradlew` wrapper, `build.sh` 是其薄封装。

```bash
cd platform/java/new_sdk_demo

# 1) 准备配置(不入库)
cp config.json.example config.json
export FLIGHTHUB_AUTH_VALUE=你的-gateway-uuid   # 鉴权值, 避免明文入库

# 2) 构建可执行 Fat JAR(内含 SDK 类 + 原生库)
./build.sh --executable        # 等价于 ./gradlew executableJar
#   产物: build/libs/new-sdk-demo-executable.jar(并自动同步一份到 tests/)

# 3) 运行(交互菜单 / 直接跑指定 demo)
注意：终端如果没开 UTF-8, 中文可能又显示成 [?? Demo], 那是终端编码问题, 加 -Dfile.encoding=UTF-8 即可。

java -Dfile.encoding=UTF-8 -jar build/libs/new-sdk-demo-executable.jar config.json
java -Dfile.encoding=UTF-8 -jar build/libs/new-sdk-demo-executable.jar config.json FlyToScenario
```

> **`demo_params.json` 放哪里？** 程序按“当前工作目录优先, 找不到才回退 jar 内置默认值”的顺序加载(`DemoParams.load()`)：
>
> - 路径是相对**执行命令时所在的目录(CWD)**, 跟 jar 文件放在哪无关。按上面的用法(`cd` 到 `new_sdk_demo/` 后原地跑 `java -jar build/libs/...`), 把要生效的 `demo_params.json` 放在 `new_sdk_demo/` 根目录下即可——可先从 `src/main/resources/demo_params.json` 复制一份出来改, 改完**无需重新编译/打包**, 直接改这份文件即可生效。
> - 如果 CWD 下没有这个文件, 会静默回退到 jar 里打包的默认值(即 `src/main/resources/demo_params.json` 编译进去的那份), 不会报错, 但你改的值不会生效——遇到“改了参数没用”先检查是不是放错了目录。
> - `tests/demo_params.json` 是仓库里留存的一份测试用副本(配合 `tests/new-sdk-demo-executable.jar` 使用), 不是运行时会自动读取的固定路径。

也可直接用 gradle 任务：

```bash
./gradlew run --args="config.json"                 # 交互菜单
./gradlew run --args="config.json FlyToScenario"   # 直接跑指定 demo(名字大小写不敏感)
./gradlew executableJar                            # 打可执行 Fat JAR
```

build.sh 选项：`-c` 清理 / `-b` 构建 / `-e` 打可执行 JAR / `-r` 运行 / `-a` 全部。

> SDK jar 路径由 `config.properties` 的 `sdk.jar.path` 指定(默认 `../dist/flight-hub-sdk.jar`)。
> 原生库(.so)已打包在 jar 内, `initialize()` 时按当前平台自动加载。

### Java 版本 & 平台支持

- **Java 版本**：产物为 **Java 8 字节码(class v52)**, 可在 **Java 8 及以上**运行；SDK jar 亦为纯 Java 8 字节码。
  构建/开发既可用 JDK 8, 也可用高版本(如 JDK 21)——`build.gradle` 已用 `options.release = 8`(JDK≥9 时启用)
  强制按 Java 8 API 面编译, 任何误用的 Java 9+ API 都会在**编译期**报错, 而不会等到客户的 Java 8 环境才 `NoSuchMethodError`。
- **中文控制台**：`gradlew run` 与启动脚本已内置 `-Dfile.encoding=UTF-8`；`java -jar` 直跑时, demo 会在启动时
  自动把 `System.out/err` 兜底为 UTF-8(非 UTF-8 默认字符集的 JVM, 如 Windows-GBK 上的 Java 8), 一般无需再手动加参数。
- **原生库平台矩阵**(SDK jar 内置)：
  | 平台 | 是否内置原生库 |
  |---|---|
  | Linux x86_64 | ✅ 支持 |
  | Windows / macOS / Linux aarch64 | ❌ 未内置, `initialize()` 会加载失败 |

  在未内置的平台上运行时, `SdkRuntime` 会抛出可读的 `FlightHubException`(含当前 `os.name/os.arch` 与已支持平台清单), 而非裸的 `UnsatisfiedLinkError`。

## 2. 目录结构

```
new_sdk_demo/
├── build.gradle / settings.gradle / config.properties
├── config.json.example            # 连接与鉴权模板(复制为 config.json, 不入库)
├── src/main/resources/demo_params.json   # 可变参数(按模块/场景分组, 30 个分组节)
├── tests/                         # 评审报告、可执行 jar 同步件、测试配置与运行留痕
└── src/main/java/com/dji/flighthubsdk/demo/
    ├── DemoEntry.java             # 统一入口 + 数据驱动注册表(替代 289-case switch)
    ├── runtime/                   # 瘦运行时(生命周期/会话/订阅/等待/JSON/配置/日志, 12 类)
    ├── base/                      # ModuleDemo / Scenario / Precondition 基类
    ├── utils/                     # Utils(控制台格式化 / readLine / describe 等跨模块工具)
    ├── module/                    # 20 个功能模块 demo(全部深度实现)
    └── scenario/                  # 4 个场景 demo(全部深度实现)
```

## 3. Demo 清单(全部深度实现, 无脚手架)

### 模块 Demo(20 个, 菜单驱动)

| 类 | 覆盖内容 |
|---|---|
| `FlightControlDemo` | 消息监听、飞行控制权、DRC 链路、一键起飞、WASD 摇杆、急停、返航/取消返航 |
| `PayloadControlDemo` | 负载控制权、相机模式/镜头/变焦/存储/夜景/红外、拍照/录像/全景(20 项子菜单, 跨 6 个模块) |
| `LiveStreamDemo` | 无人机+机场两路码流拉取落盘 .h264、拉流状态、画质设置、按序停止 |
| `LiveRecordDemo` | 直播录制开始/停止、记录查询、录制状态订阅(LiveRecordModule 全部 5 接口) |
| `DeviceOsdDemo` | 无人机/机场 OSD 与在线状态推送、HMS 消息(60+ 路监听, 定时自动退订) |
| `DeviceManageDemo` | 设备详情(含子设备)、机场/飞机设备状态查询(DeviceManageModule 3 个只读接口) |
| `DockControlDemo` | 舱内/外摄像头切换、空调、舱盖、补光灯、充电 |
| `DeviceDebugDemo` | 机场/飞行器调试模式、舱盖、充电、重启/格式化、飞行参数、电池/SIM/低功耗(7 个子菜单, 模块全接口) |
| `DrcDemo` | DRC 链路完整生命周期：负载控制权 → 建链 → 云台重置验证 → 断链 |
| `PsdkDemo` | PsdkControlModule 全部 16 个控制接口 + 状态监听 + PsdkResourceModule 全部 7 个资源接口 |
| `FlightTaskDemo` | 航线任务 CRUD/状态更新/设备推荐/TCA + 航线文件管理(17 个常用接口) |
| `FlightSafetyDemo` | FlightSettingsModule 全部 5 个飞行安全设置接口 |
| `FlySafeUnlockDemo` | 限飞解禁证书全流程(上传→检查→提交→查询→切换→清除, 6 接口), 自动开关机场调试模式并在退出时还原 |
| `SafetyServiceDemo` | 信号地图、自定义飞行区域、地形障碍物离线地图 |
| `MediaServiceDemo` | 媒体回传/后处理通知订阅、文件列表/下载/删除、STS 上传凭证与回调 |
| `TerraBuildDemo` | Terra Build 建图重建：模型文件管理、重建任务、全景拼接、WS 状态推送、LAS 坐标系 |
| `SystemManagementDemo` | 日志管理 + 固件管理两功能域(多级子菜单) |
| `FlightTaskRecordDemo` | 飞行记录导出 + 导出进度查询(模块全部 2 接口) |
| `MaintenanceDemo` | 报修配置查询 → 创建报修订单 → 订单详情 → 分页列表 |
| `AIBoxDemo` | AI Box 信息、应用列表/安装/操作、文件导出/下载链接 |

### 场景 Demo(4 个, 跨模块编排)

| 类 | 流程 |
|---|---|
| `FlyToScenario` | 监听 → 双控制权 → 建 DRC(急停硬前置)→ 指点飞行 start → 更新目标点 → stop → 完备收尾 |
| `PoiScenario` | 监听 → 双控制权 → DRC → 起飞 → 进入 POI 环绕 → 调速 → 查询 → 退出 → 返航 → 收尾 |
| `WaylineScenario` | 获取执行中/暂停中任务 → 事件监听 → pause → resume → stop |
| `DrcFlightScenario` | 6 路 OSD 订阅 → 双控制权 → DRC → 20Hz 摇杆线程 → 交互操控菜单(起飞/摇杆/返航)→ 分步收尾(补返航兜底) |

新增 demo 只需：新建类继承 `ModuleDemo`/`Scenario`, 在 `DemoEntry` 注册表加一行 `add(...)`。

## 4. 相较旧 demo 的改进

| 维度 | 旧 demo | 本 demo |
|---|---|---|
| 结构 | 1 个 ~1.6 万行巨类、数个巨型 switch | 每模块/场景独立类 + 数据驱动注册表 |
| 监听器 | 233 处 listen / 仅 ~23 处 cancel(~210 泄漏) | `Subscription`(AutoCloseable) + `SubscriptionRegistry` 统一自动退订 |
| 回调等待 | `AtomicBoolean + while sleep` 忙等 | `Await`(CountDownLatch) 无忙等、可超时 |
| 结果语义 | 成功/失败二值, 超时含糊 | `Outcome` 三态(SUCCESS / FAILED / TIMEOUT=命令可能已生效), 结果解析统一 SUCCESS 门控 |
| 参数 | 手动交互输入 / 硬编码 | `demo_params.json` 文件化, 分组读取 |
| 配置/密钥 | 13 份含真实 JWT 的 config 入库 | `config.json` 不入库 + 环境变量 `FLIGHTHUB_AUTH_VALUE` 注入 |
| JSON | 正则解析(嵌套即坏) | 手写递归下降 `Json`(零依赖, 支持嵌套/转义/Unicode) |
| 日志 | 600+ 处裸 `System.out` | 统一走自封装滚动文件日志 `DemoLog`(不裸 `System.out`) |
| 生命周期 | 分散、易漏 shutdown、库内 `System.exit` | try-with-resources, `SdkRuntime`/`DeviceSession` 均 AutoCloseable, 无 `System.exit` |
| 拉流退订顺序 | 先关文件后退订 → 静默丢帧 | 先退订再关文件(修正 bug), 登记进 registry 兜底 |

## 5. 注意事项

- **危险动作默认注释/二次确认**：`FlightControlDemo` 的返航/急停默认注释；格式化、重启等破坏性动作在 `DeviceDebugDemo` 中均有二次确认。
- **飞行安全兜底**：场景 demo 对「已下发可能生效」的指令采用保守收尾(TIMEOUT 置位、服务端明确拒绝时复位标志、finally 分步兜异常补返航/急停), 单条指令失败不会终止 `DrcFlightScenario` 的空中操控菜单。
- **设备状态还原**：`FlySafeUnlockDemo` 自动开启的机场远程调试模式会在 demo 退出时自动 `closeDebugMode` 还原。

- **回调线程**：SDK 回调在 native/RTC 线程触发, 回调里只做「保存结果 + countDown / 计数」, 勿阻塞。
- **拉流 0 字节**：通常是上游无真实推流(非 demo/SDK bug), 先确认设备确实在推流；0 字节文件会被自动删除。
- **鉴权模型**：本 SDK 版本无 `setToken`, 采用 gateway header 注入(`setHttpHeaders`/`setWebSocketAuthHeaders`)。
- **退出与退出码**：SDK 用过直播(RTC)后残留 WebRTC 原生线程, 进程退出会死锁在 RTC 库的
  exit 析构(SDK 已知问题, `shutdown()` 返回 SUCCESS 也一样)。demo 在 `halt(0)` 前布置了
  「3 秒后进程外 `kill -9`」兜底：**未用过直播时正常退出码 0；用过直播时由 SIGKILL 收尾, 
  退出码 137(shell 可能打印一行 `Killed`, `gradle run` 会显示 FAILED)——均属预期, 不是崩溃**, 
  外层脚本/CI 判定时请将 137 视为成功。容器内裸跑 `java`(PID 1)时该兜底无效(内核会丢弃
  同命名空间发给 PID 1 的 SIGKILL), 请用 `docker run --init`(或 tini)启动。
