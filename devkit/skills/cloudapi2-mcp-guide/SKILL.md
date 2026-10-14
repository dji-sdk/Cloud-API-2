---
name: cloudapi2-mcp-guide
description: |
  仅在 AI Agent 通过 MCP 连接 DJI CloudAPI2 Server 并需要调用 fh_* 工具时使用。
  帮助 AI 判断该用哪个 MCP Tool、以什么参数调用、以什么顺序编排多步工具链。
  不适用于编写 Java SDK 集成代码（那是 cloudapi2-sdk-core skill 的职责）。
  适用场景：用户要求查看设备、创建航线任务、控制无人机、查询遥测数据——
  这些操作通过 MCP Tool 直接完成，无需写代码。
  关键词：fh_flight_task, fh_workspace, fh_wayline_upload, fh_wayline_execute,
  fh_device, fh_drone_control, fh_livestream, fh_connect, MCP, tool,
  创建任务, 上传航线, 执行航线, 设备列表, 遥测, KMZ, wayline_uuid,
  drone_sn, dock_sn, 选哪个工具, which tool
author: DJI CloudAPI2 Team
version: 1.1.0
---

# DJI CloudAPI2 MCP Tool 路由指南

## 适用场景

当 AI Agent 已通过 MCP 连接到 DJI CloudAPI2 Server，用户要求执行以下操作时触发：

- 查看设备列表 / 设备状态
- 创建、执行、暂停、取消航线任务
- 上传航线文件（本地 KMZ 或远程 URL）
- 控制无人机（起飞、返航、急停）
- 开始 / 停止直播
- 查询遥测数据

## 与 cloudapi2-sdk-core Skill 的区别

| 维度 | cloudapi2-mcp-guide（本 Skill） | cloudapi2-sdk-core |
|------|--------------------------|-------------------|
| 做什么 | 教 AI **选对工具、编排调用** | 教 AI **写正确的 Java 代码** |
| 输出物 | MCP Tool JSON-RPC 调用 | Java 源代码 |
| 消费者 | AI Agent（运行时决策） | ISV 开发者（开发时） |
| 需要 MCP 连接 | 是 | 不需要 |
| 适用平台 | Claude Code / 任何 MCP 客户端 | Claude Code / Cursor / Copilot |

## 三问决策法

在调用任何 `fh_*` 工具前，先回答：

1. **用户手里有什么？**（wayline_uuid、本地文件、URL、设备 SN、什么都没有）
2. **用户想做什么？**（查询、创建、执行、监控、控制）
3. **目标工具的前置条件满足了吗？**（如果不满足 → 先找能产出前置数据的工具）

## 歧义信号表

| 用户输入信号 | 可能含义 | 处理方式 |
|---|---|---|
| UUID 格式 (8-4-4-4-12) | 可能是 task_uuid 或 wayline_uuid | 从上下文判断或询问用户 |
| 文件路径 (`/path/to/xxx.kmz`) | 本地 KMZ 文件 | → `fh_workspace(action=submit)` |
| URL (`https://...xxx.kmz`) | 远程 KMZ 文件 | → `fh_wayline_upload` |
| SN 含 "DOCK" 或机场型号前缀 | Dock SN | 用于 `fh_flight_task` |
| SN 含无人机型号前缀 | Drone SN | 用于 `fh_wayline_upload` 或 DRC 工具 |

## 工具分类速查

| 用户意图 | 首选工具 |
|---|---|
| "查看/列出/有哪些..." | `fh_workspace(list)`, `fh_flight_task(list)`, `fh_device(list)` |
| "创建/新建/飞一下..." | 见 `references/workflows/flight-task.md` 决策树 |
| "执行/开始飞..." | `fh_flight_task(execute)` 或 `fh_wayline_execute` |
| "停/暂停/取消..." | `fh_wayline_control(action=pause/cancel)` |
| "状态/进度..." | `fh_flight_task(detail)` 或 `fh_wayline_status` |

## 反模式

- 没有前置数据就调工具（如无 wayline_uuid 直接 `fh_flight_task(create)`）
- 猜测 UUID 类型——必须从上下文或工具返回值确认
- drone SN 和 dock SN 用反（`fh_flight_task` 要 dock SN，`fh_wayline_upload` 要 drone SN）
- 用户没指定目标就跳过 list 步骤直接操作

## 决策树路由

```
用户操作请求
├─ 航线任务相关（创建/执行/上传）
│    → 加载 references/workflows/flight-task.md
├─ 设备控制（起飞/返航/急停）
│    → 暂未实现独立工作流文档，参考 `fh_control_acquire → fh_drc_connect → fh_takeoff/fh_flyto/fh_return_home → fh_drc_disconnect → fh_control_release` 标准链路
├─ 直播相关
│    → 暂未实现独立工作流文档，使用 `fh_live_start(action=start)` / `fh_live_start(action=stop)` 即可
└─ 固件/OTA
     → 暂未实现独立工作流文档，使用 `fh_firmware(action=check_status)` → `fh_firmware(action=upgrade)` 链路
```

> 当前仅 flight-task 工作流有完整文档，其余提供内联调用链指引。如需详细文档可向团队反馈。

## 响应中的 next_steps

MCP Server 的工具返回值可能包含 `next_steps` 字段，格式如下：

```json
{
  "next_steps": [
    {
      "tool": "fh_flight_task",
      "action": "execute",
      "params_hint": { "task_uuid": "xxx", "confirm": true },
      "description": "Execute the created task"
    }
  ]
}
```

**当看到 next_steps 时**：优先参考 next_steps 的建议选择下一个工具，但仍需结合用户意图判断是否跟随。

## 共享规范

> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载，不要一次性读取所有 references/。
