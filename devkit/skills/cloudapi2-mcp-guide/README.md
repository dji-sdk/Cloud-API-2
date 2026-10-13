# DJI CloudAPI2 MCP Guide

帮助 AI Agent 在运行时正确选择和编排 DJI CloudAPI2 MCP Tool 调用。

## 与 cloudapi2-sdk-core 的关系

| 维度 | cloudapi2-mcp-guide（本 Skill） | cloudapi2-sdk-core |
|------|--------------------------|-------------------|
| 做什么 | 教 AI 选对工具、编排多步调用 | 教 AI 写正确的 Java 集成代码 |
| 输出物 | MCP Tool 调用决策 | Java 源代码 |
| 触发条件 | 用户要求操控设备/执行任务 | 用户要求编写 SDK 集成代码 |
| 需要 MCP 连接 | 是 | 不需要 |
| 适用平台 | Claude Code / 任何 MCP 客户端 | Claude Code / Cursor / Copilot |

两者互补，不冲突。同一对话中可能两个 skill 同时生效：
- 用户说"帮我创建个航线任务" → 本 skill（通过 MCP Tool 操作）
- 用户说"帮我写个航线任务管理的 Service 类" → cloudapi2-sdk-core（生成 Java 代码）

## 目录结构

```
cloudapi2-mcp-guide/
├── SKILL.md                          ← 主文件：三问决策法 + 歧义信号 + 工具分类
├── references/
│   ├── tool-routing.md               ← 通用路由规则 + 反模式
│   └── workflows/
│       └── flight-task.md            ← 航线任务决策树 + 参数速查
├── snippets/
│   └── flight-task-scenarios.md      ← A/B/C/D 四种场景 few-shot
└── README.md                         ← 本文件
```

## 扩展计划

| 工作流 | 文件 | 状态 |
|--------|------|------|
| 航线任务 | `references/workflows/flight-task.md` | 已实现 |
| 设备控制 | `references/workflows/device-control.md` | 计划中 |
| 直播管理 | `references/workflows/livestream.md` | 计划中 |
| 固件升级 | `references/workflows/firmware.md` | 计划中 |

## 安装

推荐通过 `devkit@cloud-api2` Plugin 一键安装全套 Skill（见 [skills/README.md](../README.md#安装方式)）。

单独安装本 Skill：

```bash
# Claude Code（放入项目级或用户级 skill 目录）
cp -r skills/cloudapi2-mcp-guide ~/.claude/skills/

# 或者保持在仓库内，Claude Code 会自动发现 skills/ 下的 skill
```

## 前提条件

本 skill 生效的前提是 DJI CloudAPI2 MCP Server 已连接：
- MCP Server 正在运行（stdio 或 SSE 模式）
- Claude Code 已配置 MCP 连接（见 `mcp/` 目录的 README）
- 至少有一个 `fh_*` 工具可用
