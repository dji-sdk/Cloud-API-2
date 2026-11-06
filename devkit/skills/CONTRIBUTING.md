# Skill 开发规范

本文档定义创建新 Skill 的标准流程和质量基线。适用于团队内部 AI 工程师和外部集成商开发者。

> **快速上手**：`npm run eval -- scaffold <skill-name>` 一键生成骨架目录（需先安装 skill-eval）。
> 手动创建请按本文档逐项检查。

---

## §1 设计哲学

### 三层架构

```
┌──────────────────────────────────────────────────────┐
│  横切层（最高优先级，匹配即拦截）                       │
│  troubleshooting / feedback / knowledge-ingest        │
├──────────────────────────────────────────────────────┤
│  水平基础层（教通用规则，被垂直层引用）                  │
│  sdk-core / frontend-component / mcp-guide            │
├──────────────────────────────────────────────────────┤
│  垂直场景层（教具体业务，引用水平层）                    │
│  device-management / sei-osd / skeleton-deploy / ...  │
└──────────────────────────────────────────────────────┘
```

新 Skill 必须明确归属到一个层级。层级决定路由优先级和仲裁规则（详见 `_shared/skill-routing.md`）。

### 四条原则

1. **SSOT（单一真值源）**：共享约束只在 `_shared/constraints-common.yaml` 维护一份，各 Skill 通过 `extends` 引用，不复制
2. **懒加载**：Reference 按需加载，不一次性读取全部 `references/`；决策树决定加载哪些文件
3. **反幻觉**：代码示例必须来源于 `references/`，不确定的 API 必须标注 `[需验证]`
4. **边界清晰**："何时触发"和"不触发"必须互斥，让路由引擎可以做确定性分发

---

## §2 命名规范

### 目录名

- 格式：小写字母 + 数字 + 连字符（`[a-z0-9-]+`）
- 建议：`<domain>-<capability>`（如 `cloudapi2-wayline-task`）
- `cloudapi2-` 前缀为 DJI Cloud API 2 领域保留，其他领域自定义前缀

### frontmatter name 字段

必须与目录名一致。`name: cloudapi2-wayline-task` 对应 `skills/cloudapi2-wayline-task/`。

### 版本号

遵循语义化版本（SemVer `MAJOR.MINOR.PATCH`）：

| 变更类型 | 版本位 | 示例 |
|----------|--------|------|
| 删除/重命名触发词、删除 reference | MAJOR | 1.0.0 → 2.0.0 |
| 新增能力、新增触发词、新增 reference | MINOR | 1.0.0 → 1.1.0 |
| 修 bug、补文档、补 eval 用例 | PATCH | 1.0.0 → 1.0.1 |

- 新 Skill 首次合入统一 `0.1.0`（验证期）
- 经过至少一轮生产 bad case 回收并修复后，升为 `1.0.0`

---

## §3 目录结构

```
skills/<skill-name>/
├── SKILL.md              # [必须] Skill 主文件，含 YAML frontmatter + 正文
├── README.md             # [必须] 面向人类的使用说明（功能、触发词、示例）
├── constraints.yaml      # [推荐] 领域特有约束规则
├── eval/
│   ├── routing.yaml      # [必须] L1 路由评测用例（≥5 正向 + 3 负向）
│   ├── param.yaml        # [推荐] L2 参数提取评测
│   ├── quality.yaml      # [推荐] L3 输出质量评测
│   └── trajectory.yaml   # [推荐] L4 SOP 轨迹评测
├── references/           # [推荐] 参考文档（被决策树引用）
├── snippets/             # [可选] 代码模板片段
└── playbooks/            # [可选] 步骤方案（YAML 格式）
```

**必须 vs 推荐**：
- `[必须]` 项缺失则 MR 不可合入（CI 卡点或人工 Review 拦截）
- `[推荐]` 项缺失不阻塞合入，但会在 Review 中被建议补充
- `[可选]` 项按需创建，不是所有 Skill 都需要

---

## §4 SKILL.md 编写规范

### frontmatter（必填）

```yaml
---
name: <skill-name>          # 必填：与目录名一致
description: |               # 必填：多行，必须包含中文触发词
  一句话说明 Skill 做什么。
  触发词：关键词1, 关键词2, 关键词3。
  不触发：排除场景1, 排除场景2。
version: 0.1.0               # 必填：语义化版本
author: <author>             # 必填
---
```

### 正文章节（按顺序）

| 章节 | 强制 | 说明 |
|------|------|------|
| `## 这个 Skill 是干什么的` | 必须 | 1-2 句话，集成商读得懂 |
| `## 何时触发` | 必须 | 正向触发条件列表（每条一个场景） |
| `## 不触发` | 必须 | 明确边界，防止误路由 |
| `## 工作流` | 必须 | 引用 `_shared/sop-template.md`，写本 Skill 的 Ch2/Ch5/Ch6 特化规则 |
| `## 决策树路由` | 必须 | 用户意图 → 加载哪个 reference 的路由树 |
| `## 反幻觉规则` | 必须 | 至少继承通用三条（来源可溯/禁止编造/不确定标 `[需验证]`） |
| `## 共享规范` | 必须 | 固定引用段（见下方模板） |
| `## 心智模型` | 推荐 | 领域核心概念的简明描述 |
| `## 核心规则` | 推荐 | 领域硬约束 + 正反代码示例 |

### 共享规范引用段（固定内容，直接复制）

```markdown
## 共享规范

> 跨 Skill 共享约束见 `skills/_shared/constraints-common.yaml`。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载，不要一次性读取所有 references/。
```

### 四条铁律

1. **`description` 必须包含中文触发词** — 这是路由引擎匹配的关键信号源，只写英文会导致中文用户输入无法命中
2. **"何时触发"与"不触发"互斥** — 存在交叉意味着路由歧义，cross-routing eval 会暴露此类问题
3. **决策树末端指向具体 reference 路径** — 禁止"参考相关文档"等模糊引用，必须写 `references/xxx.md#章节名`
4. **version 遵循 SemVer** — 触发词变更属于 breaking change（影响路由），必须升大版本

### 最佳实践示例

以 `cloudapi2-sdk-core` 为参考：
- frontmatter `description` 包含 30+ 个中英文触发词
- "何时触发"列出 7 类场景
- 决策树 5 个分支，每个末端指向具体 reference 文件
- 6 条核心规则均有正反代码示例

---

## §5 约束规则编写

约束文件 `constraints.yaml` 定义 Skill 领域特有的强制规则。AI 生成代码时必须检查并遵守所有 `critical` 级约束。

### 格式

```yaml
# extends: _shared/constraints-common.yaml

- id: unique-kebab-case-id        # 全局唯一，跨 Skill 不可重名
  rule: 一句话描述必须做什么        # 正面表述（"必须 X"而非"不能 Y"）
  violation: 违反后的具体后果       # 具体到现象（NPE/OOM/401），不写"可能出问题"
  level: critical                  # critical | warning
```

### 规则

- 第一行 `extends` 声明引用共享约束，本文件只写 Skill **特有**规则
- `level: critical` — 违反必导致功能失败或安全事故，CI 生成代码时强制检查
- `level: warning` — 违反可能导致问题但不立即失败，输出时以 ⚠️ 提醒
- `id` 全局唯一：先 `grep -r "id:" skills/*/constraints.yaml skills/_shared/constraints-common.yaml` 确认不重名
- `violation` 必须具体：写 "NPE 导致回调链断裂"，不写 "可能出错"

### 参考

- 共享约束（8 条）：`_shared/constraints-common.yaml`
- 领域约束示例：`cloudapi2-device-management/constraints.yaml`（6 条特有规则）

---

## §6 评测用例要求

### L1 路由 eval（强制，CI gate）

新 Skill 合入前**必须**提供 `eval/routing.yaml`，满足以下最低要求：

| 要求 | 最低数量 | 说明 |
|------|----------|------|
| 正向用例 | ≥ 5 | 覆盖 SKILL.md 中列出的主要触发场景 |
| 负向用例 | ≥ 3 | 与相邻 Skill 的边界区分 |
| `assert_fail` | 每个正向用例 | 指定不应路由到的相邻 Skill |
| `min_pass_rate` | 0.85 | 可根据 Skill 成熟度后续上调 |
| `tags` | 建议 | `positive` / `negative` / `boundary` / `edge-case` |

### 跨 Skill 联动

在 `_shared/eval/cross-routing.yaml` 中补充 **≥ 2 条**与相邻 Skill 的仲裁用例，验证新 Skill 不会抢走已有 Skill 的流量。

### L2-L4（推荐但不阻塞合入）

| 层 | 文件 | 何时建议写 |
|----|------|-----------|
| L2 | `eval/param.yaml` | Skill 需要从用户输入提取结构化参数（SN、错误码、URL 等） |
| L3 | `eval/quality.yaml` | 验证输出质量（至少 2 条 `llm-rubric` 用例） |
| L4 | `eval/trajectory.yaml` | Skill 有明确多阶段 SOP（如 4 阶段 RCA） |

### eval YAML 格式速查

```yaml
meta:
  skill: <skill-name>              # 与目录名一致
  level: L1                        # L1 | L2 | L3 | L4
  author: <author>
  min_pass_rate: 0.85              # gate 阈值
  source: manual                   # manual | feedback | adversarial

tests:
  - description: "用例描述"
    vars:
      prompt: "用户输入"
    assert:
      - type: tool_called           # contains | regex | javascript | tool_called | param-extract | llm-rubric
        value: "<skill-name>"
    assert_fail:                    # 可选：匹配任一条则立即 FAIL
      - type: contains
        value: "<adjacent-skill>"
    tags: [positive]
```

### 断言类型速查

| 类型 | 适用层 | 说明 |
|------|--------|------|
| `contains` / `regex` / `javascript` | L1 | 确定性字符串匹配 |
| `tool_called` | L1 | 路由 JSON 的 skill 字段精确匹配 |
| `param-extract` | L2 | 路由 JSON 中提取的参数包含期望 KV |
| `llm-rubric` | L3/L4 | LLM Judge 评分（需 `threshold` 字段） |

---

## §7 路由集成

新 Skill 加入后必须更新 `_shared/skill-routing.md`，确保路由引擎能正确仲裁：

### 必须更新

1. **层级图**（§一）：在对应层级中添加新 Skill 名称
2. **路由决策表**（§二）：添加一行 `用户意图信号 → 主 Skill → 辅助 Skill`
3. **转交信号表**（§四）：添加与新 Skill 相关的转交条目

### 按需更新

4. **冲突仲裁**（§五）：如果新 Skill 与已有 Skill 存在潜在触发词重叠，添加仲裁规则
5. **多 Skill 组合模式**（§三）：如果新 Skill 可与其他 Skill 组合使用，添加编排示例

### 仲裁规则设计原则

- 横切层优先于水平层优先于垂直层
- 垂直层替代（非叠加）水平层执行
- 触发词重叠时，更具体的 Skill 优先（如 device-management 优先于 sdk-core）

---

## §8 发布 checklist

MR 合入前逐项检查。CI 卡点项不通过则 pipeline 失败。

### 必须项（CI 卡点）

- [ ] `SKILL.md` frontmatter 完整（name / description / version / author）
- [ ] `description` 包含中文触发词
- [ ] "何时触发"与"不触发"边界清晰，无交叉
- [ ] `eval/routing.yaml` 存在且 ≥ 5 正向 + 3 负向用例
- [ ] `npm run eval -- run --level L1 --skill <name>` pass rate ≥ `min_pass_rate`
- [ ] 决策树末端指向具体 reference 文件路径（非模糊引用）

### 必须项（人工 Review）

- [ ] `_shared/eval/cross-routing.yaml` 补充 ≥ 2 条与相邻 Skill 的仲裁用例
- [ ] `_shared/skill-routing.md` 更新：层级图 + 路由决策表 + 转交信号表
- [ ] `skills/README.md` Skill 列表新增一行
- [ ] `skills/CHANGELOG.md` 新增条目

### 推荐项

- [ ] `constraints.yaml` 定义领域特有约束（critical ≥ 1 条）
- [ ] L2 `eval/param.yaml` 用例（如果 Skill 需要参数提取）
- [ ] L3 `eval/quality.yaml` 用例（≥ 2 条）
- [ ] `references/` 下至少 1 份参考文档

---

## §9 CLI 快速上手

> 需要先安装 skill-eval（`cd skill-eval && npm install`）。

### 生成新 Skill 骨架

```bash
npm run eval -- scaffold <skill-name>

# 指定层级（默认 vertical）
npm run eval -- scaffold <skill-name> --layer horizontal

# 预览生成文件（不写入磁盘）
npm run eval -- scaffold <skill-name> --dry-run
```

生成结果：

```
skills/<skill-name>/
├── SKILL.md              ← frontmatter + 7 个必须章节骨架
├── README.md             ← 标准 README 骨架
├── constraints.yaml      ← extends 声明 + 示例规则
├── eval/
│   ├── routing.yaml      ← 3 正向 + 2 负向占位（填入真实 prompt 即可）
│   ├── param.yaml        ← 1 个占位用例
│   ├── quality.yaml      ← 1 个占位用例
│   └── trajectory.yaml   ← 1 个占位用例
├── references/.gitkeep
└── snippets/.gitkeep
```

### 验证路由

```bash
# 跑 L1 路由评测
npm run eval -- run --level L1 --skill <skill-name>

# 扫描发现的用例（不执行）
npm run eval -- scan --skill <skill-name>
```

### 收集 bad case

```bash
# 生产中发现路由错误时，一键报告
npm run eval -- collect --prompt "用户输入" --expected <skill-name>
```
