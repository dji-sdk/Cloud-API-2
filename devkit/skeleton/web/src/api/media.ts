/**
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import type { MediaListParams, MediaListResponse, DownloadUrlResponse, BatchDeleteResponse } from '@/types/media'
import { authFetch, getToken } from '@/api/auth'
import { API_BASE } from './baseUrl'

const BASE = `${API_BASE}/api/v1/media`

/** 预检取流用的 Range 头：只取首字节，避免为了一次探测把整个文件拉下来。 */
export const PROBE_RANGE = 'bytes=0-0'

/**
 * 构造媒体取流地址（走后端代理端点 `GET /api/v1/media/{fileId}/content`）。
 *
 * <p>不可把 SDK 返回的预签名 URL 直接交给 `<img>` / `<video>`：那个地址指向对象存储的
 * 内网端口 :30802，客户网段不可达，表现为缩略图正常但点开预览全黑。
 * 走后端代理后浏览器只需与本服务通信，由服务端回源对象存储。
 *
 * <p>原生标签无法自定义请求头，token 只能经 query 传递 —— 与 useTerra.ts 的 EventSource
 * 是同一条回退路径，后端 `JwtAuthenticationProvider.extractToken` 已支持。
 *
 * @param fileId   媒体文件 ID
 * @param download 是否要求以附件形式下载（触发 `Content-Disposition: attachment`）
 * @returns 可直接作为原生标签 src 或下载链接的完整地址
 */
export function buildMediaContentUrl(fileId: number, download = false): string {
  const params = new URLSearchParams()
  const token = getToken()
  if (token) {
    params.set('token', token)
  }
  if (download) {
    params.set('download', 'true')
  }
  const qs = params.toString()
  return qs ? `${BASE}/${fileId}/content?${qs}` : `${BASE}/${fileId}/content`
}

/**
 * 探测媒体取流是否真的可用（预检）。
 *
 * <p>下载走的是 `<a>` 导航而非 `fetch`，浏览器不会把失败回传给页面，点击一个不可用的
 * 地址与点击一个可用地址在调用侧毫无差别 —— 客户网段下就是「点了没反应」。
 * 故此处先发一个只取首字节的 Range 请求，把可达性判断提前到触发下载之前。
 *
 * @param fileId 媒体文件 ID
 * @returns 可达时 resolve；不可达时 reject 并附带可展示的中文原因
 */
export async function probeMediaContent(fileId: number): Promise<void> {
  const res = await authFetch(`${BASE}/${fileId}/content`, {
    headers: { Range: PROBE_RANGE },
  })
  // 只关心状态码，响应体立刻断开：上游万一忽略 Range 回 200，整段视频也不该被拉进内存。
  await res.body?.cancel().catch(() => undefined)
  if (!res.ok) {
    throw new Error(`媒体文件不可用（HTTP ${res.status}）`)
  }
}

/** 获取媒体文件列表。 */
export async function fetchMediaList(params: MediaListParams = {}): Promise<MediaListResponse> {
  const query = new URLSearchParams()
  if (params.deviceSn) {
    query.set('device_sn', params.deviceSn)
  }
  if (params.fileType && params.fileType !== 'all') {
    query.set('file_type', params.fileType)
  }
  if (params.startTime) {
    query.set('start_time', params.startTime)
  }
  if (params.endTime) {
    query.set('end_time', params.endTime)
  }
  if (params.startIndex !== undefined) {
    query.set('start_index', String(params.startIndex))
  }
  if (params.pageSize !== undefined) {
    query.set('page_size', String(params.pageSize))
  }
  const qs = query.toString()
  const url = qs ? `${BASE}?${qs}` : BASE
  const res = await authFetch(url)
  if (!res.ok) {
    throw new Error(`fetchMediaList failed: ${res.status}`)
  }
  return res.json() as Promise<MediaListResponse>
}

/** 获取媒体文件下载链接。 */
export async function fetchDownloadUrl(fileId: number): Promise<DownloadUrlResponse> {
  const res = await authFetch(`${BASE}/${fileId}/download-url`)
  if (!res.ok) {
    throw new Error(`fetchDownloadUrl failed: ${res.status}`)
  }
  return res.json() as Promise<DownloadUrlResponse>
}

/** 批量删除媒体文件。 */
export async function batchDeleteMedia(fileIds: number[]): Promise<BatchDeleteResponse> {
  const res = await authFetch(`${BASE}/batch-delete`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ fileIds }),
  })
  if (!res.ok) {
    throw new Error(`batchDeleteMedia failed: ${res.status}`)
  }
  return res.json() as Promise<BatchDeleteResponse>
}
