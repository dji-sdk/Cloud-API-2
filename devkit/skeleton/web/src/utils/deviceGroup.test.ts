/*
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

import { describe, it, expect } from 'vitest'
import { deviceCategory, buildDeviceGroups, buildDroneGatewayIndex } from './deviceGroup'
import type { DeviceState } from '@/types/api'

function make(overrides: Partial<DeviceState> = {}): DeviceState {
  return {
    sn: 'sn',
    deviceType: 'DRONE',
    online: true,
    recentHms: [],
    ...overrides,
  }
}

describe('deviceCategory', () => {
  it('识别 base_station（deviceType 为 DOCK 时也不误判为机场）', () => {
    const bs = make({ deviceClass: 'base_station', deviceType: 'DOCK' })
    expect(deviceCategory(bs)).toBe('base_station')
  })

  it('识别 dock', () => {
    expect(deviceCategory(make({ deviceClass: 'dock', deviceType: 'DOCK' }))).toBe('dock')
  })

  it('识别 drone', () => {
    expect(deviceCategory(make({ deviceClass: 'drone', deviceType: 'DRONE' }))).toBe('drone')
  })

  it('deviceClass 大小写不敏感', () => {
    expect(deviceCategory(make({ deviceClass: 'BASE_STATION', deviceType: 'DOCK' }))).toBe('base_station')
  })

  it('deviceClass 缺省时回退到 deviceType（DOCK→dock）', () => {
    expect(deviceCategory(make({ deviceClass: undefined, deviceType: 'DOCK' }))).toBe('dock')
  })

  it('deviceClass 缺省时回退到 deviceType（DRONE→drone）', () => {
    expect(deviceCategory(make({ deviceClass: undefined, deviceType: 'DRONE' }))).toBe('drone')
  })
})

describe('buildDeviceGroups', () => {
  it('340 拓扑：基站为顶层，机场挂其下，飞机挂机场下 —— 机场 SN 不被基站 SN 顶替', () => {
    const drone = make({ sn: 'DRONE-340', deviceClass: 'drone', deviceType: 'DRONE', name: '飞机-340' })
    const dock = make({
      sn: 'DOCK-340',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      name: 'ea340d',
      childDevices: [drone],
    })
    const baseStation = make({
      sn: 'BS-340',
      deviceClass: 'base_station',
      deviceType: 'DOCK',
      name: '地面站-001',
      childDevices: [dock],
    })

    const groups = buildDeviceGroups([baseStation])

    expect(groups).toHaveLength(1)
    const g = groups[0]
    expect(g.baseStation?.sn).toBe('BS-340')
    expect(g.dock?.sn).toBe('DOCK-340')
    expect(g.drones.map(d => d.sn)).toEqual(['DRONE-340'])
    // 340 网络无人机：进驾驶舱 / 直播 RTC 的网关是飞行器自身 SN，而非基站或机场 SN
    expect(g.gatewaySn).toBe('DRONE-340')
    expect(g.gatewaySn).not.toBe('BS-340')
  })

  it('340 拓扑无在线飞行器时，网关回退机场 SN（无飞行器可当 RTC 网关）', () => {
    const dock = make({
      sn: 'DOCK-340',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      name: 'ea340d',
    })
    const baseStation = make({
      sn: 'BS-340',
      deviceClass: 'base_station',
      deviceType: 'DOCK',
      name: '地面站-001',
      childDevices: [dock],
    })

    const groups = buildDeviceGroups([baseStation])

    expect(groups).toHaveLength(1)
    // 组网拓扑但无飞行器：回退机场 SN，仍不取基站 SN
    expect(groups[0].gatewaySn).toBe('DOCK-340')
  })

  it('普通机场拓扑：机场为顶层，飞机为直接子设备', () => {
    const drone = make({ sn: 'drone-1', deviceClass: 'drone', deviceType: 'DRONE' })
    const dock = make({
      sn: 'dock-1',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      childDevices: [drone],
    })

    const groups = buildDeviceGroups([dock])

    expect(groups).toHaveLength(1)
    const g = groups[0]
    expect(g.baseStation).toBeUndefined()
    expect(g.dock?.sn).toBe('dock-1')
    expect(g.drones.map(d => d.sn)).toEqual(['drone-1'])
    expect(g.gatewaySn).toBe('dock-1')
  })

  it('deviceClass 缺省（事件缓存兜底）时按 deviceType 分组仍可用', () => {
    const drone = make({ sn: 'drone-1', deviceClass: undefined, deviceType: 'DRONE' })
    const dock = make({
      sn: 'dock-1',
      deviceClass: undefined,
      deviceType: 'DOCK',
      childDevices: [drone],
    })

    const groups = buildDeviceGroups([dock])

    expect(groups).toHaveLength(1)
    expect(groups[0].dock?.sn).toBe('dock-1')
    expect(groups[0].drones.map(d => d.sn)).toEqual(['drone-1'])
    expect(groups[0].gatewaySn).toBe('dock-1')
  })

  it('过滤离线顶层设备', () => {
    const offlineDock = make({ sn: 'dock-off', deviceClass: 'dock', deviceType: 'DOCK', online: false })
    const onlineDock = make({ sn: 'dock-on', deviceClass: 'dock', deviceType: 'DOCK', online: true })

    const groups = buildDeviceGroups([offlineDock, onlineDock])

    expect(groups.map(g => g.dock?.sn)).toEqual(['dock-on'])
  })

  it('忽略裸飞行器顶层设备（不构成机场分组）', () => {
    const bareDrone = make({ sn: 'drone-bare', deviceClass: 'drone', deviceType: 'DRONE', online: true })

    const groups = buildDeviceGroups([bareDrone])

    expect(groups).toHaveLength(0)
  })

  it('同一机场挂多架飞行器时收敛为唯一主机 —— 复现 PF60BK6HTCJF5Y 双驾驶舱入口', () => {
    // 现场：真机在报遥测且今天上线；ghost 从未上报 OSD、昨天上线，两者 online 均为 true
    const realDrone = make({
      sn: 'PFYQLBPLSCHFM9',
      deviceClass: 'drone',
      deviceType: 'DRONE',
      online: true,
      lastOsd: { attributes: { battery: 91 } },
      onlineSince: '2026-10-14T04:43:58Z',
    })
    const ghostDrone = make({
      sn: 'PFYE8HAG4EC2XVT99QL9',
      deviceClass: 'drone',
      deviceType: 'DRONE',
      online: true,
      lastOsd: undefined,
      onlineSince: '2026-10-13T10:01:58Z',
    })
    const dock = make({
      sn: 'PF60BK6HTCJF5Y',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      online: true,
      childDevices: [realDrone, ghostDrone],
    })

    const groups = buildDeviceGroups([dock])

    expect(groups).toHaveLength(1)
    // 只保留在报遥测的真机，不再出现重复驾驶舱入口
    expect(groups[0].drones.map(d => d.sn)).toEqual(['PFYQLBPLSCHFM9'])
  })

  it('两架都在线且都有遥测时，按最近上线选主机', () => {
    const older = make({
      sn: 'drone-older',
      deviceClass: 'drone',
      deviceType: 'DRONE',
      online: true,
      lastOsd: { attributes: {} },
      onlineSince: '2026-10-10T00:00:00Z',
    })
    const newer = make({
      sn: 'drone-newer',
      deviceClass: 'drone',
      deviceType: 'DRONE',
      online: true,
      lastOsd: { attributes: {} },
      onlineSince: '2026-10-14T00:00:00Z',
    })
    const dock = make({
      sn: 'dock-x',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      online: true,
      childDevices: [older, newer],
    })

    const groups = buildDeviceGroups([dock])

    expect(groups[0].drones.map(d => d.sn)).toEqual(['drone-newer'])
  })

  it('在线主机优先于离线旧绑定（即使离线那架有遥测）', () => {
    const offlineWithOsd = make({
      sn: 'drone-offline',
      deviceClass: 'drone',
      deviceType: 'DRONE',
      online: false,
      lastOsd: { attributes: {} },
    })
    const onlineNoOsd = make({
      sn: 'drone-online',
      deviceClass: 'drone',
      deviceType: 'DRONE',
      online: true,
      lastOsd: undefined,
    })
    const dock = make({
      sn: 'dock-y',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      online: true,
      childDevices: [offlineWithOsd, onlineNoOsd],
    })

    const groups = buildDeviceGroups([dock])

    expect(groups[0].drones.map(d => d.sn)).toEqual(['drone-online'])
  })

  it('同一 sn 重复出现时按 sn 去重', () => {
    const drone = make({ sn: 'dup', deviceClass: 'drone', deviceType: 'DRONE', online: true })
    const dock = make({
      sn: 'dock-dup',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      online: true,
      childDevices: [drone, { ...drone }],
    })

    const groups = buildDeviceGroups([dock])

    expect(groups[0].drones.map(d => d.sn)).toEqual(['dup'])
  })

  it('唯一飞行器待机离线时不被收敛逻辑误删', () => {
    const restingDrone = make({
      sn: 'drone-resting',
      deviceClass: 'drone',
      deviceType: 'DRONE',
      online: false,
      lastOsd: undefined,
    })
    const dock = make({
      sn: 'dock-resting',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      online: true,
      childDevices: [restingDrone],
    })

    const groups = buildDeviceGroups([dock])

    expect(groups[0].drones.map(d => d.sn)).toEqual(['drone-resting'])
  })
})

describe('buildDroneGatewayIndex', () => {
  it('机场离线时仍保留其下属飞行器的驾驶舱网关', () => {
    // 回归：buildDeviceGroups 只取在线分组根，机场掉线会导致驾驶舱入口整体消失
    const drone = make({ sn: 'drone-off', deviceClass: 'drone', deviceType: 'DRONE', online: false })
    const offlineDock = make({
      sn: 'dock-off',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      online: false,
      childDevices: [drone],
    })

    const index = buildDroneGatewayIndex([offlineDock])

    expect(index.get('drone-off')).toBe('dock-off')
  })

  it('在线与离线分组并存时，在线分组的网关优先', () => {
    const drone = make({ sn: 'drone-dup', deviceClass: 'drone', deviceType: 'DRONE', online: true })
    const offlineDock = make({
      sn: 'dock-off',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      online: false,
      childDevices: [drone],
    })
    const onlineDock = make({
      sn: 'dock-on',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      online: true,
      childDevices: [drone],
    })

    const index = buildDroneGatewayIndex([offlineDock, onlineDock])

    expect(index.get('drone-dup')).toBe('dock-on')
  })

  it('340 拓扑下映射到飞行器自身 SN（与 buildDeviceGroups 的网关解析一致）', () => {
    const drone = make({ sn: 'DRONE-340', deviceClass: 'drone', deviceType: 'DRONE' })
    const dock = make({
      sn: 'DOCK-340',
      deviceClass: 'dock',
      deviceType: 'DOCK',
      childDevices: [drone],
    })
    const baseStation = make({
      sn: 'BS-340',
      deviceClass: 'base_station',
      deviceType: 'DOCK',
      childDevices: [dock],
    })

    const index = buildDroneGatewayIndex([baseStation])

    expect(index.get('DRONE-340')).toBe('DRONE-340')
  })

  it('忽略裸飞行器顶层设备（无分组可依）', () => {
    const bareDrone = make({ sn: 'drone-bare', deviceClass: 'drone', deviceType: 'DRONE', online: true })

    const index = buildDroneGatewayIndex([bareDrone])

    expect(index.size).toBe(0)
  })
})