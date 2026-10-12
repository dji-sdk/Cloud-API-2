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
import { mount, flushPromises } from '@vue/test-utils'
import DeviceSelector from './DeviceSelector.vue'
import type { DeviceState } from '@/types/api'

// mock api：避免真实网络
const mockFetchDevices = vi.fn()

vi.mock('@/api/devices', () => ({
  fetchDevices: (...args: unknown[]) => mockFetchDevices(...args),
}))

function makeDrone(overrides: Partial<DeviceState> = {}): DeviceState {
  return {
    sn: 'drone-sn-1',
    deviceType: 'DRONE',
    online: true,
    recentHms: [],
    ...overrides,
  }
}

function makeDock(drones: DeviceState[]): DeviceState {
  return {
    sn: 'dock-sn-1',
    deviceType: 'DOCK',
    name: '机场A',
    online: true,
    recentHms: [],
    childDevices: drones,
  }
}

async function mountSelector(devices: DeviceState[]) {
  mockFetchDevices.mockResolvedValue(devices)
  // DeviceMap 依赖 Cesium 全局对象（CDN 注入），测试环境不具备，stub 掉子组件
  const wrapper = mount(DeviceSelector, {
    global: { stubs: { DeviceMap: true } },
  })
  await flushPromises()
  return wrapper
}

describe('DeviceSelector 飞机名称显示', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('name 与 model 同时存在时优先显示 name', async () => {
    const drone = makeDrone({ name: '我的飞机', model: 'M30T' })
    const wrapper = await mountSelector([makeDock([drone])])

    expect(wrapper.find('.drone-name').text()).toBe('我的飞机')
  })

  it('name 缺失时回退到 model', async () => {
    const drone = makeDrone({ name: undefined, model: 'M30T' })
    const wrapper = await mountSelector([makeDock([drone])])

    expect(wrapper.find('.drone-name').text()).toBe('M30T')
  })

  it('name 和 model 都缺失时回退到 sn', async () => {
    const drone = makeDrone({ name: undefined, model: undefined, sn: 'sn-only' })
    const wrapper = await mountSelector([makeDock([drone])])

    expect(wrapper.find('.drone-name').text()).toBe('sn-only')
  })
})
