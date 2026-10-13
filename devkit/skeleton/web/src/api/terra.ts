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

import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'
import type {
  BatchDeleteRequest,
  CreatePanoramaRequest,
  CreateReconstructionRequest,
  Folder,
  ImageItem,
  ModelDetail,
  ModelFile,
  ModelUrl,
  PageResult,
  QualityReport,
  ReconstructionTask,
  SfmResult,
  TaskStatus,
} from '@/types/terra'

const BASE = `${API_BASE}/api/v1/terra`

/** 媒体文件夹列表（重建数据源） */
export async function fetchFolders(): Promise<Folder[]> {
  const res = await authFetch(`${BASE}/folders`)
  if (!res.ok) {
    throw new Error(`fetchFolders failed: ${res.status}`)
  }
  return res.json() as Promise<Folder[]>
}

/** 模型列表 */
export async function fetchModels(
  page: number = 1,
  pageSize: number = 20,
  type?: string,
): Promise<PageResult<ModelFile>> {
  const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize) })
  if (type) {
    params.set('type', type)
  }
  const res = await authFetch(`${BASE}/models?${params}`)
  if (!res.ok) {
    throw new Error(`fetchModels failed: ${res.status}`)
  }
  return res.json() as Promise<PageResult<ModelFile>>
}

/** 获取模型下载链接 */
export async function fetchModelUrl(fileId: number): Promise<ModelUrl> {
  const res = await authFetch(`${BASE}/models/${fileId}/url`)
  if (!res.ok) {
    throw new Error(`fetchModelUrl failed: ${res.status}`)
  }
  return res.json() as Promise<ModelUrl>
}

/** 获取模型详情（3D 查看器用） */
export async function fetchModelDetail(fileId: number): Promise<ModelDetail> {
  const res = await authFetch(`${BASE}/models/${fileId}/detail`)
  if (!res.ok) {
    throw new Error(`fetchModelDetail failed: ${res.status}`)
  }
  return res.json() as Promise<ModelDetail>
}

/** 删除单个模型 */
export async function deleteModel(fileId: number): Promise<void> {
  const res = await authFetch(`${BASE}/models/${fileId}`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(`deleteModel failed: ${res.status}`)
  }
}

/** 批量删除模型 */
export async function batchDeleteModels(fileIds: number[]): Promise<void> {
  const body: BatchDeleteRequest = { fileIds }
  const res = await authFetch(`${BASE}/models/batch`, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    throw new Error(`batchDeleteModels failed: ${res.status}`)
  }
}

/** 创建重建任务 */
export async function createReconstruction(
  request: CreateReconstructionRequest,
): Promise<ReconstructionTask> {
  const res = await authFetch(`${BASE}/tasks/reconstruction`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!res.ok) {
    const errorBody = await res.json().catch(() => null) as { message?: string } | null
    const msg = errorBody?.message ?? `创建重建失败: HTTP ${res.status}`
    throw new Error(msg)
  }
  return res.json() as Promise<ReconstructionTask>
}

/** 创建全景任务 */
export async function createPanorama(request: CreatePanoramaRequest): Promise<ReconstructionTask> {
  const res = await authFetch(`${BASE}/tasks/panorama`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!res.ok) {
    throw new Error(`createPanorama failed: ${res.status}`)
  }
  return res.json() as Promise<ReconstructionTask>
}

/** 查询任务状态 */
export async function fetchTaskStatus(taskId: string): Promise<TaskStatus> {
  const res = await authFetch(`${BASE}/tasks/${encodeURIComponent(taskId)}/status`)
  if (!res.ok) {
    throw new Error(`fetchTaskStatus failed: ${res.status}`)
  }
  return res.json() as Promise<TaskStatus>
}

/** 重建任务列表（任务页初始加载） */
export async function fetchTasks(page: number = 1, pageSize: number = 20): Promise<PageResult<TaskStatus>> {
  const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize) })
  const res = await authFetch(`${BASE}/tasks?${params}`)
  if (!res.ok) {
    throw new Error(`fetchTasks failed: ${res.status}`)
  }
  return res.json() as Promise<PageResult<TaskStatus>>
}

/** 重试任务 */
export async function retryTask(taskId: string): Promise<void> {
  const res = await authFetch(`${BASE}/tasks/${encodeURIComponent(taskId)}/retry`, {
    method: 'POST',
  })
  if (!res.ok) {
    throw new Error(`retryTask failed: ${res.status}`)
  }
}

/** 停止任务 */
export async function stopTask(taskId: string): Promise<void> {
  const res = await authFetch(`${BASE}/tasks/${encodeURIComponent(taskId)}/stop`, {
    method: 'POST',
  })
  if (!res.ok) {
    throw new Error(`stopTask failed: ${res.status}`)
  }
}

/** 质量报告 */
export async function fetchQualityReport(
  taskId: string,
  language: string = 'zh',
): Promise<QualityReport> {
  const res = await authFetch(
    `${BASE}/tasks/${encodeURIComponent(taskId)}/report?language=${language}`,
  )
  if (!res.ok) {
    throw new Error(`fetchQualityReport failed: ${res.status}`)
  }
  return res.json() as Promise<QualityReport>
}

/** SfM 结果 */
export async function fetchSfmResult(taskId: string): Promise<SfmResult> {
  const res = await authFetch(`${BASE}/tasks/${encodeURIComponent(taskId)}/sfm`)
  if (!res.ok) {
    throw new Error(`fetchSfmResult failed: ${res.status}`)
  }
  return res.json() as Promise<SfmResult>
}

/** 原始影像列表 */
export async function fetchReconstructionImages(taskId: string): Promise<ImageItem[]> {
  const res = await authFetch(`${BASE}/tasks/${encodeURIComponent(taskId)}/images`)
  if (!res.ok) {
    throw new Error(`fetchReconstructionImages failed: ${res.status}`)
  }
  return res.json() as Promise<ImageItem[]>
}
