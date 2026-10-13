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

/** 媒体文件类型。`other` 是后端按后缀无法归入照片/视频时的兜底值。 */
export type MediaFileType = 'photo' | 'video' | 'other'

/** 媒体列表筛选类型。不含 `other`：SDK 只支持按照片/视频编码过滤，传其他值等同于不过滤。 */
export type MediaFilterType = 'photo' | 'video' | 'all'

/** 媒体文件信息。 */
export interface MediaFileInfo {
  fileId: number
  fileName: string
  fileType: MediaFileType
  fileSize: number
  deviceSn: string
  flightId: string
  createTime: number
  payloadType: string
}

/** 媒体列表响应。 */
export interface MediaListResponse {
  files: MediaFileInfo[]
  totalCount: number
  startIndex: number
  pageSize: number
  hasMore: boolean
}

/** 媒体列表查询参数。 */
export interface MediaListParams {
  deviceSn?: string
  fileType?: MediaFilterType
  startTime?: string
  endTime?: string
  startIndex?: number
  pageSize?: number
}

/** 下载 URL 响应。 */
export interface DownloadUrlResponse {
  fileId: number
  downloadUrl: string
  expiresIn: number
}

/** 批量删除响应。 */
export interface BatchDeleteResponse {
  deletedCount: number
}
