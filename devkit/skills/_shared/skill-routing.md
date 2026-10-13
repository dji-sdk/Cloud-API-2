# Skill 路由与编排规则

> 本文件定义 cloudapi2-* Skill 在**同时匹配**时的仲裁、组合和转交规则。
> 各 Skill 的 SKILL.md 应引用本文件：`路由规则见 skills/_shared/skill-routing.md`。

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
│  device-management / sei-osd / skeleton-deploy / mcp-deploy   │
└──────────────────────────────────────────────────────────────┘
```

### 仲裁规则

1. **横切层优先**：用户消息包含反馈/提 issue 信号时，`feedback` 先介入；包含报错/错误码/排障信号时，`troubleshooting` 先介入，定位问题归属后再路由到具体 Skill
2. **水平层互斥**：sdk-core（写 Java 代码）、frontend-component（写前端代码）、mcp-guide（选 MCP 工具）三者按输出物类型互斥，不会同时执行
3. **垂直层从属**：device-management 和 sei-osd 是 sdk-core 的场景细化，匹配时**替代** sdk-core 执行（更精准的约束和参考文档）
4. **层间可组合**：垂直层可与前端层组合（如 sei-osd + frontend-component = 后端 SEI 获取 + 前端 Cesium 渲染）

---

## 二、路由决策表

| 用户意图信号 | 主 Skill | 辅助 Skill（按需加载 reference） |
|---|---|---|
| 写 Java SDK 集成代码（通用） | sdk-core | — |
| 写设备管理/OSD 订阅/HMS 代码 | device-management | sdk-core（核心规则兜底） |
| 写 SEI/OSD 遥测获取代码 | sei-osd | sdk-core（核心规则兜底） |
| 接入前端 PaaS 组件（Vue/HTML） | frontend-component | — |
| 选 MCP 工具 / 编排调用链 | mcp-guide | — |
| 报错 / 错误码 / 排障 | troubleshooting | 定位后路由到对应 Skill |
| **用户要反馈问题 / 提 issue**（"我要反馈一个问题"/"报个 bug"/"提个 issue"） | feedback | 走 `diag.sh session start` 闭环；**不要当成需求开发转交垂直层**；发现有错误码时转交 troubleshooting |
| **设备无法直播** / 直播黑屏 / 设备离线 `212015` / EA340 / EA610 | troubleshooting | 走 `references/live-stream-device-troubleshooting.md`（固件 → 上云方式 → 底座 `.env` → 端口，通常非应用代码问题） |
| **组合场景**：后端 + 前端 | sei-osd 或 device-management | frontend-component |
| 部署 MCP Server / Docker 部署 MCP / MCP 接入 | mcp-deploy | — |
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
| sdk-core | 用户问 MCP 工具选择 / fh_* 调用 | mcp-guide |
| mcp-guide | 用户要写 Java 代码而非调 MCP 工具 | sdk-core 或垂直层 Skill |
| device-management | 用户问前端页面 / Vue 组件 / Cesium 可视化 | frontend-component |
| sei-osd | 用户问前端 Cesium 渲染 / FOV 可视化 | frontend-component（辅助） |
| frontend-component | 用户问后端 SDK 调用 / Java 代码 | sdk-core 或垂直层 Skill |
| 任意 Skill | 用户报错 / 错误码 / "不工作" / "白屏" | troubleshooting（先介入） |
| 任意 Skill | 用户说"我要反馈一个问题 / 报个 bug / 提个 issue"（**无错误码也算**） | feedback → `diag.sh session start` 闭环（**不要当成功能需求转交垂直层**；发现有错误码时由 feedback 转交 troubleshooting） |
| 任意 Skill | 用户说"直播打不开 / 直播黑屏 / 设备离线 `212015` / EA340 / EA610" | troubleshooting → `references/live-stream-device-troubleshooting.md`（**不要转给写代码的 Skill**：此类根因通常在固件/上云方式/底座 `.env`/端口，改应用代码无效） |
| 任意 Skill | 用户说"部署 MCP"/"安装 MCP"/"MCP deploy" | mcp-deploy |
| 任意 Skill | 用户说"部署 skeleton"/"安装 skeleton" | skeleton-deploy |
| 任意 Skill | 用户说"加到知识库"/"入库"/"收录" | knowledge-ingest |

---

## 五、冲突仲裁

### 场景 1：sdk-core vs 垂直层 Skill

当用户意图同时匹配 sdk-core 和 device-management / sei-osd 时：

- **优先垂直层**：垂直层有更精准的约束、参考文档和代码模板
- **sdk-core 兜底**：垂直层 `extends` 共享约束（含 sdk-core 6 条核心规则），无需回退到 sdk-core

### 场景 2：前端 vs 后端

用户描述中同时包含前端和后端诉求：

- **不要合并为一个 Skill 回答**：分段处理，先后端后前端（或反之），明确标注切换点
- **数据契约先行**：先定义后端推送给前端的数据格式（WebSocket JSON schema），再分别实现两端

### 场景 3：mcp-guide vs sdk-core

用户说"帮我控制无人机"——可能是"用 MCP 工具直接操作"也可能是"写 Java 代码"：

- 如果用户**在 MCP 连接的会话中**（有 fh_* 工具可用）→ mcp-guide
- 如果用户**在开发 IDE 中写代码** → sdk-core 或垂直层 Skill
- 如果不确定 → 问："你是想直接操控设备（通过 MCP 工具），还是写 Java 代码集成到你的项目中？"

---

## 六、各 Skill 引用本文件的方式

在 SKILL.md 的"不触发"或"决策树"段后加入：

```markdown
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。
> 当发现用户意图属于其他 Skill 职责时，按转交协议切换。
```
