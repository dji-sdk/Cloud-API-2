---
name: cloudapi2-ai
description: |
  DJI Cloud API 2 AI 域 SDK 接入助手。当用户要编写 Java 代码管理 AI Box 应用、机载 AI 识别/跟踪/告警、
  AI Inside 机载算法生命周期、或 AI 目标检测告警配置与查询时调用。
  触发词：AIBoxModule、OnboardAiModule、AIInsideManageModule、AiAlertModule、AI Box、机载 AI、
  AI 识别、AIIdentifySet、AIModelSelect、DrcAISpotlightZoom、AI 跟踪、DrcAIAlarm、AI 主动告警、
  AI Inside、机载算法、algorithmType、operateAIInsideModel、uploadAlgorithmFile、
  AI 告警配置、postDeviceSetting、postAlgorithmSetting、getAiAlertFlights、getAiAlertRecords、
  目标检测告警、fetchAIBoxInfo、installApp、operateApp。
  不触发：调用 MCP Server 的 fh_* 工具做运维操作（→ cloudapi2-mcp-guide）、
  通用 SDK 规则/飞行控制/航线任务（→ cloudapi2-sdk-core）、地图域（→ cloudapi2-map）、
  PSDK 负载控制（探照灯/喊话器/Widget，→ cloudapi2-payload）、
  设备生命周期与固件升级（→ cloudapi2-device-management）、SEI/OSD 遥测（→ cloudapi2-sei-osd）。
version: 1.0.0
author: DJI Cloud API 2 Team
---

# DJI Cloud API 2 AI 域接入助手

## 这个 Skill 是干什么的

帮集成商开发者正确编写调用 **DJI Cloud API 2 Java SDK AI 域四个模块**的集成代码：`AIBoxModule`（10 方法）、
`OnboardAiModule`（18 方法）、`AIInsideManageModule`（9 方法）、`AiAlertModule`（7 方法），共 44 个方法。

四个模块服务于同一个大场景（无人机 AI 能力），但职责互不重叠：

- **`AIBoxModule`**：管外挂 AI 盒子硬件本身（查信息、装应用、导文件）——纯 HTTP + 1 个内部 STS 直传
- **`OnboardAiModule`**：管机载 AI 的**实时控制与订阅**（识别开关/阈值、模型选择、目标跟踪、主动告警）——全部走 DRC
- **`AIInsideManageModule`**：管机载算法包本身的**生命周期**（列表、绑定设备、装载/卸载、私有化上传）——纯 HTTP
- **`AiAlertModule`**：管**目标检测告警的配置与历史查询**（设备级开关、算法级阈值、架次统计、明细记录）——纯 HTTP

**核心价值**：本域最贵的坑是**方法命名与参数类型的不规则性**——`OnboardAiModule` 里 14 个方法首字母大写（`AIIdentifySet`、
`DrcAIAlarmStart`），6 个方法的参数类型是 `generated` 包下的 snake_case 类而非常规 camelCase bean；`AIBoxModule.getAIBoxModule()`
取模块失败时不记日志（其余三个模块的 getter 都记）；`AiAlertModule` 的 HTTP 路径带 `/drc/` 前缀却不依赖 DRC 链路。
按通用 SDK 直觉编造这些细节必然导致编译失败或运行时行为误判。

## 何时触发

- 用户要写代码查询/管理 AI Box（信息查询、应用列表、安装、导出文件、上传文件）
- 用户要写代码控制机载 AI 识别开关、过滤条件、分数阈值、模型选择
- 用户要写代码做 AI 目标跟踪（Spotlight Zoom：框选、确认、跟随、停止）
- 用户要写代码开启/关闭机载 AI 主动告警、配置告警模型
- 用户要写代码管理 AI Inside 机载算法（查列表、查绑定设备、批量装载/卸载、上传私有化算法包）
- 用户要写代码配置 AI 目标检测告警（设备级开关、算法级阈值）或查询告警架次/明细记录
- 用户遇到相关报错：`AIIdentifySet` 编译不过、`getAIBoxModule()` 返回 null 但没日志、`droneSNList` 编译报错

**不触发**：

- 用户要**运行时直接调用 MCP Tool** → `cloudapi2-mcp-guide`
- 通用 SDK 规则（初始化、鉴权、回调顺序、listen/cancel 配对）、飞行控制、航线任务、相机/云台控制 → `cloudapi2-sdk-core`
- 地图域（飞行区、离线地图、解锁证书、地图元素）→ `cloudapi2-map`
- PSDK 第三方负载控制（探照灯、喊话器、Widget）→ `cloudapi2-payload`
- 设备生命周期、固件升级、HMS 告警（非 AI 目标检测告警）→ `cloudapi2-device-management`
- SEI/OSD 遥测、视频流 → `cloudapi2-sei-osd`

## 模块位置（先确认挂在哪）

```java
// ✅ AIBoxModule / OnboardAiModule 在 Drone 上
AIBoxModule aiBox = drone.getAIBoxModule();
OnboardAiModule onboardAi = drone.getOnboardAiModule();

// ✅ AIInsideManageModule / AiAlertModule 在 Workspace 上
AIInsideManageModule aiInside = workspace.getAIInsideManageModule();
AiAlertModule aiAlert = workspace.getAIAlertModule();
```

| 模块 | 挂载对象 | 方法数（源码核实） | 能力 | 详细文档 |
|---|---|---:|---|---|
| `AIBoxModule` | `Drone` | 10 | AI 盒子信息/应用管理（HTTP + 1 内部直传） | `references/ai-box.md` |
| `OnboardAiModule` | `Drone` | 18 | AI 识别/跟踪/告警实时控制（DRC + DRC Northbound） | `references/onboard-ai.md` |
| `AIInsideManageModule` | `Workspace` | 9 | 机载算法生命周期管理（HTTP） | `references/ai-inside.md` |
| `AiAlertModule` | `Workspace` | 7 | 目标检测告警配置与查询（HTTP） | `references/ai-alert.md` |

**四个 getter 的判空行为并不统一**：

| getter | 底层为 null 时 |
|---|---|
| `drone.getAIBoxModule()` | **静默返回 null，不记日志**（唯一的例外） |
| `drone.getOnboardAiModule()` | 构造异常时 `Logger.error` 并返回 null |
| `workspace.getAIInsideManageModule()` | `Logger.error(TAG, "...module is null")` 并返回 null |
| `workspace.getAIAlertModule()` | `Logger.error(TAG, "...module is null")` 并返回 null |

## 核心规则

### 规则 1：`OnboardAiModule` 里 14 个方法名首字母大写，必须原样调用

`AIIdentifySet`、`AIIdentifyFilterSet`、`AIIdentifyScoreSet`、`AIIdentifyScoreModeSet`、`AIIdentifyScoreReset`、`AIModelSelect`、
`DrcAISpotlightZoomSet/Select/Confirm/Stop/Track`、`DrcAIAlarmStart/Stop`、`DrcAIModelsConfig`——这些不是驼峰命名笔误，
“修正”成小写开头会编译不过。只有 `listenAIDataInfo`/`cancelListenAIDataInfo`/`listenDrcAIAlarmState`/`cancelListenDrcAIAlarmState`
是正常小写驼峰。详见 `references/onboard-ai.md`。

### 规则 2：AI 识别 6 个方法用 `generated` 包 snake_case 参数，不是 camelCase bean

`AIIdentifySet`/`AIIdentifyFilterSet`/`AIIdentifyScoreSet`/`AIIdentifyScoreModeSet`/`AIIdentifyScoreReset`/`AIModelSelect`
的参数类型是 `com.dji.flighthubsdk.generated.Ai*Param`，字段访问器是 `setLight_type`/`setScore_mode` 这类 snake_case，
**不要**编造一个 camelCase 的 bean 包装类。而 `DrcAISpotlightZoomSelect`/`DrcAIAlarmStart`/`DrcAIModelsConfig` 用的才是
`com.dji.flighthubsdk.bean.*Params`（camelCase，内部走 `FlightHubSdkConvert` 转 native）。两套参数体系不能混用。

### 规则 3：`AIBoxModule.getAIBoxModule()` 失败时不记日志，判空不能依赖日志排查

与另外三个模块的 getter 不同，`drone.getAIBoxModule()` 在底层模块为 null 时直接返回缓存的 null，没有 `Logger.error`。
业务层必须显式判空并给出提示，不能靠搜日志定位问题。

### 规则 4：`AiAlertModule` 路径带 `/drc/paas/v2/` 前缀，但不依赖 DRC 链路

`AiAlertModule` 全部 6 个业务方法走纯 HTTP，**不需要** `connectDrcLink`。看到路径里有 `/drc/` 就加 DRC 前置校验是误判。

### 规则 5：`AIBoxModule` 的 `fetchXxxProgress` 三组方法是一次性查询，不是 listen/cancel 订阅

`fetchAppListProgress`/`exportFileProgress`/`installAppProgress` 本质是又一次 HTTP 查询，需要调用方自己写轮询循环，
不是长连接订阅，没有对应的 cancel 方法。

### 规则 6：Bean 字段命名有多处不规则大小写，照抄前先看源码

- `OperateAIInsideModelParam.droneSNList`（`SN` 全大写）——不是 `droneSnList`
- `PostAiAlertDeviceSettingParam.enableTargetAlert` 的 getter 是 `isEnableTargetAlert()`——不是 `getEnableTargetAlert()`
- `GetAiAlertRecordsParam.flightId` 是 `List<String>` 但字段名是单数——不是 `flightIds`/`flightIdList`

详见各 `references/` 文件的字段表。

## 决策树路由

```
用户问题
├─ AI Box 信息查询 / 应用列表 / 安装 / 导出文件 / 上传文件
│    → references/ai-box.md
├─ 机载 AI 识别开关 / 过滤 / 分数阈值 / 模型选择
│    → references/onboard-ai.md#2-ai-识别开关过滤阈值6-个-action
├─ AI 目标跟踪（框选 / 确认 / 跟随 / 停止）
│    → references/onboard-ai.md#3-drc-ai-spotlight-zoomai-跟踪5-方法
├─ AI 主动告警开启/关闭/模型配置（机载 DRC 侧）
│    → references/onboard-ai.md#4-drc-ai-alarm主动告警3-方法--1-对订阅
├─ AI 数据订阅 / AI 告警状态推送（DRC Northbound）
│    → references/onboard-ai.md#1-ai-数据订阅1-对-listencancel
├─ AI Inside 机载算法列表 / 绑定设备 / 批量装载卸载
│    → references/ai-inside.md
├─ 私有化场景算法包上传（STS / 直传）
│    → references/ai-inside.md#私有化场景上传算法两条路径不要混用
├─ AI 目标检测告警配置（设备级开关 / 算法级阈值）
│    → references/ai-alert.md#两层配置设备级--算法级
├─ 告警架次统计 / 告警明细记录查询
│    → references/ai-alert.md#查询告警架次与明细两段式不要混用
├─ `getAIBoxModule()`/其它 getter 返回 null 排查
│    → 本文件「规则 3」
└─ 要调 MCP 工具做运维（不写代码）
     → 转交 cloudapi2-mcp-guide
```

## 工作流

本 Skill 遵循 `skills/_shared/sop-template.md` 定义的 7 章节 SOP，按 L1/L2/L3 自动选档。

### Ch2 澄清问题

AI 域需求必须澄清的点（缺一项就可能写错）：

1. **目标是哪个模块**：AI 盒子硬件管理、机载 AI 实时控制、算法生命周期管理，还是告警配置/查询——四者职责不重叠，选错模块会南辕北辙
2. **是否需要 DRC 链路**：`OnboardAiModule` 的 action 方法需要先 `connectDrcLink`；`AIBoxModule`/`AIInsideManageModule`/`AiAlertModule` 不需要
3. **是生产环境场景还是私有化场景**：`AIInsideManageModule` 的算法上传三个方法（`create`/`file-upload`/`upload`）仅用于私有化部署场景
4. **查询的是实时状态还是历史记录**：告警域区分"设备级开关查询"（`getDeviceSetting`）与"历史架次/明细查询"（`getAiAlertFlights`/`getAiAlertRecords`）

### Ch5 编码约束

参见 `constraints.yaml`，所有 `critical` 规则在生成代码时必须满足；通用 SDK 规则（初始化、鉴权、null 检查、
listen/cancel 配对）引用 `_shared/constraints-common.yaml`，不在本文件重复。

### Ch6 验证扩展

除通用验证外，本域代码必须检查：

- [ ] 模块 getter 取用对象正确（AIBox/OnboardAi → `drone`，AIInside/AiAlert → `workspace`），全部已判空
- [ ] `OnboardAiModule` 的 14 个大写开头方法名未被"修正"为小写驼峰
- [ ] AI 识别 6 个方法使用 `generated` 包 snake_case 参数类，未编造 camelCase bean
- [ ] `OnboardAiModule` 的 action 调用前有 `connectDrcLink` 成功分支
- [ ] `AiAlertModule` 调用未附加不必要的 DRC 前置校验
- [ ] `fetchAppListProgress`/`exportFileProgress`/`installAppProgress` 按轮询逻辑实现，未当成订阅
- [ ] `droneSNList`/`isEnableTargetAlert()`/`flightId` 等不规则命名未被改写为常规驼峰猜测
- [ ] `AIInsideManageModule`/`AiAlertModule` 在模块销毁时调用了 `destroy()`

## 反幻觉规则（强制）

1. **示例代码必须来源于本 Skill 的 references/ 或用户提供的文档**，不得凭模型记忆生成 API 签名、字段名、枚举值
2. **禁止按通用驼峰规则反推字段访问器**——本域已确认多处不规则命名（`droneSNList`、`isEnableTargetAlert()`、单数 `flightId`），
   必须以 `references/` 中列出的表格为准
3. **引用必须可溯源**——任何建议必须能追溯到 `references/` 文档路径或官方 SDK 源码
   （`core/AIBoxModule.java`、`core/OnboardAiModule.java`、`core/AIInsideManageModule.java`、`core/AiAlertModule.java`）

## 权威参考

| 文档 | 内容 |
|---|---|
| `references/ai-box.md` | `AIBoxModule` 全部 10 个方法：判空陷阱、两个偏离标准模式的方法、应用列表/导出/安装的轮询模式 |
| `references/onboard-ai.md` | `OnboardAiModule` 全部 18 个方法：大写命名清单、两套参数体系、Spotlight Zoom 跟踪流程、主动告警流程 |
| `references/ai-inside.md` | `AIInsideManageModule` 全部 9 个方法：`destroy()` 生命周期、私有化上传两条路径、字段命名陷阱 |
| `references/ai-alert.md` | `AiAlertModule` 全部 7 个方法：DRC 路径不依赖 DRC 陷阱、两层配置、架次/明细两段式查询 |
| `skills/_shared/constraints-common.yaml` | 跨 Skill 通用约束（回调/初始化/null 检查） |

## 共享规范

> 跨 Skill 共享约束见 `skills/_shared/constraints-common.yaml`。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载，不要一次性读取所有 references/。
