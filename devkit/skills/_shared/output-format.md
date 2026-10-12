# 输出格式标准

> 本文件定义所有 cloudapi2-* Skill 响应时的统一输出结构。
> 各 Skill 的 SKILL.md 应引用本文件：`输出格式见 skills/_shared/output-format.md`。

---

## 一、通用响应结构

所有 Skill 的输出（L2/L3 档位）应包含以下段落，按需组装：

```
1. [可选] 意图确认      — 一句话复述理解
2. [必须] 主体内容      — 代码/方案/诊断结果
3. [必须] 约束提醒      — 涉及的 critical 约束
4. [可选] 引用来源      — 参考文档溯源
5. [必须] 下一步建议    — 用户接下来该做什么
```

L1（极简）档位只输出主体内容 + 引用来源，不需要完整结构。

---

## 二、约束提醒段

当生成代码或给出方案时，**必须**在输出末尾附带涉及的 critical 约束提醒：

```markdown
> **约束检查**
> - ✅ `listen-cancel-pairing`：订阅已配对 cancel（@PreDestroy）
> - ✅ `null-guard-instances`：getDroneInstance 返回值已判空
> - ⚠️ `thread-safety`：回调中操作共享状态，请确认使用 ConcurrentHashMap
```

**规则**：
- 只列出与本次输出**实际相关**的约束，不要列全部
- 已满足的用 ✅，需用户注意的用 ⚠️
- 约束 ID 直接引用 `constraints-common.yaml` 或对应 Skill 的 `constraints.yaml`

---

## 三、引用来源格式

引用 Skill 内部文档时使用统一格式：

```
[来源: references/xxx.md#章节名]
```

示例：
- `[来源: references/server-side-guide.md#SEI订阅生命周期]`
- `[来源: references/wayline-editor.md#初始化参数]`
- `[来源: error-codes.yaml#514004]`
- `[来源: constraints-common.yaml#flight-control-authority]`

**规则**：
- 代码示例的来源必须可追溯，不得凭模型记忆生成 API
- 如果信息来自用户提供的文档（非 Skill 内置），标注 `[来源: 用户提供]`
- 如果无法确认来源，必须标注 `[需验证: 此 API 签名需确认 SDK 文档]`

---

## 四、置信度信号

区分"已确认"和"需验证"的输出：

| 信号 | 含义 | 使用场景 |
|------|------|----------|
| 无标注 | 已确认准确 | 信息来自 Skill references/ 且未标注已知局限 |
| `[需验证]` | 需用户确认 SDK 文档 | API 签名/参数/枚举值不在 references/ 中 |
| `[已知局限]` | 参考文档存在取舍 | 信息来自标注了"已知局限"的 reference |

**反幻觉铁律**（继承自 sdk-core 规则）：
- 不确定某个 API 是否存在 → 标注 `[需验证]` 而非编造
- 代码模板来自 references/ → 可直接输出
- 代码涉及 references/ 未覆盖的 API → 明确告知需查阅 SDK 文档

---

## 五、下一步建议

每次输出结尾给出明确的下一步建议，格式：

```markdown
**下一步**：
1. 将上述代码添加到 `feature/feature-xxx/src/main/java/...`
2. 运行 `mvn compile` 确认编译通过
3. 如需前端可视化，告诉我目标页面位置
```

**规则**：
- 建议必须具体可执行（不要"请自行测试"）
- 如果涉及跨 Skill 的后续工作，明确说明（见 `skill-routing.md` 转交协议）
- 如果任务已完成，建议验证步骤

---

## 六、各 Skill 特化格式

| Skill | 主体内容特化 |
|---|---|
| sdk-core | Java 代码块 + 对应核心规则编号（`// 规则 3：listenId 配对`） |
| device-management | 模块结构树 + Java 代码 + AutoConfiguration 注册说明 |
| frontend-component | HTML/Vue 代码 + 加载顺序示意 + 地址占位符说明 |
| mcp-guide | 工具调用链（步骤编号） + 参数表 + 前置条件检查 |
| sei-osd | 数据选型建议（三层对比表） + 代码 + 数据结构速查 |
| troubleshooting | 4 阶段标题（Reproduce/Locate/Fix/Verify） + 当前阶段标注 |

---

## 七、Reference 加载策略

**核心原则**：按需懒加载，不要一次性读取所有 references/。

```
用户消息 → SKILL.md 决策树匹配 → 确定需要的 1-2 个 reference → 加载 → 输出
```

**加载优先级**：
1. 决策树路由中明确指定的 reference（必须加载）
2. constraints.yaml（生成代码时必须检查）
3. 其他 reference（仅在用户追问或 L3 深度时加载）

**禁止行为**：
- ❌ 一次性加载 skills/cloudapi2-xxx/references/ 下所有文件
- ❌ 加载与当前问题无关的 reference（浪费上下文窗口）
- ✅ 按决策树路由，只加载匹配分支所需的文件
