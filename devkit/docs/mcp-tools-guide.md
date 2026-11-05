# DJI Cloud API 2 MCP 工具使用手册

本文档详细说明每个 MCP 工具的参数、返回值、调用顺序和注意事项。

> **范围声明**：本手册只**逐工具详解高频子集（约 21 个工具）**。MCP Server 当前共有 67 个 `fh_*` 工具（另有 4 个 `sim_*` 模拟器工具），全量清单见 → **[工具版本与更新指南](../mcp/docs/tool-versioning.md)**，或启动服务后通过 `tools/list` 查询。

---

## 目录

- [前置条件](#前置条件)
- [调用流程](#调用流程)
- [工具详细说明](#工具详细说明)
  - [fh_connect — 连接平台](#fh_connect)
  - [fh_session_info — 查询会话](#fh_session_info)
  - [fh_ops — 运维操作](#fh_ops)
  - [fh_device_list — 设备列表](#fh_device_list)
  - [fh_drone_snapshot — 无人机快照](#fh_drone_snapshot)
  - [fh_dock_snapshot — 机场快照](#fh_dock_snapshot)
  - [fh_dock_control — 机场控制](#fh_dock_control)
  - [fh_device_manage — 设备管理](#fh_device_manage)
- [实时指挥（DRC）](#实时指挥drc)
  - [fh_control_acquire — 获取飞行控制权](#fh_control_acquire)
  - [fh_control_release — 释放飞行控制权](#fh_control_release)
  - [fh_drc_connect — 建立 DRC 链路](#fh_drc_connect)
  - [fh_drc_disconnect — 断开 DRC 链路](#fh_drc_disconnect)
  - [fh_takeoff — 一键起飞](#fh_takeoff)
  - [fh_return_home — 返航](#fh_return_home)
  - [fh_flyto — 指点飞行](#fh_flyto)
  - [fh_poi — 环绕飞行](#fh_poi)
  - [fh_emergency_stop — 紧急停止](#fh_emergency_stop)
  - [fh_payload_acquire / fh_payload_release — 负载控制权](#fh_payload)
  - [fh_live_start — 直播推流](#fh_live_start)
  - [fh_osd_snapshot — OSD 遥测快照](#fh_osd_snapshot)
  - [fh_stick_move — 受限摇杆控制](#fh_stick_move)
- [安全等级机制](#安全等级机制)
- [错误处理](#错误处理)
- [典型业务场景](#典型业务场景)

---

## 前置条件

1. **JDK 17+**：MCP Server 运行在 JVM 上
2. **DJI Cloud API 2 服务器**：需要可达的 DJI Cloud API 2 服务端地址
3. **AI 工具**：支持 MCP 协议的 AI 工具（Claude Code / Cursor / Claude Desktop 等）
4. **JVM 内存限制**（本地运行必须配置）：MCP Server 与 Claude Code 共存时总内存约 1-1.5 GB，建议使用内置启动脚本：
   - stdio 模式：`cd mcp && ./run-mcp-local.sh`（`-Xmx384m`）
   - SSE 模式：`cd mcp && ./run-mcp-sse.sh`（`-Xmx512m`，读取 `.env`）
   - 不使用脚本时必须在 `java` 命令手动加 `-Xmx384m`，否则在 macOS 8 GB 系统上持续对话约 15-20 分钟后易触发 OOM Kill

---

## 调用流程

所有业务操作前必须先调用 `fh_connect` 建立会话。典型调用顺序：

```
fh_connect                    ← 必须第一步
    │
    ├── fh_device_list        ← 查看有哪些设备
    │       │
    │       ├── fh_drone_snapshot(sn)   ← 查看无人机状态
    │       └── fh_dock_snapshot(sn)    ← 查看机场状态
    │
    ├── fh_dock_control(sn, action)     ← 控制机场
    ├── fh_device_manage(sn, action)    ← 管理设备
    │
    ├── fh_control_acquire(drone_sn)    ← 获取飞行控制权 (L1)
    │       │
    │       ├── fh_drc_connect(drone_sn)         ← 建立 DRC 链路 (L1)
    │       │       │
    │       │       ├── fh_takeoff(drone_sn)              ← 一键起飞 (L2)
    │       │       ├── fh_flyto(drone_sn, ...)           ← 指点飞行 (L2)
    │       │       ├── fh_poi(drone_sn, ...)             ← 环绕飞行 (L2)
    │       │       ├── fh_return_home(drone_sn)          ← 返航 (L2)
    │       │       ├── fh_stick_move(drone_sn, ...)      ← 摇杆控制 (L2)
    │       │       ├── fh_emergency_stop(drone_sn)       ← 紧急停止 (L3)
    │       │       ├── fh_payload_acquire(drone_sn, ...)  ← 负载控制权获取 (L1)
    │       │       ├── fh_live_start(drone_sn, ...)      ← 直播推流 (L1)
    │       │       └── fh_osd_snapshot(drone_sn)         ← OSD 遥测 (L0)
    │       │
    │       └── fh_drc_disconnect(drone_sn)      ← 断开 DRC 链路 (L1)
    │
    ├── fh_control_release(drone_sn)    ← 释放飞行控制权 (L1)
    │
    ├── fh_session_info       ← 随时查看会话状态
    └── fh_ops(action=shutdown)        ← 结束时断开
```

> **关键原则**：未调用 `fh_connect` 时，其他所有工具都会返回 "No active session" 错误。

---

## 工具详细说明

<a id="fh_connect"></a>
### fh_connect — 连接 DJI Cloud API 2 平台

初始化 SDK 并建立会话。内部完成 4 步：SDK 加载 → 鉴权配置 → 会话注册 → 会话校验。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `server_address` | string | 否* | DJI Cloud API 2 服务器地址（如 `http://<your-server-ip>`）。如已配置 `FH_SERVER_ADDRESS` 环境变量则可省略，自动使用环境变量值 |
| `port` | integer | 否 | 服务器端口。如已配置 `FH_SERVER_PORT` 环境变量则可省略，默认 443 |
| `notification_server_address` | string | 否 | WebSocket 推送地址（如 `ws://<your-server-ip>:30812`）。未传入时从 `server_address` + `port` 自动推导 |

> \* 如果 MCP 配置的 `env` 块中已设置 `FH_SERVER_ADDRESS`，AI 调用 `fh_connect` 时无需再传 `server_address` 参数——Server 会自动使用环境变量值。仅当需要覆盖环境变量时才显式传入。
>
> **注意**：`FH_ACCESS_TOKEN` 和 `FH_WORKSPACE_UUID` 必须在服务**启动前**通过环境变量预设（写入 MCP 配置的 `env` 块），不支持在 `fh_connect` 调用时作为参数传入。

**返回示例**

```json
{
  "success": true,
  "data": {
    "session_id": "sess_a1b2c3d4",
    "workspace_name": "Workspace-default",
    "server_address": "http://your-fh2-server.example.com",
    "connected_devices": 0,
    "drones_online": 0,
    "docks_online": 0,
    "max_security_level": "L3",
    "status": "ready"
  },
  "message": "Successfully connected to DJI Cloud API 2. Session is ready.",
  "next_steps": [
    "Call fh_device_list to see available devices",
    "Call fh_session_info to view session details"
  ]
}
```

**注意事项**

- 如果 MCP 配置中已设置 `FH_SERVER_ADDRESS` 和 `FH_SERVER_PORT` 环境变量，AI 调用 `fh_connect()` 时**无需传任何参数**——Server 自动使用环境变量值
- 同一时刻只允许一个活跃 Session，重复调用会返回错误
- 如需重连，先调用 `fh_ops(action="shutdown")` 关闭当前会话

---

<a id="fh_session_info"></a>
### fh_session_info — 查询当前会话状态

查看当前 Session 的详细信息，包括运行时间、控制权状态、DRC 链路状态等。

**参数**：无

**返回示例**

```json
{
  "success": true,
  "data": {
    "session_id": "sess_a1b2c3d4",
    "workspace_uuid": "ws-uuid-456",
    "workspace_name": "Workspace-ws-uuid-",
    "max_security_level": "L3",
    "uptime_minutes": 15,
    "control_authority": { "active": false },
    "drc_link": { "active": false },
    "active_subscriptions": 0,
    "last_activity": "2026-06-22T14:30:00Z"
  }
}
```

---

<a id="fh_ops"></a>
### fh_ops — 运维操作

执行运维命令：健康检查、调整日志级别、关闭 MCP Server。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `action` | string | 是 | `status` / `set_log_level` / `shutdown` |
| `level` | string | 否 | 日志级别（`action=set_log_level` 时必填）：`DEBUG` / `INFO` / `WARN` / `ERROR` |

**示例调用**

```
# 健康检查
fh_ops(action="status")
→ 返回版本号、运行时间、传输模式、Session 状态

# 调整日志级别（排障时使用）
fh_ops(action="set_log_level", level="DEBUG")

# 关闭连接
fh_ops(action="shutdown")
→ 释放所有资源（控制权、DRC 链路、订阅），然后关闭 MCP Server
```

---

<a id="fh_device_list"></a>
### fh_device_list — 查询设备列表

查询当前工作空间下的所有设备，支持按类型和在线状态过滤。返回的设备拓扑包含父子关系——
顶层设备是网关类硬件（机场 `airport` / 地面站 `base_station`），挂载的子设备
（`children` 字段）才是无人机 `drone`、坞站配件 `dock`、中继 `relay`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | 否 | 设备类型过滤：`drone` / `dock` / `airport` / `base_station` / `all`（默认 `all`）。过滤时会同时匹配顶层设备和子设备 |
| `online_only` | boolean | 否 | 是否只返回在线设备（默认 `false`） |

**返回示例**

```json
{
  "success": true,
  "data": {
    "devices": [
      {
        "sn": "AIRPORT-7CQDKR1020003V",
        "name": "Dock 2",
        "type": "airport",
        "online": true,
        "model": "dock-2",
        "children": [
          {
            "sn": "DRONE-1581F6GKD241T00E40",
            "name": "Matrice 3D",
            "type": "drone",
            "online": true,
            "model": "matrice-3d"
          }
        ]
      },
      {
        "sn": "BS-4LFDK100030182",
        "name": "地面站-001",
        "type": "base_station",
        "online": false,
        "model": "base-station-t",
        "children": [
          {
            "sn": "DOCK-1581F6Q8D2400700B3",
            "name": "ea340d",
            "type": "dock",
            "online": true,
            "model": "ea340d"
          }
        ]
      }
    ],
    "total": 2,
    "filtered": 2,
    "drones_online": 1,
    "docks_online": 1,
    "airports_online": 1,
    "base_stations_online": 0
  },
  "message": "Found 2 top-level devices (2 shown after filter). 1 airports online, 0 base stations online, 1 drones online, 1 docks online."
}
```

**使用技巧**

- 用 `online_only=true` 快速筛选可操作的设备
- `children` 字段表示机场/地面站下挂载的子设备（无人机/坞站/中继），是父子关系
- `airports_online`/`base_stations_online` 统计顶层网关设备；`drones_online`/`docks_online`
  统计所有层级里对应类型的子设备——四个字段互相独立，不要假设某个数值包含另一个
- 返回的 `sn` 是后续所有工具的设备标识参数

---

<a id="fh_drone_snapshot"></a>
### fh_drone_snapshot — 获取无人机实时快照

一次性获取无人机的位置、电量、姿态、速度等实时遥测数据。内部通过短暂订阅 SDK 遥测通道实现。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-4LFDK100030182",
    "latitude": 22.5431,
    "longitude": 113.9465,
    "height": 120.5,
    "elevation": 85.2,
    "battery_percent": 87,
    "battery_landing_power": 25,
    "pitch": -2.3,
    "roll": 0.5,
    "yaw": 145.2,
    "horizontal_speed": 5.3,
    "vertical_speed": 0.0
  },
  "message": "Drone DRONE-4LFDK100030182: battery=87%, alt=120.5m"
}
```

**字段说明**

| 字段 | 含义 |
|------|------|
| `height` | 相对起飞点的高度（米） |
| `elevation` | 海拔高度（米） |
| `battery_percent` | 电池剩余容量百分比 |
| `battery_landing_power` | 剩余可降落电量百分比 |
| `pitch/roll/yaw` | 无人机姿态角（度） |
| `horizontal_speed` | 水平飞行速度（米/秒） |
| `vertical_speed` | 垂直飞行速度（米/秒，正=上升） |

---

<a id="fh_dock_snapshot"></a>
### fh_dock_snapshot — 获取机场实时快照

一次性获取机场的位置、舱盖状态、电源状态、无人机在仓状态等。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `dock_sn` | string | 是 | 目标机场 SN |

**返回示例**

```json
{
  "success": true,
  "data": {
    "dock_sn": "DOCK-7CQDKR1020003V",
    "latitude": 22.5435,
    "longitude": 113.9470,
    "height": 25.0,
    "heading": 90.0,
    "cover_state": 0,
    "cover_state_desc": "closed",
    "drone_in_dock": 1
  },
  "message": "Dock DOCK-7CQDKR1020003V: cover=closed, drone=in-dock"
}
```

**字段说明**

| 字段 | 含义 |
|------|------|
| `cover_state` | 舱盖状态码（0=关闭，1=打开，2=半开，3=异常） |
| `drone_in_dock` | 无人机在仓状态（0=不在，1=在仓） |
| `heading` | 机场朝向角度 |

---

<a id="fh_dock_control"></a>
### fh_dock_control — 控制机场设备

远程控制机场的舱盖、充电、补光灯。**安全等级 L1**——需要传 `confirm=true` 确认操作。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `dock_sn` | string | 是 | 目标机场 SN |
| `action` | string | 是 | 控制动作（见下表） |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**可用动作**

| action | 说明 | 前置条件 |
|--------|------|----------|
| `cover_open` | 打开舱盖 | 机场在线 |
| `cover_close` | 关闭舱盖 | 机场在线，无人机未起飞 |
| `charge_start` | 开始充电 | 无人机在仓 |
| `charge_stop` | 停止充电 | 正在充电中 |
| `light_on` | 打开补光灯 | 机场在线 |
| `light_off` | 关闭补光灯 | 补光灯已开启 |

**确认机制**

如果不传 `confirm=true`，工具不会执行操作，而是返回提示信息：

```json
{
  "success": false,
  "error_message": "Confirmation required",
  "suggestion": "About to execute 'cover_open' on dock DOCK-001. Pass confirm=true to proceed."
}
```

AI 会自动在对话中向用户确认后再传 `confirm=true` 重新调用。

**返回示例（成功）**

```json
{
  "success": true,
  "data": { "dock_sn": "DOCK-001", "action": "cover_open", "status": "completed" },
  "message": "Dock DOCK-001: cover_open executed successfully.",
  "next_steps": ["Call fh_dock_snapshot(dock_sn='DOCK-001') to verify state"]
}
```

---

<a id="fh_device_manage"></a>
### fh_device_manage — 设备管理

聚合多个低频设备管理操作：查详情、改名、解绑、HMS 告警查询。通过 `action` 参数区分。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `action` | string | 是 | 操作类型（见下表） |
| `device_sn` | string | 是 | 目标设备 SN（无人机或机场） |
| `new_name` | string | 否 | 新显示名（`action=rename` 时必填） |
| `confirm` | boolean | 否 | 确认标志（`action=rename/unbind` 时必填） |

**可用动作**

| action | 说明 | 安全等级 | 额外参数 |
|--------|------|----------|----------|
| `detail` | 查询设备详细信息（固件版本、工作状态等） | L0 | — |
| `rename` | 修改设备显示名 | L1 | `new_name` + `confirm=true` |
| `unbind` | 解绑设备（从工作空间移除） | L1 | `confirm=true` |
| `hms_current` | 查询当前 HMS 健康/告警数据 | L0 | — |

**示例调用**

```
# 查看设备详情
fh_device_manage(action="detail", device_sn="DRONE-003")

# 修改设备名称
fh_device_manage(action="rename", device_sn="DOCK-001", new_name="东区3号机场", confirm=true)

# 查看健康告警
fh_device_manage(action="hms_current", device_sn="DOCK-001")
→ 如果 10 秒内无告警数据返回，说明设备当前健康（无告警）
```

**rename 返回示例**

```json
{
  "success": true,
  "data": { "device_sn": "DOCK-001", "new_name": "东区3号机场" },
  "message": "Device DOCK-001 renamed to '东区3号机场'"
}
```

---

## 实时指挥（DRC）

DRC（Direct Remote Control）工具组提供对无人机的实时飞行指挥能力。使用前必须先调用 `fh_control_acquire` 获取控制权，再调用 `fh_drc_connect` 建立 DRC 链路，之后才能执行飞行指令。

<a id="fh_control_acquire"></a>
### fh_control_acquire — 获取飞行控制权

向 DJI Cloud API 2 服务端申请对指定无人机的独占飞行控制权。**安全等级 L1**——需要传 `confirm=true`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "control_authority": "acquired",
    "acquired_at": "2026-06-25T10:00:00Z"
  },
  "message": "Flight control authority acquired for DRONE-001.",
  "next_steps": ["Call fh_drc_connect(drone_sn='DRONE-001', confirm=true) to establish DRC link"]
}
```

**注意事项**

- 每次只能持有一台无人机的控制权；再次申请不同设备前必须先释放
- 控制权不会自动释放——任务结束后必须显式调用 `fh_control_release`
- 设备离线或被其他客户端占用时会返回错误

---

<a id="fh_control_release"></a>
### fh_control_release — 释放飞行控制权

释放当前持有的飞行控制权。**安全等级 L1**——需要传 `confirm=true`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "control_authority": "released"
  },
  "message": "Flight control authority released for DRONE-001."
}
```

**注意事项**

- 执行任何飞行操作前必须已持有控制权；释放后飞行指令将被拒绝
- 即使飞行任务失败也必须在 finally 路径中调用此方法，防止控制权泄漏
- 释放前请确保无人机已安全悬停或降落

---

<a id="fh_drc_connect"></a>
### fh_drc_connect — 建立 DRC 链路

在已持有控制权的前提下，建立到无人机的 DRC WebSocket 实时指挥通道。**安全等级 L1**——需要传 `confirm=true`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "drc_link": "connected",
    "connected_at": "2026-06-25T10:01:00Z"
  },
  "message": "DRC link established for DRONE-001.",
  "next_steps": ["Now you can issue flight commands: fh_takeoff, fh_flyto, fh_return_home, etc."]
}
```

**注意事项**

- 必须先成功调用 `fh_control_acquire` 再调用此工具，否则返回 "No control authority" 错误
- DRC 链路建立后，所有飞行指令均通过此通道下发
- 链路断开后需重新调用此工具才能恢复飞行控制

---

<a id="fh_drc_disconnect"></a>
### fh_drc_disconnect — 断开 DRC 链路

主动断开当前的 DRC 实时指挥通道。**安全等级 L1（REVERSIBLE）**——可逆操作，无需 `confirm`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "drc_active": false
  },
  "message": "DRC link disconnected for DRONE-001.",
  "next_steps": ["Call fh_control_release(drone_sn='DRONE-001') to release control authority"]
}
```

当本地已无该设备的 DRC 链路时，工具是**幂等**的：返回 `success: true`，`data` 中带 `already_disconnected: true` 与 `_warning` 说明，并同样记录「用户显式断开」标记。

**注意事项**

- 断开 DRC 链路后无法再下发飞行指令，无人机会保持当前状态（悬停或按任务继续）
- 推荐在所有飞行任务完成后按顺序：`fh_drc_disconnect` → `fh_control_release`

---

<a id="fh_takeoff"></a>
### fh_takeoff — 一键起飞

命令无人机从当前位置垂直起飞至指定目标高度。**安全等级 L2**——必须持有控制权且 DRC 链路已建立，需要传 `confirm=true`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `target_height` | number | 否 | 目标相对高度（米），默认 30.0 |
| `out_of_control_action` | string | 否 | 失控动作，默认 `return_home`；仅接受 `return_home` / `hover` / `continue` |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "command": "takeoff",
    "target_height": 80.0,
    "status": "command_sent"
  },
  "message": "Takeoff command sent to DRONE-001, target height 80.0m.",
  "next_steps": ["Call fh_osd_snapshot(drone_sn='DRONE-001') to monitor altitude"]
}
```

**注意事项**

- 调用此工具前需确认无人机处于"在地面/在仓"状态，否则命令会被拒绝
- `target_height` 为相对起飞点高度，需符合作业区域限飞高度规定
- `out_of_control_action` 仅接受 `return_home` / `hover` / `continue`，传入其他值将返回错误
- 命令发出后状态为 `command_sent`，使用 `fh_osd_snapshot` 监测实际高度

---

<a id="fh_return_home"></a>
### fh_return_home — 返航

命令无人机执行自动返航程序，飞回起飞点或机场。**安全等级 L2**——必须持有控制权且 DRC 链路已建立，需要传 `confirm=true`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "command": "return_home",
    "status": "command_sent"
  },
  "message": "Return-to-home command sent to DRONE-001.",
  "next_steps": ["Call fh_osd_snapshot(drone_sn='DRONE-001') to monitor return progress"]
}
```

**注意事项**

- 返航高度由无人机本地配置决定，如需调整请在起飞前设置
- 返航完成后建议调用 `fh_drc_disconnect` + `fh_control_release` 释放资源
- 返航过程中仍可调用 `fh_emergency_stop` 中断

---

<a id="fh_flyto"></a>
### fh_flyto — 指点飞行

命令无人机飞向指定 GPS 坐标和高度，或停止当前指点飞行。**安全等级 L2**——必须持有控制权且 DRC 链路已建立，需要传 `confirm=true`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `action` | string | 是 | `start`（开始指点）/ `stop`（停止指点） |
| `latitude` | number | 否 | 目标纬度（`action=start` 时必填） |
| `longitude` | number | 否 | 目标经度（`action=start` 时必填） |
| `height` | number | 否 | 目标相对高度（2~10000 米，`action=start` 时必填） |
| `max_speed` | number | 否 | 最大飞行速度（米/秒），默认 10.0 |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "command": "flyto",
    "action": "start",
    "target": { "latitude": 22.55, "longitude": 113.95, "height": 80.0 },
    "max_speed": 10.0,
    "status": "command_sent"
  },
  "message": "FlyTo command sent: DRONE-001 heading to (22.55, 113.95) at 80.0m."
}
```

**注意事项**

- 坐标使用 WGS-84 十进制度格式
- `height` 范围 **2~10000 米**，超出范围时直接返回错误（无需先传 `confirm=true`）
- 目标点需在无人机限飞区域之外，否则 SDK 会拒绝指令
- 到达目标后无人机会自动悬停，可调用 `fh_osd_snapshot` 确认位置

---

<a id="fh_poi"></a>
### fh_poi — 环绕飞行

命令无人机以指定 GPS 坐标为中心点进行水平圆周环绕飞行，或停止环绕。**安全等级 L2**——必须持有控制权且 DRC 链路已建立，需要传 `confirm=true`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `action` | string | 是 | `start`（开始环绕）/ `stop`（停止环绕） |
| `latitude` | number | 否 | 环绕中心纬度（`action=start` 时必填） |
| `longitude` | number | 否 | 环绕中心经度（`action=start` 时必填） |
| `height` | number | 否 | 飞行高度（2~10000 米，`action=start` 时必填） |
| `radius` | number | 否 | 环绕半径（米），默认 30.0 |
| `speed` | number | 否 | 环绕速度（米/秒），默认 5.0 |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "command": "poi",
    "action": "start",
    "center": { "latitude": 22.55, "longitude": 113.95 },
    "height": 80.0,
    "radius": 30.0,
    "speed": 5.0,
    "status": "command_sent"
  },
  "message": "POI orbit started: DRONE-001 orbiting (22.55, 113.95) at r=30m, h=80m."
}
```

**注意事项**

- 飞行过程中镜头会自动朝向环绕中心点
- `height` 范围 **2~10000 米**，超出范围时直接返回错误（无需先传 `confirm=true`）
- 使用 `fh_poi(action="stop", ...)` 停止环绕后无人机会悬停在当前位置
- 环绕半径需大于最小安全距离（通常 ≥ 5m）

---

<a id="fh_emergency_stop"></a>
### fh_emergency_stop — 紧急停止

立即中断无人机当前所有飞行动作，执行紧急悬停或下降。**安全等级 L3**——最高危操作，必须传 `confirm=true` 和操作原因 `reason`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `mode` | string | 否 | `hover`（悬停，默认）/ `land`（就地降落） |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |
| `reason` | string | 是 | 操作原因（用于审计日志，AI 必须填写） |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "command": "emergency_stop",
    "mode": "hover",
    "executed_at": "2026-06-25T10:30:00Z",
    "reason": "User reports obstacle ahead"
  },
  "message": "⚠️ Emergency stop executed for DRONE-001 (mode=hover). Reason: User reports obstacle ahead.",
  "next_steps": [
    "Confirm environment is safe before resuming operations",
    "Call fh_osd_snapshot(drone_sn='DRONE-001') to check current position"
  ]
}
```

**注意事项**

- L3 最高危等级：操作会立即中断所有进行中的航线任务和飞行程序
- 参数校验（`mode` 枚举、`reason` 非空）在 `confirm` 检查**之前**执行——即使不传 `confirm=true`，参数错误也会立即返回
- `reason` 字段内容将写入审计日志并实时推送给管理员，AI 必须如实填写用户意图
- 紧急停止后，如需继续飞行需重新规划任务；如需降落请调用 `fh_return_home` 或 `fh_flyto`

---

<a id="fh_payload"></a>
### fh_payload_acquire / fh_payload_release — 负载控制权

获取 / 释放无人机挂载负载（相机、云台等）的控制权。**安全等级 L1**——需要传 `confirm=true`。
负载控制权独立于飞行控制权；操作完成后必须调用 `fh_payload_release` 释放。

> **⚠️ 工具拆分说明**：原 `fh_payload`（负载控制）工具已拆分——控制权部分独立为
> `fh_payload_acquire` / `fh_payload_release`，实际负载动作分散到各专用工具：
> 相机/云台动作（`camera_photo`、`camera_zoom`、`gimbal_rotate` 等）→ **`fh_camera`**；
> 喊话器动作（`tts_play`、`audio_play`、`set_volume` 等）→ **`fh_speaker`**；
> 探照灯动作（`brightness_set`、`mode_set` 等）→ **`fh_searchlight`**。
> 下文保留的参数/动作表按原 `fh_payload` 格式记录，实际调用时请使用对应新工具的参数名。

**fh_payload_acquire 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `payload_index` | string | 否 | 负载标识（如 `81-0-0`），默认主相机 |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**前置条件**：session 有效 + 已持有该无人机的飞行控制权（`fh_control_acquire`）。

**负载动作 → 专用工具对照（原 fh_payload action 迁移）**

| 原 action | 现所属工具 | 现参数名 |
|-----------|-----------|---------|
| `camera_photo` / `camera_video_start` / `camera_video_stop` | `fh_camera` | 同名 action |
| `camera_zoom` | `fh_camera` | 同名 action（`zoom_factor` 2~200） |
| `camera_mode` | `fh_camera` | `switch_mode`（`camera_mode` 枚举 PHOTO/VIDEO 等） |
| `gimbal_reset` / `gimbal_rotate` / `gimbal_look_at` | `fh_camera` | 同名 action |
| `speaker_tts` | `fh_speaker` | `tts_play`（`text_to_say`） |
| `speaker_audio` | `fh_speaker` | `audio_play` |
| `speaker_stop` | `fh_speaker` | `stop` |
| `speaker_volume` | `fh_speaker` | `set_volume`（0~100） |
| `spotlight_on` / `spotlight_off` | `fh_searchlight` | `mode_set` |
| `spotlight_brightness` | `fh_searchlight` | `brightness_set`（0~100） |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "payload_index": "81-0-0",
    "granted": true
  },
  "message": "Payload control authority granted for DRONE-001 [81-0-0]."
}
```

**注意事项**

- 负载控制不要求 DRC 链路已建立，但无人机必须在线且已持有飞行控制权
- 同一时刻只能持有一个负载控制权；持有其他负载时需先 `fh_payload_release`
- 拍照/录像结果通过媒体回传通道异步上传，可在 DJI Cloud API 2 控制台查看
- **⚠️ 录像前必须先切换相机模式**：调用 `camera_video_start` 前，必须先调用 `fh_camera(action=switch_mode, camera_mode=VIDEO)` 确保相机处于 VIDEO 模式，否则 SDK 返回 `INVALID_STATE` 错误。正确顺序：`switch_mode(VIDEO)` → `camera_video_start`
- **机载录像 vs 直播录制**：`camera_video_start` 是机载 SD 卡录像（视频存储在无人机本地）；如需录制直播流（服务端存储），应使用 `fh_media_record`（前提是 `fh_live_start` 已开启直播推流）
- **SDK 链路偶发抖动**：`switch_mode` 等相机控制指令首次调用可能因通信链路不稳定返回 `connect_error`，重试一次通常可成功
- `gimbal_look_at`：`target_lat`、`target_lng`、`target_height` 三个参数均为必填，缺少任意一个将返回错误
- `camera_zoom`：`zoom_factor` 范围 **2~200**，超出返回错误
- `set_volume`（fh_speaker）：`volume` 范围 **0~100**，超出返回错误
- `brightness_set`（fh_searchlight）：`brightness` 范围 **0~100**，超出返回错误

---

<a id="fh_live_start"></a>
### fh_live_start — 直播推流

开启或停止无人机相机的实时视频推流（RTMP / RTSP）。**安全等级 L1**——需要传 `confirm=true`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `action` | string | 是 | `start`（开始推流）/ `stop`（停止推流） |
| `stream_url` | string | 否 | 推流目标地址（`action=start` 时必填，如 `rtmp://live.example.com/live/stream1`） |
| `video_quality` | string | 否 | 视频质量：仅接受 `auto`（默认）/ `sd` / `hd`，传入其他值将返回错误 |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "action": "start",
    "stream_url": "rtmp://live.example.com/live/stream1",
    "video_quality": "hd",
    "status": "streaming"
  },
  "message": "Live stream started for DRONE-001 → rtmp://live.example.com/live/stream1 (HD)."
}
```

**注意事项**

- 推流地址需确保网络可达，否则会导致推流失败但工具调用本身返回成功（命令已下发）
- `video_quality` 仅接受 `auto` / `sd` / `hd`，传入其他值（如 `smooth`、`fluent`、`ultra_hd`）将返回错误
- 直播推流不依赖 DRC 链路，但无人机必须在线

---

<a id="fh_media_record"></a>
### fh_media_record — 直播流录制

控制**服务端直播流录制**：将正在推流的视频保存到服务端存储。**安全等级 L1**——start/stop 需要 `confirm=true`；list 为只读。

> **与 `camera_video_start` 的区别**：
> - `camera_video_start`（fh_camera 动作）= 机载 SD 卡录像，视频存储在无人机本地
> - `fh_media_record` = 服务端直播流录制，将 `fh_live_start` 已开启的直播流录制到云端
>
> 两者独立，可同时使用。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `device_sn` | string | 是 | 设备 SN（无人机或机场） |
| `action` | string | 是 | `start`（开始录制）/ `stop`（停止录制）/ `list`（查询录制记录） |
| `confirm` | boolean | 条件 | start/stop 时**必须为 `true`**，list 不需要 |
| `payload_index` | string | 否 | 相机负载标识（默认主相机），可通过 `fh_camera(get_camera_state)` 查询 |

**正确调用顺序**

```
fh_live_start(action=start)     ← 先开启直播推流
    ↓
fh_media_record(action=start)   ← 然后开始录制直播流
    ↓
fh_media_record(action=stop)    ← 停止录制
    ↓
fh_live_start(action=stop)      ← 最后停止推流（可选）
```

**返回示例**

```json
{
  "success": true,
  "data": {
    "device_sn": "DRONE-001",
    "action": "start",
    "recording": true,
    "payload_index": "53-0-0"
  },
  "message": "Live stream recording started for DRONE-001."
}
```

**注意事项**

- **⚠️ 前置依赖**：`fh_media_record(start)` 必须在 `fh_live_start(start)` 成功之后调用，否则没有活跃的直播流可供录制
- start/stop 必须配对调用
- list 动作为只读，不需要 confirm，可用于查询历史录制记录

---

<a id="fh_osd_snapshot"></a>
### fh_osd_snapshot — OSD 遥测快照

实时获取无人机的 OSD（On-Screen Display）遥测数据，包含位置、速度、姿态、电量、飞行状态等完整状态信息。**安全等级 L0**——只读查询，无需确认。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "latitude": 22.55,
    "longitude": 113.95,
    "height": 80.1,
    "elevation": 120.3,
    "battery_percent": 72,
    "horizontal_speed": 0.1,
    "vertical_speed": 0.0,
    "pitch": -0.5,
    "roll": 0.2,
    "yaw": 270.4,
    "flight_status": "hovering",
    "mode": "gps",
    "rc_signal": 4,
    "timestamp": "2026-06-25T10:15:30Z"
  },
  "message": "DRONE-001 OSD: battery=72%, alt=80.1m, status=hovering"
}
```

**字段说明**

| 字段 | 含义 |
|------|------|
| `flight_status` | 飞行状态（`on_ground` / `taking_off` / `hovering` / `flying` / `landing` / `returning_home`） |
| `mode` | 飞行模式（`gps` / `atti` / `manual`） |
| `rc_signal` | 遥控信号强度（0~5） |
| `height` | 相对起飞点高度（米） |
| `elevation` | 海拔高度（米） |

**注意事项**

- 比 `fh_drone_snapshot` 包含更多状态字段（新增 `flight_status`、`mode`、`rc_signal` 等）
- 推荐在每次飞行指令下发后调用此工具确认状态变更
- 无人机离线时返回最后缓存的遥测数据，`timestamp` 字段可判断数据新鲜度

---

<a id="fh_stick_move"></a>
### fh_stick_move — 受限摇杆控制

通过虚拟摇杆向量对无人机进行低级飞行姿态控制（前后/左右/上下/偏航），适用于精细位置微调。**安全等级 L2**——必须持有控制权且 DRC 链路已建立，需要传 `confirm=true`。

> ⚠️ **高风险工具**：摇杆控制为开环操作，AI 无法实时感知位置变化，建议仅用于微调（持续时间 ≤ 3 秒），任何自主飞行逻辑优先使用 `fh_flyto`。

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `drone_sn` | string | 是 | 目标无人机 SN |
| `roll` | number | 否 | 横滚量（左右）：-100 ～ 100，正值右移 |
| `pitch` | number | 否 | 俯仰量（前后）：-100 ～ 100，正值前进 |
| `throttle` | number | 否 | 油门量（升降）：-100 ～ 100，正值上升 |
| `yaw` | number | 否 | 偏航量（旋转）：-100 ～ 100，正值顺时针 |
| `duration_ms` | integer | 否 | 持续时间（毫秒），默认 500，最大 3000 |
| `confirm` | boolean | 是 | 必须为 `true` 才执行 |

**返回示例**

```json
{
  "success": true,
  "data": {
    "drone_sn": "DRONE-001",
    "command": "stick_move",
    "roll": 0,
    "pitch": 30,
    "throttle": 0,
    "yaw": 0,
    "duration_ms": 500,
    "status": "command_sent"
  },
  "message": "Stick move command sent to DRONE-001 (pitch=30 for 500ms)."
}
```

**注意事项**

- 所有摇杆值同时下发；未填写的轴默认为 0（即不操作该轴）
- `duration_ms` 超过 3000 会被截断至 3000；对长距离移动请改用 `fh_flyto`
- 发出命令后调用 `fh_osd_snapshot` 验证实际位移效果

---

## 安全等级机制

所有工具都标注了安全等级（L0~L3），决定调用时需要满足的条件：

| 等级 | 含义 | 调用要求 | 审计 |
|------|------|----------|------|
| **L0** | 只读查询 | 无额外要求 | 记录 |
| **L1** | 可逆操作 | 必须传 `confirm: true` | 记录 + 告警 |
| **L2** | 飞行操作 | `confirm: true` + 持有控制权 | 记录 + 告警 + 推送 |
| **L3** | 高危操作 | `operator_token` + `confirm: true` | 全量记录 + 实时推送 |

> L1 工具（`fh_control_acquire`、`fh_control_release`、`fh_drc_connect`、`fh_drc_disconnect`、`fh_dock_control`、`fh_payload_acquire`、`fh_payload_release`、`fh_live_start`）需要传 `confirm=true`。L2 工具（`fh_takeoff`、`fh_return_home`、`fh_flyto`、`fh_poi`、`fh_stick_move`）额外需要已持有飞行控制权。L3 工具（`fh_emergency_stop`）均已在 M4 里程碑实现。

**AI 的确认行为**：对 L1+ 操作，AI 会先在对话中向用户描述将要执行的操作，用户确认后才传 `confirm=true` 发起调用。这是安全设计的一部分。对 L3 操作（`fh_emergency_stop`），AI 还需在调用中填写 `reason` 字段供审计追溯。

---

## 错误处理

所有工具返回统一结构，失败时包含错误信息和修复建议：

```json
{
  "success": false,
  "error_message": "设备不在线，无法获取控制权",
  "suggestion": "请先确认设备 SN-XXX 是否已开机，可调用 fh_device_list(online_only=true) 检查"
}
```

**常见错误及处理**

| 错误 | 原因 | 解决方法 |
|------|------|----------|
| `No active session` | 未调用 `fh_connect` | 先调用 `fh_connect` 建立会话 |
| `Already connected` | 重复调用 `fh_connect` | 先 `fh_ops(action="shutdown")` 再重连 |
| `Drone not found: SN` | SN 不存在或设备离线 | 调用 `fh_device_list` 确认 SN |
| `Confirmation required` | L1+ 操作未传 `confirm=true` | 传入 `confirm=true` 重新调用 |
| `SDK timeout` | SDK 回调超时（设备通信中断） | 检查设备和网络状态 |
| `UnsatisfiedLinkError` | 在非 Linux x86_64 上运行了真实 SDK | 使用 Mock 模式或 Docker 运行 |
| `No control authority` | 执行飞行指令前未调用 `fh_control_acquire` | 先调用 `fh_control_acquire` 获取控制权 |
| `DRC link not established` | 飞行指令需要 DRC 链路但未建立 | 先调用 `fh_drc_connect` 建立链路 |
| `Invalid out_of_control_action` | `fh_takeoff` 的失控动作参数不在允许列表中 | 使用 `return_home` / `hover` / `continue` |
| `Invalid zoom_factor` | `camera_zoom` 的 `zoom_factor` 超出 2~200 范围 | 检查参数范围约束 |
| `Invalid volume` | `fh_speaker` `set_volume` 的 `volume` 超出 0~100 范围 | 检查参数范围约束 |
| `Invalid brightness` | `fh_searchlight` `brightness_set` 的 `brightness` 超出 0~100 范围 | 检查参数范围约束 |
| `Invalid quality` | `fh_live_start` 的 `video_quality` 不在允许列表中 | 使用 `auto` / `sd` / `hd` |

---

## 典型业务场景

### 场景 1：日常设备巡检

```
用户：帮我检查所有在线设备的状态，看有没有异常告警

AI 调用链：
  1. fh_connect(server_address=...)                    → 建立会话
  2. fh_device_list(online_only=true)                  → 获取在线设备
  3. fh_dock_snapshot(dock_sn="DOCK-001")              → 逐个查看机场状态
  4. fh_drone_snapshot(drone_sn="DRONE-003")           → 查看无人机状态
  5. fh_device_manage(action="hms_current", device_sn="DOCK-001")  → 检查告警

AI 回复：
  3 台设备在线：
  - Dock 2 (DOCK-7CQDKR1020003V)：在线，舱盖关闭，无人机在仓
  - Matrice 350 RTK (DRONE-4LFDK100030182)：电量 87%，位于 22.54°N
  - Matrice 3D (DRONE-1581F6GKD241T00E40)：在仓充电中
  未发现异常告警。
```

### 场景 2：准备作业前检查

```
用户：我要用 DOCK-001 执行巡检任务，帮我检查一下准备情况

AI 调用链：
  1. fh_dock_snapshot(dock_sn="DOCK-001")      → 检查机场状态
  2. fh_drone_snapshot(drone_sn="DRONE-003")   → 检查无人机电量
  3. fh_device_manage(action="hms_current", device_sn="DOCK-001")  → 检查告警

AI 回复：
  DOCK-001 准备情况：
  ✅ 机场在线，舱盖关闭
  ✅ 无人机在仓，电量 87%（满足起飞条件）
  ✅ 无异常告警
  可以执行巡检任务。
```

### 场景 3：远程控制机场

```
用户：打开 DOCK-001 的舱盖，然后打开补光灯

AI 调用链：
  1. fh_dock_control(dock_sn="DOCK-001", action="cover_open", confirm=true)
  2. fh_dock_control(dock_sn="DOCK-001", action="light_on", confirm=true)
  3. fh_dock_snapshot(dock_sn="DOCK-001")      → 确认状态变更

AI 回复：
  已完成：
  1. ✅ 舱盖已打开
  2. ✅ 补光灯已开启
  当前状态：舱盖开启，补光灯亮，无人机在仓。
```

### 场景 4：运维排障

```
用户：DOCK-002 好像有问题，帮我看看

AI 调用链：
  1. fh_device_manage(action="detail", device_sn="DOCK-002")       → 设备信息
  2. fh_dock_snapshot(dock_sn="DOCK-002")                          → 实时状态
  3. fh_device_manage(action="hms_current", device_sn="DOCK-002")  → HMS 告警

AI 回复：
  DOCK-002 诊断结果：
  - 固件版本：v02.01.0503（最新）
  - 在线状态：在线
  - ⚠️ HMS 告警：「散热风扇转速异常」（中等级别）
  建议：安排现场维护检查散热系统。
```

### 场景 5：AI 完整操控无人机飞行

```
用户：帮我用 DRONE-001 飞到坐标 (22.55, 113.95) 高度80米拍张照

AI 调用链：
  1. fh_control_acquire(drone_sn="DRONE-001", confirm=true)
  2. fh_drc_connect(drone_sn="DRONE-001", confirm=true)
  3. fh_takeoff(drone_sn="DRONE-001", target_height=80, confirm=true)
  4. fh_flyto(drone_sn="DRONE-001", action="start", latitude=22.55, longitude=113.95, height=80, confirm=true)
  5. fh_osd_snapshot(drone_sn="DRONE-001")          → 确认到达目标位置
  6. fh_payload_acquire(drone_sn="DRONE-001", confirm=true)
     fh_camera(drone_sn="DRONE-001", action="camera_photo")
  7. fh_payload_release(drone_sn="DRONE-001", confirm=true)
  8. fh_return_home(drone_sn="DRONE-001", confirm=true)
  9. fh_drc_disconnect(drone_sn="DRONE-001", confirm=true)
  10. fh_control_release(drone_sn="DRONE-001", confirm=true)

AI 回复：
  ✅ 任务完成：
  1. 已获取 DRONE-001 飞行控制权
  2. DRC 链路已建立
  3. 起飞至 80m
  4. 飞至目标坐标 (22.55, 113.95)，当前高度 80.1m，状态 hovering
  5. 已拍摄照片，照片将通过媒体回传上传至 DJI Cloud API 2
  6. 返航指令已下发
  7. DRC 链路已断开，飞行控制权已释放
```

### 场景 6：紧急停止

```
用户：DRONE-001 好像要撞到东西了，赶紧停！

AI 调用链：
  1. fh_emergency_stop(drone_sn="DRONE-001", mode="hover", confirm=true, reason="User reports obstacle ahead")

AI 回复：
  ⚠️ 紧急停止已执行，DRONE-001 已悬停在当前位置。
  请确认环境安全后决定下一步操作：
  - 继续执行原任务：重新调用 fh_flyto
  - 返回起飞点：调用 fh_return_home
  - 查看当前状态：调用 fh_osd_snapshot
```
