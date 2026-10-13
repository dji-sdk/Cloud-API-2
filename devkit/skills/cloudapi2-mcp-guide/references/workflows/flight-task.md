# Flight Task Creation — Decision Tree

## Quick Decision Matrix

| User has | Correct tool(s) | Key parameter |
|---|---|---|
| wayline_uuid (already in workspace) | `fh_flight_task(action=create)` | sn = **DOCK** SN |
| Local KMZ file path | `fh_workspace(action=submit)` → `fh_workspace(action=list)` → `fh_flight_task(action=create)` | submit 不返回 UUID，需 list 获取 |
| KMZ public URL (https://...) | `fh_wayline_upload` | drone_sn = **DRONE** SN |
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
         │    ├─ Local path (/path/xxx.kmz)
         │    │    → Step 1: fh_workspace(action=submit, file_path=..., wayline_name=...)
         │    │    → Step 2: fh_workspace(action=list) → 找到刚上传的 wayline_uuid
         │    │    → Step 3: fh_flight_task(action=create, wayline_uuid=<from step 2>)
         │    │    NOTE: submit 不返回 wayline_uuid，必须通过 list 获取！
         │    │
         │    ├─ Public URL (https://...xxx.kmz)
         │    │    → fh_wayline_upload(drone_sn=DRONE_SN, wayline_url=..., confirm=true)
         │    │    [One-step: uploads + creates task]
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
- ❌ User has local file → called `fh_flight_task(create)` directly → WRONG: must `fh_workspace(submit)` first
- ❌ User has URL → called `fh_workspace(submit)` → WRONG: `submit` only accepts local file paths

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

### fh_workspace (action=submit)

| Parameter | Value | Note |
|---|---|---|
| `file_path` | /path/to/xxx.kmz | 本地文件系统路径 |
| `wayline_name` | 航线名称 | 必填 |
| `confirm` | true | 必填 |
