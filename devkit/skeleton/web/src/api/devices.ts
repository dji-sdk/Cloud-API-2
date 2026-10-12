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

import type { DeviceState } from '@/types/api'
import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'

const BASE = `${API_BASE}/api/v1/devices`

/** 获取全量设备列表。 */
export async function fetchDevices(): Promise<DeviceState[]> {
  const res = await authFetch(BASE)
  if (!res.ok) {
    throw new Error(`fetchDevices failed: ${res.status}`)
  }
  return res.json() as Promise<DeviceState[]>
}

/** 获取单设备详情；404 时返回 null。 */
export async function fetchDevice(sn: string): Promise<DeviceState | null> {
  const res = await authFetch(`${BASE}/${encodeURIComponent(sn)}`)
  if (res.status === 404) {
    return null
  }
  if (!res.ok) {
    throw new Error(`fetchDevice failed: ${res.status}`)
  }
  // 断言原因：fetch Response.json() 返回 Promise<unknown>，此处已知 API 响应结构
  return res.json() as Promise<DeviceState>
}
