---
name: cloudapi2-knowledge-ingest
description: |
  知识库入库助手。将文档/URL/文本/飞书/Confluence 内容收录到 knowledge/ 知识库。
  触发词：加到知识库、更新知识库、入库、ingest、knowledge base、添加知识、
  知识库收录、收录到知识库、加入知识库、重建索引、同步 llms.txt、rebuild index。
  不触发：新功能开发、查询知识库内容、修改已有知识文件、问 SDK 问题。
version: 1.1.0
author: DJI CloudAPI2 Team
---

# 知识库入库助手

## 这个 Skill 是干什么的

帮用户将外部文档/内容自动收录到 `knowledge/` 知识库。一次完整入库 = 获取内容 → 分析归属 → 用户确认 → 格式化 → 写入文件 + 更新索引 + 提交。

也支持独立的索引重建（不收录新内容）。

## 何时触发

### 完整入库流程（Stage 1→5）

- 用户说"把 xxx 加到知识库"
- 用户说"入库"、"ingest"、"收录"
- 用户给了一个文档/URL 并表示要加入知识库
- 用户说"更新知识库"并提供了新内容来源

### 仅重建索引（快捷命令）

- 用户说"重建知识库索引"、"同步 llms.txt"、"rebuild index"
- 用户手动编辑了知识库文件后说"更新索引"

遇到上述快捷命令时，**跳过 Stage 1-4**，直接执行：

```bash
./knowledge/scripts/rebuild-index.sh
```

然后报告变更摘要：

```
✅ 索引已重建。

  llms.txt：{更新/已是最新}
  index.md：{列出有变更的模块，或"全部最新"}
```

**不触发**：
- 查询知识库内容（直接读 `knowledge/` 文件）
- 修改已有知识文件（用户应直接编辑）
- 新功能开发（用对应业务 Skill）
- 问 SDK 怎么用（用 sdk-core / mcp-guide）

## 工作流 — 5 阶段入库流程

**绝对不许跳步。** 每个阶段必须完成后才能进入下一阶段。

### Stage 1: Acquire（获取源内容）

**目标**：拿到用户指定的原始内容。

根据来源类型选择获取方式：

| 来源类型 | 识别方式 | 获取方法 |
|---------|---------|---------|
| 本地文件 | 路径（`/`、`./`、`~/` 开头，或 `.md`/`.txt`/`.pdf` 结尾） | Read 工具直接读取 |
| URL | `http://` 或 `https://` 开头 | WebFetch 工具 |
| 飞书文档 | 包含 `feishu.cn`、`larksuite.com`、或用户说"飞书" | Lark MCP `get_document_content` |
| Confluence | 包含 `confluence`、`wiki`、或 Confluence URL 模式 | Confluence MCP `get_page_by_id` / `search_pages` |
| 粘贴文本 | 以上都不匹配，用户直接在消息中提供了内容 | 直接使用消息中的文本 |

**失败处理**：
- URL 不可达 → 告知用户，建议检查网络或换用本地文件
- MCP 认证过期 → 提示用户重新认证（飞书用 `feishu-reconnect` skill）
- 文件不存在 → 告知用户检查路径

### Stage 2: Analyze（分析内容）

**目标**：判断内容的归属模块、文件名、标签。

分析并确定以下信息：

**2.1 归属模块**（必须是以下 7 个之一）：

| 内容特征 | 归属模块 |
|---------|---------|
| SDK 初始化、环境搭建、第一次使用 | `getting-started` |
| 系统设计、架构决策、设计模式 | `architecture` |
| 接口签名、错误码、调用流程、代码示例 | `api-reference` |
| 行业方案、业务场景、端到端方案 | `scenarios` |
| 部署、监控、故障恢复、运维 | `operations` |
| 版本差异、升级指引、breaking changes | `migration` |
| 常见问题、快速回答 | `faq` |

**2.2 文件名**：kebab-case 英文，≤40 字符，语义明确。
示例：`wayline-planning.md`、`multi-dock-coordination.md`、`rtk-calibration-guide.md`

**2.3 标题**：中文标题，≤20 字。

**2.4 标签**：3-6 个，技术术语用英文，业务术语可中文。

**2.5 受众和复杂度**：

参照 `knowledge/metadata.yaml` 的 `frontmatter_schema` 定义选择合法值。默认值：
- audience：`[agent, developer]`
- complexity：`intermediate`

### Stage 3: Confirm（用户确认）

**目标**：让用户确认或调整入库参数。

**必须**向用户展示以下信息并等待确认：

```
📥 准备入库：

  标题：{title}
  文件：knowledge/{module}/{filename}.md
  标签：{tags}
  模式：重写（浓缩为 ≤2000 字 LLM 友好格式）

确认？或调整（改模块/改文件名/原样入库/取消）
```

**用户可能的回复**：
- 确认 → 进入 Stage 4
- "原样入库" → 切换为原样模式，进入 Stage 4
- "放到 xxx 模块" → 更新模块，重新展示确认
- "文件名改成 xxx" → 更新文件名，重新展示确认
- "取消" → 终止流程

**冲突检测**：如果 `knowledge/{module}/{filename}.md` 已存在：
- 告知用户文件已存在
- 选项：① 换文件名 ② 覆盖已有文件 ③ 取消
- 覆盖时 commit message 改为 `fix(knowledge): 更新 {title}`

### Stage 4: Format（格式化）

**目标**：将源内容转换为知识库标准格式。

#### 重写模式（默认）

将源内容浓缩重写，必须遵守以下约束：

1. **总长度**：≤2000 中文字符（不含 frontmatter）
2. **开头**：第一句话总结核心结论（Agent 可能只读前 200 字判断相关性）
3. **结构**：使用表格、编号列表，避免长段落
4. **代码**：示例必须完整可运行，不用 `...` 省略关键部分
5. **前置条件**：明确声明假设和依赖
6. **去冗余**：删除重复、过时、无关内容
7. **保留关键**：所有技术细节、数据、API 签名必须保留

#### 原样模式（用户指定）

- 仅在内容体前添加 YAML frontmatter + 一句话摘要
- 内容体完全不修改
- 适用于已经结构良好的文档、官方规格、需要保留原始措辞的内容

#### 输出格式

无论哪种模式，最终文件必须符合 `knowledge/metadata.yaml` 的 `frontmatter_schema`。结构为：

```markdown
---
title: "{title}"
module: {module}
tags: [{tags}]
audience: [{audience}]
complexity: {complexity}
summary: "{≤60 字的钩子描述，面向 llms.txt 索引，让 LLM 一眼判断是否需要加载此文档}"
last_updated: {YYYY-MM-DD，当天日期}
sdk_version: "3.3.7"
source: "{原始来源，如 URL、文件路径、'用户提供文本'}"
related:
  - {相关文件路径，如有}
---

{一句话摘要（≤50 字，概括核心内容）}

{格式化后的正文}
```

### Stage 5: Commit（入库提交）

**目标**：完成文件写入、索引重建和 Git 提交。用户不需要手动运行任何脚本，全部由 AI 自动执行。

按顺序执行以下 3 步：

**5.1 写入文件**

将格式化后的内容写入 `knowledge/{module}/{filename}.md`。

**5.2 自动重建索引**

直接执行索引重建脚本（用户无需知道脚本存在）：

```bash
./knowledge/scripts/rebuild-index.sh
```

脚本自动完成：从 frontmatter 的 `summary` 字段提取描述 → 重建 `llms.txt` → 重建各模块 `index.md` → 同步 `llms-full.txt`。

**5.3 Git 提交**

仅暂存本次入库涉及的文件（避免误带其他未完成的编辑）：

```bash
git add knowledge/{module}/{filename}.md knowledge/llms.txt knowledge/*/index.md
git commit -m "feat(knowledge): 收录 {title}"
```

覆盖已有文件时：
```bash
git commit -m "fix(knowledge): 更新 {title}"
```

### 完成话术

```
✅ 已入库并提交。

  文件：knowledge/{module}/{filename}.md
  索引：已通过 rebuild-index.sh 自动重建
  commit：{commit hash 前 7 位} feat(knowledge): 收录 {title}
```

## 内容质量门槛

如果源内容太短（<100 字）或太碎片化：
- 建议用户合并到已有文件（"这段内容适合追加到 knowledge/{module}/xxx.md 中，要这样做吗？"）
- 或建议放入 faq 模块作为短回答

## 不做什么

- ❌ 不做批量导入（一次只处理一个来源）
- ❌ 不自动触发（必须用户明确说"入库"类指令）
- ❌ 不删除知识库文件
- ❌ 不修改已有文件内容（除非用户选择"覆盖"）
- ❌ 不做内容审核（信任用户判断）

## 权威参考

- `knowledge/metadata.yaml` — frontmatter schema 定义（含 `summary` 字段）
- `knowledge/scripts/rebuild-index.sh` — 索引自动重建脚本（重建 llms.txt + 各模块 index.md + llms-full.txt）
- `knowledge/scripts/build-llms-full.sh` — llms-full.txt 单独重建脚本

> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。
> 当发现用户意图属于其他 Skill 职责时，按转交协议切换。
