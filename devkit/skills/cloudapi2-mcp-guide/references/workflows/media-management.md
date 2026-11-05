# Media Management (`fh_media_*`) — Decision Tree

> 覆盖 `media/` 包下 4 个工具：`fh_media_list`/`fh_media_download`/`fh_media_delete`/`fh_media_record`。均为 Legacy 模式（无 `@McpTool` 注解），前置条件由代码内显式校验（`requireSession()` + `sdkService.getWorkspace() == null` 检查）。`fh_media_record` 管理的是**直播流录制**（服务端存储），不是机载 SD 卡录制——后者是 `fh_camera(action=camera_video_start)` 的职责，两者互不相关。

## Quick Decision Matrix

| User has | Correct tool(s) | Confirm? |
|---|---|---|
| 要查看/筛选照片或视频列表 | `fh_media_list` | 不需要 |
| 已有 file_id，要获取下载链接 | `fh_media_download` | 不需要 |
| 要批量删除媒体文件（不可逆） | `fh_media_delete` | 需要 |
| 要开始/停止直播流录制，或查录制历史 | `fh_media_record(action=start/stop/list)` | start/stop 需要，list 不需要 |
| 要录机载 SD 卡视频（不是直播流） | `fh_camera(action=camera_video_start)`，不是 `fh_media_record` | 见 payload-control.md |

## Decision Flow

```
用户意图：媒体文件管理
         │
         ├─ 查看/筛选文件列表？（L0，无需 confirm）
         │    fh_media_list(device_sn?, file_type=photo|video|all, flight_id?,
         │                   start_time?, end_time?, start_index?, page_size?)
         │    → 返回 file_id 列表，供后续 download/delete 使用
         │
         ├─ 下载某个文件？（L0，无需 confirm，但“获取链接”≠“已下载”）
         │    fh_media_download(file_id)
         │    → 返回 download_url + suggested_filename + expiration
         │    ⚠️ 必须用 suggested_filename 保存，不要直接用浏览器默认文件名
         │
         ├─ 删除文件？（L1，不可逆，需 confirm）
         │    fh_media_delete(file_ids=[...], confirm=true)
         │    支持 dry_run=true 做参数预检（不需要 confirm）
         │    最多一次 100 个 file_id
         │
         └─ 直播流录制控制？
              ├─ action=list  （L0，只读，不拦截，不需要 confirm）
              │    fh_media_record(device_sn, action=list)
              │    → 返回该设备的录制历史（record_id/state/duration/created_by）
              │
              └─ action=start/stop  （L1，需 confirm）
                   fh_media_record(device_sn, action=start, confirm=true, payload_index?)
                   fh_media_record(device_sn, action=stop, confirm=true)
                   ⚠️ 工具描述里写"fh_live_start MUST be called first"，
                      但代码**没有任何校验**去确认直播流确实已启动——
                      这是一条约定而非强制前置条件，详见 Common Mistakes
```

## Common Mistakes

- ❌ 以为 `fh_media_download` 返回的 URL 可以直接用浏览器打开下载 → 工具描述明确警告：存储服务器缺少 `Content-Disposition` 响应头，直接浏览器下载可能产生文件名损坏（如重复扩展名 `.jpegjpeg`）；**必须**用响应里的 `suggested_filename` 显式保存（例如 `curl -o <suggested_filename> '<url>'`），不要依赖浏览器自动推断的文件名
- ❌ 以为 `fh_media_list` 的 `start_index`/`page_size` 传非法值（负数 / 超范围）会报错 → **不会**，两者都是静默回退到默认值（`start_index<0` → 回退到 0；`page_size` 超出 1-500 → 回退到默认 50），不是 clamp 到边界值，也不是报错，调用方看不到任何警告字段
- ❌ 以为 `fh_media_record(action=start/stop)` 调用前必须先确认 `fh_live_start` 已经执行过 → 工具的 `description` 字段写了"MUST be called first"，但 `dispatchRealSdk()` 代码里**零校验**，只是在 `drone.getLiveRecord(liveInfo)` 返回 null 时才报错并提示"Call fh_live_start first"；这是一条**文档层面的约定**，不是代码强制的前置条件——不要替用户在后台强制插入 `fh_live_start` 调用，如果用户明确没有开直播、只是想测试接口，照常放行即可
- ❌ 以为 `fh_media_record` 管理的是无人机机载 SD 卡录像 → **不是**，它控制的是服务端对直播流的录制（`LiveRecordModule`），SD 卡录像走完全独立的 `fh_camera(action=camera_video_start/camera_video_stop)`，两者互不相关、互不感知彼此状态
- ❌ 以为 `fh_media_delete` 的 `file_ids` 可以传空数组走个形式 → 会被显式拒绝（"file_ids cannot be empty"），必须至少 1 个；超过 100 个同样被拒绝，需要分批调用
- ❌ 以为 `fh_media_record(action=list)` 和 `start`/`stop` 一样需要 `confirm` → 不需要，`list` 是只读查询，代码里显式跳过了安全等级拦截和人工确认（只有 `start`/`stop` 两个分支会触发 `enforceSecurityLevel(REVERSIBLE)` + `requireHumanConfirm()`）
- ❌ 以为 `fh_media_list` 不传 `device_sn`/`flight_id` 会报错 → 不会，所有过滤参数都是可选的，不传就是查全部（空字符串传给 SDK 表示不过滤）

## Parameter Cheat Sheet

| 工具 | Security | Confirm | 关键参数 | 备注 |
|---|---|---|---|---|
| `fh_media_list` | L0（未覆写） | 否 | `device_sn`（可选）, `file_type`（photo/video/all，默认 all）, `flight_id`（可选）, `start_time`/`end_time`（ISO 8601，可选）, `start_index`（默认 0）, `page_size`（默认 50，范围 1-500） | 非法 `start_index`/`page_size` 静默回退默认值，不报错；`start_time`/`end_time` 格式非法才会报错 |
| `fh_media_download` | L0（未覆写） | 否 | `file_id`（必填，整数，需 >0） | 返回的 URL 通常 1-24 小时有效；**必须**用 `suggested_filename` 保存文件 |
| `fh_media_delete` | **L1（REVERSIBLE）** | 是 | `file_ids`（数组，1-100 个）, `confirm`, `dry_run`（可选，预检不需 confirm） | 不可逆；超量/空数组直接拒绝，不做截断 |
| `fh_media_record` | `list`=L0；`start`/`stop`=**L1（REVERSIBLE）** | `start`/`stop` 需要，`list` 不需要 | `device_sn`（必填）, `action`（start/stop/list，必填）, `payload_index`（可选，默认 `81-0-0`，未传时自动发现） | 管理**直播流**录制，非机载 SD 卡；`fh_live_start` 前置仅为约定，代码不强制校验 |
