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

import type { TcasConfig } from '@/types/flightArea'
import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'

const BASE = `${API_BASE}/api/v1/tcas`

/** 查询 TCAS 配置。 */
export async function fetchTcasConfig(): Promise<TcasConfig> {
  const res = await authFetch(BASE)
  if (!res.ok) {
    throw new Error(`fetchTcasConfig failed: ${res.status}`)
  }
  // 断言原因：fetch Response.json() 返回 Promise<unknown>，此处已知 API 响应结构
  return res.json() as Promise<TcasConfig>
}

/** 更新 TCAS 配置。 */
export async function updateTcasConfig(config: TcasConfig): Promise<void> {
  const res = await authFetch(BASE, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config),
  })
  if (!res.ok) {
    throw new Error(`updateTcasConfig failed: ${res.status}`)
  }
}
