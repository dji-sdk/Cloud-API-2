# DJI FlightHub SDK Skill

为 AI Agent 提供 DJI FlightHub Java SDK 的专业接入能力包。安装后，AI 在你编写 FlightHub SDK 集成代码时**自动加载**，确保生成的代码遵循 5 条核心安全规则。

## 能力

| 能力 | 说明 |
|------|------|
| **核心规则引导** | 5 条必须遵守的 SDK 调用规则，防止 NPE、资源泄漏、权限错误 |
| **错误码诊断** | 通过 4 层 errorCode 结构自动定位问题 + 修复方案 |
| **接入质量评估** | 🔴🟡🟠 三级风险检查清单，逐项检查你的接入代码 |
| **业务场景 Prompt** | 6 个典型场景（设备列表/OSD/直播/航线/飞控/媒体）标准化范本 |
| **Java 代码模板** | SDK 适配器、Service 层、回调桥接工具 3 套模板 |

## 安装

推荐通过 `devkit@cloud-api2` Plugin 一键安装全套 Skill（见 [skills/README.md](../README.md#安装方式)）。

单独安装本 Skill：

```bash
# Claude Code
cp -r skills/cloudapi2-sdk-core ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-sdk-core .cursor/skills/

# GitHub Copilot (VS Code)
cp -r skills/cloudapi2-sdk-core .github/skills/
```

或 clone 本仓库后保持在项目内，Claude Code 自动发现。

### 验证安装成功

安装后在 AI 助手中输入以下任一关键词，Skill 应自动触发：
- "集成 FlightHub SDK"
- "初始化 FlightHubSDK"
- "订阅无人机 OSD"
- "让无人机返航"
- "getDroneInstance 返回 null"

如果 Skill 列表中出现 `cloudapi2-sdk-core`，说明安装成功。

---

## 使用方法

### 自动触发（推荐）

Skill 安装后**无需手动调用**。当你的对话涉及以下场景时，AI 会自动加载 Skill：

- 集成 / 接入 FlightHub SDK（Java）
- 监听无人机或机场状态
- 控制无人机（起飞、降落、返航、急停）
- 开始 / 停止直播流
- 管理飞行任务
- 遇到 SDK 报错
- 管理设备

### 使用示例

#### 场景 1：初始化 SDK

```
你：帮我在 Spring Boot 项目中初始化 FlightHub SDK

AI：（自动加载 Skill）
→ 生成 @Configuration + @PostConstruct 代码
→ 自动检查 initialize 返回值
→ 自动加 @PreDestroy shutdown
→ Token 从环境变量注入
```

#### 场景 2：错误排查

```
你：getDroneInstance 总是返回 null，怎么办？

AI：（自动路由到错误诊断）
→ 给出 4 步排查方案
→ 引用 references/错误码诊断.md
→ 提供从 getDeviceList 获取正确 SN 的代码
```

#### 场景 3：质量检查

```
你：帮我检查这段 SDK 接入代码的质量

AI：（加载接入质量检查清单）
→ 按 🔴🟡🟠 三级逐项检查
→ 标注每条的 ✅ 已满足 / ❌ 未满足
→ 给出修复代码
```

#### 场景 4：使用 Prompt 模板

```
你：帮我添加直播功能

AI：（加载 prompts/add-livestream.md）
→ 按标准步骤生成：Service 层 + Controller
→ 遵循规则 2（errorCode 检查）+ 规则 5（null 检查）
→ 控制直播流不超过 4 路
```

---

## Skill 会做什么 / 不会做什么

| ✅ Skill 会做 | ❌ Skill 不会做 |
|--------------|----------------|
| 确保代码遵循 6 条核心规则 | 替代 SDK 文档（完整 API 仍需查官方文档） |
| 自动加入 errorCode 检查 | 运行时拦截错误调用 |
| 提供正确的 listen/cancel 模式 | 提供所有 500+ API 的签名 |
| 飞控操作自动加 acquire/release | 实际控制无人机（那是 MCP Server 的事） |
| 诊断常见错误码 | 覆盖所有服务端错误码 |

## 目录结构

```
cloudapi2-sdk-core/
├── SKILL.md              ← AI 首先加载的主文件（规则 + 路由 + 反幻觉）
├── references/           ← 按需加载的专题文档
│   ├── 核心规则详解.md    — 每条规则的"为什么" + 完整正反例
│   ├── 错误码诊断.md     — 4 层错误码表 + 诊断流程图
│   ├── 接入质量检查清单.md — 🔴🟡🟠 三档 10 大检查项
│   ├── 设备管理流程.md    — 设备发现 → 订阅 → 控制完整流
│   ├── 飞控与权限.md     — acquire → 指令 → release 模板
│   └── 文档检索与问答.md  — 9 个高频 FAQ
├── prompts/             ← 6 个业务场景标准化范本
│   ├── add-device-list.md
│   ├── add-osd-subscribe.md
│   ├── add-livestream.md
│   ├── add-flight-task.md
│   ├── add-flight-control.md
│   └── add-media-action.md
├── templates/           ← Java 代码模板（复制即用）
│   ├── sdk-module-adapter.java.tpl
│   ├── spring-boot-service.java.tpl
│   └── callback-handler.java.tpl
└── README.md            ← 本文件
```

> **注意**：MCP Tool 路由指南（教 AI 选对工具）已拆分为独立的 `cloudapi2-mcp-guide` skill，
> 见 `skills/cloudapi2-mcp-guide/`。本 skill 仅负责代码生成。

## 适用版本

- **SDK**: FlightHub Java SDK v0.8.0+
- **框架**: Spring Boot 2.x / 3.x
- **IDE**: Claude Code / Cursor / GitHub Copilot / 任何支持 Agent Skills 的工具
- **最后更新**: 2026-06

## 常见问题

### Q: 安装后 Skill 没有触发？

1. 确认文件在正确路径：`~/.claude/skills/cloudapi2-sdk-core/SKILL.md`
2. 确认 SKILL.md 的 YAML frontmatter 完整（`---` 包裹的 name/description）
3. 重启 IDE 或新开会话

### Q: 我用 Cursor，怎么安装？

```bash
cp -r skills/cloudapi2-sdk-core .cursor/skills/
```
Cursor 会自动加载 `.cursor/skills/` 下的 Skill 文件。

### Q: Skill 和 MCP Server 有什么区别？

| 维度 | Skill | MCP Server |
|------|-------|------------|
| 作用时机 | AI **写代码**时 | AI **运行时调用工具**时 |
| 能力 | 教 AI "怎么写对" | 让 AI 直接操控设备 |
| 安装方式 | 复制 .md 文件 | 启动服务进程 |
| 是否需要 SDK 运行 | 不需要 | 需要 |

两者互补：Skill 教 AI 写正确的集成代码，MCP Server 让 AI 在开发/调试时直接调用 SDK。

### Q: 如何贡献 / 更新 Skill？

修改 `skills/cloudapi2-sdk-core/` 下的文件后提交 PR 即可。references/ 下的内容会被 AI 按需加载，新增文件需要在 SKILL.md 的决策树中添加路由。
