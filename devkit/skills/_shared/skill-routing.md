# Skill 路由与编排规则

> 本文件定义 cloudapi2-* Skill 在**同时匹配**时的仲裁、组合和转交规则。
> 各 Skill 的 SKILL.md 应引用本文件：`路由规则见 skills/_shared/skill-routing.md`。

---

## 零、全局排除规则（最高优先级：命中即返回 `none`）

**在套用下面任何触发词之前，先过这一节。** 触发词是**必要条件，不是充分条件**。

若用户输入落在以下任一类，**即使命中了某个 Skill 的触发词**（如 `error`、`bug`、`报错`、`连接失败`、`登录不了`、`知识库`、`DJI`），也**必须返回 `{"skill": "none"}`**，不得路由到任何 `cloudapi2-*` Skill。

| 排除类别 | 判据 | 反例（必须返回 `none`） |
|---|---|---|
| **非 DJI Cloud API 2 的 DJI 产品** | 指 DJI **消费级 App / 移动端 SDK / 整机操作**（DJI Fly、DJI Go、Mobile SDK、遥控器配对、IMU 校准）；**不含** DJI Cloud API 2 的 PaaS 组件与 MCP 工具 | `DJI Fly app 连不上遥控器`、`大疆 Mini 4 Pro 怎么校准 IMU`、`DJI Mobile SDK 的 Swift 接入方式` |
| **司空2 SaaS 平台操作** | 用户在**网页端/客户端**使用司空2 产品（登录、创建团队、账号等），而非编写 DJI Cloud API 2 集成代码 | `大疆司空2 网页登录不了`、`在司空2 网页端怎么创建团队` |
| **其他语言 / 其他厂商 SDK** | Python / Swift / Kotlin / Go 等**非 Java** 语言 SDK；AWS IoT / Azure / AirMap / UTM 等**非 DJI** 平台 | `Python SDK 报 ImportError`、`怎么对接 AirMap 的 UTM 接口`、`怎么集成 AWS IoT SDK` |
| **纯知识查询（非入库）** | 问「知识库里 X 对吗」「帮我查 X 文档」「总结这个文档」——是**读**知识库，不是**写**知识库 | `知识库里关于 SEI 的内容对吗`、`帮我查一下航线管理的文档`、`帮我总结一下这个文档的要点` |
| **与本体系完全无关** | 用户诉求**不涉及** DJI、DJI Cloud API 2、无人机/机场、航线、遥测、云端 API 中的**任何一项** | `Java 的 HashMap 和 TreeMap 有什么区别`、`服务器 CPU 使用率太高怎么排查`、`帮我写一个 Python 爬虫`、`今天天气怎么样`、`帮我算一下 37 乘以 42` |

### 正向内含边界（**不要**被上面的排除规则误伤）

以下诉求**属于** DJI Cloud API 2 体系，即使表述看起来"通用"，也**必须路由**，不得返回 `none`：

| 诉求 | 目标 Skill | 说明 |
|---|---|---|
| DJI Cloud API 2 **前端 PaaS 组件**相关：`DJILivePlayer`、多路直播大屏、`paas.js`、Cesium 地图渲染、组件接入 | `frontend-component` | `DJILivePlayer` 是本体系的直播组件，**不是** DJI 消费级 App |
| 目标坐标 / 云台角度 / 画面像素 → 经纬度反算、SEI 位置解算 | `sei-osd` | 属于 SEI/OSD 数据解析与地理换算场景 |
| **MCP Server 的接入与配置**：在 Claude Code / Cursor / 任意 MCP 客户端里配置 MCP Server、部署 MCP | `mcp-deploy` | "配置 MCP 连接"是部署职责，与"第三方 IDE 无关"无关。**但不含运行中的 MCP Server 自身运维**：`fh_ops(action=status/set_log_level/shutdown)` 是已连接会话里的运行时工具，`shutdown` 真实会终止 MCP Server 进程（L3 + 人工确认），不要因为出现"关服务/停服务"字样就推给 `mcp-deploy`，判据见「场景 3b」 |
| 用 `fh_*` 工具操控设备 / 选 MCP 工具 / 编排调用链 | `mcp-guide` | |
| **写 Java 代码**管理禁飞区 / 限飞区 / 禁降区 / 离线地图 / Flysafe 解锁证书 / 地图元素（点线面）（`SafetyServiceModule` / `ElementModule`），或问飞行区/离线地图下发后**没在设备侧生效**的原因 | `cloudapi2-map` | 判据是输出物类型：产出 Java 代码 → `cloudapi2-map`；只是运行时调 `fh_flight_area_manage`/`fh_offline_map`/`fh_unlock_license` 等 MCP 工具 → `mcp-guide`，判据见「场景 4」|
| **写 Java 代码**控制 PSDK 第三方负载（探照灯 / 喊话器 / Widget / 浮窗 / PSDK UI 资源）（`PsdkControlModule` / `PsdkResourceModule`），或管理喊话器音频资源（`getSTS` 直传、资源 CRUD） | `cloudapi2-payload` | 判据同上：产出 Java 代码 → `cloudapi2-payload`；只是运行时调 `fh_speaker`/`fh_searchlight`/`fh_psdk_widget` → `mcp-guide`，判据见「场景 5」。注意 PSDK 负载索引是 `int psdkIndex`，与相机负载索引 `String`（`"99-0-0"`）不同，相机/云台控制仍归 `sdk-core` |
| 三维重建 / 全景图 / Terra 建模（结合无人机/机场场景） | `mcp-guide` | `TerraBuildModule` 属已知缺口，仅 MCP 侧（`fh_terra`）覆盖，`cloudapi2-map` 的 SKILL.md 中显式声明不覆盖 |
| **写 Java 代码**管理 AI Box 应用 / 机载 AI 识别与跟踪 / AI 主动告警 / AI Inside 机载算法生命周期 / AI 目标检测告警配置与查询（`AIBoxModule` / `OnboardAiModule` / `AIInsideManageModule` / `AiAlertModule`） | `cloudapi2-ai` | 判据同上：产出 Java 代码 → `cloudapi2-ai`；只是运行时调 `fh_*` AI 相关 MCP 工具 → `mcp-guide`，判据见「场景 6」。注意本域目标检测告警（`AiAlertModule`）与设备健康告警（`HmsAlertEvent`，归 `device-management`）是两套不同的告警体系，不要混淆 |
| **写 Java 代码**查询信号地图：4G / SDR 信号覆盖网格、H3 网格、信号强度分布（`SafetyServiceModule.searchSignalMaps`） | `cloudapi2-map` | 虽含"信号/覆盖"字样，**不是** SEI/OSD 遥测——它是一次性 HTTP 查询返回地图覆盖数据，不涉及帧级对齐/视锥/频率配置，因此不归 `sei-osd`。判据同禁飞区/离线地图那一行（看输出物）：产出 Java 代码 → `cloudapi2-map`；运行时调 `fh_offline_map(action=search_signal_maps)` → `mcp-guide`，判据见「场景 4」 |
| **写 Java 代码**调用设备运维类模块：日志管理 / 维修工单 / 设备自诊断 / RTK 标定 / 网络测试 / 设备调试（`LogManagementModule` / `MaintenanceManagementModule` / `DiagnosisManagementModule` / `RtkCalibrationModule` / `NetworkTestModule` / `DeviceDebugModule`），或问固件自定义升级包流程（`FirmwareManageModule`）/ 机场空调·补光灯·充电·舱盖控制（`DockControlModule`） | `device-management` | 判据同 cloudapi2-map/payload/ai：产出 Java 代码 → `device-management`（见 `references/ops-reporting.md` / `references/device-debug.md` / `references/firmware-management.md` / `references/device-lifecycle.md`）；只是运行时调 `fh_device_debug`/`fh_firmware`/`fh_diagnosis`/`fh_dock_control` 等 MCP 工具 → `mcp-guide`，判据见下一行 |
| 格式化设备存储 / 重启机场或无人机 / RTK 校准 / 中继配置 / 固件升级发布（结合无人机/机场/MCP 工具场景） | `mcp-guide` | "格式化"/"重启"/"固件升级"单独看像通用 IT 运维词汇（U 盘格式化、服务器重启、路由器升级），但指向无人机/机场设备且涉及 `fh_device_debug`/`fh_firmware` 时属于 `mcp-guide` 职责；不涉及无人机/机场的通用 IT 运维诉求仍按零、全局排除规则返回 `none` |
| 设备自诊断 / 诊断报告（结合无人机/机场设备，调用 `fh_diagnosis`） | `mcp-guide` | "诊断"单独看像通用故障排查词汇，容易与 `troubleshooting` 的"报错/排障"信号混淆；但当诉求是**运行时对某台无人机/机场设备发起自检任务或查自检报告**（而非"我的调用/代码报错了"，也不是"写 Java 代码调用 `DiagnosisManagementModule`"）时属于 `mcp-guide` 职责。若用户是在描述**MCP 工具调用本身报错**（如"fh_diagnosis 调用失败"/错误码），仍按横切层规则优先交给 `troubleshooting` 定位；若用户要**写 Java 代码**集成设备自诊断能力，归 `device-management`（见上一行） |

**判定口诀**：先判断「这是不是 DJI Cloud API 2 的**开发 / 部署 / 排障**场景」，再套触发词。触发词命中 ≠ 应该路由。
**排除口径从严**：只有当诉求**完全落在 DJI Cloud API 2 体系之外**（连 DJI / 无人机 / 云端 API 都不沾）时才返回 `none`；只要沾到 DJI Cloud API 2 的任一技术面（Java SDK、PaaS 前端组件、MCP 工具、私有化部署、遥测数据），就**不要**返回 `none`。

**排障类特别说明**：只有 **DJI Cloud API 2 SDK 集成代码**或**私有化底座**的报错才归 `troubleshooting`。DJI 消费级 App、司空2 SaaS 网页端、其他语言 SDK 的报错，一律返回 `none`（可另行提示用户本 Skill 不覆盖该场景）。

---

## 一、Skill 层级与优先级

```
┌──────────────────────────────────────────────────────────────┐
│  横切层（最高优先级，匹配即拦截）                               │
│  feedback — 用户说"反馈/提 issue/报 bug"时优先                  │
│  troubleshooting — 用户报错/问错误码时优先                       │
│  knowledge-ingest — 用户说"入库/加到知识库"时优先               │
├──────────────────────────────────────────────────────────────┤
│  水平基础层（按场景仲裁）                                      │
│  sdk-core / frontend-component / mcp-guide                    │
├──────────────────────────────────────────────────────────────┤
│  垂直场景层（业务细分）                                        │
│  sdk-core 细化：device-management / sei-osd / cloudapi2-map /   │
│                cloudapi2-payload / cloudapi2-ai                │
│  工程交付：skeleton-deploy / mcp-deploy                        │
└──────────────────────────────────────────────────────────────┘
```

### 仲裁规则

1. **横切层优先**：用户消息包含反馈/提 issue 信号时，`feedback` 先介入；包含报错/错误码/排障信号时，`troubleshooting` 先介入，定位问题归属后再路由到具体 Skill。**例外**：地图域的"改完没生效/下发后设备没变"属于同步语义问题而非故障，归 `cloudapi2-map`（见场景 4 末段）
2. **水平层互斥**：sdk-core（写 Java 代码）、frontend-component（写前端代码）、mcp-guide（选 MCP 工具）三者按输出物类型互斥，不会同时执行
3. **垂直层从属**：device-management、sei-osd、cloudapi2-map、cloudapi2-payload、cloudapi2-ai 都是 sdk-core 的场景细化，匹配时**替代** sdk-core 执行（更精准的约束和参考文档）；cloudapi2-map 覆盖 `SafetyServiceModule`/`ElementModule`（飞行区、离线地图、解锁证书、地图元素），cloudapi2-payload 覆盖 `PsdkControlModule`/`PsdkResourceModule`（PSDK 探照灯、喊话器、Widget、音频资源），cloudapi2-ai 覆盖 `AIBoxModule`/`OnboardAiModule`/`AIInsideManageModule`/`AiAlertModule`（AI Box、机载 AI 识别/跟踪/告警、AI Inside 算法生命周期、AI 目标检测告警）。**例外**：写 Java 代码控制相机/云台时归 sdk-core（`CameraControlModule`，索引是 `String "99-0-0"`），不要因"负载"二字误路由到 cloudapi2-payload；但**运行时抢占控制权**（`fh_payload_acquire`/`fh_payload_release`/`fh_control_acquire`）归 mcp-guide，见「场景 3」
4. **层间可组合**：垂直层可与前端层组合（如 sei-osd + frontend-component = 后端 SEI 获取 + 前端 Cesium 渲染）

---

## 二、路由决策表

| 用户意图信号 | 主 Skill | 辅助 Skill（按需加载 reference） |
|---|---|---|
| 直接写**通用 SDK API 调用代码**：点名具体方法 / 模块（`listenAircraftLocation`、`getDroneOsdModule`、`returnHome`、`getDroneInstance`…）问「怎么调 / 回调怎么写 / 监听与取消怎么写」 | **sdk-core** | — |
| 写 **skeleton feature 模块** / `@EventListener` 消费 SDK 事件（`OsdUpdateEvent` / `DeviceOnlineEvent` / `HmsAlertEvent`）/ `DeviceStatusService` / 固件升级 / 设备上下线 | device-management | sdk-core（核心规则兜底）**注意**：这里的"设备"指的是**机场/无人机整机上下线**（`DeviceManagementModule`）。若订阅对象是 **PSDK 第三方负载**（`PsdkDeviceInfo`、`psdkIndex`、`listenDrcPsdkDeviceList`、`PsdkStateSummaryInfo`），归 cloudapi2-payload |
| **设备运维域**：写 Java 代码管理机场空调/补光灯/充电/舱盖控制、固件自定义升级包、设备调试（RTK 标定/网络测试/日志管理/维修工单/自诊断）；点名 `DockControlModule` / `FirmwareManageModule` / `DeviceDebugModule` / `RtkCalibrationModule` / `NetworkTestModule` / `LogManagementModule` / `MaintenanceManagementModule` / `DiagnosisManagementModule` | device-management | sdk-core（核心规则兜底）；运行时直接调 `fh_device_debug`/`fh_firmware`/`fh_diagnosis`/`fh_dock_control` 等 MCP 工具 → mcp-guide |
| **SEI 专有**：帧级位置、视频对齐、视锥 / FOV、像素反算、`subscribeArSeiInfo` / `subscribeFovSeiInfo` / `LiveModule` / `ArSeiInfo`；或**数据通道选型**（SEI vs OSD、`osd_frequency`、MQTT/DRC 通道怎么选） | sei-osd | sdk-core（核心规则兜底） |
| **地图域**：写 Java 代码管理自定义飞行区（限飞区/作业区/禁降区）、地形障碍物离线地图、Flysafe 解锁证书、信号地图、地图元素（点/线/面/分组）；点名 `SafetyServiceModule` / `ElementModule`；或问**飞行区/离线地图改完为什么没在设备侧生效**（同步语义，非故障） | cloudapi2-map | sdk-core（核心规则兜底） |
| **PSDK 负载域**：写 Java 代码控制探照灯 / 喊话器 / PSDK Widget / 浮窗文本 / PSDK UI 资源；管理喊话器音频资源（`getSTS` 直传、CRUD）；点名 `PsdkControlModule` / `PsdkResourceModule` / `psdkIndex`；或问**负载指令下发无反应 / listen 返回 -1 / cancel 编译不过** | cloudapi2-payload | sdk-core（核心规则兜底） |
| **AI 域**：写 Java 代码管理 AI Box 应用 / 机载 AI 识别开关与跟踪 / AI 主动告警 / AI Inside 机载算法装载卸载 / AI 目标检测告警配置与查询；点名 `AIBoxModule` / `OnboardAiModule` / `AIInsideManageModule` / `AiAlertModule`；或问 `getAIBoxModule()` 判空无日志 / 大写方法名编译不过等 | cloudapi2-ai | sdk-core（核心规则兜底） |
| 接入前端 PaaS 组件（Vue/HTML） | frontend-component | — |
| 选 MCP 工具 / 编排调用链 | mcp-guide | — |
| 报错 / 错误码 / 排障 | troubleshooting | 定位后路由到对应 Skill |
| **用户要反馈问题 / 提 issue**（"我要反馈一个问题"/"报个 bug"/"提个 issue"） | feedback | 走 `diag.sh session start` 闭环；**不要当成需求开发转交垂直层**；发现有错误码时转交 troubleshooting |
| **设备无法直播** / 直播黑屏 / 设备离线 `212015` / EA340 / EA610 | troubleshooting | 走 `references/live-stream-device-troubleshooting.md`（固件 → 上云方式 → 底座 `.env` → 端口，通常非应用代码问题） |
| **组合场景**：后端 + 前端 | sei-osd 或 device-management | frontend-component |
| 部署 MCP Server / Docker 部署 MCP / MCP 接入 | mcp-deploy | — （安装、构建镜像、写客户端配置）。运行中 MCP Server 自身的状态查询/日志级别/关停 → mcp-guide，判据见「场景 3b」 |
| 部署 Skeleton / skeleton 部署 | skeleton-deploy | — |
| 入库 / 加到知识库 / ingest | knowledge-ingest | — |

---

## 三、多 Skill 组合模式

### 模式 A：单一 Skill（最常见）

用户意图明确落入一个 Skill 范围，直接执行该 Skill 的 SOP。

### 模式 B：主从编排

用户需求跨多个 Skill 时，选定一个**主 Skill** 负责编排整体流程，其他 Skill 作为**辅助**按需提供 reference 和约束。

**主 Skill 选择规则**：
- 谁的输出物是最终交付物，谁是主 Skill
- 如果输出物是"完整系统"（后端 + 前端），以**后端 Skill 为主**，前端 Skill 为辅

**编排示例**：

```
用户："做一个完整的设备监控系统，后端 OSD + 前端 Cesium"

主 Skill：sei-osd（后端 OSD/SEI 数据获取）
辅助 Skill：frontend-component（前端 Cesium 渲染）

执行流程：
1. sei-osd Ch1-Ch4：理解需求 → 数据选型 → 分解后端任务
2. sei-osd Ch5：后端代码（WebSocket 推送 OSD/SEI JSON）
3. 切换到 frontend-component：前端 Cesium 页面
4. sei-osd Ch6：端到端验证（后端 → WebSocket → 前端）
```

### 模式 C：排障后路由

`troubleshooting` 定位问题根因后，将修复工作转交给对应 Skill。

```
用户："前端报了 6002 错误"

1. troubleshooting Stage 1-2：Reproduce + Locate → 确认是 paas.js 未加载
2. 转交 frontend-component：按 paas-js-load-order 约束修复
3. troubleshooting Stage 4：验证修复
```

---

## 四、转交协议

当 Skill A 发现当前工作属于 Skill B 的职责时，使用标准转交话术：

### 转交话术模板

```
这部分涉及 [目标领域]，需要参考 [目标 Skill] 的约束和参考文档。

切换到 [目标 Skill] 处理：
- 关键约束：[列出 1-2 个 critical 约束]
- 参考文档：[具体 reference 路径]
```

### 转交信号表

| 当前 Skill | 触发转交的信号 | 转交目标 |
|---|---|---|
| sdk-core | 用户提到 skeleton feature 模块 / @EventListener / OsdUpdateEvent | device-management |
| sdk-core | 用户提到 SEI / subscribeArSeiInfo / LiveModule / DRC OSD | sei-osd |
| sdk-core | 用户提到 SafetyServiceModule / ElementModule / 飞行区 / 离线地图 / 解锁证书 / 地图元素 | cloudapi2-map |
| sdk-core | 用户提到 PsdkControlModule / PsdkResourceModule / 探照灯 / 喊话器 / PSDK Widget / 浮窗 / psdkIndex / STS 直传 | cloudapi2-payload |
| sdk-core | 用户提到 AIBoxModule / OnboardAiModule / AIInsideManageModule / AiAlertModule / AI Box / 机载 AI 识别 / AI 跟踪 / AI Inside / AI 目标检测告警 | cloudapi2-ai |
| sdk-core | 用户问 MCP 工具选择 / fh_* 调用 | mcp-guide |
| mcp-guide | 用户要写 Java 代码而非调 MCP 工具 | sdk-core 或垂直层 Skill |
| mcp-guide | 用户要写 Java 代码管理飞行区/离线地图/解锁证书/地图元素而非调 `fh_flight_area_manage`/`fh_offline_map` 等 MCP 工具 | cloudapi2-map |
| mcp-guide | 用户要写 Java 代码控制探照灯/喊话器/Widget 而非调 `fh_searchlight`/`fh_speaker`/`fh_psdk_widget` | cloudapi2-payload |
| mcp-guide | 用户要写 Java 代码管理 AI Box/机载 AI/AI Inside/AI 目标检测告警而非调 AI 相关 `fh_*` MCP 工具 | cloudapi2-ai |
| mcp-guide | 用户要写 Java 代码管理机场空调/补光灯/固件升级包/RTK 标定/网络测试/设备自诊断而非调 `fh_device_debug`/`fh_firmware`/`fh_diagnosis`/`fh_dock_control` 等 MCP 工具 | device-management |
| device-management | 用户订/查的是 **PSDK 第三方负载**（`psdkIndex`、`PsdkDeviceInfo`、`listenDrcPsdkDeviceList`、`PsdkStateSummaryInfo`）而非机场/无人机整机上下线 | cloudapi2-payload |
| device-management | 用户问的是 **AI 目标检测告警**（`AiAlertModule`、人/车/船识别告警）而非 **HMS 设备健康告警**（`HmsAlertEvent`） | cloudapi2-ai |
| cloudapi2-payload | 用户要**运行时直接调用** MCP 工具（`fh_searchlight`/`fh_speaker`/`fh_psdk_widget`/`fh_camera`）而非写代码 | mcp-guide |
| cloudapi2-payload | 用户要**写 Java 代码**控制**相机/云台**（`CameraControlModule`，索引 `String "99-0-0"`）而非 PSDK 负载 | sdk-core |
| cloudapi2-payload | 用户要**运行时抢占/释放相机/云台控制权**（`fh_payload_acquire`/`fh_payload_release`）而非写代码 | mcp-guide（见「场景 3」的控制权判据） |
| cloudapi2-ai | 用户要**运行时直接调用** MCP 工具（如 `fh_*` AI 相关工具）而非写代码 | mcp-guide |
| sei-osd | 用户问 **4G / SDR 信号覆盖网格 / 信号地图 / H3 网格**（`SafetyServiceModule` 的信号地图接口） | cloudapi2-map（一次性 HTTP 查询，非 SEI/OSD 遥测） |
| cloudapi2-map | 用户要**运行时直接调用** MCP 工具（`fh_flight_area_manage`/`fh_offline_map`/`fh_unlock_license`）而非写代码 | mcp-guide |
| device-management | 用户问前端页面 / Vue 组件 / Cesium 可视化 | frontend-component |
| sei-osd | 用户问前端 Cesium 渲染 / FOV 可视化 | frontend-component（辅助） |
| frontend-component | 用户问后端 SDK 调用 / Java 代码 | sdk-core 或垂直层 Skill |
| 任意 Skill | 用户报错 / 错误码 / "不工作" / "白屏" | troubleshooting（先介入） |
| 任意 Skill | 用户说"我要反馈一个问题 / 报个 bug / 提个 issue"（**无错误码也算**） | feedback → `diag.sh session start` 闭环（**不要当成功能需求转交垂直层**；发现有错误码时由 feedback 转交 troubleshooting） |
| 任意 Skill | 用户说"直播打不开 / 直播黑屏 / 设备离线 `212015` / EA340 / EA610" | troubleshooting → `references/live-stream-device-troubleshooting.md`（**不要转给写代码的 Skill**：此类根因通常在固件/上云方式/底座 `.env`/端口，改应用代码无效） |
| troubleshooting | 用户问飞行区/离线地图**改完为什么没在设备侧生效**（不是调用报错） | cloudapi2-map（同步语义是两段式设计，非故障） |
| 任意 Skill | 用户说"部署 MCP"/"安装 MCP"/"MCP deploy" | mcp-deploy |
| 任意 Skill | 用户要**运行中 MCP Server 自身**的状态/日志级别/关停（`fh_ops`），而非安装部署 | mcp-guide（见「场景 3b」） |
| 任意 Skill | 用户说"部署 skeleton"/"安装 skeleton" | skeleton-deploy |
| 任意 Skill | 用户说"加到知识库"/"入库"/"收录" | knowledge-ingest |

---

## 五、冲突仲裁

### 场景 1：sdk-core vs 垂直层 Skill

当用户意图同时匹配 sdk-core 和 device-management / sei-osd 时：

- **优先垂直层**：垂直层有更精准的约束、参考文档和代码模板
- **sdk-core 兜底**：垂直层 `extends` 共享约束（含 sdk-core 6 条核心规则），无需回退到 sdk-core

**sdk-core vs sei-osd 的切分口径**（高频歧义，务必按下表判，不要见到 `OSD` 二字就归 sei-osd）：

| 用户诉求 | 归属 | 判据 |
|---|---|---|
| 点名具体 SDK 方法 / 模块，问「**怎么调用**」（`listenAircraftLocation`、`getDroneOsdModule`、`listenAircraftBatteryInfo`） | **sdk-core** | 诉求是**写调用代码**，用的是通用 listen/cancel + 回调规则 |
| 「设备 **OSD 数据频率**太低怎么调」"OSD 数据怎么拿" | **sei-osd** | 频率是数据通道参数，sei-osd 的三层对比表与 `osd_frequency` 配置是权威；不要因"没点名 SEI"就回落 sdk-core |
| SEI（帧级位置、视频对齐、视锥/FOV、像素反算）| **sei-osd** | SEI 是 sei-osd 独有领域 |
| **通道选型 / 频率配置**（SEI vs OSD vs MQTT/DRC 怎么选、`osd_frequency` 设多少）| **sei-osd** | 属于数据通道决策，sei-osd 的三层对比表是权威 |

> 口诀：**问「某个 SDK 方法怎么写」→ sdk-core；问「该用哪种数据 / 通道、频率设多少、怎么对齐画面」→ sei-osd。**
> 只有当用户诉求**超出通用 SDK 调用规则**（需要 SEI/OSD 三层选型、帧对齐、视锥等专有知识）时，才从 sdk-core 转交 sei-osd。

### 场景 1b：device-management vs sdk-core

- 用户要**在 skeleton 项目里加 feature 模块 / 用 `@EventListener` 消费 KernelEvent** → device-management
- 用户只是**写一段 SDK 调用代码**（不在 skeleton 上下文）→ sdk-core

### 场景 2：前端 vs 后端

用户描述中同时包含前端和后端诉求：

- **不要合并为一个 Skill 回答**：分段处理，先后端后前端（或反之），明确标注切换点
- **数据契约先行**：先定义后端推送给前端的数据格式（WebSocket JSON schema），再分别实现两端

### 场景 3：mcp-guide vs sdk-core

用户说"帮我控制无人机"——可能是"用 MCP 工具直接操作"也可能是"写 Java 代码"：

- 如果用户**在 MCP 连接的会话中**（有 fh_* 工具可用）→ mcp-guide
- 如果用户**在开发 IDE 中写代码** → sdk-core 或垂直层 Skill
- 如果不确定 → 问："你是想直接操控设备（通过 MCP 工具），还是写 Java 代码集成到你的项目中？"

**控制权诉求的判据**：「抢占/释放控制权」是**运行时操作**，不是写代码——即使提到"云台/负载/相机"，只要用户没要求写 Java 代码、没点名 `CameraControlModule` 等模块，就归 `mcp-guide`：

| 用户诉求 | 归属 | 对应工具 |
|---|---|---|
| 抢占 / 释放**云台或负载**控制权 | mcp-guide | `fh_payload_acquire` / `fh_payload_release` |
| 抢占 / 释放**飞行控制权** | mcp-guide | `fh_control_acquire` |
| 写 Java 代码实现控制权申请（点名 `CameraControlModule` / 控制权相关 API） | sdk-core | — |

> 注意：控制权属于**会话级独占状态**，与"怎么调 SDK 方法"是两类问题；`CameraControlModule` 归 sdk-core 的前提是用户要**写代码**，不能反过来用它把运行时操作拉回 sdk-core。

### 场景 3b：mcp-guide vs mcp-deploy（MCP Server 自身运维）

"MCP Server" 这个名词两侧都会出现，切分口径是**进程装没装起来**：

- **mcp-deploy**：还没跑起来的东西——构建镜像、写 `.env`、`docker compose up`、在 Claude Code / Cursor 里填 MCP 客户端配置、部署排障。输出物是**部署配置/命令**。
- **mcp-guide**：已经连着 MCP Server，用户在会话里要操作**运行中的 Server 自身**——`fh_ops(action=status|set_log_level|shutdown)`。输出物是**一次 `fh_*` 工具调用**。

| 用户诉求 | 归属 | 对应工具 |
|---|---|---|
| 查 MCP Server 健康状态 / 版本 / 运行时长 | mcp-guide | `fh_ops(action=status)` |
| 改 MCP Server 日志级别 | mcp-guide | `fh_ops(action=set_log_level, level=...)` |
| **关闭 / 停掉 MCP Server** | mcp-guide | `fh_ops(action=shutdown)`（L3 + 人工确认，会终止整个进程） |
| 把 MCP Server 部署到服务器 / 安装 / 生成客户端配置 | mcp-deploy | — |

> 常见误判：把"关闭 MCP Server"当成进程生命周期管理推给 `mcp-deploy`。**运行中**的 Server 关停是本体系内一等公民工具（`fh_ops`），由 mcp-guide 的 `references/workflows/ops-maintenance.md` 覆盖且有明确的风险说明；只有"卸载/下线/重新部署"这类**改变部署形态**的诉求才归 mcp-deploy。

### 场景 4：mcp-guide vs cloudapi2-map

飞行区/离线地图/解锁证书/地图元素/信号地图这几个名词在两个 Skill 的触发词里都会出现，仲裁方式与场景 3 相同——**看输出物，不看名词**：

- 用户要**运行时调用** `fh_flight_area_manage` / `fh_offline_map` / `fh_unlock_license` / `fh_element_*` 等 MCP 工具（已连接 MCP Server 的会话）→ mcp-guide
- 用户要**写 Java 代码**调用 `workspace.getSafetyServiceModule()` / `workspace.getElementModule()` → cloudapi2-map
- **信号地图**同理：Java 侧是 `SafetyServiceModule.searchSignalMaps(...)`，MCP 侧是 `fh_offline_map(action=search_signal_maps, device_sns=[...])`。问"4G/SDR 覆盖网格怎么查"本身**不带**写代码或运行时任一侧的明确指向，按"看输出物"判据：明确要写代码/点名 `SafetyServiceModule` → `cloudapi2-map`；明确在 MCP 会话里要用工具查 → `mcp-guide`；只问"怎么查"且没有任何一侧线索时，**默认归 `cloudapi2-map`**（信号地图本身的语义归属），不要因为"没提写代码"就推给 `mcp-guide`
- 如果不确定 → 问："你是想直接通过 MCP 工具操作（如创建禁飞区），还是写 Java 代码集成到你的后端项目中？"
- `TerraBuildModule`（三维重建/全景图/Terra 建模）**只有** mcp-guide 覆盖，cloudapi2-map 不涉及，不要因为"地图"二字误路由

**地图域"没生效" ≠ troubleshooting**：飞行区/离线地图改完后必须 `sync` 并轮询 `*SyncStatus` 才在设备侧生效，这是**设计上的两段式语义**，不是故障。此类提问（"sync 之后设备怎么没生效"/"调了 sync 还是老的飞行区"/"离线地图下发后机场看不到"）即使带"没生效""不工作"字样，也应路由到 `cloudapi2-map` 解释同步语义；只有**调用本身失败**（抛异常、返回错误码、回调 error 非 SUCCESS）才是 `troubleshooting`。判据是：**问"为什么没生效"→ map；问"调用报错了"→ troubleshooting**。

### 场景 5：mcp-guide vs cloudapi2-payload

探照灯/喊话器/Widget 这几个名词在两个 Skill 的触发词里都会出现，仲裁方式与场景 3、4 相同——**看输出物，不看名词**：

- 用户要**运行时调用** `fh_searchlight` / `fh_speaker` / `fh_psdk_widget` 等 MCP 工具（已连接 MCP Server 的会话）→ mcp-guide
- 用户要**写 Java 代码**调用 `drone.getPsdkControlModule()` / `workspace.getPsdkResourceModule()` → cloudapi2-payload
- 如果不确定 → 问："你是想直接通过 MCP 工具操作（如打开探照灯），还是写 Java 代码集成到你的后端项目中？"

**cloudapi2-payload vs sdk-core 的切分口径**：

| 用户诉求 | 归属 | 判据 |
|---|---|---|
| 点名 `PsdkControlModule` / `PsdkResourceModule`，或问 DRC 建链后才能控制负载 | **cloudapi2-payload** | PSDK 负载专属模型（`int psdkIndex`、DRC / DRC Northbound / WebSocket 三链路） |
| 实时喊话 / 抛投器 / 枚举值对应的控制接口是否存在 | **cloudapi2-payload** | 属于能力边界问题，payload 的 SKILL.md 有权威结论 |
| 写 Java 代码做相机 / 云台控制（`CameraControlModule`、`String` 相机负载索引） | **sdk-core** | 与 PSDK 负载是两套模型，索引类型和模块都不同 |
| 运行时抢占 / 释放控制权（云台负载或飞控） | **mcp-guide** | 走 `fh_payload_acquire`/`fh_payload_release`/`fh_control_acquire`，不是「写代码」诉求 |
| 通用 listen/cancel 配对、初始化、鉴权 | **sdk-core**（payload 已 `extends` 共享约束） | 不是 PSDK 专有知识 |

---

### 场景 6：mcp-guide vs cloudapi2-ai

AI Box / 机载 AI / AI Inside / AI 目标检测告警这几类名词在两个 Skill 的触发词里都会出现，仲裁方式与场景 3、4、5 相同——**看输出物，不看名词**：

- 用户要**运行时调用** AI 相关 `fh_*` MCP 工具（已连接 MCP Server 的会话）→ mcp-guide
- 用户要**写 Java 代码**调用 `drone.getAIBoxModule()` / `drone.getOnboardAiModule()` / `workspace.getAIInsideManageModule()` / `workspace.getAIAlertModule()` → cloudapi2-ai
- 如果不确定 → 问："你是想直接通过 MCP 工具操作（如查询 AI Box 信息），还是写 Java 代码集成到你的后端项目中？"

**cloudapi2-ai 与相邻 Skill 的切分口径**：

| 用户诉求 | 归属 | 判据 |
|---|---|---|
| 点名 `AIBoxModule` / `OnboardAiModule` / `AIInsideManageModule` / `AiAlertModule`，或问大写方法名 / `generated` 包参数类型 / getter 判空行为 | **cloudapi2-ai** | AI 域专属命名与参数体系不规则，权威结论在本 Skill 的 reference 中 |
| PSDK 第三方负载控制（探照灯/喊话器/Widget，`PsdkControlModule`/`PsdkResourceModule`） | **cloudapi2-payload** | AI 域不涉及 PSDK 负载控制，两者模块完全独立 |
| 机场/无人机整机上下线、HMS 设备健康告警（`DeviceManagementModule`、`HmsAlertEvent`） | **device-management** | 与 `AiAlertModule` 的目标检测告警是两套不同的告警体系，不要混淆 |
| SEI/OSD 遥测、帧级位置解算 | **sei-osd** | AI 识别结果与 SEI 帧级位置是不同数据面，AI 域不做坐标换算 |
| **AI 域代码本身报错 / 编译不过 / 回调拿不到数据** | **troubleshooting 先介入**，定位后转回 cloudapi2-ai | 与其它垂直层一致：先排障定位，再回到业务 Skill 修复 |
| 通用 listen/cancel 配对、初始化、鉴权 | **sdk-core**（ai 已 `extends` 共享约束） | 不是 AI 域专有知识 |

---

## 六、各 Skill 引用本文件的方式

在 SKILL.md 的"不触发"或"决策树"段后加入：

```markdown
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。
> 当发现用户意图属于其他 Skill 职责时，按转交协议切换。
```
