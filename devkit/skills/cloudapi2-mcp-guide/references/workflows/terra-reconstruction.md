# 3D Reconstruction (Terra) — Decision Tree

## Quick Decision Matrix

| User has | Correct tool(s) | Key parameter |
|---|---|---|
| 要用飞行影像生成正射/3D 模型/点云 | `fh_terra(action=create_reconstruction)` | `media_ids`, `reconstruction_type`, `confirm=true` |
| 要生成全景图 | `fh_terra(action=create_panorama)` | `media_ids`, `name`, `confirm=true` |
| 要查建模任务进度 | `fh_terra(action=status)` | `task_id` |
| 要查已有模型文件列表 | `fh_terra(action=model_list)` | 全部可选（分页） |
| 要下载模型文件 | `fh_terra(action=model_url)` | `file_id`（先用 model_list 查到） |
| 要查质量报告 | `fh_terra(action=quality_report)` | `task_id` |
| 要删除模型文件 | `fh_terra(action=delete_model)` | `file_id`, `confirm=true` |
| 要重试失败的建模任务 | `fh_terra(action=retry)` | `task_id`, `confirm=true` |

## Decision Flow

```
用户意图：三维重建/全景图/建模进度/模型管理
         │
         ├─ 创建新任务？
         │    ├─ 正射影像/3D 网格/点云
         │    │    → fh_terra(action=create_reconstruction, media_ids=[...],
         │    │                reconstruction_type=ortho_2d|mesh_3d|point_cloud, confirm=true)
         │    └─ 全景图
         │         → fh_terra(action=create_panorama, media_ids=[...], name=..., confirm=true)
         │
         ├─ 查询类？
         │    ├─ 任务进度       → fh_terra(action=status, task_id=...)
         │    ├─ 模型文件列表    → fh_terra(action=model_list, page=?, page_size=?)
         │    ├─ 模型下载链接    → fh_terra(action=model_url, file_id=...)   [先 model_list 拿 file_id]
         │    └─ 质量报告       → fh_terra(action=quality_report, task_id=...)
         │
         └─ 管理类（均需 confirm=true）？
              ├─ 删除模型文件 → fh_terra(action=delete_model, file_id=..., confirm=true)
              └─ 重试失败任务 → fh_terra(action=retry, task_id=..., confirm=true)
```

## Common Mistakes

- ❌ 以为只有 `create_reconstruction`/`create_panorama` 需要 `confirm=true` → **`delete_model` 和 `retry` 同样需要 `confirm=true`**。工具内部 `isCreateAction()` 的真实判断集合是四者都算"写操作"，不要只看类注释里"L1 仅指 create_* "的描述，要按实际拦截行为处理
- ❌ 直接传 `file_id` 却不知道从哪来 → 先调 `action=model_list` 拿到 `file_id`，再用于 `model_url`/`delete_model`
- ❌ `reconstruction_type` 传了数字（0/1/2）而不是字符串 → 该参数在工具层是字符串枚举 `ortho_2d`/`mesh_3d`/`point_cloud`，数字映射只是内部转给 SDK 用，AI 调用时必须传字符串
- ❌ `create_panorama` 漏传 `name` → 该 action 的 `name` 是必填项（`create_reconstruction` 的 `name` 则是可选，省略会自动生成）
- ❌ `model_url`/`delete_model` 的 `file_id` 传了非正整数或字符串形式的非数字 → 必须是正整数，服务端会直接校验失败
- ❌ 把 `status`/`quality_report` 需要的 `task_id` 和 `model_url`/`delete_model` 需要的 `file_id` 搞混 → `task_id` 是重建任务 UUID（`file_uuid`），`file_id` 是模型文件的数字 ID，两者不可互换

## Parameter Cheat Sheet

### fh_terra (action=create_reconstruction)

| Parameter | Value | Note |
|---|---|---|
| `media_ids` | 媒体文件夹 UUID 数组 | 必填 |
| `reconstruction_type` | ortho_2d / mesh_3d / point_cloud | 必填，映射 SDK 枚举：`ortho_2d=0, mesh_3d=1, point_cloud=2` |
| `name` | 任务名称 | 可选，省略则自动生成 |
| `wkt` | WKT 区域坐标 | 可选 |
| `confirm` | true | 必填 |

### fh_terra (action=create_panorama)

| Parameter | Value | Note |
|---|---|---|
| `media_ids` | 媒体文件夹 UUID 数组 | 必填 |
| `name` | 任务名称 | **必填**（与 create_reconstruction 不同） |
| `wkt` | WKT 区域坐标 | 可选 |
| `confirm` | true | 必填 |

### fh_terra (action=status / quality_report)

| Parameter | Value | Note |
|---|---|---|
| `task_id` | 重建任务 ID（file_uuid） | 必填 |
| `language` | 报告语言，默认 zh | 仅 quality_report 使用 |

### fh_terra (action=model_list)

| Parameter | Value | Note |
|---|---|---|
| `page` | 默认 1 | 可选 |
| `page_size` | 默认 20，最大 100 | 可选 |

### fh_terra (action=model_url)

| Parameter | Value | Note |
|---|---|---|
| `file_id` | 模型文件 ID（正整数） | 必填，先用 model_list 查到 |
| `model_type` | 0=正射, 1=3D 网格, 2=点云，默认 0 | 可选 |

### fh_terra (action=delete_model / retry)

| Parameter | Value | Note |
|---|---|---|
| `file_id` | 模型文件 ID（delete_model 必填，整数） | — |
| `task_id` | 重建任务 ID（retry 必填） | — |
| `confirm` | true | 两者均必填 |
