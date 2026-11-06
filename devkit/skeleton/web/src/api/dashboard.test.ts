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

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

// Mock 开关取值供 mock 工厂在调用时读取。dashboard 走运行时配置（window.__FH_CONFIG__），
// 而不是 import.meta.env —— 容器部署时只有前者会被注入。
let mockEnabled = 'false'

vi.mock('@/utils/runtimeConfig', () => ({
  getRuntimeConfig: (key: string, fallback = '') => {
    if (key === 'MOCK_ENABLED') {
      return mockEnabled
    }
    return fallback
  },
}))

/** 后端返回的最小看板统计体。 */
const SERVER_STATS = {
  docks: { total: 9, idle: 9, working: 0, debugging: 0, offline: 0 },
  drones: { total: 9, online: 0, offline: 9 },
  relays: { total: 0, online: 0, offline: 0 },
  counts: { waylines: 0, personnel: { value: 0 } },
  dataResults: {
    media: { total: 0, trend: [] },
    models: { total: 0, trend: [] },
    reports: { total: 0, trend: [] },
  },
  tasks: { daily: [] },
  execution: { daily: [] },
  flight: { count: 0, durationMinutes: 0, distanceKm: { value: 0 }, daily: [] },
  devices: [],
}

describe('dashboard api', () => {
  beforeEach(() => {
    vi.resetModules()
    localStorage.clear()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('fetchDashboardStats_shouldReturnLocalMockAndSkipBackend_whenRuntimeMockEnabled', async () => {
    mockEnabled = 'true'
    const fetchSpy = vi.fn(async () =>
      new Response(JSON.stringify(SERVER_STATS), {
        status: 200,
        headers: { 'content-type': 'application/json' },
      }),
    )
    vi.stubGlobal('fetch', fetchSpy)

    const { fetchDashboardStats } = await import('@/api/dashboard')
    const stats = await fetchDashboardStats('month')

    expect(fetchSpy).not.toHaveBeenCalled()
    // 本地 mock 的固定值，与后端返回的 9 台区分开
    expect(stats.docks.total).toBe(2)
  })

  it('fetchDashboardStats_shouldCallBackend_whenRuntimeMockDisabled', async () => {
    mockEnabled = 'false'
    const fetchSpy = vi.fn(async () =>
      new Response(JSON.stringify(SERVER_STATS), {
        status: 200,
        headers: { 'content-type': 'application/json' },
      }),
    )
    vi.stubGlobal('fetch', fetchSpy)

    const { fetchDashboardStats } = await import('@/api/dashboard')
    const stats = await fetchDashboardStats('month')

    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy.mock.calls[0]?.[0]).toContain('/api/v1/dashboard/stats')
    expect(stats.docks.total).toBe(9)
  })
})
