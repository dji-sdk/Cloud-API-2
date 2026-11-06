# Flight Task Creation — Decision Tree

## Quick Decision Matrix

| User has | Correct tool(s) | Key parameter |
|---|---|---|
| wayline_uuid (already in workspace) | `fh_flight_task(action=create)` | sn = **DOCK** SN |
| KMZ 已在对象存储（key） | `fh_workspace(action=submit)` → `fh_workspace(action=list)` → `fh_flight_task(action=create)` | submit 返回 `wayline_uuid`/`async_request_id`，超时可 list 复查 |
| 本地 KMZ 文件（绝对路径） | `fh_workspace(action=submit)` → `fh_flight_task(action=create)` | `file_path` 传绝对路径（如 `/tmp/wayline-xxx.kmz`），需位于 `/tmp` 或 `FH_WAYLINE_UPLOAD_DIR` 内；MCP 自动 `importKmz` 上传 |
| KMZ public URL (https://...) | `fh_wayline_upload` → `fh_flight_task(action=create)` | drone_sn = **DRONE** SN；upload 只导入航线，不建任务 |
| Nothing specified | `fh_workspace(action=list)` first | — |

## Decision Flow

```
User intent: 创建/执行航线任务
         │
         ├─ Has wayline_uuid?
         │    YES → fh_flight_task(action=create, sn=DOCK_SN, wayline_uuid=...)
         │    NO  ↓
         │
         ├─ Has KMZ file?
         │    ├─ KMZ 已在对象存储（key）
         │    │    → Step 1: fh_workspace(action=submit, file_path=<object_key>, wayline_name=...)
         │    │    → Step 2: 取返回的 wayline_uuid（导入事件超时则 fh_workspace(action=list) 复查）
         │    │    → Step 3: fh_flight_task(action=create, wayline_uuid=<from step 2>)
         │    │
         │    ├─ 本地 KMZ 文件（绝对路径，如 /tmp/wayline-xxx.kmz）
         │    │    → Step 1: fh_workspace(action=submit, file_path=<absolute_path>, wayline_name=...)
         │    │    NOTE: 以 `/` 开头的引用走本地文件通道，MCP 先 importKmz 上传再登记导入；
         │    │          路径必须在 /tmp 或 FH_WAYLINE_UPLOAD_DIR 目录内，否则直接拒绝
         │    │    → Step 2: 取返回的 wayline_uuid / kmz_object_key
         │    │    → Step 3: fh_flight_task(action=create, sn=DOCK_SN, wayline_uuid=<from step 2>)
         │    │
         │    ├─ Public URL (https://...xxx.kmz)
         │    │    → Step 1: fh_wayline_upload(drone_sn=DRONE_SN, wayline_url=..., confirm=true)
         │    │    → Step 2: 取返回的 wayline_uuid（status=imported）
         │    │    → Step 3: fh_flight_task(action=create, sn=DOCK_SN, wayline_uuid=...)
         │    │    NOTE: upload 只导入航线、不创建任务；status=submitted 且带
         │    │          import_event_timeout=true 时，用 fh_workspace(action=list) 查 UUID
         │    │
         │    └─ Neither → treat as "nothing specified"
         │
         └─ Nothing specified
              → fh_workspace(action=list) to show available waylines
              → Let user choose, then fh_flight_task(action=create)
```

## Common Mistakes

- ❌ `fh_flight_task(create)` with `sn` = drone SN → WRONG: must be **Dock SN**
- ❌ `fh_wayline_upload` with `drone_sn` = dock SN → WRONG: must be **Drone SN**
- ❌ User gives a UUID → assumed to be wayline_uuid → might be task_uuid! Verify.
- ❌ User has a local KMZ file → called `fh_flight_task(create)` directly → WRONG: 本地磁盘文件不能传给 `fh_flight_task`；应先 `fh_workspace(action=submit, file_path=<绝对路径>)` 导入工作区
- ❌ 以为 `fh_wayline_upload` 会顺带创建任务 → WRONG：它只把航线导入工作区并返回 `wayline_uuid`，创建任务仍需 `fh_flight_task(action=create)`
- ❌ User has URL → called `fh_workspace(submit)` → WRONG: `submit` 接受的是**本地绝对路径**或**对象存储 key**，不是公网 URL；公网 URL 用 `fh_wayline_upload`
- ❌ 以为 `fh_workspace(submit)` 的 `file_path` 只能传对象存储 key → WRONG：以 `/` 开头的值按本地绝对路径处理，MCP 会先 `importKmz` 上传；但路径必须位于 `/tmp` 或 `FH_WAYLINE_UPLOAD_DIR` 内，越界（如 `../` 或符号链接逃逸）会被拒绝

## Parameter Cheat Sheet

### fh_flight_task (action=create)

| Parameter | Value | Note |
|---|---|---|
| `sn` | Dock SN | 机场序列号，不是无人机 |
| `wayline_uuid` | from workspace | get via `fh_workspace(action=list)` |
| `task_type` | 1 or 2 | 1=立即执行, 2=按时间窗执行一次（begin_at 起、end_at 止） |
| `rth_altitude` | 15~1500 | 返航高度（米） |
| `confirm` | true | 必填 |

> ⚠️ **`task_type=2` 不是「重复定时」**。PaaS/SDK 通道**无法表达重复计划**：
> SDK 的 `CreateTaskReq`/`EditTaskReq` 没有 `repeat_type`/`interval` 等字段，
> 网关协议也没有承载位，这些字段在 BFF 层被丢弃，服务端最终按**单次**任务调度。
> 若用户要「每天/每周执行」，不要用 `task_type=2` 承诺周期触发 —— 它只会执行一次。
> 应引导用户改用 SaaS 通道，或明确告知需逐次创建。（已知限制）

### fh_wayline_upload

| Parameter | Value | Note |
|---|---|---|
| `drone_sn` | Drone SN | 无人机序列号，不是机场 |
| `wayline_url` | https://... | 公网可访问的 KMZ 文件 URL |
| `confirm` | true | 必填 |

> 返回 `status`：`submitted`（服务端已受理，`wayline_uuid` 由导入事件回填）、`imported`（已导入，可直接建任务）、`import_failed`（KMZ 非法，带 `import_code`/`import_message`）。导入事件 30 秒未达时 `status=submitted` 且 `import_event_timeout=true`，此时航线可能已导入成功，用 `fh_workspace(action=list)` 复查 UUID。

### fh_workspace (action=submit)

| Parameter | Value | Note |
|---|---|---|
| `file_path` | `wayline/xxx.kmz`（对象存储 key） | 单文件提交；**不是**本地文件系统路径 |
| `file_paths` | `["a.kmz", "b.kmz"]`（对象存储 key 数组） | 可选，批量提交；与 `file_path` 同时给出时合并去重，单次最多 5 个 |
| `wayline_name` | 航线名称 | 必填 |
| `confirm` | true | 必填 |

> 每个文件的结果单独返回在 `results[]` 中，含 `async_request_id`（服务端 `ImportRequest.AsyncRequestId`，导入结果事件的配对键）、`status`（`submitted` 已受理 / `imported` 导入成功 / `timeout` 事件超时 / `import_failed` 服务端拒绝 / `failed` 提交失败）、`wayline_uuid`。整批全失败才返回 `success:false`；部分失败时 `status=partially_submitted` 且 `failed_count` 非 0。导入事件等待超时时 `status=timeout`，此时该文件仍可能已在服务端导入成功，需用 `fh_workspace(action=list)` 复查。

## fh_wayline_recommend — 智能选线（航线推荐）

> 用户不知道该用哪条航线，只知道机场 SN，想让系统按距离/机型匹配推荐。L0，无 `action`，无需 `confirm`，`dry_run` 在 Legacy 模式下不生效（静默忽略）。

**何时用**：用户说"帮我选条合适的航线"/"这个机场附近有什么航线可以飞"。
**何时不用**：用户已经给了 `wayline_uuid` → 直接 `fh_flight_task(action=create)`；用户想看工作区全部航线（不局限于某个机场）→ 用 `fh_workspace(action=list)`。

| Parameter | Value | Note |
|---|---|---|
| `dock_sn` | 机场 SN | 必填 |
| `max_distance_km` | 默认 10.0 | 必须为正数；超时找不到坐标时该过滤会被跳过（见下） |
| `wayline_types` | int 数组 | 可选；若传则不能为空数组（空数组会被当作非法参数拒绝） |
| `max_results` | 默认 10 | ⚠️ **双重含义**：既是最终推荐条数上限，**也是 `getWaylineDetail` 调用次数预算**——预算是按候选顺序消耗的，不是按距离从近到远消耗，可能导致真正最近的航线因为预算耗尽而没被取到，此时响应会带 `truncated:true` |

**关键陷阱**：
- 机场坐标遥测订阅 10 秒超时时，**不会报错**，而是静默跳过"按距离过滤"这一步，直接返回未经距离过滤的候选列表（检查 `dock_location.source`/`_warning` 字段判断是否发生了降级）
- 看到 `truncated:true` 时，正确的修复是**调大 `max_results`**（放宽 detail 调用预算），不是调大 `max_distance_km`（那只会扩大候选池，让预算更快耗尽）
- mock 模式和真实模式走同一套字段组装逻辑，返回结构完全一致（只多一个 `_mock:true`），可以放心用 mock 验证字段结构
- 若 `recommended` 非空，响应里的 `next_steps` 会直接给出可以原样传给 `fh_flight_task(action=create)` 的参数（`wayline_uuid=recommended[0].wayline_uuid`, `sn=dock_sn`, `task_type=1`, `rth_altitude=120.0`），不需要自己再拼接
- 若 `recommended` 为空，`next_steps` 建议把 `max_distance_km` 翻倍重试（而不是返回一个占位 UUID）

## 航线执行生命周期 — fh_wayline_control / fh_wayline_execute / fh_wayline_status

> 覆盖航线任务从"执行"到"暂停/恢复/取消"再到"查状态"的全生命周期。三者均为 Legacy 模式。`fh_wayline_control`/`fh_wayline_execute` 是 **L2（FLIGHT）**，class 级 `requiresHumanConfirmation()`+`getSecurityLevel()` 覆写；`fh_wayline_status` 是 **L0**，只读。

**何时用**：已有 `task_uuid`（通常来自 `fh_flight_task(action=create)` 或 `fh_wayline_status` 列表查询结果），要开始执行、暂停、恢复、取消，或查询执行进度。
**何时不用**：还没有 `task_uuid` → 先用 `fh_flight_task(action=create)` 或 `fh_workspace(action=list)` 走创建流程（见本文档开头的决策树）。

| 动作 | 工具 | Security | Confirm |
|---|---|---|---|
| 开始执行任务 | `fh_wayline_execute(task_uuid, confirm=true)` | L2 | 是 |
| 暂停/恢复/取消 | `fh_wayline_control(task_uuid, action=pause\|resume\|cancel, confirm=true)` | L2 | 是 |
| 查详情/查列表 | `fh_wayline_status(task_uuid?, drone_sn?, status?, page?, page_size?)` | L0 | 否 |

> ⚠️ **已知的安全等级设计不一致 —— 优先用本节工具，不要用 `fh_flight_task(execute/update_status)`**
>
> `fh_wayline_execute`/`fh_wayline_control` 和 `fh_flight_task` 的 `execute`/`update_status` action 最终调用的是同一个底层 SDK 接口（`FlightTaskModule.updateFlightTaskStatus`），但准入门槛不同：
> - `fh_wayline_execute`（**L2**）vs `fh_flight_task(action=execute)`（**L1**）—— 同一个"开始执行"操作，`fh_flight_task` 这条路径的安全等级更低。两者都会更新 session 的 `FlightPhase`/`activeWaylineTaskUuid` 记账，差别**仅在于**准入门槛，不在于是否记账。
> - `fh_wayline_control`（**L2**，操作后才更新 session 状态）vs `fh_flight_task(action=update_status)`（**L1**，接受裸状态码直接透传给 SDK）—— 这一组差别更大：`update_status` **完全不做** session `FlightPhase` 记账，是真正"绕过状态记账"的那条路径。
>
> 这是已知的系统设计不一致，不是推荐路径，也不建议"修复"（不改变现有工具行为）。**AI 应始终优先选用 `fh_wayline_execute`/`fh_wayline_control`**，除非用户明确要求绕过 FlightPhase 记账（极少见，通常意味着用户在做底层调试）。

**`fh_wayline_execute` 的 `verifyTaskOwnership()` 实际语义（措辞需谨慎，不要过度渲染安全保证）**：

这个方法名听起来像是做"任务归属校验"，但实际只确认 `getFlightTaskDetail(taskUuid)` 能查到该任务（即该任务在服务端按当前 workspace token 过滤后可见）。底层 SDK 接口本身**没有** `workspaceId` 参数（代码注释称为"SDK 同源缺陷"），真正的 workspaceId 级别强制校验发生在服务端 REST 层，不是这里。session 里的 `workspaceUuid` 和任务的 `sn`/`landingDockSn` 只用于审计日志，不用于客户端白名单拦截（刻意为之：session 缺少完整设备快照，贸然做客户端拦截容易误杀合法任务）。该校验只在真实 SDK 模式下执行，mock 模式跳过；`dry_run=true` 时也会跳过（避免产生副作用）。超时/SDK 异常/通用异常都按"拒绝执行"处理（fail-closed）。

**`fh_wayline_status` 的 mock 无状态陷阱**：

mock 模式下查询任务详情，无论之前在同一 session 里对该 `task_uuid` 做过多少次 `fh_wayline_control(pause/resume/cancel)`，`fh_wayline_status` 返回的永远是固定的 `status=EXECUTING, progress=45`——**不反映**真实的操作历史。不要把 mock 下连续调用"pause → 查状态"看到的 `EXECUTING` 当作 bug 去排查，这是 mock 数据源本身的局限，不代表真实环境行为；真实模式下状态是如实从 SDK 查询的。

**Parameter Cheat Sheet**

| 工具 | 关键参数 | 备注 |
|---|---|---|
| `fh_wayline_execute` | `task_uuid`, `confirm`, `dry_run`（可选） | 调用 `updateFlightTaskStatus(taskUuid, 2, cb)`；`verifyTaskOwnership()` 仅真实模式执行 |
| `fh_wayline_control` | `task_uuid`, `action`（pause/resume/cancel）, `confirm`, `dry_run`（可选） | SDK 状态码：pause=3, resume=2, cancel=4；session 状态更新**只在 SDK 调用成功后**才执行，避免失败时状态污染 |
| `fh_wayline_status` | `task_uuid`（可选，传了查详情，不传查列表）, `drone_sn`/`status`（仅列表模式过滤）, `page`/`page_size`（默认 1/20，上限 100） | `status` 过滤枚举：CREATED/EXECUTING/PAUSED/SUCCESS/FAILED/CANCELLED；mock 详情无状态（见上） |

## fh_flight_record — 飞行记录导出

> L0，无需 `confirm`。用于导出历史飞行任务的记录数据（非实时遥测），例如生成巡检报告。

**何时用**：用户要导出/下载某几次飞行任务的记录数据（CSV/PDF 等），或查询之前发起的导出任务进度。
**何时不用**：用户要查任务当前执行状态/进度 → 用 `fh_wayline_status`；用户要下载照片/视频媒体文件 → 用 `fh_media_download`（见 `media-management.md`）。

| Parameter | Value | Note |
|---|---|---|
| `action` | export / get_progress | 必填 |
| `user_id` | 发起导出的用户 ID | `action=export` 时必填 |
| `flight_task_ids` | 任务 UUID 数组 | `action=export` 时必填，至少 1 个 |
| `file_name` | 导出文件名 | 可选，默认 `flight_record_export` |
| `file_type` | 如 `["csv","pdf"]` | 可选 |
| `metrics` | 要导出的指标数组 | 可选 |
| `time_zone` | 默认 `Asia/Shanghai` | 可选 |
| `locale` | 默认 `zh_CN` | 可选 |
| `export_id` | 导出任务 ID | `action=get_progress` 时查询单条；不传则分页列出全部导出记录 |

**关键陷阱**：
- `export` 是**异步**发起：返回的是 `export_id` + `status=processing`，不是文件本身，必须再调用 `get_progress` 轮询，直到 `status=completed` 才能拿到 `download_url`
- `get_progress` 不传 `export_id` 时不会报错，而是返回分页的导出记录列表（`records` 数组），这是刻意设计的双用途行为，不是参数缺失
- mock 模式下 `workspace == null`（即尚未 `fh_connect`）也会走降级 mock 分支而不是报 `noSessionError`——但仍然要求 session 存在；真正的"没有 workspace"场景在真实部署里几乎不会发生（`fh_connect` 之后 workspace 必然可用）
