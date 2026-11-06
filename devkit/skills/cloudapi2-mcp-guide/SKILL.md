---
name: cloudapi2-mcp-guide
description: |
  仅在 AI Agent 通过 MCP 连接 DJI Cloud API 2 并需要调用 fh_* 工具时使用。
  帮助 AI 判断该用哪个 MCP Tool、以什么参数调用、以什么顺序编排多步工具链。
  不适用于编写 Java SDK 集成代码（那是 cloudapi2-sdk-core skill 的职责）。
  适用场景：用户要求查看设备、创建航线任务、控制无人机、查询遥测数据——
  这些操作通过 MCP Tool 直接完成，无需写代码。
  关键词：fh_flight_task, fh_workspace, fh_wayline_upload, fh_wayline_execute,
  fh_device, fh_drone_control, fh_livestream, fh_connect, MCP, tool,
  创建任务, 上传航线, 执行航线, 设备列表, 遥测, KMZ, wayline_uuid,
  drone_sn, dock_sn, 选哪个工具, which tool,
  fh_flight_area_manage, fh_flight_area_list, fh_flight_area_sync, fh_offline_map, fh_terra,
  禁飞区, 限飞区, NFZ, 飞行区, 离线地图, 飞安数据库, 三维重建, Terra建模, 全景图, 建模任务,
  fh_device_list, fh_device_manage, fh_dock_control, fh_dock_snapshot, fh_drone_snapshot,
  fh_device_debug, fh_firmware, fh_maintenance, fh_hms, fh_log, fh_session_info, fh_ops,
  设备详情, 重命名, 解绑, 舱盖, 充电, 补光灯, 格式化, 重启, RTK校准, 中继, 固件升级, 固件发布,
  维修工单, 健康告警, HMS, 日志报告, 会话信息, 关闭服务,
  fh_control_acquire, fh_control_release, fh_drc_connect, fh_drc_disconnect, fh_takeoff, fh_flyto,
  fh_return_home, fh_poi, fh_emergency_stop, fh_payload_acquire, fh_payload_release, fh_stick_move,
  fh_osd_snapshot, fh_device_recommend, fh_wayline_recommend,
  急停, 绕点飞行, 摇杆, 云台授权, 智能选机, 航线推荐,
  fh_media_list, fh_media_download, fh_media_delete, fh_media_record,
  媒体文件, 照片列表, 视频列表, 下载媒体, 删除媒体, 直播录制,
  fh_camera, fh_psdk_widget, fh_speaker, fh_searchlight,
  拍照, 录像, 变焦, 云台旋转, PSDK, 喊话, 喊话器, 播放音频, 探照灯, 探照灯模式,
  fh_wayline_control, fh_wayline_execute, fh_wayline_status, fh_flight_record,
  暂停航线, 恢复航线, 取消航线, 航线进度, 飞行记录导出,
  fh_flysafe, fh_tcas, fh_unlock_license,
  空域解禁, 解禁证书, 防撞, TCAS, 飞安数据库升级,
  fh_diagnosis, 自诊断, 诊断报告, 设备诊断,
  fh_connect, 会话连接, 建立连接, 连接DJI Cloud API 2
author: DJI Cloud API 2 Team
version: 1.5.0
---

# DJI Cloud API 2 MCP Tool 路由指南

## 适用场景

当 AI Agent 已通过 MCP 连接到 DJI Cloud API 2，用户要求执行以下操作时触发：

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
| 消费者 | AI Agent（运行时决策） | 集成商开发者（开发时） |
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
| "查看/列出/有哪些..." | `fh_workspace(list)`, `fh_flight_task(list)`, `fh_device_list` |
| "创建/新建/飞一下..." | 见 `references/workflows/flight-task.md` 决策树 |
| "执行/开始飞..." | `fh_flight_task(execute)` 或 `fh_wayline_execute` |
| "停/暂停/取消..." | `fh_wayline_control(action=pause/cancel)` |
| "状态/进度..." | `fh_flight_task(detail)` 或 `fh_wayline_status` |
| "禁飞区/限飞区/飞行区同步..." | 见 `references/workflows/safety-service.md` 决策树 |
| "三维重建/全景图/建模进度..." | 见 `references/workflows/terra-reconstruction.md` 决策树 |
| "设备列表/机场控制/遥测快照..." | 见 `references/workflows/device-query-control.md` 决策树 |
| "格式化/重启/RTK校准/中继..." | 见 `references/workflows/device-debug.md` 决策树 |
| "固件升级/固件发布..." | 见 `references/workflows/firmware.md` 决策树 |
| "维修工单/HMS告警/日志报告/关闭服务/设备诊断/建立会话连接..." | 见 `references/workflows/ops-maintenance.md` 决策树 |
| "起飞/返航/绕点飞行/急停/摇杆/云台授权/智能选机/航线推荐..." | 见 `references/workflows/device-control.md` 决策树 |
| "暂停/恢复/取消航线/查航线进度/导出飞行记录..." | 见 `references/workflows/flight-task.md`"航线执行生命周期"一节 |
| "空域解禁/TCAS防撞/飞安数据库升级..." | 见 `references/workflows/safety-service.md`"空域合规"一节 |
| "照片列表/下载媒体/删除媒体/直播录制..." | 见 `references/workflows/media-management.md` 决策树 |
| "拍照/录像/变焦/云台旋转/喊话/探照灯/PSDK..." | 见 `references/workflows/payload-control.md` 决策树 |

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
├─ 禁飞区/限飞区/离线地图/飞行区同步
│    → 加载 references/workflows/safety-service.md
├─ 三维重建/全景图/建模进度/模型管理
│    → 加载 references/workflows/terra-reconstruction.md
├─ 设备查询/机场控制/遥测快照（列表/详情/重命名/解绑/舱盖/充电/补光灯）
│    → 加载 references/workflows/device-query-control.md
├─ 设备调试高危操作（格式化/重启/RTK校准/网络诊断/中继/SIM切换）
│    → 加载 references/workflows/device-debug.md
├─ 固件升级/固件发布
│    → 加载 references/workflows/firmware.md
├─ 维修工单/HMS健康告警/日志报告/会话信息/MCP Server运维/设备自诊断/建立会话连接
│    → 加载 references/workflows/ops-maintenance.md
├─ 设备控制（起飞/返航/绕点飞行/急停/摇杆/云台授权/智能选机/航线推荐）
│    → 加载 references/workflows/device-control.md
├─ 媒体文件管理（照片/视频列表/下载/删除/直播录制）
│    → 加载 references/workflows/media-management.md
├─ 负载/配件控制（相机云台/PSDK/喊话器/探照灯）
│    → 加载 references/workflows/payload-control.md
└─ 直播相关
     → 暂未实现独立工作流文档，使用 `fh_live_start(action=start)` / `fh_live_start(action=stop)` 即可
```

> 当前 flight-task / safety-service / terra-reconstruction / device-query-control / device-debug / firmware / ops-maintenance / device-control / media-management / payload-control 工作流有完整文档，其余提供内联调用链指引。如需详细文档可向团队反馈。

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
