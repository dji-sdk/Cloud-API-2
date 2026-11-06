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
import { defineComponent } from 'vue'
import { mount, flushPromises } from '@vue/test-utils'
import DeviceSelector from './DeviceSelector.vue'
import type { DeviceState } from '@/types/api'
import type { DeviceMarker } from '@/types/dashboard'

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

describe('DeviceSelector 地图标注类型', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  /**
   * 挂载并取回传给 DeviceMap 的标注点。
   *
   * <p>DeviceMap 依赖 Cesium 全局对象，测试环境不可用，故用收集 props 的桩组件替代；
   * 数据在 onMounted 后异步到达，需等待 flushPromises 再读 props。
   */
  async function mountAndGetMarkers(devices: DeviceState[]): Promise<DeviceMarker[]> {
    const DeviceMapStub = defineComponent({
      name: 'DeviceMap',
      props: { devices: { type: Array, required: true } },
      setup: () => () => null,
    })
    mockFetchDevices.mockResolvedValue(devices)
    const wrapper = mount(DeviceSelector, {
      global: { stubs: { DeviceMap: DeviceMapStub } },
    })
    await flushPromises()
    const map = wrapper.findComponent(DeviceMapStub)
    return map.props('devices') as DeviceMarker[]
  }

  it('基站标注应为 dock，而不是被误判为 drone', async () => {
    const baseStation: DeviceState = {
      sn: 'BS-1',
      deviceType: 'DOCK',
      deviceClass: 'base_station',
      online: true,
      recentHms: [],
      childDevices: [],
    }
    const drone = makeDrone({ sn: 'DRONE-1', deviceClass: 'drone' })

    const markers = await mountAndGetMarkers([baseStation, drone])

    const bySn = new Map(markers.map(m => [m.sn, m.type]))
    expect(bySn.get('BS-1')).toBe('dock')
    expect(bySn.get('DRONE-1')).toBe('drone')
  })

  it('deviceClass 缺省时回退 deviceType 判定标注类型', async () => {
    const dock: DeviceState = {
      sn: 'DOCK-1',
      deviceType: 'DOCK',
      deviceClass: null,
      online: true,
      recentHms: [],
    }
    const drone = makeDrone({ sn: 'DRONE-1', deviceClass: null })

    const markers = await mountAndGetMarkers([dock, drone])

    const bySn = new Map(markers.map(m => [m.sn, m.type]))
    expect(bySn.get('DOCK-1')).toBe('dock')
    expect(bySn.get('DRONE-1')).toBe('drone')
  })
})

describe('DeviceSelector 340 拓扑（基站→机场→飞机）设备 SN 显示', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('机场卡片分别展示机场、基站、飞机，机场 SN 不被基站 SN 顶替', async () => {
    const drone = makeDrone({ sn: 'DRONE-340', deviceClass: 'drone', name: '飞机-340' })
    const dock: DeviceState = {
      sn: 'DOCK-340',
      deviceType: 'DOCK',
      deviceClass: 'dock',
      name: 'ea340d',
      online: true,
      recentHms: [],
      childDevices: [drone],
    }
    const baseStation: DeviceState = {
      sn: 'BS-340',
      deviceType: 'DOCK',
      deviceClass: 'base_station',
      name: '地面站-001',
      online: true,
      recentHms: [],
      childDevices: [dock],
    }

    const wrapper = await mountSelector([baseStation])

    // 机场、基站各有一条带类别标签的 infra 行
    const labels = wrapper.findAll('.infra-label').map(n => n.text())
    expect(labels).toContain('机场')
    expect(labels).toContain('基站')

    // 两条 infra 行分别展示机场 SN 与基站 SN —— 机场 SN 不再被基站 SN 顶替
    const sns = wrapper.findAll('.infra-row .meta-value.mono').map(n => n.text())
    expect(sns).toContain('DOCK-340')
    expect(sns).toContain('BS-340')

    // 挂在机场下的飞机（基站的孙节点）正常展示
    expect(wrapper.find('.drone-name').text()).toBe('飞机-340')
    expect(wrapper.find('.drone-sn').text()).toBe('DRONE-340')
  })

  it('进驾驶舱时下发 340 网关 SN（飞行器自身）而非机场/基站 SN', async () => {
    const drone = makeDrone({ sn: 'DRONE-340', deviceClass: 'drone' })
    const dock: DeviceState = {
      sn: 'DOCK-340',
      deviceType: 'DOCK',
      deviceClass: 'dock',
      name: 'ea340d',
      online: true,
      recentHms: [],
      childDevices: [drone],
    }
    const baseStation: DeviceState = {
      sn: 'BS-340',
      deviceType: 'DOCK',
      deviceClass: 'base_station',
      name: '地面站-001',
      online: true,
      recentHms: [],
      childDevices: [dock],
    }

    const wrapper = await mountSelector([baseStation])
    await wrapper.find('.btn-action--primary').trigger('click')

    const selectEvents = wrapper.emitted('select')
    expect(selectEvents).toBeTruthy()
    // 340 组网下飞行器直连云端，驾驶舱/直播 RTC 网关即飞行器自身 SN，
    // 既不是基站 SN（BS-340）也不是机场 SN（DOCK-340）——见 resolveGatewaySn
    expect(selectEvents?.[0]).toEqual(['DRONE-340', 'DRONE-340'])
  })
})
