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

import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'
import { getRuntimeConfig } from '@/utils/runtimeConfig'
import { buildDeviceGroups } from '@/utils/deviceGroup'
import type { LiveDeviceInfo } from '@/types/liveStream'
import type { DeviceState } from '@/types/api'

const DEVICES_BASE = `${API_BASE}/api/v1/devices`

function isMockEnabled(): boolean {
  return getRuntimeConfig('MOCK_ENABLED') === 'true'
}

const MOCK_DEVICES: LiveDeviceInfo[] = [
  {
    name: '机场-001',
    dock: { model: 'DJI Dock 2', sn: 'MOCK_DOCK_0001' },
    drone: { model: 'Matrice 3D', sn: 'MOCK_DRONE_0002' },
  },
  {
    name: '机场-002',
    dock: { model: 'DJI Dock 2', sn: 'MOCK_DOCK_0002' },
    drone: { model: 'Matrice 3D', sn: 'MOCK_DRONE_0003' },
  },
  {
    name: '机场-003',
    dock: { model: 'DJI Dock', sn: '7XYZM430010003' },
    drone: { model: 'Matrice 30T', sn: '1581F5HKC230003' },
  },
]

/**
 * 获取在线设备列表（用于直播大屏快速添加）。
 *
 * <p>后端返回 DeviceState[] 顶层设备列表（childDevices 为嵌套子树）。复用
 * {@link buildDeviceGroups} 按 SDK deviceClass 解析每个在线机场分组的基站 / 机场 / 飞行器节点，
 * 各节点保留自己的 SN —— 修复「340 拓扑下机场卡片显示基站 SN」的问题：340 机场的顶层设备是
 * 基站（deviceType 同为 DOCK），直接取顶层 sn 会把基站 SN 当成机场 SN。
 *
 * <p>仅保留解析出机场节点的分组（纯基站无机场、无可直播画面时跳过）。
 */
export async function fetchOnlineDevices(): Promise<LiveDeviceInfo[]> {
  if (isMockEnabled()) {
    return MOCK_DEVICES
  }

  const res = await authFetch(DEVICES_BASE)
  if (!res.ok) {
    throw new Error(`获取在线设备列表失败: ${res.status}`)
  }

  const data = await res.json() as DeviceState[]

  return buildDeviceGroups(data)
    .map((group): LiveDeviceInfo | null => {
      const dock = group.dock
      if (!dock) {
        return null
      }
      const baseStation = group.baseStation
      const drone = group.drones[0]
      return {
        name: dock.name ?? dock.sn,
        baseStation: baseStation
          ? {
              model: baseStation.model ?? 'Unknown',
              sn: baseStation.sn,
            }
          : undefined,
        dock: {
          model: dock.model ?? 'Unknown',
          sn: dock.sn,
        },
        drone: drone
          ? {
              model: drone.model ?? 'Unknown',
              sn: drone.sn,
            }
          : undefined,
      }
    })
    .filter((info): info is LiveDeviceInfo => info !== null)
}
