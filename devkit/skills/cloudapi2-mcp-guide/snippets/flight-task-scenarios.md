# Flight Task Scenarios — Few-Shot Examples

## Scenario A: User has wayline_uuid

**User**: 帮我用航线 wl_abc123 在 DOCK-001 上创建一个立即执行的任务，返航高度 100 米

**Correct tool call**:

```json
{
  "tool": "fh_flight_task",
  "arguments": {
    "action": "create",
    "wayline_uuid": "wl_abc123",
    "sn": "DOCK-001",
    "task_type": 1,
    "rth_altitude": 100,
    "confirm": true
  }
}
```

**Why**: User already has wayline_uuid and dock SN → direct creation, no upload needed.

---

## Scenario B: User has local KMZ file

**User**: 我有个航线文件在 /data/routes/inspection-A.kmz，帮我创建任务，用 DOCK-002 执行

**Correct tool calls (2 steps)**:

Step 1 — Upload to workspace:

```json
{
  "tool": "fh_workspace",
  "arguments": {
    "action": "submit",
    "file_path": "/data/routes/inspection-A.kmz",
    "wayline_name": "inspection-A",
    "confirm": true
  }
}
```

Returns: `{ "data": { "wayline_name": "inspection-A", "status": "submitted" } }`

> ⚠️ **注意**: submit 不返回 wayline_uuid！必须通过 list 获取。

Step 2 — List to get wayline_uuid:

```json
{
  "tool": "fh_workspace",
  "arguments": {
    "action": "list"
  }
}
```

Returns: `{ "data": { "waylines": [{"uuid": "wl_xyz789", "name": "inspection-A", ...}] } }`

Step 3 — Create task:

```json
{
  "tool": "fh_flight_task",
  "arguments": {
    "action": "create",
    "wayline_uuid": "wl_xyz789",
    "sn": "DOCK-002",
    "task_type": 1,
    "rth_altitude": 100,
    "confirm": true
  }
}
```

**Why**: Local file → upload to workspace → list to get UUID (submit doesn't return it) → create task.

---

## Scenario C: User has KMZ URL

**User**: 航线文件在 https://oss.example.com/routes/patrol.kmz，无人机 SN 是 DRONE-003，直接创建任务吧

**Correct tool call**:

```json
{
  "tool": "fh_wayline_upload",
  "arguments": {
    "drone_sn": "DRONE-003",
    "wayline_url": "https://oss.example.com/routes/patrol.kmz",
    "confirm": true
  }
}
```

**Why**: Public URL + drone SN → one-step shortcut via fh_wayline_upload. Note: uses `drone_sn` (not dock_sn).

---

## Scenario D: User has nothing specific

**User**: 帮我飞一个航线任务

**Correct approach**: First query available waylines.

```json
{
  "tool": "fh_workspace",
  "arguments": {
    "action": "list"
  }
}
```

Then present the list to user and ask which wayline and dock to use. After user selects → proceed with Scenario A flow.

**Why**: No wayline info → don't guess. Query first, then let user decide.

---

## Common Mistakes Illustrated

### Mistake: Wrong SN type

**User**: 用 DRONE-001 创建一个航线任务

**Wrong** ❌:
```json
{ "tool": "fh_flight_task", "arguments": { "action": "create", "sn": "DRONE-001", ... } }
```

**Right** ✅: Ask user for the Dock SN, or query `fh_device(action=list)` to find which dock is paired with DRONE-001.

### Mistake: URL treated as local file

**User**: 航线文件是 https://oss.example.com/route.kmz

**Wrong** ❌:
```json
{ "tool": "fh_workspace", "arguments": { "action": "submit", "file_path": "https://oss.example.com/route.kmz" } }
```

**Right** ✅:
```json
{ "tool": "fh_wayline_upload", "arguments": { "drone_sn": "...", "wayline_url": "https://oss.example.com/route.kmz" } }
```
