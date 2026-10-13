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

import type { OfflineMapModelInfo, OfflineMapUpdateState } from '@/types/flightArea'
import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'

const BASE = `${API_BASE}/api/v1/offline-maps`

/** 查询地形模型信息。 */
export async function fetchOfflineMapModelInfo(): Promise<OfflineMapModelInfo> {
  const res = await authFetch(`${BASE}/model-info`)
  if (!res.ok) {
    throw new Error(`fetchOfflineMapModelInfo failed: ${res.status}`)
  }
  return res.json() as Promise<OfflineMapModelInfo>
}

/** 设置地形障碍物开关。 */
export async function setOfflineMapEnabled(enabled: boolean): Promise<void> {
  const res = await authFetch(`${BASE}/enabled`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ enabled }),
  })
  if (!res.ok) {
    throw new Error(`setOfflineMapEnabled failed: ${res.status}`)
  }
}

/** 触发地形数据更新（异步）。 */
export async function triggerOfflineMapUpdate(): Promise<void> {
  const res = await authFetch(`${BASE}/update`, { method: 'POST' })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '更新失败' })) as { error: string }
    throw new Error(body.error || '更新失败')
  }
}

/** 查询更新进度。 */
export async function fetchOfflineMapUpdateStatus(): Promise<OfflineMapUpdateState> {
  const res = await authFetch(`${BASE}/update-status`)
  if (!res.ok) {
    throw new Error(`fetchOfflineMapUpdateStatus failed: ${res.status}`)
  }
  return res.json() as Promise<OfflineMapUpdateState>
}

/** 取消地形数据更新。 */
export async function cancelOfflineMapUpdate(): Promise<void> {
  const res = await authFetch(`${BASE}/update`, { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(`cancelOfflineMapUpdate failed: ${res.status}`)
  }
}

/** 重置更新状态（前端确认终态后调用）。 */
export async function resetOfflineMapUpdateState(): Promise<void> {
  const res = await authFetch(`${BASE}/update-reset`, { method: 'POST' })
  if (!res.ok) {
    throw new Error(`resetOfflineMapUpdateState failed: ${res.status}`)
  }
}
