# Flight Hub SDK 模块化 Demo

> 位置：`platform/java/new_sdk_demo`　| 　源码 & 产物：Java 8(字节码 class v52)　| 　构建/开发 JDK：8 或 21 均可　| 　依赖：仅 SDK jar(零第三方)

一个按模块拆分、场景化、参数文件化、无监听器泄漏的 Java SDK demo, 作为接入方的**官方标准用法示例**, 也可作为问题定位辅助手段。它与旧的 `flight_hub_sdk_demo/FlightHubSDKDemo.java`(~1.6 万行巨类)**并存互不影响**。

功能模块 demo 与场景 demo **全部深度实现**(无脚手架/TODO), 并已通过一轮全量代码评审与整改。

**当前规模**(唯一出处; 目前由人工核对维护, 尚无自动校验/生成脚本, 改动代码后请手动比对并更新下表):

<!-- BEGIN generated-counts (人工维护, 尚无自动校验/生成脚本, 改动代码后请手动核对并更新) -->
| 项 | 数量 |
|---|---|
| Java 源文件 | 50 |
| 代码行数 | 26438 |
| 功能模块 demo（注册表） | 25 |
| 场景 demo（注册表） | 5 |
| `runtime/` 类 | 13 |
| `base/` 类 | 4 |
| `demo_params.json` 分组节 | 37 |
| `FlightControlDemo` 菜单项 | 16 |
<!-- END generated-counts -->

> **为什么规模数字只写在这一处**：这些数字是**可计算的事实**，散写进正文就会随代码腐化——此前正文写「43 个源文件、约 2.3 万行、20 个功能模块 demo」而实际已是 48/2.5 万+/23。现在正文一律**不再抄写**规模数字，只引用本表；数字、菜单序号是否腐化目前依赖人工核对，尚无自动校验脚本。

---

## 1. 快速开始

构建/运行方式与老 demo 工程 `flight_hub_sdk_demo` 一致：自带 `gradlew` wrapper, `build.sh` 是其薄封装。

```bash
cd platform/java/new_sdk_demo

# 1) 准备配置(不入库)
cp config.json.example config.json
cp drtc_config_node.json.example drtc_config_node.json   # 飞机 DRTC 拉流才需要, 见下方说明
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
> - 如果 CWD 下没有这个文件, 会回退到 jar 里打包的默认值(即 `src/main/resources/demo_params.json` 编译进去的那份), 不会报错, 但你改的值不会生效——遇到“改了参数没用”先检查是不是放错了目录(日志首行会写明本次「参数取自文件: &lt;绝对路径&gt;」还是「参数取自内置资源 /demo_params.json」)。
> - `tests/demo_params.json` 是仓库里留存的一份测试用副本(配合 `tests/new-sdk-demo-executable.jar` 使用), 不是运行时会自动读取的固定路径。

<!-- 两段引用块之间的分隔, 避免被渲染成同一段 -->

> **日志写到哪、写多少？** 三项配置跨两个文件, 启动时由入口一次性注入并在控制台打一行 `[DemoLog] 日志根目录 = ...` 横幅回显生效值：
>
> - `demo_params.json` 的 `logging.demo_logs_dir` — 日志根目录(下面再按天分 `yyyy-MM-dd/` 子目录), 缺省 `demo_logs`；
> - `demo_params.json` 的 `logging.max_file_size_mb` — 单个日志文件上限, 缺省 30MB, 超限切下一个序号；
> - `config.json` 的 `sdk.log_level` — **同时**作用于 SDK 与 demo 自身日志的最低落盘级别, 取 `debug|info|warn|error`(忽略大小写, 兼容 `trace/verbose/warning/fatal` 等别名)。写错或留空一律回退 `debug`(不过滤)并在横幅里说明, 不会静默吞日志；`error` 永不被过滤。

<!-- 两段引用块之间的分隔, 避免被渲染成同一段 -->

> **`drtc_config_node.json` 要不要准备？** 只有**飞机 DRTC 拉流**用得到(Linux `BUILD_WITH_DRTC_USER` 场景), 其余 demo 不碰它：
>
> - `config.json` 的 `sdk.drtc_config_path` 指向它, 模板里的缺省值 `drtc_config_node.json` 是**相对 CWD** 的路径(原样透传给 native, 见 `SdkRuntime`)——按上面的用法(`cd` 到 `new_sdk_demo/` 后原地跑)放在本目录根下即可；该项留空则飞机 DRTC 拉流 Init 会失败。
> - `drtc_config_node.json.example` 是**占位空模板**, host 全为 `xx.xx.xx.xx`(发包可用), 真实环境要改成媒体服务地址——仲裁 / 信令 / token / monitor 各段独立配置。
> - 🔒 模板里所有 `verify_ssl` 已默认 `true`(发包硬性要求, 与老 demo 的 `demo/drtc_config_node.json` 一致)。`false` 仅用于本地无 CA 证书的联调环境, **生产环境禁止 `false`**(否则中间人攻击可劫持 DRTC 会话)——本地要关校验请改自己那份 `drtc_config_node.json`(已 gitignore), 别改 `.example`。

也可直接用 gradle 任务：

```bash
./gradlew run --args="config.json"                 # 交互菜单(不打包, 开发主循环走这条)
./gradlew run --args="config.json FlyToScenario"   # 直接跑指定 demo(名字大小写不敏感)
./gradlew executableJar                            # 只打可执行 Fat JAR
./gradlew build                                    # 完整构建(编译+检查), 同样产出可执行 Fat JAR
```

build.sh 选项：`-c` 清理 / `-b` 构建(含可执行 JAR) / `-e` 打可执行 JAR / `-r` 运行 / `-a` 全部。

> **改完代码请手动核对一次文档一致性**(build.sh 不提供文档校验入口, 需要人工完成)。
> 「文档里写死、代码一改就腐化」的以下三项事实目前需要人工比对，暂无自动化校验：
>
> 1. **规模数字** —— 上方规模表 8 项与代码现算值逐项比对；不一致请手动更新该表；
> 2. **菜单序号** —— 文档里每个「`[N]` 动作名」都应按动作名回代码反查真实序号核对。这条防的是真实事故：`FlightControlDemo` 中段插入 3 项后序号整体位移, 文档停在旧号, 照文档点 `[13]` 会点到「指点飞行更新目标点」(会让飞机真实改向)；
> 3. **文档引用的文件存在性** —— `src/`、`docs/` 下被引用的路径应确保真实存在(`tests/` 是 gitignore 的生成物目录——也正因如此, 别把 `tests/` 下的东西当交付物引用)。
>
> 以上三项目前均为人工核对清单, 尚无自动化校验脚本。

> **`build/libs/` 下其实有两个 jar, 本工程只认 Fat JAR 一个：** `executableJar`(产出 `new-sdk-demo-executable.jar`)已挂在 `assemble` 上, 但这是新增挂载而非替换——Java 插件默认的 `jar` 任务(产出瘦包 `new-sdk-demo.jar`, 无 `Main-Class`、不含依赖, 本工程从未用过它启动)仍随 `assemble` 一起跑, 会同时出现在 `build/libs/` 里, 忽略它即可。真正对外承认、且下面这条"不是残留"的保证只适用于 `new-sdk-demo-executable.jar`：
> `./gradlew build` 与 `./build.sh -b` 产出它时不会是上一次的残留(此前它不在 `build` 链上, `build.sh -b` 会把陈旧 jar 当作"本次产物"列出来)；`./gradlew clean`
> 也会一并删掉同步到 `tests/` 的那份副本。同时 `application` 插件默认的 `distZip`/`distTar` 分发包
> (实测 111MB + 113MB = 224MB, 本工程从不使用)已禁用, `build/` 由 227MB 降到 116MB。
>
> 代价：完整构建由 5s 升到 13s(fat jar 需解开 SDK jar 再整体重压, 且 manifest 的 `Built-Date` 每次取值
> 不同, 该任务永不 UP-TO-DATE)。别把「砍掉 224MB 分发包」理解成变快——公平的对照是「拿到可执行 JAR 要
> 多久」: 此前得 `build`(5s) + 单独跑 `executableJar`(12s) = 17s / 345MB, 现在 13s / 116MB。日常开发不必
> 付这笔钱——`./gradlew run` 只走 `compileJava→classes→run` 四个任务, 不经 assemble; `installDist`/启动
> 脚本亦不受影响。

<!-- 两段引用块之间的分隔, 避免被渲染成同一段 -->

> SDK jar 路径由 `config.properties` 的 `sdk.jar.path` 指定——仓库自带的 `config.properties` 已预填 `../dist/flight-hub-sdk.jar`, 但那只是文件里写好的值, 并非 Gradle 的代码级回退：`build.gradle` 的 `resolveSdkJar()` 本身**没有**任何默认值逻辑, 该行若被清空或删除会直接抛 `GradleException` 导致构建失败(`build.sh` 的预检查 `check_sdk_jar()` 虽有类似兜底路径用于提前报错提示, 但那条路径不会传给 Gradle)。
> 原生库(.so/.dylib)已打包在 jar 内, `initialize()` 时按当前平台自动加载。

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
  | Linux aarch64 | ✅ 支持 |
  | macOS arm64(Apple Silicon) | ✅ 支持 |
  | Windows / macOS x86_64(Intel) | ❌ 未内置, `initialize()` 会加载失败 |

  在未内置的平台上运行时, `SdkRuntime` 会抛出可读的 `FlightHubException`(含当前 `os.name/os.arch` 与已支持平台清单), 而非裸的 `UnsatisfiedLinkError`。

## 2. 目录结构

```
new_sdk_demo/
├── build.gradle / settings.gradle / config.properties
├── config.json.example            # 连接与鉴权模板(复制为 config.json, 不入库)
├── drtc_config_node.json.example  # DRTC Native SDK 配置模板(复制为 drtc_config_node.json, 不入库)
├── src/main/resources/demo_params.json   # 可变参数(按模块/场景分组; 分组节数见开头规模表)
├── tests/                         # 评审报告、可执行 jar 同步件、测试配置与运行留痕
└── src/main/java/com/dji/flighthubsdk/demo/
    ├── DemoEntry.java             # 统一入口 + 数据驱动注册表(替代旧 demo 里的巨型 switch 分发)
    ├── runtime/                   # 瘦运行时(生命周期/会话/订阅/等待/JSON/配置/日志; 类数见开头规模表)
    ├── base/                      # ModuleDemo / Scenario / Precondition 基类
    ├── utils/                     # Utils(控制台格式化 / readLine / describe 等跨模块工具)
    ├── module/                    # 功能模块 demo + GroundFlightMonitor 协作类(全部深度实现; 数量见开头规模表)
    └── scenario/                  # 场景 demo(全部深度实现; 数量见开头规模表)
```

## 3. Demo 清单(全部深度实现, 无脚手架)

### 模块 Demo(菜单驱动)

| 类 | 覆盖内容 |
|---|---|
| `FlightControlDemo` | 消息监听、飞行控制权、DRC 链路、一键起飞、WASD 摇杆、急停、返航/取消返航、DRC 实时设置飞行速度、指点飞行(启动/更新目标点)、舱外起飞/舱外降落、独立入口 [16] 舱外起降状态监听(逻辑在 `GroundFlightMonitor`) |
| `PayloadControlDemo` | 负载控制权、相机模式/镜头/变焦/存储/夜景/红外、拍照/录像/全景(20 项子菜单, 跨 6 个模块) |
| `LiveStreamDemo` | 无人机+机场两路码流拉取落盘 .h264、拉流状态、画质设置、按序停止、AI 识别开关(AIIdentifySet)、AI SEI 订阅观察(可指定 payloadIndex) |
| `LiveRecordDemo` | 直播录制开始/停止、记录查询、录制状态订阅(LiveRecordModule 全部 5 接口) |
| `LiveSyncDemo` | 旁路推流：设备摄像头直播流转推至 RTMP/RTSP/GB28181 第三方平台, 创建/开关/更新/删除转码器、可用设备与转码器列表、汇总统计(LiveSyncModule 全部 6 接口, [4] 开关/[5] 更新均调用同一 `updateLiveSync`) |
| `DeviceOsdDemo` | 无人机/机场 OSD 与在线状态推送、HMS 消息(60+ 路监听, 定时自动退订) |
| `DeviceManageDemo` | 设备详情(含子设备)、机场/飞机设备状态查询(DeviceManageModule 3 个只读接口) |
| `DockControlDemo` | 舱内/外摄像头切换、空调、舱盖、补光灯、充电 |
| `DeviceDebugDemo` | 机场/飞行器调试模式、舱盖、充电、重启/格式化、飞行参数、电池/SIM/低功耗、云环境切换(8 个子菜单, 模块全接口) |
| `DrcDemo` | DRC 链路完整生命周期：负载控制权 → 建链 → 云台重置验证 → 断链 |
| `PsdkDemo` | PsdkControlModule 全部 16 个控制接口 + 状态监听 + PsdkResourceModule 全部 7 个资源接口 |
| `FlightTaskDemo` | 航线任务 CRUD/状态更新/设备推荐/TCA/飞行安全风险 + 航线文件管理(18 个常用接口) |
| `FrogLeapDemo` | 蛙跳(A 起 B 降)任务：创建蛙跳任务 + 任务状态/多机场返航信息/任务异常三类事件订阅 + 指定机场返航(6 项子菜单) |
| `FlightSafetyDemo` | FlightSettingsModule 全部 5 个飞行安全设置接口 |
| `FlySafeUnlockDemo` | 限飞解禁证书全流程(上传→检查→提交→查询→切换→清除, 6 接口), 自动开关机场调试模式并在退出时还原 |
| `SafetyServiceDemo` | 信号地图、自定义飞行区域、地形障碍物离线地图 |
| `MediaServiceDemo` | 媒体回传/后处理通知订阅、文件列表/下载/删除、STS 上传凭证与回调 |
| `PanoStitchDemo` | 全景拍照(负载控制权 → DRC 建链 → 切模式/切镜头 → drcStartPanorama)+ 全景拼接开始/停止/重试、3X 成片查询、瓦片 URL 与下载地址获取、拼接与拍照进度订阅(11 项子菜单) |
| `TerraBuildDemo` | Terra Build 建图重建：模型文件管理、重建任务、全景拼接、WS 状态推送、LAS 坐标系 |
| `SystemManagementDemo` | 日志管理 + 固件管理两功能域(多级子菜单) |
| `FlightTaskRecordDemo` | 飞行记录导出 + 导出进度查询(模块全部 2 接口) |
| `MaintenanceDemo` | 报修配置查询 → 创建报修订单 → 订单详情 → 分页列表 |
| `AIBoxDemo` | AI Box 信息、应用列表/安装/操作、文件导出/下载链接 |
| `AutoRecordDemo` | 自动录制配置获取/保存(AutoRecordModule 全部 2 接口) |
| `AiAlertDemo` | AI 告警：设备级/算法级告警配置读写、告警架次统计、告警明细查询、告警推送监听、告警记录删除(IAIAlertModule 7 个 HTTP 接口 + 1 个 WS Listen 接口, 共 8 项子菜单) |

> `module/` 下还有一个 `GroundFlightMonitor` —— 它**不是** `ModuleDemo` 子类、不在 `DemoEntry` 注册表里,
> 而是 `FlightControlDemo` 菜单项 `[16] 舱外起降状态监听` 背后的自包含协作类, 详见下方「舱外起降状态监听」。

### 场景 Demo(跨模块编排)

| 类 | 流程 |
|---|---|
| `FlyToScenario` | 建 DRC(停止的硬前置)→ 双控制权(负载最多尝试 3 次即首次 + 重试 2 次, 耗尽则跳过)→ 监听 + 订阅任务事件(先于 start)→ 指点飞行 start → 更新目标点 → 观察 → drcStop → 完备收尾。**前置: 飞机须已在空中；本场景不起飞、不返航** |
| `PoiScenario` | 监听 → 双控制权 → DRC → 起飞 → 进入 POI 环绕 → 调速 → 查询 → 退出 → 返航 → 收尾 |
| `WaylineScenario` | 创建任务并轮询至执行中/已暂停 → 事件监听 → pause → resume → 返航 → 挂起任务(status=6) |
| `DrcFlightScenario` | 6 路 OSD 订阅 → 双控制权 → DRC → 20Hz 摇杆线程 → 交互操控菜单(起飞/摇杆/返航)→ 分步收尾(补返航兜底) |
| `DrcPanoScenario` | 负载控制权 → DRC → 订阅拼接进度/全景状态 → 切全景模式与镜头 → 启动全景拍照 → 等拍照完成 → 等后端自动拼接 → 取瓦片 URL → 收尾 |

新增 demo 只需：新建类继承 `ModuleDemo`/`Scenario`, 在 `DemoEntry` 注册表加一行 `add(...)`。

## 3.1 [16] 舱外起降状态监听(`FlightControlDemo`)

面向「舱外(地面)起飞 → 机场降落」全过程的**独立**旁路观察入口, 与 [1] 通用消息监听**互不影响、可同时开启**
(SDK 按 `listenId` 逐个登记回调, 同一 topic 多次订阅各自独立, 取消其一不影响其二)。

逻辑全部自包含在 `module/GroundFlightMonitor.java`：自己的订阅列表、自己的开/关方法、自己的状态码释义,
不复用也不改动 [1] 的 `monitorSubs`/`startMonitors`/`stopMonitors`。`FlightControlDemo` 只做菜单挂接与退出收尾。

订阅 11 路：

| 组 | 内容 |
|---|---|
| 核心 6 路(起降主链) | 飞机模式码、机场舱盖、飞机在舱、机场模式码、机场推杆、DRC 降落任务状态 |
| 高度 / 位置 | DRC OSD(高度/相对起飞点高度/经纬度/升降速度)、飞机位置状态(定位固定解/星数) |
| 在线 / 链路 / 异常 | 飞机在线、DRC 链路、飞行任务异常——监听「没数据」时用来快速定位是掉线/没建链/任务报错 |

- **推送只落日志文件、不刷控制台**(与 [1] 现状一致), 控制台只回显开/关与逐路建立结果;
- 机场侧 4 路需当前设备**含机场**, 无机场时自动跳过、其余照常; DRC 两路须先经 [4] 建链才有数据(订阅本身照常成功);
- 事后捞整条状态链推荐按日志 tag 过滤(纯 ASCII、天然区分 [1] 消息监听 与 [16] 舱外起降状态监听)：
  `grep 'SDKDemo::GroundFlightMonitor' sdk_demo_log_*.log`。
  推送行另带 `[舱外起降]` 前缀, 按前缀过滤**必须加 `-F`**(否则方括号会被当成正则字符类)：
  `grep -F '[舱外起降]' sdk_demo_log_*.log`。

## 4. 相较旧 demo 的改进

| 维度 | 旧 demo | 本 demo |
|---|---|---|
| 结构 | 1 个 ~1.6 万行巨类、数个巨型 switch | 每模块/场景独立类 + 数据驱动注册表 |
| 监听器 | 大量 listen 调用、cancel 覆盖极少(存在监听器泄漏) | `Subscription`(AutoCloseable) + `SubscriptionRegistry` 统一自动退订 |
| 回调等待 | `AtomicBoolean + while sleep` 忙等 | `Await`(CountDownLatch) 无忙等、可超时 |
| 结果语义 | 成功/失败二值, 超时含糊 | `Outcome` 三态(SUCCESS / FAILED / TIMEOUT=命令可能已生效), 结果解析统一 SUCCESS 门控 |
| 参数 | 手动交互输入 / 硬编码 | `demo_params.json` 文件化, 分组读取 |
| 配置/密钥 | 13 份含真实 JWT 的 config 入库 | `config.json` 不入库 + 环境变量 `FLIGHTHUB_AUTH_VALUE` 注入 |
| JSON | 正则解析(嵌套即坏) | 手写递归下降 `Json`(零依赖, 支持嵌套/转义/Unicode) |
| 日志 | 600+ 处裸 `System.out` | 统一走自封装滚动文件日志 `DemoLog`(不裸 `System.out`) |
| 生命周期 | 分散、易漏 shutdown、库内 `System.exit` | try-with-resources, `SdkRuntime`/`DeviceSession` 均 AutoCloseable, 无 `System.exit` |
| 拉流退订顺序 | 先关文件后退订 → 静默丢帧 | 先退订再关文件(修正 bug), 登记进 registry 兜底 |

## 5. 注意事项

- **危险动作二次确认**：`FlightControlDemo` 中会让飞机真实起飞/移动/改向/降落入舱的**七个**动作——[6] 一键起飞、[7] WASD 摇杆、[11] DRC 实时设置飞行速度、[12] 指点飞行启动、[13] 指点飞行更新目标点、[14] 舱外起飞、[15] 的「降落至机场」——执行前须输入 `yes` 确认；急停/返航/取消返航/取消降落属**恢复类**动作, 刻意不设确认(需要时越快越好)。格式化、重启等破坏性动作在 `DeviceDebugDemo` 中均有二次确认。
- **起飞后退出提醒**：`FlightControlDemo` 记录本次会话是否执行过起飞([6] 一键起飞 / [14] 舱外起飞), 退出时告警提醒——**不自动返航/降落**(demo 静默触发返航比把决定留给操作者更危险)。若期间下发过收回类动作([9] 返航 / [15] 降落至机场), 措辞放缓为「已下发但需确认落地」, 但**两档都会告警、不存在「已安全」这一档**：指令下发成功只代表服务端收下了指令, 不代表飞机已落地。取消返航/取消降落/再次起飞都会让措辞退回强告警。
- **飞行安全兜底**：场景 demo 对「已下发可能生效」的指令采用保守收尾(TIMEOUT 置位、服务端明确拒绝时复位标志、finally 分步兜异常补返航/急停), 单条指令失败不会终止 `DrcFlightScenario` 的空中操控菜单。
- **设备状态还原**：`FlySafeUnlockDemo` 自动开启的机场远程调试模式会在 demo 退出时自动 `closeDebugMode` 还原。

- **回调线程**：SDK 回调在 native/RTC 线程触发, 回调里只做「保存结果 + countDown / 计数」, 勿阻塞。
- **拉流 0 字节**：停止时会先看本路有没有发生过写盘失败——**没有**才判为上游无真实推流(非 demo/SDK bug), 此时删除空文件, 请确认设备确实在推流；**有**则是本机落盘问题(磁盘满/无写权限/文件被移走), 文件保留并在停止统计与 `[2] 拉流状态` 里标出失败次数与首因。
- **停止拉流未完成时会拒绝重新拉流**：`[4]` 停止对每路各起一个线程并最多等 6 秒, 若某路原生 `stop` 卡在 RTC teardown, 该路**不算已停**, 此后对**同一设备**选 `[1]` 会提示「上一次停止尚未完成」并拒绝执行。这是有意为之：SDK 的 `getLiveStream` 按 `payloadIndex` **缓存 `LiveModule` 实例**, 提前重拉会拿到同一个对象, 旧 `stop` 返回时会关掉新一路正在用的回调线程池、并在同一 native 模块上 teardown 刚建好的链路(新一路静默 0 帧)。拒绝是**按设备 SN** 逐台判定的——飞机路卡住不影响机场路, 换选另一台设备也不受影响。等停止线程跑完即自动放行(下次列菜单或选 `[1]` 时自动重查, 无需重启); 菜单表头会常驻提示还有哪台设备没停完。长时间不恢复只能重启进程 —— `r` 重选**同一台**设备不会清掉 SDK 的 `LiveModule` 缓存(`Drone`/`Dock` 实例本身按 SN 进程级缓存)。
- **鉴权模型**：本 SDK 版本无 `setToken`, 采用 gateway header 注入(`setHttpHeaders`/`setWebSocketAuthHeaders`)。
- **退出与退出码**：SDK 用过直播(RTC)后残留 WebRTC 原生线程, 进程退出会死锁在 RTC 库的
  exit 析构(SDK 已知问题, `shutdown()` 返回 SUCCESS 也一样)。demo 在 `halt(0)` 前布置了
  「3 秒后进程外 `kill -9`」兜底：**未用过直播时正常退出码 0；用过直播时由 SIGKILL 收尾, 
  退出码 137(shell 可能打印一行 `Killed`, `gradle run` 会显示 FAILED)——均属预期, 不是崩溃**, 
  外层脚本/CI 判定时请将 137 视为成功。容器内裸跑 `java`(PID 1)时该兜底无效(内核会丢弃
  同命名空间发给 PID 1 的 SIGKILL), 请用 `docker run --init`(或 tini)启动。
