# DJI Cloud API 2 SDK AI Skill 使用说明

## 一、概述

DJI Cloud API 2 SDK AI Skill 是面向集成商开发者的**智能开发辅助工具集**，为集成 DJI Cloud API 2 SDK 提供一站式的 AI 辅助能力。

### 核心能力

| 能力 | 说明 | 对应 Skill | 示例 |
|------|------|-----------|------|
| 代码生成 | 自动生成符合 SDK 规范的 Java 集成代码 | sdk-core、device-management、sei-osd、map、payload、ai | "帮我写一个返航的 Service 方法" |
| 工具路由 | 帮 AI 选择正确的 MCP Tool 并编排多步调用 | mcp-guide | "让无人机飞到这个坐标" |
| 前端接入 | 指导 PaaS 前端组件的集成 | frontend-component | "接入航线编辑器组件" |
| 部署引导 | 半自动引导部署与配置 | mcp-deploy、skeleton-deploy | "帮我把 MCP Server 部署到服务器" |
| 知识管理 | 将外部文档收录到知识库并维护索引 | knowledge-ingest | "把这个文档加到知识库" |
| 排障诊断 | 基于 4 阶段 RCA 方法论快速定位问题 | troubleshooting | "SDK 返回 error_code: 4001" |
| 问题反馈 | 采集→脱敏→确认→自动创建 issue 的反馈闭环 | feedback | "我要反馈一个问题" |

> **Skill 教 AI "怎么做对"，MCP Server 让 AI "直接操作"——两者互补，共同构成 DJI Cloud API 2 AI 开发体验。**

### Skill 全景

当前共 **13 个安装可用 `cloudapi2-*` 目录**（`cloudapi2-diagnosis` 的能力已于 v2.0.0 并入 `cloudapi2-troubleshooting`，目录已删除；`cloudapi2-wayline-init` 目录保留但暂不对外发布，不计入可安装数），分三层组织：

```
┌──────────────────────────────────────────────────────────────────────┐
│  横切层（匹配即优先介入）                                              │
│  troubleshooting · knowledge-ingest · feedback                        │
├──────────────────────────────────────────────────────────────────────┤
│  水平基础层（按输出物类型互斥）                                         │
│  sdk-core · frontend-component · mcp-guide                            │
├──────────────────────────────────────────────────────────────────────┤
│  垂直场景层（业务细分，引用水平层规则）                                  │
│  device-management · sei-osd · map · payload · ai                      │
│  skeleton-deploy · mcp-deploy                                         │
└──────────────────────────────────────────────────────────────────────┘
```

> `cloudapi2-diagnosis` 的能力已全部并入 `cloudapi2-troubleshooting`，原目录已删除，新实现请走 `cloudapi2-troubleshooting`。

---

## 二、快速开始

### 前置条件

- 已安装 [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI（版本 >= 1.0）
- 或已配置 Cursor / GitHub Copilot / Windsurf 等支持 Skill 的 AI IDE

### 安装方式

#### 方式 1：Plugin 一键安装（推荐）

```bash
# 第 1 步：添加 Marketplace（仅首次）
claude plugin marketplace add https://github.com/dji-sdk/Cloud-API-2.git

# 第 2 步：安装 Plugin（包含全部 13 个安装可用 Skill）
claude plugin install cloudapi2@dji
```

安装完成后新开会话即生效。marketplace 名为 `dji`、plugin 名为 `cloudapi2`（以仓库内 `.claude-plugin/` 为准）。

#### 方式 2：手动配置 settings.json

编辑 `~/.claude/settings.json`：

```jsonc
{
  "enabledPlugins": {
    "cloudapi2@dji": true
  },
  "extraKnownMarketplaces": {
    "dji": {
      "source": {
        "source": "directory",
        "path": "/path/to/Cloud-API-2"  // 替换为实际仓库路径
      }
    }
  }
}
```

#### 方式 3：手动拷贝到 IDE Skill 目录

```bash
# 克隆仓库（如尚未克隆）
git clone https://github.com/dji-sdk/Cloud-API-2.git
cd Cloud-API-2/devkit

# Claude Code — 全部安装
for d in skills/cloudapi2-*/; do cp -r "$d" ~/.claude/skills/; done

# Cursor
for d in skills/cloudapi2-*/; do cp -r "$d" .cursor/skills/; done

# GitHub Copilot (VS Code)
for d in skills/cloudapi2-*/; do cp -r "$d" .github/skills/; done
```

#### 方式 4：保持在项目内（零配置）

如果日常开发在本仓库内工作，Claude Code 会自动发现 `skills/` 下的 Skill，无需安装。

### 验证安装

新开 AI IDE 会话，输入以下测试语句：

| 测试输入 | 期望触发的 Skill | 验证标志 |
|---------|---------|---------|
| "帮我初始化 DJI Cloud API 2 SDK" | `cloudapi2-sdk-core` | 提到 initialize 返回值检查 |
| "让无人机飞到这个坐标" | `cloudapi2-mcp-guide` | 提到 fh_flyto / fh_control_acquire |
| "接入航线编辑器组件" | `cloudapi2-frontend-component` | 提到 WaylineEditor / paas.js |
| "添加 OSD 订阅功能" | `cloudapi2-device-management` | 提到 @EventListener / OsdUpdateEvent |
| "怎么获取帧级位置数据" | `cloudapi2-sei-osd` | 提到 SEI / subscribeArSeiInfo |
| "写代码创建一个圆形限飞区" | `cloudapi2-map` | 提到 `SafetyServiceModule` / `createCustomizedFlightAreas` |
| "写代码控制 PSDK 探照灯" | `cloudapi2-payload` | 提到 `PsdkControlModule` / `connectDrcLink` |
| "写代码开启机载 AI 识别并跟踪目标" | `cloudapi2-ai` | 提到 `OnboardAiModule` / `AIIdentifySet` |
| "帮我把 MCP Server 部署到服务器" | `cloudapi2-mcp-deploy` | 提到环境探测 / SSE 部署 |
| "帮我部署 skeleton 到这台机器" | `cloudapi2-skeleton-deploy` | 提到三形态选型 / 环境探测 |
| "把这个文档加到知识库" | `cloudapi2-knowledge-ingest` | 提到模块归属 / 入库确认 |
| "SDK 返回 error_code: 4001" | `cloudapi2-troubleshooting` | 提到 4 阶段 RCA |
| "我要反馈一个问题" | `cloudapi2-feedback` | 提到采集 / 脱敏 / 确认 / issue 创建 |

如果 AI 的回复中包含上述标志性内容，说明 Skill 已成功加载。

---

## 三、Skill 能力详解

### 3.1 SDK 代码生成（cloudapi2-sdk-core）

**定位**：水平基础层 — 教 AI 生成符合 SDK 规范的 Java 集成代码。

基于 **6 条核心规则** 确保生成代码 100% 符合调用规范：

| 规则 | 说明 | 典型错误 |
|------|------|---------|
| 规则 1 | initialize 必须先完成且检查返回值 | 未检查返回值就调用其他 API → null |
| 规则 2 | 所有回调必须检查 errorCode | errorCode 非 SUCCESS 时直接使用 data → NPE |
| 规则 3 | listenId 必须保存，关闭时 cancel | 不保存 listenId → 回调泄漏 |
| 规则 4 | 飞控指令必须先抢权、用完释放 | 未 acquire 就发指令 → 权限错误 |
| 规则 5 | getInstance 可能返回 null | 不做 null 检查 → NPE |
| 规则 6 | 危险操作必须先输出风险警告 | 生成 takeoff/急停代码时未提醒用户确认 → 误操作 |

**适用场景**：编写 Java 代码调用 SDK API、实现飞控/直播/航线逻辑、生成 Spring Boot Service 集成层、审查 SDK 调用代码质量。

**示例对话**：

```
用户：帮我写一个返航的 Service 方法

AI：（自动加载 cloudapi2-sdk-core，按规则 4 生成代码）

@Service
public class FlightControlService {
    public void returnHome(String droneSn) {
        Drone drone = FlightHubSDK.getDroneInstance(droneSn);
        if (drone == null) {                     // 规则 5：null 检查
            throw new DeviceNotFoundException(droneSn);
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        ControlAuthModule auth = drone.getControlAuthModule();
        auth.acquireFlightControlAuthority(acquireError -> {    // 规则 4：先抢权
            if (acquireError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                future.completeExceptionally(...);              // 规则 2：检查 errorCode
                return;
            }
            drone.getFlightControlModule().returnHome(cmdError -> {
                auth.releaseFlightControlAuthority(e -> {});    // 规则 4：用完释放
                if (cmdError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                    future.completeExceptionally(...);
                } else {
                    future.complete(null);
                }
            });
        });
        future.get(30, TimeUnit.SECONDS);
    }
}
```

### 3.2 MCP 工具路由（cloudapi2-mcp-guide）

**定位**：水平基础层 — 帮 AI 在运行时选择正确的 MCP Tool 并编排多步调用链。

使用"三问决策法"确定工具选择：

1. **用户手里有什么？**（wayline_uuid / 本地文件 / URL / 设备 SN）
2. **用户想做什么？**（查询 / 创建 / 执行 / 监控 / 控制）
3. **前置条件满足了吗？**（不满足 → 先找能产出前置数据的工具）

**工具分类速查**：

| 用户意图 | 首选工具 |
|---|---|
| "查看设备" | `fh_device_list` |
| "创建飞行任务" | `fh_flight_task(create)` |
| "上传航线文件" | `fh_workspace(submit)` 或 `fh_wayline_upload` |
| "执行任务" | `fh_wayline_execute` |
| "暂停/取消" | `fh_wayline_control(pause/cancel)` |
| "控制无人机起飞" | `fh_control_acquire` → `fh_drc_connect` → `fh_takeoff` |

**适用场景**：设备列表/状态查询、航线任务创建与执行、无人机控制（起飞/返航/急停）、直播/遥测数据查询。

### 3.3 前端组件接入（cloudapi2-frontend-component）

**定位**：水平基础层 — 指导 PaaS 前端组件集成。

为 Vue 3 / 纯 HTML 项目提供 4 大 PaaS 前端组件的接入指导：

| 组件 | 功能 | 适用场景 |
|------|------|---------|
| WaylineEditor | 航线创建/编辑/预览 | 航线规划页面 |
| UranusDeviceManagerApp | 设备管理面板 | 设备监控页面 |
| FH2.loadCockpit | 驾驶舱实时操控 | 实时飞行控制台 |
| FlyDataReplay | 飞行记录回放 | 历史数据分析 |

每个组件附带完整的 initConfig 配置模板、auth 鉴权接入、销毁清理、事件订阅示例，以及跨域（Mixed Content）和 Whistle 代理调试方案。

### 3.4 设备管理模块扩展（cloudapi2-device-management）

**定位**：垂直场景层 — 指导在 skeleton 后端项目中扩展设备管理功能。

覆盖以下场景：

- 设备列表与状态查询
- OSD 遥测数据订阅（`@EventListener` + `OsdUpdateEvent`）
- HMS 告警处理（`HmsAlertEvent`）
- 设备上下线事件（`DeviceOnlineEvent` / `DeviceOfflineEvent`）
- 固件升级管理
- 新建自定义 feature 模块

### 3.5 SEI/OSD 遥测接入（cloudapi2-sei-osd）

**定位**：垂直场景层 — 帧级位置数据(SEI) + 设备遥测(OSD) 全链路接入。

覆盖以下场景：

- 服务端 OSD 订阅与推送
- SEI 视频帧级位置信息解析
- 前端 Cesium 三维可视化
- 高频数据性能优化

### 3.6 地图域接入（cloudapi2-map）

**定位**：垂直场景层 — 自定义飞行区 / 离线地图 / 解锁证书 / 地图元素的 Java SDK 接入。

覆盖以下场景：

- 自定义飞行区（限飞区 / 作业区 / 禁降区）— `SafetyServiceModule.createCustomizedFlightAreas` / `syncCustomizedFlightAreas`
- 地形障碍物离线地图、信号地图（H3）
- Flysafe 解锁证书上传与管理（`uploadUnlockLicenseFile`）
- 地图元素（点/线/面/分组，GeoJSON Feature）— `ElementModule`

> 特别覆盖"改完 / sync 之后设备侧没生效"这类**同步语义**问题——飞行区采用两段式设计：修改后必须 `sync` 并轮询 `*SyncStatus`，属设计行为而非故障。

### 3.7 PSDK 负载域接入（cloudapi2-payload）

**定位**：垂直场景层 — 第三方 PSDK 负载（探照灯 / 喊话器 / Widget / 浮窗）的 Java SDK 接入。

覆盖以下场景：

- 探照灯控制（`searchlightModeSet`）、喊话器播放与音量（`speakerPlayVolumeSet`、TTS）
- PSDK Widget 与浮窗文本、PSDK UI 资源管理（`PsdkResourceModule`）
- 喊话器音频资源上传，含 STS 直传（`getSTS` → `uploadSpeakerAudioFile`）

> 负载控制指令走 **DRC 链路**，必须先 `connectDrcLink`；"指令下发无反应""listen 返回 -1"多为 DRC 未连接或订阅签名问题。

### 3.8 AI 域接入（cloudapi2-ai）

**定位**：垂直场景层 — AI Box 应用、机载 AI 识别/跟踪、AI Inside 算法生命周期的 Java SDK 接入。

覆盖以下场景：

- AI Box 应用管理（`AIBoxModule.fetchAIBoxInfo` / `installApp` / `operateApp`）
- 机载 AI 识别与跟踪（`OnboardAiModule`：`AIIdentifySet` / `AIModelSelect` / `DrcAISpotlightZoom`）
- AI Inside 机载算法生命周期（`AIInsideManageModule`：`uploadAlgorithmFile` / `operateAIInsideModel`）
- AI 目标检测告警配置与查询（`AiAlertModule`：`postAlgorithmSetting` / `getAiAlertRecords`）

### 3.9 MCP Server 部署（cloudapi2-mcp-deploy）

**定位**：垂直场景层 — 引导将 DJI Cloud API 2 MCP Server 部署到自有环境。

采用**半自动引导**模式（AI 负责思考与生成，用户负责执行系统级操作）：

```
环境探测 → 路径选择 → 逐步执行 → 验证 → 排障（如需）
```

**两条部署路径**：

| 路径 | 场景 | 形态 |
|------|------|------|
| 远程 SSE 部署 | 生产环境，多客户端共享 | Docker Compose + SSE |
| 本地 stdio 开发 | 本地调试，单客户端 | JAR 直接运行（Mock 模式）或 Docker stdio 代理 |

**适用场景**：Docker Compose 远程 SSE 部署、stdio 本地开发配置、Claude Code / Cursor MCP 接入配置、部署排障。

### 3.10 Skeleton 部署（cloudapi2-skeleton-deploy）

**定位**：垂直场景层 — 引导将 FH Skeleton（集成商接入骨架）部署到自有环境。

同样采用**半自动引导**模式，支持三种部署形态：

| 形态 | 推荐场景 | 前提条件 |
|------|---------|---------|
| 普通机器 | 单机无 Docker | JDK 17 + Maven + MySQL |
| Docker Compose | 单机有 Docker | Docker 24+ |
| Kubernetes | 已有 K8s 集群 | kubectl + 镜像仓库 |

**适用场景**：Skeleton 三形态部署引导、环境探测与选型建议、密钥生成与配置填写、部署排障。

### 3.11 知识库入库（cloudapi2-knowledge-ingest）

**定位**：横切层 — 将外部文档自动收录到 `knowledge/` 知识库。

**5 阶段入库流程**：

```
Acquire（获取源内容）→ Analyze（分析归属）→ Confirm（用户确认）
  → Format（格式化）→ Commit（写入+索引+提交）
```

**支持的输入源**：

| 来源 | 示例 |
|------|------|
| 本地文件 | `把 docs/新接口.md 加到知识库` |
| URL | `把 https://developer.dji.com/xxx 入库` |
| 飞书文档 | `把飞书上这个文档入库` |
| Confluence | `把 Confluence 上 xxx 页面加到知识库` |
| 粘贴文本 | `把下面这段加到知识库：...` |

也支持独立的索引重建：`重建知识库索引`、`rebuild index`。

### 3.12 排障诊断（cloudapi2-troubleshooting）

**定位**：横切层 — 用户报错时优先介入，定位后路由到具体 Skill。

基于 4 阶段 RCA（Root Cause Analysis）方法论；v2.0.0 起 RCA 之前先做**本地采集 + 静态判级**（`scripts/diag.sh collect/classify`）：L1 就地给结论与建议，L2/L3 带证据包再进入 RCA。

```
阶段 0：本地采集与判级（diag.sh collect/classify → L1/L2/L3）
    ↓
阶段 1：现象收集（错误码、日志、复现步骤）
    ↓
阶段 2：范围定位（SDK 层 / 网络层 / 服务端 / 设备端）
    ↓
阶段 3：根因分析（错误码字典匹配 + 链路追踪）
    ↓
阶段 4：修复验证（修复方案 + 回归测试）
```

内置错误码字典，覆盖 SDK 层枚举（FlightHubSdkErrorCode）、HTTP 状态码、服务端业务码、设备端错误码。

### 3.13 问题反馈（cloudapi2-feedback）

**定位**：横切层 — 把一次会话中的口语描述整理成结构化 GitHub issue，无需先有错误码。

> 横切层 Skill（`knowledge-ingest` / `troubleshooting` / `feedback`）不区分前端/后端/运维，按用户意图直接介入，路由优先级高于水平基础层和垂直场景层——一旦匹配即拦截处理。

```
采集（diag.sh session 采集会话与诊断元数据）
  → 脱敏（去除 token / SN / 内网地址 / 个人信息）
  → 确认（把脱敏后的 issue body 交用户过目）
  → 提交（生成 issue URL 或经 gh CLI 创建）
```

**4 类反馈**：问题（Bug）/ 需求 / 咨询 / 其他，AI 开场先确认类型再决定 issue 模板与 label。

**与 troubleshooting 的分工**：有明确错误码 → `troubleshooting` 走 4 阶段 RCA；只是"用得不顺 / 想提个需求" → `feedback` 走反馈闭环。脚本共用 `cloudapi2-troubleshooting/scripts/diag.sh`，出网内容仅限已脱敏元数据，完整内容出网由用户主动决定。

---

## 四、Skill 与 MCP Server 的关系

| 维度 | Skill（本文档所述） | MCP Server |
|------|---------------|--------------------------|
| 作用时机 | AI **辅助开发**时（写代码/部署/入库/排障） | AI **运行时调用工具**时 |
| 能力 | 教 AI "怎么做对" | 让 AI 直接操控设备 |
| 安装方式 | Plugin / 复制 .md 文件 | 配置 SSE 端点连接或本地 stdio |
| 运行依赖 | 无（纯知识文件） | 需要 SDK 运行环境 |
| 输出物 | 源代码 / 部署指引 / 诊断建议 | 设备控制指令执行结果 |
| 适用 IDE | Claude Code / Cursor / Copilot / Windsurf | Claude Code / Cursor / 任何 MCP 客户端 |

**协作示例**：

```
用户："帮我创建一个每天 9 点自动巡检的航线任务"

1. cloudapi2-mcp-guide 介入 → 引导 AI 选择 fh_workspace(list) + fh_flight_task(create) 工具链
2. MCP Server 执行 → 实际调用 SDK 完成任务创建

用户："帮我写一个航线任务管理的 Service 类"

1. cloudapi2-sdk-core 介入 → 生成符合规范的 Java 代码（含 6 条规则检查）
2. 输出源代码 → 开发者集成到项目中

用户："帮我把 MCP Server 部署到远程服务器"

1. cloudapi2-mcp-deploy 介入 → 环境探测 → 推荐 Docker SSE 部署路径
2. 半自动引导 → AI 生成命令，用户执行并回报结果
```

---

## 五、Skill 路由与协作

### 5.1 触发词速查

每个 Skill 有明确的触发词，AI 根据用户输入中的关键词自动判断加载哪个 Skill。

| Skill | 触发词示例 |
|-------|-----------|
| sdk-core | initialize、listenXxx、acquireFlightControlAuthority、getDroneInstance |
| frontend-component | WaylineEditor、loadCockpit、paas.js、航线编辑器、驾驶舱页面 |
| mcp-guide | fh_flyto、fh_device_list、让无人机飞、查看设备、选哪个工具 |
| device-management | OSD 订阅、HMS 告警、设备上下线、feature 模块、@EventListener |
| sei-osd | SEI、帧级位置、subscribeArSeiInfo、Cesium 可视化 |
| map | 自定义飞行区、限飞区、离线地图、解锁证书、地图元素、SafetyServiceModule |
| payload | 探照灯、喊话器、PSDK 负载、Widget、浮窗、PsdkControlModule |
| ai | AI Box、机载 AI、AI 识别、目标跟踪、AI Inside、算法生命周期、AI 告警 |
| mcp-deploy | MCP 部署、deploy MCP、SSE 部署、Docker 部署 MCP、FH_SSE_AUTH_TOKEN |
| skeleton-deploy | skeleton 部署、部署到服务器、Docker 部署、K8s 部署、systemd |
| knowledge-ingest | 加到知识库、入库、ingest、知识库收录、重建索引、rebuild index |
| troubleshooting | error_code、错误码、排障、诊断、RCA、白屏、加载失败、本地诊断、现场采集、diag.sh |
| feedback | 反馈问题、提 issue、bug 反馈、问题上报、脱敏上报 |

### 5.2 多 Skill 协作

同一对话中多个 Skill 可以协作，AI 根据路由规则自动编排：

- **运维操作**（"让无人机返航"）→ `mcp-guide` 选工具 + MCP Server 执行
- **写代码**（"写一个返航 Service"）→ `sdk-core` 生成规范代码
- **接前端**（"页面上加个驾驶舱"）→ `frontend-component` 指导组件集成
- **全栈场景**（"做一个设备监控，后端 OSD + 前端 Cesium"）→ `sei-osd`（主）+ `frontend-component`（辅）
- **业务域扩展**（"写代码控制探照灯"）→ `payload`（垂直）+ `sdk-core`（规则）
- **部署**（"帮我部署 MCP Server"）→ `mcp-deploy` 半自动引导
- **入库**（"把这个文档加到知识库"）→ `knowledge-ingest` 5 阶段流程
- **排错**（"报了 4001 错误"）→ `troubleshooting` 优先介入，定位后路由到对应 Skill
- **反馈**（"这个功能用着不顺手，帮我提个 issue"）→ `feedback` 采集脱敏后创建 issue；若采集阶段发现明确错误码，可先转 `troubleshooting` 定位

路由优先级：横切层 > 水平基础层 > 垂直场景层。详细规则见 `_shared/skill-routing.md`。

---

## 六、目录结构

```
skills/
├── README.md                          ← 总览与安装指南
├── CONTRIBUTING.md                    ← 新增 Skill 规范
├── docs/
│   └── Skill使用说明.md                ← 本文件
├── _shared/                           ← 共享基础设施（各 Skill 内部引用，非独立 Skill）
│   ├── constraints-common.yaml        ← 跨 Skill 共享约束 SSOT
│   ├── sop-template.md                ← SOP 流程模板
│   ├── skill-routing.md               ← Skill 路由与编排规则
│   ├── output-format.md               ← 统一输出格式标准
│   ├── templates/                     ← 新 Skill 脚手架模板（SKILL/README/constraints）
│   └── eval/                          ← 跨 Skill 路由评测（含负向用例）
│
├── cloudapi2-sdk-core/                ← 后端 Java SDK 核心规则
│   ├── SKILL.md
│   ├── references/                    ← 指向 knowledge/ 的软链
│   ├── prompts/                       ← 7 个场景 Prompt 范本
│   └── templates/                     ← 3 个 Java 代码模板（.tpl）
│
├── cloudapi2-frontend-component/      ← 前端组件接入
│   ├── SKILL.md
│   ├── references/
│   ├── snippets/
│   └── commands/                      ← 6 个组件接入命令
│
├── cloudapi2-mcp-guide/               ← MCP 工具路由
│   ├── SKILL.md
│   ├── references/
│   └── snippets/
│
├── cloudapi2-device-management/       ← 设备管理模块扩展
│   ├── SKILL.md
│   └── references/
│
├── cloudapi2-sei-osd/                 ← SEI/OSD 遥测接入
│   ├── SKILL.md
│   └── references/
│
├── cloudapi2-map/                     ← 地图域接入（飞行区/离线地图/解锁证书/元素）
│   ├── SKILL.md · README.md · constraints.yaml
│   ├── references/                    ← safety-service.md / element.md
│   └── eval/                          ← routing / param / quality / trajectory
│
├── cloudapi2-payload/                 ← PSDK 负载域接入（探照灯/喊话器/Widget/浮窗）
│   ├── SKILL.md · README.md · constraints.yaml
│   ├── references/                    ← psdk-control.md / psdk-resource.md
│   └── eval/
│
├── cloudapi2-ai/                      ← AI 域接入（AI Box/机载识别/AI Inside/告警）
│   ├── SKILL.md · README.md · constraints.yaml
│   ├── references/                    ← ai-box / onboard-ai / ai-inside / ai-alert
│   └── eval/
│
├── cloudapi2-mcp-deploy/              ← MCP Server 部署引导
│   ├── SKILL.md
│   ├── references/                    ← 部署步骤 + 排障 + 环境变量速查
│   └── snippets/                      ← 验证命令
│
├── cloudapi2-skeleton-deploy/         ← Skeleton 三形态部署引导
│   ├── SKILL.md
│   ├── references/                    ← 普通机器/Docker/K8s 步骤 + 排障
│   └── snippets/                      ← 验证命令
│
├── cloudapi2-feedback/                ← 问题反馈闭环
│   ├── SKILL.md · README.md
│   └── eval/
│
├── cloudapi2-knowledge-ingest/        ← 知识库入库助手
│   └── SKILL.md
│
└── cloudapi2-troubleshooting/         ← 本地诊断 + 排障
    ├── SKILL.md
    ├── error-codes.yaml               ← 错误码字典（唯一真值源）
    ├── scripts/
    │   └── diag.sh                    ← 采集/脱敏/判级/报告/反馈
    ├── rules/
    │   └── classification.yaml        ← 判级决策表（人读）
    └── references/
```

---

## 七、最佳实践

### 7.1 搭配 superpowers 使用

devkit Skill 提供 DJI 领域知识，搭配 [superpowers](https://github.com/anthropics/superpowers) 可获得通用方法论支持：

```bash
claude plugin install superpowers
```

| | superpowers | devkit |
|---|---|---|
| 解决 | "怎么思考和设计" | "怎么用 DJI SDK 实现" |
| 举例 | "从零设计设备监控架构" → 发散/收敛 | "在 skeleton 中加 OSD 订阅" → 直接给代码 |
| 类比 | 通用思维框架 | 领域专家手册 |

两者协作时 AI 自动分工：superpowers 管方法论流程，devkit 管领域约束和代码模板。

### 7.2 按场景选择 Skill

| 你想做什么 | 用哪个 Skill | 你会得到什么 |
|-----------|-------------|-------------|
| 写 Java SDK 集成代码 | sdk-core | 符合 6 条核心规则的 Java 代码 |
| 接前端 PaaS 组件 | frontend-component | initConfig 模板 + 事件订阅代码 |
| 用 MCP 直接操控设备 | mcp-guide | 正确的工具调用链 + 前置条件检查 |
| 扩展 skeleton 设备管理 | device-management | feature 模块代码 + 事件监听器 |
| 接入 SEI/OSD 遥测 | sei-osd | 后端订阅 + 前端 Cesium 渲染代码 |
| 接飞行区 / 离线地图 / 解锁证书 | map | SafetyServiceModule / ElementModule 调用代码 |
| 控制 PSDK 负载 | payload | 探照灯 / 喊话器 / Widget 控制代码（含 DRC 前置） |
| 接 AI 识别 / AI Box / AI Inside | ai | 4 个 AI 模块的调用代码与生命周期管理 |
| 部署 MCP Server | mcp-deploy | 逐步引导命令 + 配置文件 |
| 部署 Skeleton | skeleton-deploy | 三形态选型建议 + 逐步引导命令 |
| 收录文档到知识库 | knowledge-ingest | 格式化文件 + 索引更新 + Git 提交 |
| 排障定位问题 | troubleshooting | 4 阶段 RCA 诊断 + 修复方案 |
| 反馈问题 / 提 issue | feedback | 脱敏后的 issue 草稿 + 自动创建 |

---

## 八、常见问题

### Q: Plugin 安装后 Skill 没有触发？

1. 运行 `claude plugin list` 确认 `cloudapi2@dji` 状态为 enabled
2. 确认 settings.json 中 path 为仓库根目录的**绝对路径**
3. 验证 JSON 格式：`cat ~/.claude/settings.json | python3 -m json.tool`
4. 重启 AI IDE（新开终端窗口）

### Q: 如何更新 Skill 到最新版本？

- Plugin 模式：`git pull` 本仓库即可，新会话自动加载最新版
- 手动拷贝模式：需重新执行 `cp -r` 命令

### Q: 可以只安装部分 Skill 吗？

Plugin 模式安装全部 13 个 Skill（推荐）。如果只需要部分，使用手动拷贝方式安装指定的 Skill 目录。

### Q: Skill 支持哪些 AI IDE？

| IDE | 支持状态 | 安装方式 |
|-----|---------|---------|
| Claude Code | 完整支持 | Plugin / settings.json / 手动拷贝 |
| Cursor | 完整支持 | 拷贝到 `.cursor/skills/` |
| GitHub Copilot (VS Code) | 完整支持 | 拷贝到 `.github/skills/` |
| Windsurf | 完整支持 | 拷贝到对应 skill 目录 |

### Q: Skill 和 MCP Server 需要同时使用吗？

不是必须的，两者可以独立使用：

- **只用 Skill**：AI 帮你写集成代码 + 引导部署，但不能直接操控设备
- **只用 MCP Server**：AI 直接操控设备，但写代码时缺少 SDK 规范约束
- **两者配合**（推荐）：写代码有规范保障，操控设备有路由指引，部署有半自动引导

### Q: 部署类 Skill 与 troubleshooting 什么关系？

部署类 Skill（mcp-deploy / skeleton-deploy）内置了部署排障流程。当用户在部署过程中遇到错误，由部署 Skill 本身处理；当用户在使用 SDK / 前端组件时遇到运行时错误，由 troubleshooting 处理。

### Q: 如何贡献新的 Skill？

1. 新建 Skill 目录：`skills/cloudapi2-<skill-name>/`
2. 编写 `SKILL.md`（包含 YAML frontmatter：name、description、version、author）
3. 按需添加 `references/`、`snippets/`、`commands/` 子目录
4. 编写 `README.md`
5. 更新 `skills/README.md` 的 Skill 列表和 `_shared/skill-routing.md` 的路由表
6. 提交 MR

---

## 九、获取支持

| 渠道 | 用途 |
|------|------|
| 仓库 Issues | Bug 报告、功能建议 |
| 内部飞书群 | 实时技术支持 |
| `skills/` 各 Skill 的 README.md | 单个 Skill 详细文档 |
| `skills/_shared/skill-routing.md` | Skill 路由与编排规则 |
| `mcp/README.md` | MCP Server 接入与部署 |
