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

import type { FlightArea, CreateFlightAreaRequest, UpdateFlightAreaRequest, FlightAreaType, FlightAreaStatus } from '@/types/flightArea'
import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'

const BASE = `${API_BASE}/api/v1/flight-areas`

/** 查询飞行区域列表。 */
export async function fetchFlightAreas(params?: {
  type?: FlightAreaType
  status?: FlightAreaStatus
  name?: string
  pageIndex?: number
  pageSize?: number
}): Promise<FlightArea[]> {
  const query = new URLSearchParams()
  if (params?.type) query.set('type', params.type)
  if (params?.status) query.set('status', params.status)
  if (params?.name) query.set('name', params.name)
  if (params?.pageIndex != null) query.set('pageIndex', String(params.pageIndex))
  if (params?.pageSize != null) query.set('pageSize', String(params.pageSize))

  const url = query.toString() ? `${BASE}?${query}` : BASE
  const res = await authFetch(url)
  if (!res.ok) {
    throw new Error(`fetchFlightAreas failed: ${res.status}`)
  }
  // 断言原因：fetch Response.json() 返回 Promise<unknown>，此处已知 API 响应结构
  return res.json() as Promise<FlightArea[]>
}

/** 创建飞行区域。 */
export async function createFlightArea(req: CreateFlightAreaRequest): Promise<FlightArea> {
  const res = await authFetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '创建失败' })) as { error: string }
    throw new Error(body.error || '创建失败')
  }
  // 断言原因：fetch Response.json() 返回 Promise<unknown>，此处已知 API 响应结构
  return res.json() as Promise<FlightArea>
}

/** 更新飞行区域。 */
export async function updateFlightArea(id: string, req: UpdateFlightAreaRequest): Promise<FlightArea> {
  const res = await authFetch(`${BASE}/${encodeURIComponent(id)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (!res.ok) {
    throw new Error(`updateFlightArea failed: ${res.status}`)
  }
  // 断言原因：fetch Response.json() 返回 Promise<unknown>，此处已知 API 响应结构
  return res.json() as Promise<FlightArea>
}

/** 删除飞行区域。 */
export async function deleteFlightArea(id: string): Promise<void> {
  const res = await authFetch(`${BASE}/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  })
  if (!res.ok && res.status !== 404) {
    throw new Error(`deleteFlightArea failed: ${res.status}`)
  }
}
