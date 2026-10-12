# FlightHub PaaS Knowledge Base

DJI FlightHub PaaS SDK 的领域知识库 —— 整个 AI 辅助接入生态的**知识单一源头（SSOT）**。

当前已建成的消费通道（llms.txt、Skills）从这里取内容，而非各自维护副本。规划中的通道（MCP Resources、静态文档站、向量检索）同样以此为源。

## 定位：和 Skills / MCP 的关系

| | Knowledge Base（本目录） | Skills（`skills/`） | MCP Server（`mcp/`） |
|---|---|---|---|
| **是什么** | 结构化知识文件集合 | AI 编码行为规则包 | 运行时工具服务 |
| **回答** | "SDK 是怎么工作的" | "写代码时该怎么做" | "现在帮我执行这个操作" |
| **消费者** | 任何 LLM / 人类 | Claude Code / Cursor 等编码助手 | AI Agent 运行时 |
| **可独立使用** | 是（通过 llms.txt） | 是（Plugin 安装） | 是（MCP 协议） |

**数据流向**：Knowledge Base → Skills 引用 → AI 生成代码时参考。三者互补，不重复。

## 使用方式

### AI Agent / LLM（推荐）

通过 `llms.txt` 获取知识索引，按需加载具体文档：

```
1. Fetch llms.txt → 获取文档列表和摘要
2. 根据任务选择相关文档 → 注入 context
3. 基于领域知识生成代码或做决策
```

`llms.txt` 遵循 [llms.txt 标准](https://llmstxt.org/)，任何支持该协议的 AI 工具可直接使用。

> **路径说明**：`llms.txt` 中的文档路径（如 `knowledge/architecture/system-overview.md`）是相对于仓库根目录的，便于从仓库任意位置定位文件。

### Claude Code 用户

安装 `devkit@cloud-api2` Skills 即可。Skills 自动引用本知识库内容，无需单独操作。

### 人类开发者

直接阅读 `.md` 文件。每个模块有 `index.md` 导航页，从 [目录结构](#目录结构) 开始浏览。

## 目录结构

```
knowledge/
├── llms.txt              # LLM 友好的文档索引（遵循 llms.txt 标准）
├── metadata.yaml         # 知识库元数据（版本、模块列表、frontmatter schema）
├── README.md             # 本文件
├── scripts/              # 自动化脚本（索引重建、llms-full 生成）
├── getting-started/      # 快速入门：SDK 初始化、环境配置
├── architecture/         # 系统架构：三层架构、飞控权限模型
├── api-reference/        # 接口参考：核心规则、错误码、飞控 API、设备管理
├── scenarios/            # 行业场景方案（巡检、安防、测绘）[建设中]
├── operations/           # 运维：部署、监控、故障恢复
├── migration/            # 版本迁移指南 [建设中]
└── faq/                  # 高频问题汇总
```

模块详细描述见 `metadata.yaml` 的 `modules` 字段。

## 贡献与维护

### 通过 Skill 自动入库（推荐）

安装 `cloudapi2@dji` Skills 后，在 Claude Code 中直接说：

```
把这篇文档加到知识库：<URL / 文件路径 / 直接粘贴内容>
```

Skill `cloudapi2-knowledge-ingest` 会自动完成：获取内容 → 分析归属模块 → 用户确认 → 格式化（含 LLM 生成 `summary`）→ 写入文件 + 重建索引 + Git 提交。全程无需手动编辑 frontmatter 或运行脚本。

支持的来源类型：

| 来源 | 示例 |
|------|------|
| 本地文件 | `./docs/guide.md`、`~/Desktop/notes.txt` |
| URL | `https://developer.dji.com/...` |
| 飞书文档 | 飞书链接或说"把飞书上的 xxx 入库" |
| Confluence | Confluence 页面链接 |
| 粘贴文本 | 直接在消息中贴内容 |

其他快捷命令：

- **"重建知识库索引"** — 只重建索引（不收录新内容），适合手动编辑文件后同步
- **"同步 llms.txt"** — 同上

### 手动新增知识文档

1. 确定文档归属模块（参考上方目录结构）
2. 在对应模块目录创建 `.md` 文件
3. 添加 YAML frontmatter（格式见下方，**`summary` 字段必填**）
4. 运行索引重建脚本：
   ```bash
   ./knowledge/scripts/rebuild-index.sh
   ```
   脚本自动更新 `llms.txt`、各模块 `index.md` 和 `llms-full.txt`
5. 提交变更（包含新文件和脚本更新的索引文件）

### Frontmatter 格式

每篇知识文档必须包含以下 frontmatter：

```yaml
---
title: "文档标题"                    # 必填，一行描述
module: api-reference               # 必填，归属模块 ID（见 metadata.yaml）
tags: [tag1, tag2]                  # 必填，便于检索的关键词
audience: [agent, developer]        # 必填，目标读者：agent / developer / operator
summary: "≤60 字钩子描述"           # 必填，用于 llms.txt 和 index.md（LLM 生成）
last_updated: 2026-08-05            # 必填，最后更新日期
complexity: intermediate            # 可选，beginner / intermediate / advanced
sdk_version: "3.3.7"               # 可选，适用的 SDK 版本
related:                            # 可选，相关文档路径
  - architecture/control-authority.md
---
```

完整 schema 定义在 `metadata.yaml` 的 `frontmatter_schema` 字段。

### 维护规则

- **SSOT 原则**：同一知识只在本目录维护一份，Skills 和其他通道通过引用获取
- **audience 标注**：面向 AI Agent 的文档标记 `agent`，面向人类标记 `developer` 或 `operator`，可多选
- **保持原子性**：每篇文档聚焦一个主题，通过 `related` 字段关联相关文档
- **SDK 版本绑定**：当 SDK 升级引入 Breaking Change 时，同步更新受影响的文档并修改 `sdk_version`
- **索引自动化**：新增或修改文档后运行 `./knowledge/scripts/rebuild-index.sh`，可通过 `--check` 模式验证索引是否同步（适合集成到 CI pre-commit）

### 什么该放这里 vs 不该放

| 放这里 | 不放这里 |
|-------|---------|
| SDK 工作原理、设计决策 | 内部项目的具体源码细节（文件路径、函数名、内部服务实现） |
| 错误码含义和排查方法 | 内部 bug 单号、Jira/Confluence 链接 |
| 标准接入流程和代码模板 | 只对单个内部项目有意义的 workaround |
| 版本迁移 Breaking Changes | Git commit history 能回答的问题 |
| 跨场景的最佳实践 | 临时 debug 笔记 |
| **跨项目/跨版本都适用的业务流程逻辑和事实**（即使根因分析发生在某个具体内部项目里，只要现象和排查思路是通用的） | |

> **判断标准**：内部代码细节（例如某内部项目的具体实现）别人理解不了，不放；但业务处理流程、平台行为、SDK/工具的通用事实，大家都能理解，可以放——描述时只引用本项目自身公开的 SDK 方法/字段/工具名（如 `fh_wayline_control`、`mode_code`），不引用其他内部项目的文件路径或代码。

## 版本

| 字段 | 值 |
|------|-----|
| 知识库版本 | 1.0.0 |
| 适用 SDK 版本 | 3.3.7 |
| 最后更新 | 2026-08-05 |

版本信息以 `metadata.yaml` 为准。
