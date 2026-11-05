# MCP Tool Routing Rules

## General Principle

Before calling any fh_* tool, answer these three questions:

1. **What does the user HAVE?** (wayline_uuid, local file, URL, device SN, nothing)
2. **What does the user WANT?** (query, create, execute, monitor, control)
3. **What PREREQUISITE does the target tool need?**

If the prerequisite is not met → find the tool that produces it first.

## Disambiguation Signals

| Signal in user input | Likely meaning | Action |
|---|---|---|
| UUID format (8-4-4-4-12) | Could be task_uuid OR wayline_uuid | Check context or ask user |
| File path (`/path/to/xxx.kmz`) | Local KMZ file | Use `fh_workspace(action=submit)` |
| URL (`https://...xxx.kmz`) | Remote KMZ file | Use `fh_wayline_upload` |
| SN with "DOCK" or dock model prefix | Dock SN | Use with `fh_flight_task` |
| SN with drone model prefix | Drone SN | Use with `fh_wayline_upload` or DRC tools |

## Anti-Patterns

- ❌ Calling a tool without its prerequisite data (e.g., `fh_flight_task(create)` without wayline_uuid)
- ❌ Guessing UUID types — always verify from context or prior tool output
- ❌ Using drone SN where dock SN is expected (and vice versa)
- ❌ Skipping the "list" step when the user hasn't specified which resource to act on

## Tool Categories by Intent

| User intent | Start with |
|---|---|
| "查看/列出/有哪些..." (query) | `fh_workspace(list)`, `fh_flight_task(list)`, `fh_device_list` |
| "创建/新建/飞一下..." (create) | See workflow-specific decision tree |
| "执行/开始飞..." (execute) | `fh_flight_task(execute)` or `fh_wayline_execute` |
| "停/暂停/取消..." (abort) | `fh_wayline_control(action=pause/cancel)` |
| "状态/进度..." (monitor) | `fh_flight_task(detail)` or `fh_wayline_status` |
| "no-fly zone/geofence/offline map/sync to device..." | See `references/workflows/safety-service.md` decision tree |
| "3D reconstruction/panorama/model build progress..." | See `references/workflows/terra-reconstruction.md` decision tree |
| "device list/dock control/telemetry snapshot..." | See `references/workflows/device-query-control.md` decision tree |
| "format/reboot/RTK calibration/relay/network test..." | See `references/workflows/device-debug.md` decision tree |
| "firmware upgrade/release/publish..." | See `references/workflows/firmware.md` decision tree |
| "maintenance order/HMS alert/log report/shutdown server..." | See `references/workflows/ops-maintenance.md` decision tree |
| "takeoff/return home/orbit flight/emergency stop/stick move/gimbal authority/device recommend/wayline recommend..." | See `references/workflows/device-control.md` decision tree |
| "pause/resume/cancel wayline/wayline progress/export flight record..." | See `references/workflows/flight-task.md` "航线执行生命周期" section |
| "airspace unlock license/TCAS/flysafe database upgrade..." | See `references/workflows/safety-service.md` "空域合规" section |
| "device self-diagnosis/diagnosis report..." | See `references/workflows/ops-maintenance.md` decision tree |
| "no active session/call fh_connect first..." | See `references/workflows/ops-maintenance.md` decision tree |
| "media list/download media/delete media/livestream recording..." | See `references/workflows/media-management.md` decision tree |
| "take photo/record video/zoom/gimbal rotate/PSDK/speaker/searchlight..." | See `references/workflows/payload-control.md` decision tree |
