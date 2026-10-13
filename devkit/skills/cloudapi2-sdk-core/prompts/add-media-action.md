# 添加媒体管理功能

## 目标
查询和下载无人机拍摄的媒体文件（照片、视频）。

## 涉及 SDK API
- `workspace.getMediaServiceModule()` → MediaServiceModule
- `mediaModule.getMediaFilesList(params, callback)` — 获取文件列表（注意：`Files` 复数）
- `mediaModule.getMediaFilesDownloadURL(fileId, callback)` — 获取下载链接（注意：`Files` 复数，`URL` 全大写）

## 必须遵守的规则
- ✅ 规则 2：回调中检查 errorCode
- ✅ 规则 5：getWorkspaceInstance / getMediaServiceModule null 检查

## 实现步骤

1. **创建 MediaService**
   - 获取 MediaServiceModule
   - 实现文件列表查询（支持分页/筛选）
   - 实现下载链接获取

2. **创建 Controller**
   - GET `/api/media?deviceSn=xxx&page=1` — 查询文件列表
   - GET `/api/media/{fileId}/download-url` — 获取下载链接

3. **文件列表参数**
   - deviceSn — 指定设备
   - 分页参数（page/size）
   - 时间范围筛选（可选）

## 注意事项
- 下载链接通常有时效性（URL 过期）
- 大文件下载建议代理或重定向，不经 BFF 转发
- 媒体文件可能存储在 DJI 云端，网络延迟需考虑

## 常见错误
- ❌ 不检查 MediaServiceModule 是否为 null
- ❌ 缓存下载链接不考虑过期
- ❌ 通过 BFF 转发大文件内容（应返回 URL 让前端直接下载）
