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

/** 媒体文件夹（重建数据源） */
export interface Folder {
  folderId: number
  folderUuid: string | null
  folderName: string | null
}

/** 模型文件 */
export interface ModelFile {
  fileId: number
  uuid: string
  name: string
  fileType: string
  subFileType: string
  state: string
  reconstructionType: 'ortho_2d' | 'mesh_3d' | 'point_cloud' | 'panorama'
  thumbnailUrl: string | null
  createdAt: number
}

/** 分页结果 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/** 模型下载链接 */
export interface ModelUrl {
  url: string
  expiresAt: string
}

/** 创建重建请求 */
export interface CreateReconstructionRequest {
  name: string
  /** 媒体文件夹 id 列表（数字，优先使用，按文件夹选择） */
  folderIds?: number[]
  /** 媒体文件夹 UUID 列表（兼容按航线/手动输入） */
  folderUuids?: string[]
  reconstructionType: 'ortho_2d' | 'mesh_3d' | 'point_cloud'
  wkt?: string
}

/** 创建全景请求 */
export interface CreatePanoramaRequest {
  name: string
  folderUuids: string[]
  wkt?: string
}

/** 重建任务结果 */
export interface ReconstructionTask {
  id: number
  uuid: string
  jobUuid: string
  status: string
}

/** 任务状态 */
export interface TaskStatus {
  taskId: string
  state: 'processing' | 'success' | 'failed'
  progress: number
  mediaInputObjectKey: string
  sfmOutputObjectPrefix: string
}

/** 质量报告 */
export interface QualityReport {
  qualityReportUrl: string
}

/** SfM 结果 */
export interface SfmResult {
  outputPrefix: string
  cameraCount: number
  pointCount: number
  matchedPercentage: number
}

/** 原始影像 */
export interface ImageItem {
  id: string
  name: string
  url: string
}

/** 模型详情（3D 查看器用） */
export interface ModelDetail {
  fileId: number
  uuid: string
  name: string
  reconstructionType: ReconstructionType
  url: string
  thumbnailUrl: string
  previewUrl: string
  latitude: number
  longitude: number
  altitude: number
  transform: number[]
  createdAt: number
}

/** 批量删除请求 */
export interface BatchDeleteRequest {
  fileIds: number[]
}

/** 重建类型选项 */
export type ReconstructionType = 'ortho_2d' | 'mesh_3d' | 'point_cloud' | 'panorama' | '3dgs' | 'lidar'

/** 存储统计信息（Sidebar 展示用） */
export interface StorageStats {
  mediaTotalGB: number
  modelTotalGB: number
  model2dGB: number
  model3dGB: number
  gcpCount: number
  recycleBinMB: number
}

/** Sidebar 导航项 */
export interface SidebarNavItem {
  key: string
  label: string
  icon: string
  size?: string
  children?: SidebarNavItem[]
  active?: boolean
}
