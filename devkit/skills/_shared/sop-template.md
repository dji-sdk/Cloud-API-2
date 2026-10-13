# SOP 模板 — ISV 接入全流程骨架

> 本文件定义所有业务 Skill 共享的 **7 章节 SOP** 和 **3 档执行深度**。
> 各 Skill 的 SKILL.md 通过 `参考 skills/_shared/sop-template.md` 引用本文件，无需重复描述流程。

---

## 与 superpowers 协作规则

本 SOP 专注于 **DJI SDK 集成执行流程**（领域知识 + 编码约束），不覆盖通用设计方法论。

**运行时自适应**：
- 如果 `superpowers:brainstorming` 已在本次对话加载：本 SOP 从 Ch2（澄清）开始执行，设计探索阶段交由 brainstorming 完成
- 如果 `superpowers:brainstorming` 未加载：本 SOP 完整执行所有章节，但面对开放式架构设计问题时，主动提示用户"建议安装 superpowers 插件以获得更完整的设计思考支持"

**职责边界**：
- superpowers 负责：发散思考、多方案对比、架构决策
- 本 SOP 负责：DJI SDK 约束检查、代码模板、listen/cancel 配对、安全等级校验

---

## 执行深度定义

| 档位 | 名称 | 自动选档条件 | 典型场景 |
|------|------|-------------|----------|
| **L1** | 极简 | 用户问"怎么做 X"且无需修改代码 | 快问快答、API 查询、错误码解释 |
| **L2** | 标准 | 用户要求添加/修改一个功能点 | 加功能、修 bug、单文件改动 |
| **L3** | 完整 | 涉及多模块/多文件改动，或用户明确说"完整方案" | 做完整方案、重构、新建 feature 模块 |

### 选档规则

```
IF 用户问题是纯知识查询 → L1（跳到 Ch1 直接回答）
ELIF 改动范围 ≤ 3 个文件 AND 不涉及新模块 → L2
ELIF 改动范围 > 3 个文件 OR 新建模块 OR 用户说"完整方案/全面/重构" → L3
```

---

## 7 章节骨架

### Ch1: 理解（Understand）

**触发**：每次对话开始。

**L1 产出**：直接回答问题，附参考出处。  
**L2 产出**：一句话复述用户意图 + 标注涉及的 SDK 模块/组件。  
**L3 产出**：书面需求理解 + 影响面分析（涉及哪些模块、文件、接口）。

---

### Ch2: 澄清（Clarify）

**触发**：用户意图存在歧义，或缺少必要上下文。

**L1**：无需澄清（已在 Ch1 完成）。  
**L2**：2-3 个定向问题锁定实现细节。  
**L3**：结构化澄清清单（what/why/who/when/constraints），必须等用户确认。

**典型澄清问题模板**：
- "你希望在 skeleton 中新建 feature 模块，还是在已有模块中添加？"
- "这个页面需要支持哪些 FH2 组件？驾驶舱 / 航线 / 回放？"
- "目标是本地调试还是生产部署？"

---

### Ch3: Spec（规格化）

**触发**：L3 必须执行；L2 可选（复杂改动时建议）。

**L2**：口头确认实现方案（不产出文件）。  
**L3 产出**：`spec.yaml` 或等效结构化文档，包含：
- 目标状态描述
- 接口契约（输入/输出/错误码）
- 非功能约束（并发、性能、安全等级）
- 验收标准

---

### Ch4: 分解（Decompose）

**触发**：L2 改动涉及 2+ 步骤时；L3 必须执行。

**L2 产出**：编号步骤列表（1-5 步）。  
**L3 产出**：结构化任务列表，含依赖关系和验收标准。

**分解原则**：
- 每步可独立验证
- 先改被依赖方（SPI/接口），后改消费方（feature/controller）
- 先写测试骨架，后填实现

---

### Ch5: 编码（Implement）

**触发**：用户确认方案后。

**L1**：不编码，只输出代码片段供参考。  
**L2**：直接生成代码，遵守业务 Skill 的 constraints.yaml。  
**L3**：按 Ch4 任务逐步实现，每步完成后检查约束。

**编码铁律**：
1. 必须遵守当前 Skill 的 `constraints.yaml` 所有 `critical` 规则
2. 生成代码必须与 skeleton 现有模式一致（包名、注解、命名）
3. 新增公共类必须同步生成测试类骨架

---

### Ch6: 验证（Verify）

**触发**：编码完成后。

| 档位 | 验证深度 |
|------|----------|
| L1 | 无（知识回答无需验证） |
| L2 | 编译通过 + 冒烟验证（关键路径手动确认） |
| L3 | 强制 TDD — 先写测试再实现；全量测试通过 + 覆盖率达标 |

**验证检查清单**：
- [ ] `mvn compile` 通过（后端）/ `npm run build` 通过（前端）
- [ ] constraints.yaml 中所有 `critical` 规则已满足
- [ ] listen/cancel 配对检查（如涉及 SDK 订阅）
- [ ] null guard 检查（如涉及 `getInstance(sn)` 调用）
- [ ] 资源释放检查（`@PreDestroy` / `onUnmounted` / `destroy()`）

---

### Ch7: Memory（经验沉淀）

**触发**：任务完成后，且过程中有值得记录的经验。

**规则**：
- 修 bug 成功 → 询问用户是否记录到 auto memory（type: `feedback`）
- 发现文档未覆盖的 SDK 行为 → 记录到 auto memory（type: `project`）
- 新的集成模式（不在 skeleton 示例中）→ 记录到 auto memory（type: `project`）

> Memory 路径由 Claude Code auto memory 系统自动管理（`~/.claude/projects/*/memory/`），无需手动创建目录。

**记录格式**（符合 auto memory frontmatter 规范）：
```markdown
---
name: {kebab-case-slug，如 error-4001-token-expired}
description: {一句话描述问题和修复方案}
metadata:
  type: feedback
---

- **场景**：{触发条件}
- **根因**：{问题原因}
- **方案**：{解决方法}
- **约束**：{需要注意的限制}
```

---

## 各 Skill 如何引用本模板

在 SKILL.md 的工作流部分加入：

```markdown
## 工作流

本 Skill 遵循 `skills/_shared/sop-template.md` 定义的 7 章节 SOP。
根据用户请求自动选择执行深度（L1/L2/L3）。

以下为本 Skill 的特化规则：
- Ch2 澄清问题：{列出 2-3 个领域特定问题}
- Ch5 编码约束：参见 constraints.yaml
- Ch6 验证扩展：{列出领域特定的验证项}
```

---

## 档位快速参考

```
L1 极简: Ch1 → 直接回答
L2 标准: Ch1 → Ch2(可选) → Ch4(可选) → Ch5 → Ch6
L3 完整: Ch1 → Ch2 → Ch3 → Ch4 → Ch5 → Ch6 → Ch7
```
