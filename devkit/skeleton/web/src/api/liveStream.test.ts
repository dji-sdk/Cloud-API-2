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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { fetchOnlineDevices } from './liveStream'
import { authFetch } from '@/api/auth'
import { getRuntimeConfig } from '@/utils/runtimeConfig'
import type { DeviceState } from '@/types/api'

vi.mock('@/api/auth', async importOriginal => {
  const actual = await importOriginal<typeof import('@/api/auth')>()
  return { ...actual, authFetch: vi.fn() }
})

vi.mock('@/utils/runtimeConfig', () => ({
  getRuntimeConfig: vi.fn(),
}))

const authFetchMock = vi.mocked(authFetch)
const getRuntimeConfigMock = vi.mocked(getRuntimeConfig)

function make(overrides: Partial<DeviceState> = {}): DeviceState {
  return {
    sn: 'sn',
    deviceType: 'DRONE',
    online: true,
    recentHms: [],
    ...overrides,
  }
}

function okJson(body: unknown): Response {
  return { ok: true, status: 200, json: async () => body } as Response
}

/**
 * 直播大屏在线设备列表单测。
 *
 * 守住的回归：340 拓扑（基站为顶层、机场挂其下）下，机场卡片必须显示**机场自己的 SN**，
 * 而不是把基站 SN 当成机场 SN。此前该路径用扁平 `deviceType === 'DOCK'` 筛选，基站的
 * deviceType 同为 DOCK，直接取顶层 sn 即基站 SN —— 本测试锁死复用 buildDeviceGroups 的修法。
 */
describe('fetchOnlineDevices', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // 关闭 mock 模式，走真实解析分支
    getRuntimeConfigMock.mockReturnValue('false')
  })

  it('shouldUseDockOwnSn_when340BaseStationTopology', async () => {
    const drone = make({ sn: 'DRONE-340', deviceClass: 'drone', deviceType: 'DRONE', model: 'M5TD', name: '飞机-340' })
    const dock = make({ sn: 'DOCK-340', deviceClass: 'dock', deviceType: 'DOCK', model: 'ea340d', name: '机场-340' })
    const baseStation = make({
      sn: 'BS-340',
      deviceClass: 'base_station',
      deviceType: 'DOCK',
      model: 'O4 Ground Station',
      name: '地面站-001',
      childDevices: [drone, dock],
    })
    authFetchMock.mockResolvedValue(okJson([baseStation]))

    const result = await fetchOnlineDevices()

    expect(result).toHaveLength(1)
    // 机场 SN 必须是机场自己的，而非基站 SN
    expect(result[0].dock.sn).toBe('DOCK-340')
    expect(result[0].dock.sn).not.toBe('BS-340')
    expect(result[0].dock.model).toBe('ea340d')
    expect(result[0].name).toBe('机场-340')
    expect(result[0].drone?.sn).toBe('DRONE-340')
    // 基站作为独立节点保留自己的 SN，供直播大屏单独展示「基站」行
    expect(result[0].baseStation?.sn).toBe('BS-340')
    expect(result[0].baseStation?.model).toBe('O4 Ground Station')
  })

  it('shouldOmitBaseStation_whenNormalTopology', async () => {
    const drone = make({ sn: 'drone-1', deviceClass: 'drone', deviceType: 'DRONE', model: 'M30T' })
    const dock = make({
      sn: 'dock-1',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      model: 'DJI Dock 2',
      name: '机场-A',
      childDevices: [drone],
    })
    authFetchMock.mockResolvedValue(okJson([dock]))

    const result = await fetchOnlineDevices()

    expect(result).toHaveLength(1)
    // 普通机场拓扑无基站节点，不应凭空出现基站行
    expect(result[0].baseStation).toBeUndefined()
  })

  it('shouldMapDockAndDrone_whenNormalTopology', async () => {
    const drone = make({ sn: 'drone-1', deviceClass: 'drone', deviceType: 'DRONE', model: 'M30T' })
    const dock = make({
      sn: 'dock-1',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      model: 'DJI Dock 2',
      name: '机场-A',
      childDevices: [drone],
    })
    authFetchMock.mockResolvedValue(okJson([dock]))

    const result = await fetchOnlineDevices()

    expect(result).toHaveLength(1)
    expect(result[0].dock.sn).toBe('dock-1')
    expect(result[0].drone?.sn).toBe('drone-1')
  })

  it('shouldSkipOfflineDock_whenFiltering', async () => {
    const offline = make({ sn: 'dock-off', deviceClass: 'dock', deviceType: 'DOCK', online: false })
    const online = make({ sn: 'dock-on', deviceClass: 'dock', deviceType: 'DOCK', online: true })
    authFetchMock.mockResolvedValue(okJson([offline, online]))

    const result = await fetchOnlineDevices()

    expect(result.map(r => r.dock.sn)).toEqual(['dock-on'])
  })

  it('shouldOmitDrone_whenDockHasNoBoundDrone', async () => {
    const dock = make({ sn: 'dock-nodrone', deviceClass: 'dock', deviceType: 'DOCK', online: true })
    authFetchMock.mockResolvedValue(okJson([dock]))

    const result = await fetchOnlineDevices()

    expect(result).toHaveLength(1)
    expect(result[0].drone).toBeUndefined()
  })

  it('shouldThrow_whenResponseNotOk', async () => {
    authFetchMock.mockResolvedValue({ ok: false, status: 500 } as Response)

    await expect(fetchOnlineDevices()).rejects.toThrow('获取在线设备列表失败: 500')
  })
})
