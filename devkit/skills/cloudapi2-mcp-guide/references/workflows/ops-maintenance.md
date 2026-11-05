# Ops & Maintenance (`fh_maintenance` / `fh_hms` / `fh_log` / `fh_session_info` / `fh_ops` / `fh_diagnosis` / `fh_connect`) — Decision Tree

> 覆盖 7 个"运维杂项"工具：维修工单、健康告警（HMS）、日志报告、会话信息、MCP Server 自身运维、设备自诊断、会话连接引导。彼此功能独立，但都容易被当成"随便查一下不会有副作用"而忽略其中的陷阱。

## Quick Decision Matrix

| User has | Correct tool(s) | Security |
|---|---|---|
| 要查维修工单配置项 | `fh_maintenance(action=get_configs)` | L0 |
| 要创建维修工单 | `fh_maintenance(action=create_order)` | **L0**（有副作用但无需 confirm） |
| 要查工单详情 | `fh_maintenance(action=get_detail)` | L0 |
| 要查工单列表 | `fh_maintenance(action=list_orders)` | L0（status/keyword 等过滤实际不生效，见下） |
| 要查设备健康告警历史 | `fh_hms(action=query_history)` | L0 |
| 要查日志 | `fh_log(action=query)` | L0 |
| 要生成日志报告 | `fh_log(action=create_report)` | L0（module_list/时间范围参数实际不生效，见下） |
| 要查日志报告列表 | `fh_log(action=list_reports)` | L0 |
| 要下载/删除日志报告 | `fh_log(action=operate, operation=download\|delete)` | delete 需 confirm（L1），download 不需要 |
| 要上传日志文件 | `fh_log(action=upload)` | L0 |
| 要查当前 MCP session 状态 | `fh_session_info` | L0，纯只读，无 action |
| 要查 MCP Server 自身状态/改日志级别/关闭服务 | `fh_ops(action=status\|set_log_level\|shutdown)` | **shutdown 无任何确认机制** |
| 要给设备发起自诊断/查诊断报告 | `fh_diagnosis(action=create\|get_report\|list_reports\|operate)` | L0，无需 confirm |
| 要建立/恢复到 DJI Cloud API 2 的 MCP 会话连接 | `fh_connect()` | L0，**无参数**，所有其它 `fh_*` 工具的前提条件 |

## Decision Flow

```
用户意图：运维 / 工单 / 告警 / 日志 / 会话 / 服务管理
         │
         ├─ 维修工单相关？
         │    → fh_maintenance(action=get_configs|create_order|get_detail|list_orders)
         │    ⚠️ create_order 有副作用但不需要 confirm；list_orders 的 status/keyword/sort_column/sort_type
         │       参数虽在 schema 里但实际只按 device_sn 过滤
         │
         ├─ 健康告警（HMS）相关？
         │    → fh_hms(action=query_history, device_sn=..., begin_time=?, end_time=?, page=?, page_size=?)
         │    ⚠️ page_size 超出 1-100 范围会直接报错（不像其它工具会静默截断）
         │
         ├─ 日志相关？
         │    ├─ 查日志       → fh_log(action=query, ...)
         │    ├─ 生成报告     → fh_log(action=create_report, device_sn=...)   ⚠️ module_list/start_time/end_time 不生效
         │    ├─ 查报告列表   → fh_log(action=list_reports, ...)
         │    ├─ 下载报告     → fh_log(action=operate, operation=download, report_id=...)
         │    ├─ 删除报告     → fh_log(action=operate, operation=delete, report_id=..., confirm=true)
         │    └─ 上传日志文件 → fh_log(action=upload, file_path=...)   ⚠️ 无目录白名单限制
         │
         ├─ 查当前 MCP 连接会话信息？
         │    → fh_session_info()   （无参数、无 action，纯只读 dump）
         │
         ├─ 设备自诊断相关？
         │    ├─ 创建诊断任务   → fh_diagnosis(action=create, device_sn=..., type=?, part=?, index=?)
         │    ├─ 查单份报告     → fh_diagnosis(action=get_report, report_uuid=...)
         │    ├─ 查报告列表     → fh_diagnosis(action=list_reports, device_sn=?, status=?, page=?, page_size=?)
         │    └─ 操作诊断（如取消）→ fh_diagnosis(action=operate, report_uuid=..., cmd_method="cancel")
         │         ⚠️ 全部 4 个 action 均为 L0，无需 confirm；page_size 超范围静默回退默认值（不报错，和 fh_hms 相反）
         │
         ├─ 还没建立会话 / 收到 "Call fh_connect first" 提示？
         │    → fh_connect()   （空参数对象 {}，不要传 server_address/access_token 等）
         │    ⚠️ 已有 session 时再次调用会报错，不会自动顶替，见 Common Mistakes
         │
         └─ MCP Server 本身的运维（和设备无关）？
              ├─ 查服务状态   → fh_ops(action=status)   （缺省 action 时默认即此）
              ├─ 改日志级别   → fh_ops(action=set_log_level, level=...)   ⚠️ 全局生效，影响所有并发 session
              └─ 关闭服务     → fh_ops(action=shutdown)   ⚠️⚠️ 无 confirm，直接终止整个进程，不是单 session
```

## Common Mistakes

- ❌ 以为 `fh_maintenance(action=create_order)` 会像其它工具的写操作一样要求 `confirm=true` → 该工具代码注释明确写了"所有操作均为 L0，无需人工确认"，`create_order` 虽然有副作用（真实创建工单），**仍然不会被 confirm 拦截**，调用前必须靠上层业务逻辑自行判断是否该创建，不能依赖本工具的安全机制
- ❌ 给 `fh_maintenance(action=list_orders)` 传 `status`/`keyword`/`sort_column`/`sort_type` 以为能过滤/排序结果 → 这 4 个参数**在 schema 里声明了，但真实调用代码只用了 `device_sn`**，传了也会被静默忽略，不会报错也不会生效，不要据此判断查询条件是否命中
- ❌ `fh_hms(action=query_history)` 的 `page_size` 传了超出范围的值（如 `0` 或 `500`）期待被自动纠正 → 该工具**会直接报错而不是静默截断**到 1-100 范围内，和项目里大多数"超范围静默裁剪"的工具行为不一致，调用前自行校验范围
- ❌ 在未显式设置 `FH_MOCK=true` 但 `workspace` 还未初始化完成时调用 `fh_hms`，看到正常返回的结构化 `alerts[]` 就当作连上了真实设备 → `workspace==null` 时该工具会**静默降级为 mock 返回**，和显式开 mock 没有区别，容易误判为"已连接真实环境查询成功"
- ❌ 调用 `fh_log(action=create_report)` 时传 `module_list`/`start_time`/`end_time` 期待能按模块/时间范围生成报告 → 这三个参数**在 schema 里声明了但代码完全不读取**，实际只会用 `device_sn` 生成全量报告，无法按范围裁剪
- ❌ 用 mock 模式返回的 `report_id` 去调用真实环境的 `fh_log(action=operate)` → mock 模式 `create_report` 返回字段叫 `report_id`，**真实模式返回的字段叫 `report_uuid`**，两者不是同一个命名；如果混用环境，拿错字段去引用会导致下游 `operate`/`list_reports` 找不到对应报告
- ❌ 认为 `fh_log(action=operate, operation=download)` 的真实模式返回值里也会有 `download_url` → **真实模式该 action 的 SDK 回调是 void-only**，不返回下载链接（mock 才有），需要通过其它方式（如 `list_reports`）获取下载地址
- ❌ **调用 `fh_ops(action=shutdown)` 当作"只是重启一下这次会话"** → 这是本文档最高风险点：该 action 完全没有 `confirm`/安全等级门槛，也不检查 session，调用后会在 500ms 延迟线程里执行 `sessionManager.closeAllSessions()` 和 `System.exit(0)`，**直接终止整个 MCP Server 进程**（所有并发连接的所有 session 一起断开），不是只影响当前对话；非明确运维场景不要调用
- ❌ 调用 `fh_ops` 时不传 `action` 期待报错提醒 → 该工具 `action` 缺省时默认当作 `status`（`args.getOrDefault("action","status")`），与项目里其它工具"缺 action 直接报错"的约定不同，不要靠"报错"来判断参数是否齐全
- ❌ 以为 `fh_diagnosis(action=list_reports)` 的 `page_size` 超范围（如 `0` 或 `500`）会像 `fh_hms` 一样直接报错 → **不会**，`fh_diagnosis` 对超范围 `page`/`page_size` 是**静默回退默认值**（page→1，page_size→20），不报错也不截断到边界，和同一文档里 `fh_hms` 的"硬性报错"行为正好相反，调用前不要假设两者行为一致
- ❌ 以为 `fh_diagnosis` 在 `workspace` 未初始化（SDK 未就绪）时会像 `fh_hms` 一样静默降级为 mock → **不会**，`fh_diagnosis` 的真实 SDK 分支在 `getDiagnosisModule()` 拿不到 `Workspace` 时直接返回 `errorResult("DiagnosisManagementModule not available", ...)`，提示先调用 `fh_connect`，不是伪装成 mock 返回假数据
- ❌ 以为 `fh_diagnosis(action=operate, cmd_method=...)` 会校验 `cmd_method` 取值合法性（如只允许 `"cancel"`） → **不会**，这个字符串原样透传给 SDK（`setReportCmdMethod`），工具层没有枚举校验，传错值不会在 MCP 层被拦截，而是等 SDK/设备侧返回错误码
- ❌ 以为 `fh_connect` 需要传 `server_address`/`access_token`/`workspace_uuid` 等参数 → **不需要**，`access_token`/`workspace_uuid` 固定从服务端环境变量读取，工具的 `description` 明确写"Do NOT pass any parameters"；只有在默认地址连接失败时，工具会主动通过 elicitation 表单向用户索要 `server_address`/`port`，不需要提前猜测传参
- ❌ 以为已有活跃 session 时再调用 `fh_connect` 会自动重连/顶替旧 session → stdio 模式下（单会话）会直接报错"Already connected"，提示先 `fh_ops(action=shutdown)` 断开；SSE 模式下，若当前**客户端**已绑定 session 同样报错，但若是**同一用户**（JWT userId）从新客户端重新调用，则会静默复用旧 session 并返回 `status=reconnected`（除非旧 session 已过期，过期则会先关闭旧 session 再建新的）——这两种路径返回的数据结构不同，不要假设每次 `fh_connect` 都代表一次全新会话
- ❌ 以为 `fh_connect` 失败后应该让用户重新组织完整请求再调一次 → 工具已经内置"默认地址失败 → elicitation 问询 → 重试一次"的闭环，只在默认同机地址 `http://localhost:30812` 连接失败时触发；不要替用户猜测地址或在 elicitation 之外自行拼接参数重试

## Parameter Cheat Sheet

### fh_maintenance

| Action | Key parameters | Note |
|---|---|---|
| `get_configs` | 无必填 | 查工单可选配置项 |
| `create_order` | `device_sn`, 工单详情字段 | L0，无需 confirm，但有真实副作用 |
| `get_detail` | `order_id` | mock 字段 `fault_desc`/`fault_time`，真实字段 `fault_description`/`fault_occurrence_time` |
| `list_orders` | `device_sn` | status/keyword/sort_column/sort_type 声明但不生效 |

### fh_hms

| Parameter | Value | Note |
|---|---|---|
| `device_sn` | 设备 SN | 必填 |
| `begin_time` / `end_time` | 时间戳 | 可选，默认最近 24 小时；`begin_time < end_time` 校验 |
| `page` | 默认 1 | 可选 |
| `page_size` | 1-100 | 可选，默认 20，**超范围直接报错** |

> mock 返回结构化 `alerts[]`；真实模式仅返回 `data.result` 原始字符串，两者结构完全不同。

### fh_log

| Action | Key parameters | Note |
|---|---|---|
| `query` | `device_sn`, 查询条件 | L0 |
| `create_report` | `device_sn` | `module_list`/`start_time`/`end_time` 声明但不生效 |
| `list_reports` | `device_sn` | L0 |
| `operate` | `operation`=download/delete, `report_id` | **仅 delete 需 confirm**；download 真实模式无 `download_url` |
| `upload` | `file_path` | **无目录白名单限制**（与 `fh_firmware.upload` 不同） |

> `report_id`（mock）vs `report_uuid`（real）字段名不统一，跨环境引用时注意。

### fh_session_info

无参数，无 `action`。返回 `session_id/workspace_uuid/workspace_name/max_security_level/uptime_minutes/control_authority/drc_link/active_subscriptions/last_activity`，不含过期时间。

### fh_ops

| Action | Key parameters | Note |
|---|---|---|
| `status` | 无 | 默认 action；返回 `server_version`/`mcp_protocol_version` 等硬编码常量 |
| `set_log_level` | `level` | 修改**全局** logback 根日志级别，影响所有并发 session |
| `shutdown` | 无 | ⚠️⚠️ 无 confirm，终止整个 MCP Server 进程（不是单 session），`requireSession()` 也不校验 |

### fh_diagnosis

| Action | Key parameters | Note |
|---|---|---|
| `create` | `device_sn`（必填）, `type`（0=comprehensive/1=module，默认0）, `part`（0=whole/1=flight/2=camera 等，默认0）, `index`（默认0） | L0，无需 confirm，但真实创建诊断任务（有副作用） |
| `get_report` | `report_uuid`（必填） | mock 固定返回 `device_sn=DRONE-M3D-001`、`status=completed` |
| `list_reports` | `device_sn`/`status`（可选过滤，仅真实模式生效）, `page`（默认1）, `page_size`（默认20，上限100） | 超范围 `page`/`page_size` **静默回退默认值**，不报错；mock 模式固定返回 2 条样例数据，**忽略**过滤参数 |
| `operate` | `report_uuid`, `cmd_method`（如 `"cancel"`，必填） | `cmd_method` 不做枚举校验，原样透传给 SDK |

> 全部 4 个 action 均为 L0，类级显式覆写 `requiresHumanConfirmation()=false`；真实模式下 `Workspace` 不可用时返回**明确错误**（非静默降级 mock），与 `fh_hms` 的降级行为不同。

### fh_connect

| Parameter | Value | Note |
|---|---|---|
| （无） | 空参数对象 `{}` | `access_token`/`workspace_uuid` 固定从服务端环境变量读取，不通过参数传入；工具 description 明确要求"不要传参数、不要问用户要地址/凭证" |
| `server_address`/`port`（可选，仅内部重试用） | 默认 `http://localhost:30812`（同机部署假设） | 默认地址连接失败时，工具会自动触发 elicitation 向用户索要正确地址，不需要调用方提前猜测 |

> 所有其它 `fh_*` 工具的前提条件——`noSessionError()` 会提示"Call fh_connect first"。**已有 session 时的行为分叉**：stdio 单会话模式下直接报错"Already connected"（需先 `fh_ops(shutdown)`）；SSE 多会话模式下，同一用户（JWT userId）从新客户端重连会**静默复用旧 session**（返回 `status=reconnected`），除非旧 session 已超时（此时会先关闭旧 session 再建新会话）。连接成功会在 `next_steps` 里建议依次调用 `fh_device_list`/`fh_session_info`/`fh_drone_snapshot`。
