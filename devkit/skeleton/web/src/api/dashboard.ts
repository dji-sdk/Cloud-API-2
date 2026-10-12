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

import type { DashboardStats, TimeRange } from '@/types/dashboard'
import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'

const BASE = `${API_BASE}/api/v1/dashboard`

const MOCK_STATS: DashboardStats = {
  docks: { total: 2, idle: 1, working: 1, debugging: 0, offline: 0 },
  drones: { total: 3, online: 2, offline: 1 },
  relays: { total: 0, online: 0, offline: 0, _mock: true },
  counts: { waylines: 18, personnel: { value: 6, _mock: true } },
  dataResults: {
    media: { total: 128, trend: [12, 18, 9, 22, 15, 20, 32] },
    models: { total: 14, trend: [1, 2, 1, 3, 1, 2, 4] },
    reports: { total: 5, trend: [0, 1, 1, 0, 1, 1, 1], _mock: true },
  },
  tasks: { daily: [] },
  execution: { daily: [] },
  flight: {
    count: 256,
    durationMinutes: 7680,
    distanceKm: { value: 0, _mock: true },
    daily: [],
  },
  devices: [
    { sn: 'DOCK-SZ-001', name: '深圳坂田机场', type: 'dock', lat: 22.6765, lng: 114.0527, status: 'online' },
    { sn: 'DOCK-SZ-002', name: '深圳龙华机场', type: 'dock', lat: 22.7092, lng: 114.0369, status: 'online' },
    { sn: 'DRONE-M3E-001', name: '御3行业版-01', type: 'drone', lat: 22.6831, lng: 114.0612, status: 'online' },
    { sn: 'DRONE-M3E-002', name: '御3行业版-02', type: 'drone', lat: 22.7015, lng: 114.0421, status: 'online' },
  ],
}

/**
 * 获取看板聚合统计数据。
 */
export async function fetchDashboardStats(
  range: TimeRange = 'month',
  start?: string,
  end?: string,
): Promise<DashboardStats> {
  if (import.meta.env.VITE_MOCK_ENABLED === 'true') {
    // 返回深拷贝，防止调用方变更污染模块级常量
    return structuredClone(MOCK_STATS)
  }

  const params = new URLSearchParams({ range })
  if (start) {
    params.set('start', start)
  }
  if (end) {
    params.set('end', end)
  }
  const res = await authFetch(`${BASE}/stats?${params}`)
  if (!res.ok) {
    throw new Error(`fetchDashboardStats failed: ${res.status}`)
  }
  return res.json() as Promise<DashboardStats>
}
